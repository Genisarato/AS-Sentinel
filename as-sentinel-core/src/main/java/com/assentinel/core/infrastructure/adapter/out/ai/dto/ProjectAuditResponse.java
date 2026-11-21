package com.assentinel.core.infrastructure.adapter.out.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProjectAuditResponse(
    @JsonProperty("directory_path") String directoryPath,
    @JsonProperty("total_files_scanned") int totalFilesScanned,
    @JsonProperty("total_violations") int totalViolations,
    @JsonProperty("is_project_compliant") boolean projectCompliant,
    List<FileValidationResult> results
) {}
