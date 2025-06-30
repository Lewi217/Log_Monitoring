package DevLewi.LogMonitor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogEntryDto {
    @NotBlank
    private String source;

    @NotBlank
    private String message;

    private String ipAddress;
    private String userAgent;
    private String statusCode;
    private String logLevel;
    private LocalDateTime timestamp;
}