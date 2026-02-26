package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoFallCheck extends Check {

    private final Map<UUID, Double> fallTracker = new ConcurrentHashMap<>();

    public NoFallCheck(EclipseAC plugin) {
        super(plugin, "NoFall", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        fallTracker.put(player.getUniqueId(), 0.0);
        reward(player);
    }

    public void checkLanding(Player player) {
        if (!isEnabled()) return;
        if (player.getGameMode() == GameMode.CREATIVE ||
            player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;

        float fall    = data.getFallDistance();
        double minFall = getConfigDouble("min-fall-distance", 3.5);
        if (fall < minFall) return;

        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;
        if (data.isInLiquid() || data.getLiquidExitTicks() > 0)    return;

        Material landBlock  = player.getLocation().getBlock().getType();
        Material blockBelow = player.getLocation().subtract(0, 0.1, 0).getBlock().getType();
        if (isFallCanceller(landBlock) || isFallCanceller(blockBelow)) return;

        var boots = player.getInventory().getBoots();
        if (boots != null) {
            int ffLevel = boots.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.FEATHER_FALLING);
            if (ffLevel > 0) {
                float effectiveFall = fall - (ffLevel * 2.5f);
                if (effectiveFall < (float) minFall) return;
            }
        }

        flag(player, "fall=" + String.format("%.1f", fall) + " blocks, no damage taken");
        data.setFallDistance(0);
    }

    private boolean isFallCanceller(Material mat) {
        if (Tag.BEDS.isTagged(mat)) return true;
        return switch (mat) {
            case WATER, LAVA, SLIME_BLOCK, HAY_BLOCK,
                 COBWEB, POWDER_SNOW, SWEET_BERRY_BUSH,
                 HONEY_BLOCK, BIG_DRIPLEAF, BIG_DRIPLEAF_STEM -> true;
            default -> false;
        };
    }
}
