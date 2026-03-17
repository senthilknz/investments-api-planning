package com.westpac.xapi.investments.dto;

import java.util.List;

public class CustomerProfileResponseData {

    private String partyId;
    private PersonName name;
    private String dateOfBirth;
    private String gender;
    private String partyStatus;
    private List<Address> addresses;
    private String mobilePhone;
    private String irdNumber;
    private Branch owningBranch;
    private boolean nzResident;    // derived from LocationAddr.countryCode == "NZ"

    public String getPartyId() { return partyId; }
    public void setPartyId(String partyId) { this.partyId = partyId; }

    public PersonName getName() { return name; }
    public void setName(PersonName name) { this.name = name; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPartyStatus() { return partyStatus; }
    public void setPartyStatus(String partyStatus) { this.partyStatus = partyStatus; }

    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public String getMobilePhone() { return mobilePhone; }
    public void setMobilePhone(String mobilePhone) { this.mobilePhone = mobilePhone; }

    public String getIrdNumber() { return irdNumber; }
    public void setIrdNumber(String irdNumber) { this.irdNumber = irdNumber; }

    public Branch getOwningBranch() { return owningBranch; }
    public void setOwningBranch(Branch owningBranch) { this.owningBranch = owningBranch; }

    public boolean isNzResident() { return nzResident; }
    public void setNzResident(boolean nzResident) { this.nzResident = nzResident; }
}
