package com.assentinel.core.application.exception;

import java.util.UUID;

public class RepositoryScanConfigurationException extends RuntimeException {
    public RepositoryScanConfigurationException(UUID projectId, String detail) {
        super("Repository scan configuration is incomplete for project " + projectId + ": " + detail);
    }
}
