
package com.loanapplication.service.impl;

import com.loanapplication.dto.KycDocumentResponse;
import com.loanapplication.entities.Customer;
import com.loanapplication.entities.KycDocument;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.CustomerRepository;
import com.loanapplication.repo.KycDocumentRepository;
import com.loanapplication.service.KycService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class KycServiceImpl implements KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerRepository customerRepository;

    public KycServiceImpl(
            KycDocumentRepository kycDocumentRepository,
            CustomerRepository customerRepository) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public KycDocument submitDocument(
            Integer customerId, String documentType, String filePath) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        KycDocument document = new KycDocument();
        document.setCustomer(customer);
        document.setDocumentType(documentType);
        document.setFilePath(filePath);
        document.setVerificationStatus("PENDING");

        return kycDocumentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocument> getCustomerDocuments(Integer customerId) {
        return kycDocumentRepository.findByCustomerCustomerId(customerId);
    }


    @Override
    @Transactional
    public KycDocument verifyKyc(Integer documentId) {

        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("KYC document not found"));

        document.setVerificationStatus("VERIFIED");
        document.setRejectionReason(null);

        return kycDocumentRepository.save(document);
    }

    @Override
    @Transactional
    public KycDocument rejectKyc(
            Integer documentId, String rejectionReason) {

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rejection reason is required"
            );
        }
        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("KYC document not found"));

        document.setVerificationStatus("REJECTED");
        document.setRejectionReason(rejectionReason);

        return kycDocumentRepository.save(document);
    }


    @Override
    public KycDocumentResponse toResponse(KycDocument document) {
        KycDocumentResponse response = new KycDocumentResponse();

        response.setDocumentId(document.getDocumentId());
        response.setDocumentType(document.getDocumentType());
        response.setFilePath(document.getFilePath());
        response.setVerificationStatus(document.getVerificationStatus());
        response.setRejectionReason(document.getRejectionReason());

        return response;
    }

}
