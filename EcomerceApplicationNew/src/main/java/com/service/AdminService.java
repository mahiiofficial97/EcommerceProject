package com.service;

import java.util.List;
import java.util.Optional;

import com.model.Admin;



public interface AdminService {
	
	
	public com.model.Admin saveAdmin(com.model.Admin admin);

	public com.model.Admin findByEmail(String email);

	com.model.Admin findById(Long id);

	List<Admin> findAll();

	void deleteById(Long id);
	
	public  Optional<Admin> findByIdd(Long id);

}



