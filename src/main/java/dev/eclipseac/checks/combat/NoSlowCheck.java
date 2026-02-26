package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class NoSlowCheck extends Check {

    private static final double ITEM_USE_SPEED_MULTIPLIER = 0.4;
    private static final double BASE_SPRINT               = 0.2873;

    public NoSlowCheck(EclipseAC plugin) {
        super(plugin, "NoSlow", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE
                || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod() || data.isInLiquid()) return;
        if (!player.isHandRaised()) return;

        ItemStack item = player.getActiveItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (!isSlowingItem(item.getType())) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);
        if (speed < 0.01) return;

        double maxAllowed = BASE_SPRINT * ITEM_USE_SPEED_MULTIPLIER
                * getConfigDouble("speed-multiplier-buffer", 1.3);

        if (speed > maxAllowed) {
            flag(player, "item=" + item.getType().name()
                + " speed=" + String.format("%.4f", speed)
                + " max=" + String.format("%.4f", maxAllowed));
        } else {
            reward(player);
        }
    }

    private boolean isSlowingItem(Material mat) {
        return switch (mat) {
            case BOW, CROSSBOW, TRIDENT,
                 APPLE, BREAD, PORKCHOP, COOKED_PORKCHOP,
                 BEEF, COOKED_BEEF, CHICKEN, COOKED_CHICKEN,
                 MUTTON, COOKED_MUTTON, RABBIT, COOKED_RABBIT,
                 SALMON, COOKED_SALMON, COD, COOKED_COD,
                 GOLDEN_APPLE, ENCHANTED_GOLDEN_APPLE,
                 MUSHROOM_STEW, RABBIT_STEW, BEETROOT_SOUP,
                 SHIELD -> true;
            default -> false;
        };
    }
}
