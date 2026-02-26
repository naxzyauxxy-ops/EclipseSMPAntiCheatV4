package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class StepCheck extends Check {

    public StepCheck(EclipseAC plugin) {
        super(plugin, "Step", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isGliding()) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;
        if (data.isInLiquid() || data.getLiquidExitTicks() > 0) return;

        Location from = event.getFrom().clone();
        Location to   = event.getTo().clone();

        double deltaY      = to.getY() - from.getY();
        boolean isOnGround  = player.isOnGround();
        boolean wasOnGround = data.wasOnGround();

        if (!isOnGround || !wasOnGround) { reward(player); return; }
        if (deltaY <= 0.0)               { reward(player); return; }

        double jumpBoostBonus = 0;
        var jbEffect = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jbEffect != null) jumpBoostBonus = 0.2 * (jbEffect.getAmplifier() + 1);

        double maxStep = getConfigDouble("max-step-height", 0.62) + jumpBoostBonus;

        if (isNaturalStepBlock(to.clone().subtract(0, 0.1, 0).getBlock().getType())) { reward(player); return; }
        if (isNaturalStepBlock(to.clone().subtract(0, 0.5, 0).getBlock().getType())) { reward(player); return; }
        if (isNaturalStepBlock(to.clone().subtract(0, 1.0, 0).getBlock().getType())) { reward(player); return; }
        if (isNaturalStepBlock(from.getBlock().getType()))                            { reward(player); return; }
        if (isNaturalStepBlock(from.clone().subtract(0, 0.5, 0).getBlock().getType())) { reward(player); return; }

        if (deltaY > maxStep) {
            flag(player, "step=" + String.format("%.3f", deltaY) + " max=" + maxStep);
        } else {
            reward(player);
        }
    }

    private boolean isNaturalStepBlock(Material mat) {
        if (mat == Material.AIR || mat == Material.CAVE_AIR) return false;
        if (Tag.STAIRS.isTagged(mat)) return true;
        if (Tag.SLABS.isTagged(mat))  return true;
        if (Tag.FENCES.isTagged(mat)) return true;
        if (Tag.WALLS.isTagged(mat))  return true;
        String name = mat.name();
        return name.contains("SNOW")
            || mat == Material.SOUL_SAND
            || mat == Material.POWDER_SNOW
            || mat == Material.COMPOSTER
            || mat == Material.CAULDRON
            || mat == Material.LECTERN
            || mat == Material.BIG_DRIPLEAF
            || mat == Material.GRASS
            || mat == Material.FARMLAND
            || mat == Material.DIRT_PATH;
    }
}
