package com.loanapplication.service;

import com.loanapplication.dto.CibilScoreRequest;
import com.loanapplication.dto.CibilScoreResponse;

import java.util.List;

public interface CibilService {

    CibilScoreResponse generateScore(CibilScoreRequest request);

    CibilScoreResponse getCurrentScore(Integer customerId);

}
