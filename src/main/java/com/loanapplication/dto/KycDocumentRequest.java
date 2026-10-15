
package com.loanapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class KycDocumentRequest {

    @NotBlank(message = "Document type is required")
    @Size(max = 100, message = "Document type cannot exceed 100 characters")
    private String documentType;

    @NotBlank(message = "File path is required")
    @Size(max = 1000, message = "File path cannot exceed 1000 characters")
    private String filePath;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
