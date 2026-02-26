package dev.eclipseac.checks;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Check implements Listener {

    protected final EclipseAC plugin;
    protected final String checkName;
    protected final String checkType;

    private static final double MIN_TPS = 17.0;

    public Check(EclipseAC plugin, String checkName, String checkType) {
        this.plugin    = plugin;
        this.checkName = checkName;
        this.checkType = checkType;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("checks." + checkName + ".enabled", true);
    }

    protected void flag(Player player, String info) {
        flag(player, info, 1);
    }

    protected void flag(Player player, String info, int vlIncrease) {
        if (!isEnabled()) return;
        if (player.hasPermission("eclipseac.bypass")) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isExempt() || data.isInGracePeriod()) return;

        if (Bukkit.getTPS()[0] < MIN_TPS) return;

        int ping = player.getPing();
        if (ping > 350) return;

        int vl = plugin.getViolationManager().increaseVL(player, checkName, vlIncrease);

        int alertEvery = getConfigInt("alert-every", 1);
        if (vl % alertEvery == 0 || vl == 1) {
            plugin.getAlertManager().flag(player, checkName, checkType, info, vl, ping);
        }

        int punishVl = getConfigInt("punishment-vl", 20);
        if (vl >= punishVl) {
            plugin.getPunishmentManager().punish(player, checkName, vl);
        }
    }

    protected void reward(Player player) {
        plugin.getViolationManager().decreaseVL(player, checkName, 1);
    }

    protected int getVL(Player player) {
        return plugin.getViolationManager().getVL(player, checkName);
    }

    protected int     getConfigInt   (String key, int     def) { return plugin.getConfig().getInt    ("checks." + checkName + "." + key, def); }
    protected double  getConfigDouble(String key, double  def) { return plugin.getConfig().getDouble  ("checks." + checkName + "." + key, def); }
    protected boolean getConfigBool  (String key, boolean def) { return plugin.getConfig().getBoolean ("checks." + checkName + "." + key, def); }

    public String getCheckName() { return checkName; }
    public String getCheckType() { return checkType; }
}
