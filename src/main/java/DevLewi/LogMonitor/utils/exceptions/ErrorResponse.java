package DevLewi.LogMonitor.utils.exceptions;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {
}