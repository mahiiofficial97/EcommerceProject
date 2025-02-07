package com.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.model.SuperAdmin;

@Repository
public interface SuperAdminRepo extends JpaRepository<SuperAdmin, Long> {
	
	@Query("SELECT a FROM SuperAdmin a WHERE a.email = :email")
	SuperAdmin findbyemailofsuoerAdmin(@Param("email") String email);
	
	@Query("SELECT sa FROM SuperAdmin sa WHERE sa.lastupdate < :sinceDate AND sa.status = 'Active'")
	List<SuperAdmin> findInactiveSince(@Param("sinceDate") Date sinceDate);
	
	@Query("SELECT a FROM SuperAdmin a WHERE a.email = :email AND a.otp = :otp AND a.otpExpiry > CURRENT_TIMESTAMP")
    SuperAdmin validateOtp(@Param("email") String email, @Param("otp") String otp);
	
	
}
