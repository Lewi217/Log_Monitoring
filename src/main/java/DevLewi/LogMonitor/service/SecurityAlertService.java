package DevLewi.LogMonitor.service;

import DevLewi.LogMonitor.model.SecurityAlert;
import DevLewi.LogMonitor.repository.SecurityAlertRepository;
import DevLewi.LogMonitor.utils.exceptions.CustomExceptionResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SecurityAlertService implements ISecurityAlertService{

    private final SecurityAlertRepository alertRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SecurityAlert> getActiveAlerts() {
        String jpql = "SELECT a FROM SecurityAlert a WHERE a.resolved = false ORDER BY a.timestamp DESC";
        TypedQuery<SecurityAlert> query = entityManager.createQuery(jpql, SecurityAlert.class);
        return query.getResultList();
    }

    @Override
    public List<SecurityAlert> getRecentAlerts() {
        String jpql = "SELECT a FROM SecurityAlert a ORDER BY a.timestamp DESC";
        TypedQuery<SecurityAlert> query = entityManager.createQuery(jpql, SecurityAlert.class);
        query.setMaxResults(50);
        return query.getResultList();
    }

    @Override
    public long getTotalUnresolvedAlertsCount() {
        String jpql = "SELECT COUNT(a) FROM SecurityAlert a WHERE a.resolved = false";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult();
    }

    @Override
    public List<Object[]> getAlertsBySeverityCount() {
        String jpql = "SELECT a.severity, COUNT(a) FROM SecurityAlert a WHERE a.resolved = false GROUP BY a.severity";
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        return query.getResultList();
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUnresolvedAlerts", getTotalUnresolvedAlertsCount());
        data.put("alertsBySeverity", getAlertsBySeverityCount());
        data.put("recentAlerts", getRecentAlerts());
        return data;
    }

    @Override
    public SecurityAlert resolveAlert(Long id) {
        SecurityAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new CustomExceptionResponse("Alert not found with id: " + id));

        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Override
    public List<SecurityAlert> getAlertsBySeverity(String severity) {
        String jpql = "SELECT a FROM SecurityAlert a WHERE a.severity = :severity ORDER BY a.timestamp DESC";
        TypedQuery<SecurityAlert> query = entityManager.createQuery(jpql, SecurityAlert.class);
        query.setParameter("severity", severity.toUpperCase());
        return query.getResultList();
    }

    @Override
    public List<SecurityAlert> getAlertsByType(String alertType) {
        String jpql = "SELECT a FROM SecurityAlert a WHERE a.alertType = :type ORDER BY a.timestamp DESC";
        TypedQuery<SecurityAlert> query = entityManager.createQuery(jpql, SecurityAlert.class);
        query.setParameter("type", alertType.toUpperCase());
        return query.getResultList();
    }

    @Override
    public List<SecurityAlert> getAlertsByIpAddress(String ipAddress) {
        String jpql = "SELECT a FROM SecurityAlert a WHERE a.sourceIp = :ip ORDER BY a.timestamp DESC";
        TypedQuery<SecurityAlert> query = entityManager.createQuery(jpql, SecurityAlert.class);
        query.setParameter("ip", ipAddress);
        return query.getResultList();
    }
}

