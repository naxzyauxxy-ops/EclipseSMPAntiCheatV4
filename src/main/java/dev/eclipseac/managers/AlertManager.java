package dev.eclipseac.managers;

import dev.eclipseac.EclipseAC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlertManager {

    private final EclipseAC plugin;
    private final Set<UUID> silenced = new HashSet<>();
    private PrintWriter logWriter;
    private final SimpleDateFormat fileFmt = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public AlertManager(EclipseAC plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("alerts.log-to-file", true)) {
            openLog();
        }
    }

    private void openLog() {
        File dir = new File(plugin.getDataFolder(), "logs");
        if (!dir.exists()) dir.mkdirs();
        try {
            logWriter = new PrintWriter(new FileWriter(
                    new File(dir, "violations-" + fileFmt.format(new Date()) + ".log"), true), true);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not open violation log: " + e.getMessage());
        }
    }

    public void flag(Player player, String checkName, String checkType, String info, int vl, int ping) {
        String time = timeFmt.format(new Date());
        double tps  = Bukkit.getTPS()[0];

        String logLine = String.format("[%s] FLAG %-16s | %-12s | VL:%-3d | ping:%dms | tps:%.1f | %s",
                time, player.getName(), checkName, vl, ping, tps, info);

        if (plugin.getConfig().getBoolean("alerts.log-to-console", true)) {
            plugin.getLogger().warning(logLine);
        }
        if (logWriter != null) {
            logWriter.println(logLine);
        }

        if (!plugin.getConfig().getBoolean("alerts.broadcast-to-staff", true)) return;

        String vlColor   = vl < 6 ? "<green>" : vl < 15 ? "<yellow>" : "<red>";
        String pingColor = ping < 80 ? "<green>" : ping < 200 ? "<yellow>" : "<red>";
        String tpsColor  = tps > 18 ? "<green>" : tps > 15 ? "<yellow>" : "<red>";

        Component alert = MM.deserialize(
            "<dark_gray>◈ <gradient:#7B2FBE:#00C8FF>EclipseAC</gradient> <dark_gray>│ " +
            "<white>" + player.getName() + " <dark_gray>│ " +
            "<aqua>" + checkName + " <dark_gray>(<gray>" + checkType + "<dark_gray>) <dark_gray>│ " +
            vlColor + "VL:" + vl + " <dark_gray>│ " +
            "<dark_gray>(<gray>" + info + "<dark_gray>) " +
            pingColor + "ping:" + ping + "ms " +
            tpsColor  + "tps:" + String.format("%.1f", tps)
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!silenced.contains(p.getUniqueId()) && p.hasPermission("eclipseac.alerts")) {
                p.sendMessage(alert);
            }
        }
    }

    public void toggleAlerts(Player player) {
        String msg;
        if (silenced.remove(player.getUniqueId())) {
            msg = "<dark_gray>◈ <gradient:#7B2FBE:#00C8FF>EclipseAC</gradient> <dark_gray>│ <green>Alerts enabled.";
        } else {
            silenced.add(player.getUniqueId());
            msg = "<dark_gray>◈ <gradient:#7B2FBE:#00C8FF>EclipseAC</gradient> <dark_gray>│ <red>Alerts silenced.";
        }
        player.sendMessage(MM.deserialize(msg));
    }

    public boolean isSilenced(Player player) {
        return silenced.contains(player.getUniqueId());
    }

    public void closeLog() {
        if (logWriter != null) logWriter.close();
    }
}
