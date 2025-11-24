package com.ninjaone.dundie_awards.dto;

public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private Long organizationId;
    private String organizationName;
    private Integer dundieAwards;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public Integer getDundieAwards() { return dundieAwards; }
    public void setDundieAwards(Integer dundieAwards) { this.dundieAwards = dundieAwards; }
}
