package com.westpac.xapi.investments.controller;

import com.westpac.xapi.investments.dto.CustomerProfileServiceResponse;
import com.westpac.xapi.investments.service.CustomerProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/xapi/v1/investments/customers/me")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    public CustomerProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @GetMapping("/profile")
    public CustomerProfileServiceResponse getCustomerProfile() {
        // TODO: resolve customerId from JWT / security context
        String customerId = "TODO";
        return customerProfileService.getCustomerProfile(customerId);
    }
}
