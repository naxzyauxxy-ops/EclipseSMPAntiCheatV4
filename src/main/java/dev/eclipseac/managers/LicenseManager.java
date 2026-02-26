package dev.eclipseac.managers;

import dev.eclipseac.EclipseAC;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LicenseManager {

    private static final String DEFAULT_SERVER = "https://license.eclipseac.dev";

    private final EclipseAC plugin;
    private boolean valid = false;

    public LicenseManager(EclipseAC plugin) {
        this.plugin = plugin;
    }

    public boolean validate() {
        String key       = plugin.getConfig().getString("license.key", "");
        String serverUrl = plugin.getConfig().getString("license.server-url", DEFAULT_SERVER);

        if (key.isEmpty() || key.equalsIgnoreCase("YOUR-LICENSE-KEY-HERE")) {
            plugin.getLogger().severe("[EclipseAC] No license key set in config.yml.");
            return false;
        }

        try {
            String ip   = Bukkit.getIp().isEmpty() ? "localhost" : Bukkit.getIp();
            String port = String.valueOf(Bukkit.getPort());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String body = "{\"key\":\"" + key + "\","
                    + "\"ip\":\"" + ip + "\","
                    + "\"port\":\"" + port + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/api/validate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body().contains("\"valid\":true")) {
                valid = true;
                return true;
            }

            String reason = extractField(response.body(), "reason");
            plugin.getLogger().severe("[EclipseAC] License invalid: " + reason);
            return false;

        } catch (IOException | InterruptedException e) {
            plugin.getLogger().severe("[EclipseAC] Could not reach license server: " + e.getMessage());
            return false;
        }
    }

    public boolean isValid() {
        return valid;
    }

    private String extractField(String json, String field) {
        String search = "\"" + field + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "unknown";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "unknown" : json.substring(start, end);
    }
}
