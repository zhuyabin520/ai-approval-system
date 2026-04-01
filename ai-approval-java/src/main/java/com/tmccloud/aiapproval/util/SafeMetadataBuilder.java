package com.tmccloud.aiapproval.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SafeMetadataBuilder {

    private final Map<String, Object> metadata = new HashMap<>();

    public SafeMetadataBuilder put(String key, Object value) {
        if (value == null) {
            // 根据类型设置默认值
            metadata.put(key, getDefaultValue(value));
        } else {
            metadata.put(key, value);
        }
        return this;
    }

    public SafeMetadataBuilder putString(String key, String value) {
        metadata.put(key, value != null ? value : "");
        return this;
    }

    public SafeMetadataBuilder putLong(String key, Long value) {
        metadata.put(key, value != null ? value : 0L);
        return this;
    }

    public SafeMetadataBuilder putInt(String key, Integer value) {
        metadata.put(key, value != null ? value : 0);
        return this;
    }

    private Object getDefaultValue(Object value) {
        if (value instanceof String) {
            return "";
        } else if (value instanceof Long) {
            return 0L;
        } else if (value instanceof Integer) {
            return 0;
        } else if (value instanceof Double) {
            return 0.0;
        } else if (value instanceof Boolean) {
            return false;
        }
        return "";
    }

    public Map<String, Object> build() {
        return metadata;
    }

    /**
     * 安全复制元数据，过滤 null 值
     */
    public static Map<String, Object> safeCopy(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                result.put(key, "");
            } else if (value instanceof String) {
                result.put(key, value);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
}