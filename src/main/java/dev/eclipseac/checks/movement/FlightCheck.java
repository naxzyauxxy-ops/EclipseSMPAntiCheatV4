package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class FlightCheck extends Check {

    private static final double GRAVITY = 0.08;
    private static final double DRAG    = 0.98;

    public FlightCheck(EclipseAC plugin) {
        super(plugin, "Flight", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.getAllowFlight() || player.isFlying())  return;
        if (player.isGliding() || player.isRiptiding())   return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;
        if (data.isInLiquid()    || data.getLiquidExitTicks()    > 0) return;
        if (data.isOnClimbable() || data.getClimbableExitTicks() > 0) return;

        if (player.hasPotionEffect(PotionEffectType.LEVITATION))   return;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;

        Material blockAtFeet = player.getLocation().getBlock().getType();
        Material blockAbove  = player.getLocation().add(0, 1, 0).getBlock().getType();
        if (isClimbableOrLiquid(blockAtFeet) || isClimbableOrLiquid(blockAbove)) return;

        int jumpBoostAmp = 0;
        var jbEffect = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jbEffect != null) jumpBoostAmp = jbEffect.getAmplifier() + 1;

        boolean onGround = player.isOnGround();
        double  deltaY   = event.getTo().getY() - event.getFrom().getY();

        if (!onGround) data.incrementAirTicks();
        else           data.setAirTicks(0);

        int graceTicks = getConfigInt("grace-ticks", 20) + (jumpBoostAmp * 8);
        int airTicks   = data.getAirTicks();

        if (airTicks <= graceTicks) { reward(player); return; }

        double allowedRise = jumpBoostAmp * 0.15;

        if (deltaY > allowedRise + 0.08) {
            flag(player, "ascent airTicks=" + airTicks + " dy=+" + String.format("%.3f", deltaY));
            return;
        }

        double expectedMinDrop = GRAVITY * ((airTicks - graceTicks) * DRAG);
        if (Math.abs(deltaY) < 0.01 && expectedMinDrop > 0.25 && airTicks > graceTicks + 10) {
            flag(player, "hover airTicks=" + airTicks + " dy=" + String.format("%.3f", deltaY));
            return;
        }

        reward(player);
    }

    private boolean isClimbableOrLiquid(Material m) {
        return switch (m) {
            case WATER, LAVA, BUBBLE_COLUMN,
                 LADDER, VINE, TWISTING_VINES, TWISTING_VINES_PLANT,
                 WEEPING_VINES, WEEPING_VINES_PLANT,
                 SCAFFOLDING, CAVE_VINES, CAVE_VINES_PLANT -> true;
            default -> false;
        };
    }
}
