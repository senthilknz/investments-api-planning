package com.westpac.xapi.investments.service;

import com.westpac.xapi.investments.dto.CustomerProfileServiceResponse;

public interface CustomerProfileService {

    CustomerProfileServiceResponse getCustomerProfile(String customerId);
}
