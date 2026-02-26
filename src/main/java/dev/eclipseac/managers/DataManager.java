package dev.eclipseac.managers;

import dev.eclipseac.EclipseAC;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final EclipseAC plugin;
    private final Map<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();

    public DataManager(EclipseAC plugin) {
        this.plugin = plugin;
    }

    public PlayerData getData(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player));
    }

    public PlayerData getData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void removeData(UUID uuid) {
        dataMap.remove(uuid);
    }

    public Collection<PlayerData> getAllData() {
        return dataMap.values();
    }

    public void cleanup() {
        dataMap.clear();
    }
}
