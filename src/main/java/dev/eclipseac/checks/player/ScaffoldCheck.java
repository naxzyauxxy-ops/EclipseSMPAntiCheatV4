package dev.eclipseac.checks.player;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class ScaffoldCheck extends Check {

    private static final int STREAK_THRESHOLD = 10;

    public ScaffoldCheck(EclipseAC plugin) {
        super(plugin, "Scaffold", "Player");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getData(player);
        data.recordPlace();

        int maxBps = getConfigInt("max-blocks-per-second", 10);
        if (data.getBlocksPerSecond() > maxBps) {
            flag(player, "bps=" + data.getBlocksPerSecond() + " max=" + maxBps);
            data.resetScaffoldStreak();
            return;
        }

        if (!getConfigBool("rotation-check", true)) { reward(player); return; }

        BlockFace against = event.getBlockAgainst().getFace(event.getBlock());
        if (against != BlockFace.DOWN) {
            data.resetScaffoldStreak();
            data.resetScaffoldLookingDownStreak();
            reward(player);
            return;
        }

        float pitch = player.getLocation().getPitch();

        if (pitch >= getConfigDouble("min-looking-down-pitch", 30.0)) {
            data.incrementScaffoldLookingDownStreak();
            data.resetScaffoldStreak();
            reward(player);
        } else {
            data.incrementScaffoldStreak();
            data.resetScaffoldLookingDownStreak();

            if (data.getScaffoldStreak() >= STREAK_THRESHOLD) {
                double speed = data.getLastHorizontalSpeed();
                if (speed > 0.1) {
                    flag(player, "streak=" + data.getScaffoldStreak()
                        + " pitch=" + String.format("%.1f", pitch)
                        + " speed=" + String.format("%.3f", speed));
                }
                data.resetScaffoldStreak();
            }
        }
    }
}
