package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class GroundSpoofCheck extends Check {

    public GroundSpoofCheck(EclipseAC plugin) {
        super(plugin, "GroundSpoof", "Movement");
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
        if (data.isOnClimbable()) return;

        if (!player.isOnGround()) return;

        double y      = event.getTo().getY();
        double blockY = Math.floor(y);
        double offset = y - blockY;

        if (offset > getConfigDouble("max-ground-offset", 0.3)) {
            var blockBelow = event.getTo().clone().subtract(0, 0.1, 0).getBlock();
            if (blockBelow.getType().isSolid()) return;
            flag(player, "offset=" + String.format("%.3f", offset)
                + " blockBelow=" + blockBelow.getType().name());
        } else {
            reward(player);
        }
    }
}
