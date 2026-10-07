package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KycDocuments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentId")
    private Integer documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "DocumentType", nullable = false)
    private String documentType;

    @Column(name = "FilePath", length = 1000)
    private String filePath;

    @Column(name = "VerificationStatus")
    private String verificationStatus;
}