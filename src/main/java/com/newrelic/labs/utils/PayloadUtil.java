package com.newrelic.labs.utils;

public class PayloadUtil {
    public static final String INTEGRATION_NAME = "com.newrelic.as400";
    public static final String PROTOCOL_VERSION = "3";
    public static final String INTEGRATION_VERSION = "1.2.2";
    public static final String DEFAULT_ENTITY_TYPE = "as400";

    public static String resolveEntityType() {
        String envType = System.getenv("ENTITY_REMOTE_TYPE");
        if (envType != null && !envType.trim().isEmpty()) {
            return envType.trim();
        }
        envType = System.getenv("ENTITY_TYPE");
        if (envType != null && !envType.trim().isEmpty()) {
            return envType.trim();
        }
        return DEFAULT_ENTITY_TYPE;
    }

    /**
     * Resolves the entity name based on ENTITY_NAME env var, system name, host, or fallback variables.
     */
    public static String resolveEntityName(String systemName, String fallbackHost) {
        String envEntity = System.getenv("ENTITY_NAME");
        if (envEntity != null && !envEntity.trim().isEmpty()) {
            return envEntity.trim();
        }
        if (systemName != null && !systemName.trim().isEmpty()) {
            return systemName.trim();
        }
        if (fallbackHost != null && !fallbackHost.trim().isEmpty()) {
            return fallbackHost.trim();
        }
        String envHost = System.getenv("AS400HOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            return envHost.trim();
        }
        String envInstance = System.getenv("INSTANCE");
        if (envInstance != null && !envInstance.trim().isEmpty()) {
            return envInstance.trim();
        }
        return "as400-system";
    }

    /**
     * Formats the Protocol v3 JSON payload with remote entity and metrics.
     * 
     * @param entityName The unique name of the AS400 entity (e.g. system name or host)
     * @param entityType The entity type namespace (e.g. "host")
     * @param metricsJson The metrics JSON array content (e.g. "[{...},{...}]" or raw comma-separated objects)
     * @return Complete JSON string conforming to New Relic Infrastructure Integration Protocol v3
     */
    public static String buildProtocolV3Json(String entityName, String entityType, String metricsJson) {
        String cleanEntityName = (entityName != null && !entityName.trim().isEmpty()) ? entityName.trim() : "as400-system";
        String cleanEntityType = (entityType != null && !entityType.trim().isEmpty()) ? entityType.trim() : resolveEntityType();

        String formattedMetrics;
        if (metricsJson == null || metricsJson.trim().isEmpty() || metricsJson.trim().equals("[]")) {
            formattedMetrics = "[]";
        } else {
            String trimmed = metricsJson.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                formattedMetrics = trimmed;
            } else {
                if (trimmed.endsWith(",")) {
                    trimmed = trimmed.substring(0, trimmed.length() - 1);
                }
                formattedMetrics = "[" + trimmed + "]";
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(INTEGRATION_NAME).append("\",");
        sb.append("\"protocol_version\":\"").append(PROTOCOL_VERSION).append("\",");
        sb.append("\"integration_version\":\"").append(INTEGRATION_VERSION).append("\",");
        sb.append("\"data\":[");
          sb.append("{");
            sb.append("\"entity\":{");
              sb.append("\"name\":\"").append(escapeJson(cleanEntityName)).append("\",");
              sb.append("\"type\":\"").append(escapeJson(cleanEntityType)).append("\"");
            sb.append("},");
            sb.append("\"metrics\":").append(formattedMetrics).append(",");
            sb.append("\"inventory\":{},");
            sb.append("\"events\":[]");
          sb.append("}");
        sb.append("]");
        sb.append("}");

        return sb.toString();
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
