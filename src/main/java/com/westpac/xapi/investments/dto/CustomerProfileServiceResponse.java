package com.westpac.xapi.investments.dto;

public class CustomerProfileServiceResponse {

    private ProcessingCode errorCode;
    private CustomerProfileResponseData customerProfile;

    public ProcessingCode getErrorCode() { return errorCode; }
    public void setErrorCode(ProcessingCode errorCode) { this.errorCode = errorCode; }

    public CustomerProfileResponseData getCustomerProfile() { return customerProfile; }
    public void setCustomerProfile(CustomerProfileResponseData customerProfile) { this.customerProfile = customerProfile; }
}
