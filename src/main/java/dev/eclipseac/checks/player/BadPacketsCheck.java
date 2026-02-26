package dev.eclipseac.checks.player;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class BadPacketsCheck extends Check {

    private static final double MAX_AXIS_DELTA = 100.0;

    public BadPacketsCheck(EclipseAC plugin) {
        super(plugin, "BadPackets", "Player");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        var    to     = event.getTo();
        var    from   = event.getFrom();

        if (!isFinite(to.getX()) || !isFinite(to.getY()) || !isFinite(to.getZ())) {
            event.setTo(from);
            flag(player, "NaN/Inf position x=" + to.getX() + " y=" + to.getY() + " z=" + to.getZ());
            return;
        }

        float pitch = to.getPitch();
        if (pitch > 90.05f || pitch < -90.05f) {
            flag(player, "pitch=" + String.format("%.2f", pitch));
            return;
        }

        double dx = Math.abs(to.getX() - from.getX());
        double dy = Math.abs(to.getY() - from.getY());
        double dz = Math.abs(to.getZ() - from.getZ());

        if (dx > MAX_AXIS_DELTA || dy > MAX_AXIS_DELTA || dz > MAX_AXIS_DELTA) {
            event.setTo(from);
            flag(player, "impossible-delta dx=" + String.format("%.0f", dx)
                + " dy=" + String.format("%.0f", dy) + " dz=" + String.format("%.0f", dz));
            return;
        }

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.getTeleportTicks() <= 0) {
            double worldMinY = player.getWorld().getMinHeight() - 10;
            double worldMaxY = player.getWorld().getMaxHeight() + 10;
            if (to.getY() < worldMinY || to.getY() > worldMaxY) {
                event.setTo(from);
                flag(player, "out-of-bounds y=" + String.format("%.1f", to.getY())
                    + " allowed=[" + (int) worldMinY + "," + (int) worldMaxY + "]");
            }
        }
    }

    private boolean isFinite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }
}
