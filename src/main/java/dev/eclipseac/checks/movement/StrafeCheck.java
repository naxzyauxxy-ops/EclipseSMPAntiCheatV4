package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class StrafeCheck extends Check {

    public StrafeCheck(EclipseAC plugin) {
        super(plugin, "Strafe", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isGliding()) return;
        if (!player.isSprinting()) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod() || data.isInLiquid()) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);
        if (speed < 0.22) { reward(player); return; }

        double yaw  = Math.toRadians(event.getTo().getYaw());
        double lookX = -Math.sin(yaw);
        double lookZ =  Math.cos(yaw);

        double moveX = dx / speed;
        double moveZ = dz / speed;

        double dot   = Math.max(-1.0, Math.min(1.0, lookX * moveX + lookZ * moveZ));
        double angle = Math.toDegrees(Math.acos(dot));

        double maxAngle = getConfigDouble("max-strafe-angle", 82.0);
        if (angle > maxAngle) {
            flag(player, "strafe_angle=" + String.format("%.1f", angle)
                + "° max=" + maxAngle + "° speed=" + String.format("%.3f", speed));
        } else {
            reward(player);
        }
    }
}
