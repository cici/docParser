package com.example.docparser.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * User data model representing a user record from the CSV file.
 * Contains validation annotations for required fields.
 */
public class User {
    
    @NotNull
    @NotBlank(message = "User ID is required")
    private final String id;
    
    @NotNull
    @NotBlank(message = "Name is required")
    private final String name;
    
    @NotNull
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private final String email;
    
    @NotNull
    @NotBlank(message = "Company name is required")
    private final String companyName;
    
    @NotNull
    @NotBlank(message = "Address is required")
    private final String address;
    
    // Raw CSV row for debugging/logging purposes
    private final String rawCsvRow;
    
    @JsonCreator
    public User(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("companyName") String companyName,
            @JsonProperty("address") String address,
            @JsonProperty("rawCsvRow") String rawCsvRow) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.companyName = companyName;
        this.address = address;
        this.rawCsvRow = rawCsvRow;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getRawCsvRow() {
        return rawCsvRow;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", companyName='" + companyName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
    
    /**
     * Builder pattern for creating User instances
     */
    public static class Builder {
        private String id;
        private String name;
        private String email;
        private String companyName;
        private String address;
        private String rawCsvRow;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }
        
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        
        public Builder rawCsvRow(String rawCsvRow) {
            this.rawCsvRow = rawCsvRow;
            return this;
        }
        
        public User build() {
            return new User(id, name, email, companyName, address, rawCsvRow);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
