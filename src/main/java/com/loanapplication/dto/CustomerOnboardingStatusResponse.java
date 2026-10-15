
package com.loanapplication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerOnboardingStatusResponse {

    private Integer customerId;
    private boolean profileComplete;
    private boolean emailVerified;
    private String status;
}
