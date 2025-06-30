package DevLewi.LogMonitor.controller;

import DevLewi.LogMonitor.model.SecurityAlert;
import DevLewi.LogMonitor.response.ApiResponse;
import DevLewi.LogMonitor.service.ISecurityAlertService;
import DevLewi.LogMonitor.utils.exceptions.CustomExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static DevLewi.LogMonitor.utils.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static DevLewi.LogMonitor.utils.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final ISecurityAlertService alertService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveAlerts() {
        try {
            List<SecurityAlert> alerts = alertService.getActiveAlerts();
            if (alerts.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "No active alerts found"));
            }
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, alerts));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse> getRecentAlerts() {
        try {
            List<SecurityAlert> alerts = alertService.getRecentAlerts();
            if (alerts.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "No recent alerts found"));
            }
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, alerts));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboardData() {
        try {
            Map<String, Object> data = alertService.getDashboardData();
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, data));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse> resolveAlert(@PathVariable Long id) {
        try {
            SecurityAlert resolvedAlert = alertService.resolveAlert(id);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, resolvedAlert));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/by-severity")
    public ResponseEntity<ApiResponse> getAlertsBySeverity(@RequestParam String severity) {
        try {
            List<SecurityAlert> alerts = alertService.getAlertsBySeverity(severity);
            if (alerts.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "No alerts found for severity: " + severity));
            }
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, alerts));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse> getAlertsCount() {
        try {
            long count = alertService.getTotalUnresolvedAlertsCount();
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, "Total unresolved alerts: " + count));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }
}