package com.westpac.xapi.investments.dto;

public class PersonName {

    private String title;
    private String givenName;
    private String familyName;
    private String middleName;
    private String preferredName;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getPreferredName() { return preferredName; }
    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }
}
