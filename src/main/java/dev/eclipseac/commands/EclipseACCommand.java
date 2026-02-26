package dev.eclipseac.commands;

import dev.eclipseac.EclipseAC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EclipseACCommand implements CommandExecutor, TabCompleter {

    private final EclipseAC plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String HDR = "<dark_gray>◈ <gradient:#7B2FBE:#00C8FF>EclipseAC</gradient> <dark_gray>│ ";

    public EclipseACCommand(EclipseAC plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eclipseac.admin")) {
            sender.sendMessage(MM.deserialize(HDR + "<red>No permission."));
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(MM.deserialize(HDR + "<green>Config reloaded."));
            }
            case "alerts" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(MM.deserialize(HDR + "<red>In-game only.")); return true;
                }
                plugin.getAlertManager().toggleAlerts(p);
            }
            case "info" -> {
                if (args.length < 2) { sender.sendMessage(MM.deserialize(HDR + "<red>Usage: /eclipseac info <player>")); return true; }
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) { sender.sendMessage(MM.deserialize(HDR + "<red>Player not found.")); return true; }
                Map<String, Integer> vl = plugin.getViolationManager().getAllViolations(t);
                sender.sendMessage(MM.deserialize(HDR + "<white>" + t.getName() + "'s <yellow>violations<dark_gray>:"));
                if (vl.isEmpty()) {
                    sender.sendMessage(MM.deserialize("  <gray>No violations."));
                } else {
                    vl.entrySet().stream()
                      .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                      .forEach(e -> {
                          String col = e.getValue() > 15 ? "<red>" : e.getValue() > 7 ? "<yellow>" : "<green>";
                          sender.sendMessage(MM.deserialize("  <dark_gray>› <gray>" + e.getKey() + " <dark_gray>│ " + col + "VL " + e.getValue()));
                      });
                }
            }
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage(MM.deserialize(HDR + "<red>Usage: /eclipseac reset <player>")); return true; }
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) { sender.sendMessage(MM.deserialize(HDR + "<red>Player not found.")); return true; }
                plugin.getViolationManager().resetAllVL(t);
                sender.sendMessage(MM.deserialize(HDR + "<green>Reset VL for <white>" + t.getName()));
            }
            case "kick" -> {
                if (args.length < 2) { sender.sendMessage(MM.deserialize(HDR + "<red>Usage: /eclipseac kick <player>")); return true; }
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) { sender.sendMessage(MM.deserialize(HDR + "<red>Player not found.")); return true; }
                t.kick(MM.deserialize("<red><bold>EclipseAC\n<gray>Manually removed by staff."));
                sender.sendMessage(MM.deserialize(HDR + "<green>Kicked <white>" + t.getName()));
            }
            case "version" -> sender.sendMessage(MM.deserialize(
                HDR + "<gray>v<white>" + plugin.getDescription().getVersion() + " <gray>│ Paper 1.21.x"));
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(MM.deserialize("<dark_gray>╔══ <gradient:#7B2FBE:#00C8FF>EclipseAC</gradient> <dark_gray>══╗"));
        for (String line : new String[]{
            "│ <gray>/eclipseac reload          <dark_gray>– Reload config",
            "│ <gray>/eclipseac alerts          <dark_gray>– Toggle alerts",
            "│ <gray>/eclipseac info <player>   <dark_gray>– View VL",
            "│ <gray>/eclipseac reset <player>  <dark_gray>– Reset VL",
            "│ <gray>/eclipseac kick <player>   <dark_gray>– Manual kick",
            "│ <gray>/eclipseac version         <dark_gray>– Version",
            "╚══════════════════════════╝"
        }) s.sendMessage(MM.deserialize("<dark_gray>" + line));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) return Arrays.asList("reload","alerts","info","reset","kick","version");
        if (args.length == 2 && List.of("info","reset","kick").contains(args[0].toLowerCase()))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        return List.of();
    }
}
