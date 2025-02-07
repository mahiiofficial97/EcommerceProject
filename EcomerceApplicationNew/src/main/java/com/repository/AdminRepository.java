package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.model.Admin;


@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
	
	
	@Query("SELECT a FROM Admin a WHERE  a.email=:email")
	com.model.Admin findbyemailid(@Param ("email")String email);

}
