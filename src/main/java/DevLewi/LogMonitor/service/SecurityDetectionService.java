package DevLewi.LogMonitor.service;

import DevLewi.LogMonitor.event.LogEntryCreatedEvent;
import DevLewi.LogMonitor.model.LogEntry;
import DevLewi.LogMonitor.model.SecurityAlert;
import DevLewi.LogMonitor.repository.SecurityAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class SecurityDetectionService {

    private final SecurityAlertRepository securityAlertRepository;
    private final ILogProcessingService logProcessingService;

    private final Set<String> knownMaliciousIPs = Set.of(
            "192.168.1.100", "10.0.0.50", "172.16.0.25"
    );

    @EventListener
    public void handleLogEntryCreated(LogEntryCreatedEvent event) {
        analyzeLogEntry(event.getLogEntry());
    }

    public void analyzeLogEntry(LogEntry logEntry) {
        detectBruteForceAttack(logEntry);
        detectMaliciousIP(logEntry);
        detectUnusualErrors(logEntry);
        detectOffHoursAccess(logEntry);
    }

    private void detectBruteForceAttack(LogEntry logEntry) {
        if (logEntry.getMessage().toLowerCase().contains("failed login") ||
                logEntry.getStatusCode().equals("401")) {

            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long failedAttempts = logProcessingService.countByIpAddressSince(
                    logEntry.getIpAddress(), oneHourAgo);

            if (failedAttempts >= 5) {
                SecurityAlert alert = new SecurityAlert(
                        "BRUTE_FORCE",
                        "HIGH",
                        String.format("Multiple failed login attempts detected from IP: %s (%d attempts in last hour)",
                                logEntry.getIpAddress(), failedAttempts),
                        logEntry.getIpAddress()
                );
                alert.setDetectionRule("Failed login threshold exceeded");
                securityAlertRepository.save(alert);
            }
        }
    }

    private void detectMaliciousIP(LogEntry logEntry) {
        if (knownMaliciousIPs.contains(logEntry.getIpAddress())) {
            SecurityAlert alert = new SecurityAlert(
                    "MALICIOUS_IP",
                    "CRITICAL",
                    String.format("Access from known malicious IP address: %s", logEntry.getIpAddress()),
                    logEntry.getIpAddress()
            );
            alert.setDetectionRule("Known malicious IP database match");
            securityAlertRepository.save(alert);
        }
    }

    private void detectUnusualErrors(LogEntry logEntry) {
        if (logEntry.getStatusCode() != null &&
                (logEntry.getStatusCode().startsWith("5") || logEntry.getStatusCode().equals("403"))) {

            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
            long errorCount = logProcessingService.countByIpAddressSince(
                    logEntry.getIpAddress(), tenMinutesAgo);

            if (errorCount >= 10) {
                SecurityAlert alert = new SecurityAlert(
                        "UNUSUAL_ERRORS",
                        "MEDIUM",
                        String.format("High error rate detected from IP: %s (%d errors in 10 minutes)",
                                logEntry.getIpAddress(), errorCount),
                        logEntry.getIpAddress()
                );
                alert.setDetectionRule("High error rate threshold");
                securityAlertRepository.save(alert);
            }
        }
    }

    private void detectOffHoursAccess(LogEntry logEntry) {
        int hour = logEntry.getTimestamp().getHour();
        if (hour >= 22 || hour <= 6) {
            SecurityAlert alert = new SecurityAlert(
                    "OFF_HOURS_ACCESS",
                    "LOW",
                    String.format("Off-hours access detected from IP: %s at %s",
                            logEntry.getIpAddress(), logEntry.getTimestamp()),
                    logEntry.getIpAddress()
            );
            alert.setDetectionRule("Off-hours access pattern");
            securityAlertRepository.save(alert);
        }
    }
}