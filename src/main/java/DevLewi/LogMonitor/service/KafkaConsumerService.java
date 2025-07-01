package DevLewi.LogMonitor.service;

import DevLewi.LogMonitor.dto.LogEntryDto;
import DevLewi.LogMonitor.dto.SecurityAlertDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ILogProcessingService logProcessingService;
    private final SecurityDetectionService securityDetectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.logs:log-entries}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLogEntries(
            @Payload LogEntryDto logEntryDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            Acknowledgment acknowledgment) {

        try {
            logProcessingService.processLogEntry(logEntryDto);
            acknowledgment.acknowledge();
        } catch (Exception e) {

        }
    }

    @KafkaListener(topics = "${kafka.topics.alerts:security-alerts}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSecurityAlerts(
            @Payload SecurityAlertDto alertDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            Acknowledgment acknowledgment) {

        try {
            processSecurityAlert(alertDto);
            acknowledgment.acknowledge();
        } catch (Exception e) {

        }
    }

    @KafkaListener(topics = "external-logs", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeExternalLogs(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            Acknowledgment acknowledgment) {

        try {
            Map<String, Object> externalLog = objectMapper.readValue(message, Map.class);
            LogEntryDto logEntryDto = convertExternalLogToInternal(externalLog);
            logProcessingService.processLogEntry(logEntryDto);
            acknowledgment.acknowledge();
        } catch (Exception e) {

        }
    }

    @KafkaListener(topics = "bulk-logs", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "bulkKafkaListenerContainerFactory")
    public void consumeBulkLogs(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        try {
            LogEntryDto[] bulkLogs = objectMapper.readValue(message, LogEntryDto[].class);

            for (LogEntryDto logEntry : bulkLogs) {
                try {
                    logProcessingService.processLogEntry(logEntry);
                } catch (Exception e) {
                }
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {

        }
    }

    private void processSecurityAlert(SecurityAlertDto alertDto) {
        if ("CRITICAL".equals(alertDto.getSeverity())) {

        }
    }

    private LogEntryDto convertExternalLogToInternal(Map<String, Object> externalLog) {
        LogEntryDto logEntryDto = new LogEntryDto();

        logEntryDto.setSource((String) externalLog.getOrDefault("system", "external"));
        logEntryDto.setMessage((String) externalLog.getOrDefault("msg", ""));
        logEntryDto.setIpAddress((String) externalLog.get("client_ip"));
        logEntryDto.setLogLevel((String) externalLog.getOrDefault("level", "INFO"));
        logEntryDto.setStatusCode((String) externalLog.get("status"));

        if (externalLog.containsKey("timestamp")) {
        }

        return logEntryDto;
    }
}