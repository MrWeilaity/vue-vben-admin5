package com.vben.system.service.system.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.service.system.IpLocationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 通过 HTTP 接口解析 IP 归属地。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpIpLocationResolver implements IpLocationResolver {

    private final ObjectMapper objectMapper;

    @Value("${logging.operation.ip-location.enabled:true}")
    private boolean enabled;

    @Value("${logging.operation.ip-location.url:https://whois.pconline.com.cn/ipJson.jsp}")
    private String url;

    @Value("${logging.operation.ip-location.timeout-ms:1500}")
    private long timeoutMs;

    @Override
    public String resolve(String ip) {
        if (!enabled || !StringUtils.hasText(ip) || isLocalIp(ip)) {
            return null;
        }
        try {
            HttpResponse<String> response;
            try (HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .build()) {
                URI uri = UriComponentsBuilder.fromUriString(url)
                        .queryParam("json", "true")
                        .queryParam("ip", ip)
                        .build(true)
                        .toUri();
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofMillis(timeoutMs))
                        .header("Accept", "application/json")
                        .header("User-Agent", "vben-service/operation-log")
                        .GET()
                        .build();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300 || !StringUtils.hasText(response.body())) {
                return null;
            }
            JsonNode root = objectMapper.readTree(response.body());
            String addr = trimToNull(root.path("addr").asText(null));
            if (addr != null) {
                return addr;
            }
            String province = trimToNull(root.path("pro").asText(null));
            String city = trimToNull(root.path("city").asText(null));
            String district = trimToNull(root.path("region").asText(null));
            String joined = String.join("",
                    province == null ? "" : province,
                    city == null ? "" : city,
                    district == null ? "" : district
            ).trim();
            return joined.isEmpty() ? null : joined;
        } catch (Exception e) {
            log.debug("解析IP归属地失败: ip={}, message={}", ip, e.getMessage());
            return null;
        }
    }

    private boolean isLocalIp(String ip) {
        if (ip.startsWith("172.")) {
            String[] segments = ip.split("\\.");
            if (segments.length >= 2) {
                try {
                    int second = Integer.parseInt(segments[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "127.0.0.1".equals(ip)
                || "localhost".equalsIgnoreCase(ip)
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("169.254.");
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
