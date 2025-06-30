package DevLewi.LogMonitor.controller;

import DevLewi.LogMonitor.dto.LogEntryDto;
import DevLewi.LogMonitor.model.LogEntry;
import DevLewi.LogMonitor.response.ApiResponse;
import DevLewi.LogMonitor.service.ILogProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static DevLewi.LogMonitor.utils.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static DevLewi.LogMonitor.utils.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {

    private final ILogProcessingService logProcessingService;

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse> ingestLog(@Valid @RequestBody LogEntryDto logDto) {
        try {
            LogEntry processed = logProcessingService.processLogEntry(logDto);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, processed));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse> getRecentLogs() {
        try {
            List<LogEntry> logs = logProcessingService.getRecentLogs();
            if (logs.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "No recent logs found"));
            }
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, logs));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/errors")
    public ResponseEntity<ApiResponse> getErrorLogs() {
        try {
            List<LogEntry> errorLogs = logProcessingService.getErrorLogs();
            if (errorLogs.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "No error logs found"));
            }
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, errorLogs));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/by-ip")
    public ResponseEntity<ApiResponse> getLogsByIpAddress(@RequestParam String ipAddress) {
        try {
            List<LogEntry> logs = logProcessingService.getLogsByIpAddress(ipAddress);
            if (logs.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "No logs found for IP: " + ipAddress));
            }
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, logs));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }
}
