package com.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.model.JsonResponse;
import com.model.SuperAdmin;

import jakarta.servlet.http.HttpSession;

public interface SuperAdminService {

	public com.model.SuperAdmin savesuperAdmin(SuperAdmin admin);

	public SuperAdmin findByEmail(String email);

	SuperAdmin findById(Long id);

	List<SuperAdmin> findAll();

	void deleteById(Long id);
	
	public int deactivateInactiveSuperAdmins();
	
    public JsonResponse login(String email, String otp);
    
    public JsonResponse loginwithsession(String email, String otp,HttpSession session);


    JsonResponse forgotPasswordRequest(String email);

    JsonResponse verifyOtpAndResetPassword(String email, String otp, String newPassword, HttpSession session);
    
    public JsonResponse uploadImage(MultipartFile image, HttpSession session) ;
    
    public JsonResponse getImageById(Long id, HttpSession session);
    
    public ResponseEntity<?> getImageByIdd(Long id);
}
