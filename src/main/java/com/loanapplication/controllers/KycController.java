
package com.loanapplication.controllers;

import com.loanapplication.dto.KycDocumentRequest;
import com.loanapplication.dto.KycDocumentResponse;
import com.loanapplication.entities.KycDocument;
import com.loanapplication.service.CustomerService;
import com.loanapplication.service.KycService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kyc")
public class KycController {

    private final KycService kycService;
    private final CustomerService customerService;

    public KycController(
            KycService kycService,
            CustomerService customerService) {
        this.kycService = kycService;
        this.customerService = customerService;
    }


    @PostMapping("/{customerId}/documents")
    public KycDocumentResponse submitDocument(
            @PathVariable Integer customerId,
            @jakarta.validation.Valid @RequestBody KycDocumentRequest request,
            Authentication authentication) {

        boolean isOfficer = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_OFFICER"));

        if (!isOfficer) {
            Integer loggedInCustomerId = customerService
                    .getCustomerByEmail(authentication.getName())
                    .getCustomerId();

            if (!loggedInCustomerId.equals(customerId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can submit documents only for your own account"
                );
            }
        }

        KycDocument document = kycService.submitDocument(
                customerId,
                request.getDocumentType(),
                request.getFilePath()
        );

        return kycService.toResponse(document);
    }

    @GetMapping("/documents")
    public List<KycDocumentResponse> getMyDocuments(
            Authentication authentication) {

        String email = authentication.getName();

        Integer customerId = customerService
                .getCustomerByEmail(email)
                .getCustomerId();

        return kycService.getCustomerDocuments(customerId)
                .stream()
                .map(kycService::toResponse)
                .toList();
    }

    @PostMapping("/{customerId}")
    public List<KycDocumentResponse> startKyc(
            @PathVariable Integer customerId) {

        return kycService.getCustomerDocuments(customerId)
                .stream()
                .map(kycService::toResponse)
                .toList();
    }

    @GetMapping("/{customerId}")
    public List<KycDocumentResponse> getCustomerKycDetails(
            @PathVariable Integer customerId) {

        return kycService.getCustomerDocuments(customerId)
                .stream()
                .map(kycService::toResponse)
                .toList();
    }


    @PutMapping("/{documentId}/verify")
    public KycDocumentResponse verifyKyc(
            @PathVariable Integer documentId) {

        KycDocument document = kycService.verifyKyc(documentId);

        return kycService.toResponse(document);
    }

    @PutMapping("/{documentId}/reject")
    public KycDocumentResponse rejectKyc(
            @PathVariable Integer documentId,
            @RequestBody Map<String, String> request) {

        String rejectionReason = request.get("rejectionReason");

        KycDocument document =
                kycService.rejectKyc(documentId, rejectionReason);

        return kycService.toResponse(document);
    }
}
