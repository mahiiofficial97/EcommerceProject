package com.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.processing.Pattern;
import org.hibernate.annotations.processing.Pattern;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Entity
public class SuperAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    private String password;
    

    @jakarta.validation.constraints.NotNull(message = "Email cannot be null reee")
    @NotEmpty(message = "Email should be valid ree")
    @Email(message="Email Should be in proper formate")
    
    private String email;

    
    @jakarta.validation.constraints.NotNull(message = "Mobile number cannot be null")
    @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{10}$", message = "Mobile number should be 10 digits")
    private String mobileno;

    private String status = "Active";

    @OneToMany(mappedBy = "superAdmin", cascade = CascadeType.ALL)
      private List<Admin> admins;

    @CreationTimestamp
    private LocalDateTime createdDate;


    
    @Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	@DateTimeFormat(pattern = "yyyy-mm-dd HH:mm:ss")
	private Date lastupdate=new Date();
    
    
    private String otp;
    private LocalDateTime otpExpiry;
    
    private String profileImage;
    
}
