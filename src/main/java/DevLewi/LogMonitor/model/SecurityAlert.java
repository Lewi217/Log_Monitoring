package DevLewi.LogMonitor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "security_alerts")
public class SecurityAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alertType;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "affected_resource")
    private String affectedResource;

    @Column(name = "detection_rule")
    private String detectionRule;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "resolved")
    private boolean resolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public SecurityAlert(String alertType, String severity, String description, String ipAddress) {
        this.alertType = alertType;
        this.severity = severity;
        this.description = description;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
        this.resolved = false;
    }

}
