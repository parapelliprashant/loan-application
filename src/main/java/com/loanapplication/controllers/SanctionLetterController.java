package com.loanapplication.controllers;

import com.loanapplication.dto.SanctionLetterResponseDto;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.SanctionLetterPdfService;
import com.loanapplication.service.SanctionLetterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sanction-letters")
public class SanctionLetterController {

    private final SanctionLetterService sanctionLetterService;

    private final SanctionLetterPdfService sanctionLetterPdfService;

    public SanctionLetterController(
            SanctionLetterService sanctionLetterService,
            SanctionLetterPdfService sanctionLetterPdfService) {

        this.sanctionLetterService =
                sanctionLetterService;

        this.sanctionLetterPdfService =
                sanctionLetterPdfService;
    }

    @PostMapping("/{dealId}/generate")
    public ResponseEntity<ApiResponse<SanctionLetterResponseDto>>
    generateSanctionLetter(
            @PathVariable Integer dealId) {

        SanctionLetterResponseDto responseDto =
                sanctionLetterService
                        .generateSanctionLetter(
                                dealId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Sanction letter generated successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ApiResponse<SanctionLetterResponseDto>>
    getSanctionLetterByDealId(
            @PathVariable Integer dealId) {

        SanctionLetterResponseDto responseDto =
                sanctionLetterService
                        .getSanctionLetterByDealId(
                                dealId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Sanction letter fetched successfully",
                        responseDto,
                        null
                )
        );
    }

    /*
     * View PDF in browser
     */
    @GetMapping(
            value = "/{sanctionId}/view",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> viewSanctionLetter(
            @PathVariable Integer sanctionId) {

        byte[] pdf =
                sanctionLetterPdfService
                        .generateSanctionLetterPdf(
                                sanctionId
                        );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=sanction-letter-"
                                + sanctionId
                                + ".pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(
                        pdf
                );
    }

    /*
     * Download PDF
     */
    @GetMapping(
            value = "/{sanctionId}/download",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> downloadSanctionLetter(
            @PathVariable Integer sanctionId) {

        byte[] pdf =
                sanctionLetterPdfService
                        .generateSanctionLetterPdf(
                                sanctionId
                        );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=sanction-letter-"
                                + sanctionId
                                + ".pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(
                        pdf
                );
    }
}