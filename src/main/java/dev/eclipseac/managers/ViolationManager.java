package dev.eclipseac.managers;

import dev.eclipseac.EclipseAC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationManager {

    private final EclipseAC plugin;
    private final Map<UUID, Map<String, Integer>> violations = new ConcurrentHashMap<>();

    public ViolationManager(EclipseAC plugin) {
        this.plugin = plugin;
        startDecay();
    }

    public int getVL(Player player, String check) {
        return violations.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(check, 0);
    }

    public int increaseVL(Player player, String check, int amount) {
        violations.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        var map   = violations.get(player.getUniqueId());
        int newVl = map.getOrDefault(check, 0) + amount;
        map.put(check, newVl);
        return newVl;
    }

    public void decreaseVL(Player player, String check, int amount) {
        var map = violations.get(player.getUniqueId());
        if (map == null) return;
        int cur = map.getOrDefault(check, 0);
        if (cur > 0) map.put(check, Math.max(0, cur - amount));
    }

    public void resetVL(Player player, String check) {
        var map = violations.get(player.getUniqueId());
        if (map != null) map.remove(check);
    }

    public void resetAllVL(Player player) {
        violations.remove(player.getUniqueId());
    }

    public Map<String, Integer> getAllViolations(Player player) {
        return new HashMap<>(violations.getOrDefault(player.getUniqueId(), new HashMap<>()));
    }

    public void removePlayer(UUID uuid) {
        violations.remove(uuid);
    }

    private void startDecay() {
        int interval = plugin.getConfig().getInt("violations.decay-interval-seconds", 30);
        int amount   = plugin.getConfig().getInt("violations.decay-amount", 1);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (var entry : violations.entrySet()) {
                entry.getValue().replaceAll((check, vl) -> Math.max(0, vl - amount));
            }
        }, interval * 20L, interval * 20L);
    }
}
