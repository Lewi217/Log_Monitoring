package DevLewi.LogMonitor.service;

import DevLewi.LogMonitor.dto.LogEntryDto;
import DevLewi.LogMonitor.dto.SecurityAlertDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.logs:log-entries}")
    private String logsTopic;

    @Value("${kafka.topics.alerts:security-alerts}")
    private String alertsTopic;

    @Value("${kafka.topics.metrics:system-metrics}")
    private String metricsTopic;

    public void sendLogEntry(LogEntryDto logEntry) {
        try {
            String key = generateLogKey(logEntry);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(logsTopic, key, logEntry);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                }
            });
        } catch (Exception e) {
        }
    }

    public void sendSecurityAlert(SecurityAlertDto alert) {
        try {
            String key = generateAlertKey(alert);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(alertsTopic, key, alert);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                }
            });
        } catch (Exception e) {
        }
    }

    public void sendSystemMetrics(Map<String, Object> metrics) {
        try {
            String key = "system-metrics-" + System.currentTimeMillis();
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(metricsTopic, key, metrics);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                }
            });
        } catch (Exception e) {
        }
    }

    public void sendRealTimeEvent(String eventType, Object eventData) {
        try {
            String topic = "realtime-events";
            String key = eventType + "-" + System.currentTimeMillis();

            Map<String, Object> eventWrapper = Map.of(
                    "eventType", eventType,
                    "timestamp", LocalDateTime.now(),
                    "data", eventData
            );

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, eventWrapper);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                }
            });
        } catch (Exception e) {
        }
    }

    private String generateLogKey(LogEntryDto logEntry) {
        return String.format("%s-%s-%d",
                logEntry.getSource(),
                logEntry.getIpAddress() != null ? logEntry.getIpAddress() : "unknown",
                System.currentTimeMillis());
    }

    private String generateAlertKey(SecurityAlertDto alert) {
        return String.format("%s-%s-%d",
                alert.getAlertType(),
                alert.getSeverity(),
                System.currentTimeMillis());
    }
}