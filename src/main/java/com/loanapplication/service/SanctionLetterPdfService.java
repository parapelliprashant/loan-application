package com.loanapplication.service;

public interface SanctionLetterPdfService {

    byte[] generateSanctionLetterPdf(
            Integer sanctionId
    );
}