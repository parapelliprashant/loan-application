package com.loanapplication.service.impl;

import com.loanapplication.entities.Customer;
import com.loanapplication.entities.LoanDeal;
import com.loanapplication.entities.SanctionLetter;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.SanctionLetterRepo;
import com.loanapplication.service.SanctionLetterPdfService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class SanctionLetterPdfServiceImpl
        implements SanctionLetterPdfService {

    private final SanctionLetterRepo sanctionLetterRepo;

    public SanctionLetterPdfServiceImpl(
            SanctionLetterRepo sanctionLetterRepo) {

        this.sanctionLetterRepo =
                sanctionLetterRepo;
    }

    @Override
    public byte[] generateSanctionLetterPdf(
            Integer sanctionId) {

        SanctionLetter sanctionLetter =
                sanctionLetterRepo
                        .findById(sanctionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sanction letter not found with id: "
                                                + sanctionId
                                )
                        );

        LoanDeal loanDeal =
                sanctionLetter.getLoanDeal();

        Customer customer =
                loanDeal.getCustomer();

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4,
                        45,
                        45,
                        40,
                        40
                );

        PdfWriter.getInstance(
                document,
                outputStream
        );

        document.open();

        /*
         * TITLE
         */
        Font titleFont =
                new Font(
                        Font.HELVETICA,
                        18,
                        Font.BOLD
                );

        Paragraph title =
                new Paragraph(
                        "LOAN SANCTION LETTER",
                        titleFont
                );

        title.setAlignment(
                Element.ALIGN_CENTER
        );

        title.setSpacingAfter(
                25
        );

        document.add(
                title
        );

        /*
         * DATE
         */
        DateTimeFormatter formatter =
                DateTimeFormatter
                        .ofPattern(
                                "dd/MM/yyyy"
                        );

        String sanctionDate =
                sanctionLetter
                        .getCreatedAt()
                        .format(
                                formatter
                        );

        Font normalFont =
                new Font(
                        Font.HELVETICA,
                        10,
                        Font.NORMAL
                );

        Paragraph dateParagraph =
                new Paragraph(
                        "Date : "
                                + sanctionDate,
                        normalFont
                );

        dateParagraph.setSpacingAfter(
                15
        );

        document.add(
                dateParagraph
        );

        /*
         * APPLICANT NAME
         */
        String applicantName =
                getApplicantName(
                        customer
                );

        Paragraph greeting =
                new Paragraph(
                        "Dear "
                                + applicantName
                                + ",",
                        normalFont
                );

        greeting.setSpacingAfter(
                10
        );

        document.add(
                greeting
        );

        /*
         * INTRODUCTION
         */
        Paragraph introduction =
                new Paragraph(
                        "Thank you for choosing ABC Bank. "
                                + "Based on your loan application and the "
                                + "information provided therein, we are pleased "
                                + "to sanction your loan as per the preliminary "
                                + "terms and conditions mentioned below.",
                        normalFont
                );

        introduction.setAlignment(
                Element.ALIGN_JUSTIFIED
        );

        introduction.setSpacingAfter(
                15
        );

        document.add(
                introduction
        );

        /*
         * DETAILS TABLE
         */
        PdfPTable table =
                new PdfPTable(
                        2
                );

        table.setWidthPercentage(
                100
        );

        table.setWidths(
                new float[]{
                        1.3f,
                        1.4f
                }
        );

        addTableRow(
                table,
                "Application No",
                "DEAL-"
                        + loanDeal.getDealId()
        );

        addTableRow(
                table,
                "Sanction Date",
                sanctionDate
        );

        addTableRow(
                table,
                "Applicant Name",
                applicantName
        );

        addTableRow(
                table,
                "Mobile No",
                customer.getMobileNo() != null
                        ? customer.getMobileNo()
                        : "-"
        );

        addTableRow(
                table,
                "Loan Type",
                formatLoanType(
                        loanDeal.getLoanType()
                )
        );

        addTableRow(
                table,
                "Loan Amount Sanctioned",
                formatCurrency(
                        sanctionLetter.getLoanAmount()
                )
        );

        addTableRow(
                table,
                "Reference Interest Rate",
                sanctionLetter
                        .getInterestRate()
                        .stripTrailingZeros()
                        .toPlainString()
                        + "% per annum"
        );

        addTableRow(
                table,
                "Loan Tenure",
                sanctionLetter
                        .getTenureMonths()
                        + " Months"
        );

        addTableRow(
                table,
                "Amount of EMI",
                formatCurrency(
                        sanctionLetter.getEmiAmount()
                )
        );

        document.add(
                table
        );

        /*
         * CONDITIONS
         */
        Paragraph conditionHeading =
                new Paragraph(
                        "Additional Conditions To Comply Prior To Loan Disbursal:",
                        new Font(
                                Font.HELVETICA,
                                10,
                                Font.BOLD
                        )
                );

        conditionHeading.setSpacingBefore(
                18
        );

        conditionHeading.setSpacingAfter(
                8
        );

        document.add(
                conditionHeading
        );

        document.add(
                new Paragraph(
                        "1. Customer identity and loan details must remain verified.",
                        normalFont
                )
        );

        document.add(
                new Paragraph(
                        "2. Bank account details provided for disbursement must be valid.",
                        normalFont
                )
        );

        document.add(
                new Paragraph(
                        "3. Customer must comply with the approved loan terms and conditions.",
                        normalFont
                )
        );

        document.add(
                new Paragraph(
                        "4. Loan disbursement will be processed only after required verification.",
                        normalFont
                )
        );

        /*
         * SIGNATURE
         */
        Paragraph signatory =
                new Paragraph(
                        "\nAuthorized Signatory\nABC Bank",
                        new Font(
                                Font.HELVETICA,
                                10,
                                Font.BOLD
                        )
                );

        signatory.setSpacingBefore(
                20
        );

        document.add(
                signatory
        );

        document.close();

        return outputStream
                .toByteArray();
    }

    private void addTableRow(
            PdfPTable table,
            String label,
            String value) {

        Font labelFont =
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.BOLD
                );

        Font valueFont =
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.NORMAL
                );

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont
                        )
                );

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                value != null
                                        ? value
                                        : "-",
                                valueFont
                        )
                );

        labelCell.setPadding(
                5
        );

        valueCell.setPadding(
                5
        );

        labelCell.setBorderColor(
                Color.GRAY
        );

        valueCell.setBorderColor(
                Color.GRAY
        );

        table.addCell(
                labelCell
        );

        table.addCell(
                valueCell
        );
    }

    private String getApplicantName(
            Customer customer) {

        String firstName =
                customer.getFirstName() != null
                        ? customer.getFirstName()
                        : "";

        String lastName =
                customer.getLastName() != null
                        ? customer.getLastName()
                        : "";

        return (
                firstName
                        + " "
                        + lastName
        ).trim();
    }

    private String formatLoanType(
            String loanType) {

        if ("HOME_LOAN".equalsIgnoreCase(
                loanType)) {

            return "Home Loan";
        }

        if ("VEHICLE_LOAN".equalsIgnoreCase(
                loanType)) {

            return "Vehicle Loan";
        }

        return loanType;
    }

    private String formatCurrency(
            BigDecimal amount) {

        NumberFormat formatter =
                NumberFormat.getCurrencyInstance(
                        new Locale(
                                "en",
                                "IN"
                        )
                );

        return formatter.format(
                amount
        );
    }
}