package com.aws.dodo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CloudWatchLogService {

    private static final Logger log = LoggerFactory.getLogger(CloudWatchLogService.class);
    private static final String LOG_GROUP = "/aws/lambda/user-management";

    private final CloudWatchLogsClient cloudWatchLogsClient;

    public CloudWatchLogService(CloudWatchLogsClient cloudWatchLogsClient) {
        this.cloudWatchLogsClient = cloudWatchLogsClient;
    }

    public void logOperationStart(String operation, String userId, String requestId) {
        Map<String, String> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("operation", operation);
        logData.put("userId", userId);
        logData.put("requestId", requestId);
        logData.put("level", "INFO");
        logData.put("status", "START");

        log.info("Operation started: {} - userId: {} - requestId: {}", operation, userId, requestId);
        writeToCloudWatch(logData);
    }

    public void logOperationSuccess(String operation, String userId, String requestId, long duration) {
        Map<String, String> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("operation", operation);
        logData.put("userId", userId);
        logData.put("requestId", requestId);
        logData.put("durationMs", String.valueOf(duration));
        logData.put("status", "SUCCESS");
        logData.put("level", "INFO");

        log.info("Operation completed: {} - duration: {}ms - userId: {} - requestId: {}",
                operation, duration, userId, requestId);
        writeToCloudWatch(logData);
    }

    public void logOperationError(String operation, String userId, String requestId, Exception e) {
        Map<String, String> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("operation", operation);
        logData.put("userId", userId);
        logData.put("requestId", requestId);
        logData.put("error", e.getMessage());
        logData.put("errorType", e.getClass().getSimpleName());
        logData.put("status", "ERROR");
        logData.put("level", "ERROR");

        log.error("Operation failed: {} - userId: {} - requestId: {} - error: {}",
                operation, userId, requestId, e.getMessage(), e);
        writeToCloudWatch(logData);
    }

    public void logDynamoDBOperation(String operation, String table, String itemId,
                                     boolean success, long duration) {
        Map<String, String> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("service", "DynamoDB");
        logData.put("operation", operation);
        logData.put("table", table);
        logData.put("itemId", itemId);
        logData.put("success", String.valueOf(success));
        logData.put("durationMs", String.valueOf(duration));
        logData.put("level", "DEBUG");

        log.debug("DynamoDB {} on '{}' for itemId '{}' - duration: {}ms",
                operation, table, itemId, duration);
        writeToCloudWatch(logData);
    }

    public void logApiGatewayEvent(String httpMethod, String path, String requestId) {
        Map<String, String> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("source", "APIGateway");
        logData.put("httpMethod", httpMethod);
        logData.put("path", path);
        logData.put("requestId", requestId);
        logData.put("level", "INFO");

        log.info("API Gateway: {} {} - requestId: {}", httpMethod, path, requestId);
        writeToCloudWatch(logData);
    }

    public void logValidationError(String field, String reason, String requestId) {
        Map<String, String> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("field", field);
        logData.put("reason", reason);
        logData.put("requestId", requestId);
        logData.put("level", "WARN");

        log.warn("Validation error - field: {} - reason: {} - requestId: {}",
                field, reason, requestId);
        writeToCloudWatch(logData);
    }

    private void writeToCloudWatch(Map<String, String> logData) {
        try {
            String logStreamName = "lambda-" + Instant.now().getEpochSecond();
            String logMessage = formatLogMessage(logData);

            InputLogEvent logEvent = InputLogEvent.builder()
                    .timestamp(System.currentTimeMillis())
                    .message(logMessage)
                    .build();

            PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                    .logGroupName(LOG_GROUP)
                    .logStreamName(logStreamName)
                    .logEvents(Collections.singletonList(logEvent))
                    .build();

            cloudWatchLogsClient.putLogEvents(putLogEventsRequest);
        } catch (Exception e) {
            log.warn("Failed to write to CloudWatch: {}", e.getMessage());
        }
    }

    private String formatLogMessage(Map<String, String> logData) {
        StringBuilder sb = new StringBuilder();
        logData.forEach((key, value) -> sb.append(key).append("=").append(value).append(" | "));
        return sb.toString();
    }
}
