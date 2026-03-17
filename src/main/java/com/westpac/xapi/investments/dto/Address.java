package com.westpac.xapi.investments.dto;

public class Address {

    private String type;           // LocationAddr or MailingAddr
    private String addressLine1;
    private String streetNumber;
    private String streetName;
    private String streetType;
    private String suburb;
    private String city;
    private String postalCode;
    private String countryCode;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getStreetType() { return streetType; }
    public void setStreetType(String streetType) { this.streetType = streetType; }

    public String getSuburb() { return suburb; }
    public void setSuburb(String suburb) { this.suburb = suburb; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
