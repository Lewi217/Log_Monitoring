package DevLewi.LogMonitor.service;

import DevLewi.LogMonitor.dto.LogEntryDto;
import DevLewi.LogMonitor.model.LogEntry;

import java.time.LocalDateTime;
import java.util.List;

public interface ILogProcessingService {
    LogEntry processLogEntry(LogEntryDto logDto);
    List<LogEntry> getRecentLogs();
    List<LogEntry> getLogsByIpAddressAndTimestampAfter(String ipAddress, LocalDateTime timestamp);
    long countByIpAddressSince(String ipAddress, LocalDateTime since);
    List<LogEntry> getErrorLogs();
    List<String> getDistinctIpAddressesSince(LocalDateTime since);
    List<LogEntry> getLogsByIpAddress(String ipAddress);
}
