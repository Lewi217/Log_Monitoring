package DevLewi.LogMonitor.service;

import DevLewi.LogMonitor.model.SecurityAlert;

import java.util.List;
import java.util.Map;

public interface ISecurityAlertService {
    List<SecurityAlert> getActiveAlerts();
    List<SecurityAlert> getRecentAlerts();
    long getTotalUnresolvedAlertsCount();
    List<Object[]> getAlertsBySeverityCount();
    Map<String, Object> getDashboardData();
    SecurityAlert resolveAlert(Long id);
    List<SecurityAlert> getAlertsBySeverity(String severity);
    List<SecurityAlert> getAlertsByType(String alertType);
    List<SecurityAlert> getAlertsByIpAddress(String ipAddress);
}
