package dev.eclipseac.checks.player;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class PhaseCheck extends Check {

    public PhaseCheck(EclipseAC plugin) {
        super(plugin, "Phase", "Player");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isGliding()) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod() || data.isInLiquid() || data.isOnClimbable()) return;

        Location from = event.getFrom().clone();
        Location to   = event.getTo().clone();

        double dx   = to.getX() - from.getX();
        double dy   = to.getY() - from.getY();
        double dz   = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < 0.5) return;

        Location mid = from.clone().add(dx / 2, dy / 2, dz / 2);
        Material midBlock = mid.getBlock().getType();

        boolean fromOpen = !from.getBlock().getType().isSolid();
        boolean toOpen   = !to.getBlock().getType().isSolid();

        if (fromOpen && toOpen && isSolidWall(midBlock)) {
            flag(player, "phased through " + midBlock.name() + " dist=" + String.format("%.2f", dist));
            event.setTo(from);
        }
    }

    private boolean isSolidWall(Material mat) {
        if (!mat.isSolid()) return false;
        return switch (mat) {
            case GLASS, GLASS_PANE, ICE, PACKED_ICE, BLUE_ICE,
                 IRON_BARS, CHAIN, SCAFFOLDING -> false;
            default -> true;
        };
    }
}
