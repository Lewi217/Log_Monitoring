package DevLewi.LogMonitor.event;

import DevLewi.LogMonitor.model.LogEntry;
import org.springframework.context.ApplicationEvent;

public class LogEntryCreatedEvent extends ApplicationEvent {
    private final LogEntry logEntry;

    public LogEntryCreatedEvent(Object source, LogEntry logEntry) {
        super(source);
        this.logEntry = logEntry;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }
}