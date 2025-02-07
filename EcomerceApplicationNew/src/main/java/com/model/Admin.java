package com.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    private String status = "Active";

    @ManyToOne
    @JoinColumn(name = "super_admin_id")
    private SuperAdmin superAdmin;

    @Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss a")
	private Date lastupdate=new Date();
    
    
    @Temporal(TemporalType.DATE)
	@CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createddate;
	
	
}