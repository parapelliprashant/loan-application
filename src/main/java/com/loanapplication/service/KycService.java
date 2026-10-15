
package com.loanapplication.service;

import com.loanapplication.dto.KycDocumentResponse;
import com.loanapplication.entities.KycDocument;

import java.util.List;

public interface KycService {

    KycDocument submitDocument(
            Integer customerId,
            String documentType,
            String filePath
    );

    List<KycDocument> getCustomerDocuments(Integer customerId);
    KycDocument verifyKyc(Integer documentId);
    KycDocument rejectKyc(Integer documentId, String rejectionReason);
    KycDocumentResponse toResponse(KycDocument document);
}
