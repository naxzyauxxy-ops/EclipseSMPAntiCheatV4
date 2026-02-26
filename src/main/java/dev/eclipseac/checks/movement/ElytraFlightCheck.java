package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class ElytraFlightCheck extends Check {

    private static final double MAX_HORIZONTAL = 1.8;
    private static final double MAX_ASCENT     = 0.05;

    public ElytraFlightCheck(EclipseAC plugin) {
        super(plugin, "ElytraFlight", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (!player.isGliding()) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dy = event.getTo().getY() - event.getFrom().getY();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        double maxH = getConfigDouble("max-horizontal-speed", MAX_HORIZONTAL);
        double maxA = getConfigDouble("max-ascent", MAX_ASCENT);

        if (horizontal > maxH) {
            flag(player, "elytra_speed=" + String.format("%.3f", horizontal) + " max=" + maxH);
            return;
        }
        if (dy > maxA) {
            flag(player, "elytra_ascent dy=" + String.format("%.3f", dy));
            return;
        }

        reward(player);
    }
}
