package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpeedCheck extends Check {

    private static final double BASE_WALK   = 0.215;
    private static final double BASE_SPRINT = 0.2873;
    private static final int    WINDOW      = 8;

    private final Map<UUID, Deque<Double>> speedBuffers = new ConcurrentHashMap<>();

    public SpeedCheck(EclipseAC plugin) {
        super(plugin, "Speed", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isGliding() || player.isRiptiding()) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;
        if (data.isInLiquid()    || data.getLiquidExitTicks()    > 0) return;
        if (data.isOnClimbable() || data.getClimbableExitTicks() > 0) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 0.005) { reward(player); return; }

        double base = player.isSprinting() ? BASE_SPRINT : BASE_WALK;
        var speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) base *= (speedAttr.getValue() / 0.1);

        var speedEff = player.getPotionEffect(PotionEffectType.SPEED);
        if (speedEff != null) base *= 1.0 + 0.2 * (speedEff.getAmplifier() + 1);
        var slowEff = player.getPotionEffect(PotionEffectType.SLOWNESS);
        if (slowEff != null) base *= Math.max(0.05, 1.0 - 0.15 * (slowEff.getAmplifier() + 1));

        Location playerLoc = player.getLocation().clone();
        Material under = playerLoc.clone().subtract(0, 0.1, 0).getBlock().getType();
        base *= getSlipMultiplier(under);

        if (under == Material.SOUL_SAND || under == Material.SOUL_SOIL) {
            var boots = player.getInventory().getBoots();
            if (boots != null) {
                int soulSpeed = boots.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.SOUL_SPEED);
                if (soulSpeed > 0) base *= 1.0 + 0.105 * soulSpeed;
            }
        }

        if (player.isSneaking()) base *= 0.3;

        double maxAllowed = base * getConfigDouble("max-speed-multiplier", 1.35);

        Deque<Double> buf = speedBuffers.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
        buf.addLast(horizontal);
        if (buf.size() > WINDOW) buf.pollFirst();

        if (buf.size() >= WINDOW) {
            double avg = buf.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            if (avg > maxAllowed) {
                flag(player, "avgSpd=" + String.format("%.4f", avg)
                    + " max=" + String.format("%.4f", maxAllowed)
                    + " surface=" + under.name());
                buf.clear();
                return;
            }
        }

        reward(player);
        data.setLastHorizontalSpeed(horizontal);
    }

    private double getSlipMultiplier(Material mat) {
        return switch (mat) {
            case BLUE_ICE             -> 2.5;
            case PACKED_ICE           -> 1.9;
            case ICE, FROSTED_ICE     -> 1.7;
            case SOUL_SAND, SOUL_SOIL -> 0.45;
            case HONEY_BLOCK          -> 0.4;
            default                   -> 1.0;
        };
    }
}
