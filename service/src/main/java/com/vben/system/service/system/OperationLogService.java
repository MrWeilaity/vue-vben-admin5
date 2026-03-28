package com.vben.system.service.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.dto.system.log.OperationLogRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 操作日志服务。
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private static final String OP_LOG_KEY = "audit:operation:logs";
    private static final long OP_LOG_MAX_SIZE = 2000;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(OperationLogRecord record) {
        try {
            redisTemplate.opsForList().leftPush(OP_LOG_KEY, objectMapper.writeValueAsString(record));
            redisTemplate.opsForList().trim(OP_LOG_KEY, 0, OP_LOG_MAX_SIZE - 1);
        } catch (Exception ignored) {
        }
    }

    public List<OperationLogRecord> list(String keyword, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<String> rows = redisTemplate.opsForList().range(OP_LOG_KEY, 0, OP_LOG_MAX_SIZE - 1);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        List<OperationLogRecord> data = new ArrayList<>();
        String kw = StringUtils.hasText(keyword) ? keyword.toLowerCase(Locale.ROOT) : null;
        for (String row : rows) {
            try {
                OperationLogRecord log = objectMapper.readValue(row, OperationLogRecord.class);
                if (kw == null || containsKeyword(log, kw)) {
                    data.add(log);
                    if (data.size() >= safeLimit) {
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return data;
    }

    private boolean containsKeyword(OperationLogRecord log, String keyword) {
        return contains(log.getUsername(), keyword)
            || contains(log.getPath(), keyword)
            || contains(log.getMethod(), keyword)
            || contains(log.getOperation(), keyword)
            || contains(log.getIp(), keyword)
            || contains(log.getResult(), keyword)
            || contains(log.getError(), keyword);
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
