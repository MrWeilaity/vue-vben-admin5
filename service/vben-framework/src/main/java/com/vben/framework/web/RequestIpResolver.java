package com.vben.framework.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * 解析客户端真实 IP。
 */
@Component
public class RequestIpResolver {

    private static final List<String> IP_HEADER_CANDIDATES = List.of(
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_CLIENT_IP",
        "X-Forwarded"
    );

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        for (String headerName : IP_HEADER_CANDIDATES) {
            String headerValue = request.getHeader(headerName);
            String ip = extractIpFromHeader(headerValue);
            if (ip != null) {
                return normalizeIp(ip);
            }
        }
        String forwarded = request.getHeader("Forwarded");
        String forwardedIp = extractIpFromForwarded(forwarded);
        if (forwardedIp != null) {
            return normalizeIp(forwardedIp);
        }
        return normalizeIp(request.getRemoteAddr());
    }

    private String extractIpFromHeader(String headerValue) {
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        for (String part : headerValue.split(",")) {
            String candidate = cleanIpToken(part);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private String extractIpFromForwarded(String forwarded) {
        if (!StringUtils.hasText(forwarded)) {
            return null;
        }
        for (String part : forwarded.split(";")) {
            String trimmed = part.trim();
            if (!trimmed.toLowerCase(Locale.ROOT).startsWith("for=")) {
                continue;
            }
            String candidate = cleanIpToken(trimmed.substring(4));
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private String cleanIpToken(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 1) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.startsWith("[")) {
            int end = cleaned.indexOf(']');
            if (end > 0) {
                cleaned = cleaned.substring(1, end);
            }
        } else if (cleaned.chars().filter(ch -> ch == ':').count() == 1) {
            int idx = cleaned.indexOf(':');
            cleaned = cleaned.substring(0, idx);
        }
        if (!StringUtils.hasText(cleaned)) {
            return null;
        }
        String lowered = cleaned.toLowerCase(Locale.ROOT);
        if ("unknown".equals(lowered) || "null".equals(lowered)) {
            return null;
        }
        return cleaned;
    }

    private String normalizeIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return null;
        }
        String trimmed = ip.trim();
        if ("0:0:0:0:0:0:0:1".equals(trimmed) || "::1".equals(trimmed)) {
            return "127.0.0.1";
        }
        if (trimmed.startsWith("::ffff:")) {
            return trimmed.substring(7);
        }
        return trimmed;
    }
}
