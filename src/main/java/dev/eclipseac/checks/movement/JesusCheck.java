package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class JesusCheck extends Check {

    public JesusCheck(EclipseAC plugin) {
        super(plugin, "Jesus", "Movement");
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

        var boots = player.getInventory().getBoots();
        if (boots != null && boots.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.FROST_WALKER) > 0) return;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;
        if (!player.isOnGround()) return;

        var blockAtFeet = event.getTo().clone().subtract(0, 0.01, 0).getBlock().getType();
        var blockBelow  = event.getTo().clone().subtract(0, 0.5,  0).getBlock().getType();

        boolean aboveWater = blockAtFeet == Material.WATER || blockBelow == Material.WATER;
        if (!aboveWater) { reward(player); return; }

        var solidBelow = event.getTo().clone().subtract(0, 0.1, 0).getBlock();
        if (!solidBelow.getType().isSolid() && !solidBelow.isLiquid()) {
            flag(player, "walking on water at y=" + String.format("%.2f", event.getTo().getY()));
        } else {
            reward(player);
        }
    }
}
