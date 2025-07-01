package DevLewi.LogMonitor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAlertDto {
    private Long id;
    private String alertType;
    private String severity;
    private String description;
    private String ipAddress;
    private String affectedResource;
    private String detectionRule;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private boolean resolved;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    // Constructor for creating new alerts
    public SecurityAlertDto(String alertType, String severity, String description, String ipAddress) {
        this.alertType = alertType;
        this.severity = severity;
        this.description = description;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
        this.resolved = false;
    }
}
