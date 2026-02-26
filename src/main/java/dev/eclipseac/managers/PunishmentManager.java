package dev.eclipseac.managers;

import dev.eclipseac.EclipseAC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PunishmentManager {

    private final EclipseAC plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public PunishmentManager(EclipseAC plugin) {
        this.plugin = plugin;
    }

    public void punish(Player player, String checkName, int vl) {
        if (!plugin.getConfig().getBoolean("punishments.enabled", true)) return;

        String action = plugin.getConfig().getStringList("punishments.actions")
                .stream().findFirst().orElse("KICK");

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            switch (action.toUpperCase()) {
                case "KICK" -> {
                    player.kick(MM.deserialize(
                        "<red><bold>EclipseAC</bold>\n" +
                        "<gray>Removed for suspicious activity.\n" +
                        "<dark_gray>Check: <white>" + checkName + " <dark_gray>| VL:<white>" + vl
                    ));
                    plugin.getLogger().info("[PUNISH] Kicked " + player.getName() + " | " + checkName + " VL:" + vl);
                }
                case "BAN" -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "ban " + player.getName() + " [EclipseAC] " + checkName);
                    plugin.getLogger().info("[PUNISH] Banned " + player.getName() + " | " + checkName + " VL:" + vl);
                }
                case "COMMAND" -> {
                    String cmd = plugin.getConfig().getString("punishments.command",
                        "kick %player% Suspicious activity");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        cmd.replace("%player%", player.getName())
                           .replace("%check%",  checkName)
                           .replace("%vl%",     String.valueOf(vl)));
                }
            }
        });
    }
}
