package DevLewi.LogMonitor.service;

import DevLewi.LogMonitor.dto.LogEntryDto;
import DevLewi.LogMonitor.event.LogEntryCreatedEvent;
import DevLewi.LogMonitor.model.LogEntry;
import DevLewi.LogMonitor.repository.LogEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LogProcessingService implements ILogProcessingService {

    private final LogEntryRepository logEntryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public LogEntry processLogEntry(LogEntryDto logDto) {
        LogEntry logEntry = new LogEntry(
                logDto.getSource(),
                logDto.getMessage(),
                logDto.getIpAddress(),
                logDto.getLogLevel()
        );

        logEntry.setUserAgent(logDto.getUserAgent());
        logEntry.setStatusCode(logDto.getStatusCode());

        if (logDto.getTimestamp() != null) {
            logEntry.setTimestamp(logDto.getTimestamp());
        }

        LogEntry saved = logEntryRepository.save(logEntry);
        eventPublisher.publishEvent(new LogEntryCreatedEvent(this, saved));

        return saved;
    }

    @Override
    public List<LogEntry> getRecentLogs() {
        String jpql = "SELECT l FROM LogEntry l ORDER BY l.timestamp DESC";
        TypedQuery<LogEntry> query = entityManager.createQuery(jpql, LogEntry.class);
        query.setMaxResults(100);
        return query.getResultList();
    }

    @Override
    public List<LogEntry> getLogsByIpAddressAndTimestampAfter(String ipAddress, LocalDateTime timestamp) {
        String jpql = "SELECT l FROM LogEntry l WHERE l.ipAddress = :ip AND l.timestamp >= :timestamp ORDER BY l.timestamp DESC";
        TypedQuery<LogEntry> query = entityManager.createQuery(jpql, LogEntry.class);
        query.setParameter("ip", ipAddress);
        query.setParameter("timestamp", timestamp);
        return query.getResultList();
    }

    @Override
    public long countByIpAddressSince(String ipAddress, LocalDateTime since) {
        String jpql = "SELECT COUNT(l) FROM LogEntry l WHERE l.ipAddress = :ip AND l.timestamp >= :since";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("ip", ipAddress);
        query.setParameter("since", since);
        return query.getSingleResult();
    }

    @Override
    public List<LogEntry> getErrorLogs() {
        String jpql = "SELECT l FROM LogEntry l WHERE l.statusCode LIKE '4%' OR l.statusCode LIKE '5%' ORDER BY l.timestamp DESC";
        TypedQuery<LogEntry> query = entityManager.createQuery(jpql, LogEntry.class);
        return query.getResultList();
    }

    @Override
    public List<String> getDistinctIpAddressesSince(LocalDateTime since) {
        String jpql = "SELECT DISTINCT l.ipAddress FROM LogEntry l WHERE l.timestamp >= :since";
        TypedQuery<String> query = entityManager.createQuery(jpql, String.class);
        query.setParameter("since", since);
        return query.getResultList();
    }

    @Override
    public List<LogEntry> getLogsByIpAddress(String ipAddress) {
        String jpql = "SELECT l FROM LogEntry l WHERE l.ipAddress = :ip ORDER BY l.timestamp DESC";
        TypedQuery<LogEntry> query = entityManager.createQuery(jpql, LogEntry.class);
        query.setParameter("ip", ipAddress);
        return query.getResultList();
    }
}