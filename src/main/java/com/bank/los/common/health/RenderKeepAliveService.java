package com.bank.los.common.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service to automatically ping the public Render / Cloud deployment URL
 * every 10 minutes to prevent the free tier instance from spinning down due to inactivity.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.render.keep-alive.enabled", havingValue = "true", matchIfMissing = true)
public class RenderKeepAliveService {

    @Value("${app.render.keep-alive.url:}")
    private String rawBaseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Executes every 10 minutes (600,000 ms) with an initial 1-minute delay after application startup.
     */
    @Scheduled(fixedRateString = "${app.render.keep-alive.interval-ms:600000}", initialDelay = 60000)
    public void pingSelf() {
        if (rawBaseUrl == null || rawBaseUrl.trim().isEmpty()) {
            log.debug("[Render Keep-Alive] No keep-alive URL configured (app.render.keep-alive.url). Skipping internal ping.");
            return;
        }

        String baseUrl = rawBaseUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String healthEndpoint = baseUrl + "/api/v1/health";

        try {
            log.info("[Render Keep-Alive] Sending heartbeat ping to keep backend alive: {}", healthEndpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthEndpoint))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "LOS-Render-KeepAlive/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[Render Keep-Alive] Ping successful! Status: {} OK (Backend will stay active on Render)", response.statusCode());
            } else {
                log.warn("[Render Keep-Alive] Ping returned unexpected status: {}", response.statusCode());
            }
        } catch (Exception ex) {
            log.warn("[Render Keep-Alive] Ping heartbeat attempt failed for {}: {}", healthEndpoint, ex.getMessage());
        }
    }
}
