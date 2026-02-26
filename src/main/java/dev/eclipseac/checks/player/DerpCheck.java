package dev.eclipseac.checks.player;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class DerpCheck extends Check {

    public DerpCheck(EclipseAC plugin) {
        super(plugin, "Derp", "Player");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE
                || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        float fromPitch = event.getFrom().getPitch();
        float toPitch   = event.getTo().getPitch();
        float fromYaw   = event.getFrom().getYaw();
        float toYaw     = event.getTo().getYaw();

        float pitchDelta = Math.abs(toPitch - fromPitch);
        float yawDelta   = Math.abs(toYaw - fromYaw);
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        double maxRotation = getConfigDouble("max-rotation-per-tick", 120.0);

        if (pitchDelta > maxRotation) {
            flag(player, "pitch_flip=" + String.format("%.1f", pitchDelta) + "° max=" + maxRotation + "°/tick");
            return;
        }
        if (yawDelta > maxRotation) {
            flag(player, "yaw_flip=" + String.format("%.1f", yawDelta) + "° max=" + maxRotation + "°/tick");
            return;
        }
        if (toPitch > 90.01f || toPitch < -90.01f) {
            flag(player, "pitch_oob=" + String.format("%.2f", toPitch));
            return;
        }

        reward(player);
    }
}
