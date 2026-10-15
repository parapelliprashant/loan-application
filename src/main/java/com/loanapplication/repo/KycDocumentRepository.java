
package com.loanapplication.repo;

import com.loanapplication.entities.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Integer> {

    List<KycDocument> findByCustomerCustomerId(Integer customerId);

}
