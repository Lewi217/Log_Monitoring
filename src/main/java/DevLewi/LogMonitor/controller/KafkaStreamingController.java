package DevLewi.LogMonitor.controller;

import DevLewi.LogMonitor.dto.LogEntryDto;
import DevLewi.LogMonitor.dto.SecurityAlertDto;
import DevLewi.LogMonitor.response.ApiResponse;
import DevLewi.LogMonitor.service.KafkaProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static DevLewi.LogMonitor.utils.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;

@RestController
@RequestMapping("/api/streaming")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class KafkaStreamingController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/logs/publish")
    public ResponseEntity<ApiResponse> publishLogEntry(@Valid @RequestBody LogEntryDto logEntry) {
        try {
            kafkaProducerService.sendLogEntry(logEntry);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,
                    "Log entry published to Kafka successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("ERROR", "Failed to publish log entry: " + e.getMessage()));
        }
    }

    @PostMapping("/logs/bulk-publish")
    public ResponseEntity<ApiResponse> publishBulkLogEntries(@Valid @RequestBody List<LogEntryDto> logEntries) {
        try {
            for (LogEntryDto logEntry : logEntries) {
                kafkaProducerService.sendLogEntry(logEntry);
            }
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,
                    String.format("Successfully published %d log entries to Kafka", logEntries.size())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("ERROR", "Failed to publish bulk log entries: " + e.getMessage()));
        }
    }

    @PostMapping("/alerts/publish")
    public ResponseEntity<ApiResponse> publishSecurityAlert(@RequestBody @Valid SecurityAlertDto alert) {
        try {
            kafkaProducerService.sendSecurityAlert(alert);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,
                    "Security alert published to Kafka successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("ERROR", "Failed to publish security alert: " + e.getMessage()));
        }
    }

    @PostMapping("/metrics/publish")
    public ResponseEntity<ApiResponse> publishSystemMetrics(@RequestBody Map<String, Object> metrics) {
        try {
            kafkaProducerService.sendSystemMetrics(metrics);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,
                    "System metrics published to Kafka successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("ERROR", "Failed to publish system metrics: " + e.getMessage()));
        }
    }

    @PostMapping("/events/publish")
    public ResponseEntity<ApiResponse> publishRealTimeEvent(
            @RequestParam String eventType,
            @RequestBody Object eventData) {
        try {
            kafkaProducerService.sendRealTimeEvent(eventType, eventData);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,
                    "Real-time event published to Kafka successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("ERROR", "Failed to publish real-time event: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,
                "Kafka streaming service is healthy"));
    }
}