package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class BoatFlyCheck extends Check {

    public BoatFlyCheck(EclipseAC plugin) {
        super(plugin, "BoatFly", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!isEnabled()) return;
        if (!(event.getVehicle() instanceof Boat boat)) return;

        Player player = null;
        for (Entity e : boat.getPassengers()) {
            if (e instanceof Player p) { player = p; break; }
        }
        if (player == null) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;

        double dy = event.getTo().getY() - event.getFrom().getY();
        if (dy <= 0) { reward(player); return; }

        double maxAscent = getConfigDouble("max-ascent-per-tick", 0.25);
        if (dy > maxAscent) {
            flag(player, "boat_dy=+" + String.format("%.3f", dy) + " max=" + maxAscent);
        } else {
            reward(player);
        }
    }
}
