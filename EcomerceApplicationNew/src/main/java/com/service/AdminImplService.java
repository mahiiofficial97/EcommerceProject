package com.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.Admin;
import com.repository.AdminRepository;



@Service
public class AdminImplService implements AdminService{
	
	@Autowired
	AdminRepository adminRepository;

	
	@Override
	public Admin saveAdmin(Admin admin) {
		
		return adminRepository.save(admin);
	}


	@Override
	public Admin findByEmail(String email) {
		// TODO Auto-generated method stub
		return adminRepository.findbyemailid(email);
	}

	@Override
	public Admin findById(Long id) {
		
		return adminRepository.findById(id).orElse(null);
	}

	@Override
	public List<Admin> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteById(Long id) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Optional<Admin> findByIdd(Long id) {
		// TODO Auto-generated method stub
		return adminRepository.findById(id);
	}


}
