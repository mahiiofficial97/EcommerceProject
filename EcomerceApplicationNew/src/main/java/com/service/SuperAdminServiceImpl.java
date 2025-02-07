package com.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.model.JsonResponse;
import com.model.SuperAdmin;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

    @Autowired
    com.repository.SuperAdminRepo superAdminRepo;
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    private Map<String, String> otpStore = new HashMap<>(); // Temporary OTP storage

    @Autowired
    private JavaMailSender javaMailSender;

    private static final int OTP_VALIDITY_DURATION = 5; // OTP validity in minutes

    // Step 1: Generate OTP (6 digits)
    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000); // Generate 6-digit OTP
    }
    
    
    
    //otp for forgot password 
    
    private void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OTP for Password Reset");
        message.setText("Your OTP is: " + otp + ". It is valid for 5 minutes.");
        javaMailSender.send(message);
    }
    
//otp for email login
    // Step 2: Send OTP email to the provided email address
    private void sendEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP for Login");
        message.setText("Your OTP is: " + otp + ". It is valid for 5 minutes.");
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SuperAdmin savesuperAdmin(SuperAdmin superadmin) {
        return superAdminRepo.save(superadmin);    
    }

    @Override
    public SuperAdmin findByEmail(String email) {
        return superAdminRepo.findbyemailofsuoerAdmin(email);
    }

    @Override
    public SuperAdmin findById(Long id) {
        return superAdminRepo.findById(id).orElse(null);
    }

    @Override
    public List<SuperAdmin> findAll() {
        return superAdminRepo.findAll();
    }

    @Override
    public void deleteById(Long id) {
        superAdminRepo.deleteById(id);
    }

    @Override
    public int deactivateInactiveSuperAdmins() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        Date oneMonthAgoDate = Date.from(oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant());

        List<SuperAdmin> inactiveSuperAdmins = superAdminRepo.findInactiveSince(oneMonthAgoDate);
        for (SuperAdmin superAdmin : inactiveSuperAdmins) {
            superAdmin.setStatus("Inactive");
        }

        superAdminRepo.saveAll(inactiveSuperAdmins);

        return inactiveSuperAdmins.size(); // Return the count of updated SuperAdmins
    }

    // Login logic with OTP handling with session
    
    
    
    
    @Override
    public JsonResponse loginwithsession(String email, String otp, HttpSession session) {
        JsonResponse response = new JsonResponse();

        // Step 1: Check if the user is already logged in
        Object adminSession = session.getAttribute("admininstance");
        if (adminSession != null) {
            response.setMessege("You are already logged in.");
            response.setResult("success");
            response.setStatuscode("200");
            return response;
        }

        // Step 2: If OTP is not provided, generate and send OTP
        if (otp == null || otp.isEmpty()) {
            SuperAdmin superAdmin = superAdminRepo.findbyemailofsuoerAdmin(email);

            if (superAdmin == null) {
                response.setMessege("Email not found.");
                response.setResult("failure");
                response.setStatuscode("404");
                return response;
            }

            // Step 3: Check if an OTP was generated and is still valid (before 5 minutes)
            if (superAdmin.getOtp() != null && superAdmin.getOtpExpiry() != null
                    && superAdmin.getOtpExpiry().isAfter(LocalDateTime.now())) {

                // Calculate the remaining time until the OTP expires
                Duration duration = Duration.between(LocalDateTime.now(), superAdmin.getOtpExpiry());
                long minutesRemaining = duration.toMinutes();
                long secondsRemaining = duration.getSeconds() % 60;

                String remainingTime = String.format("%02d:%02d", minutesRemaining, secondsRemaining);

                response.setMessege("OTP already sent to your mail and it has not expired. Remaining time: " + remainingTime);
                response.setResult("failure");
                response.setStatuscode("400");
                return response;
            }

            // Step 4: If expired or no OTP, generate a new one
            String generatedOtp = generateOtp();
            superAdmin.setOtp(generatedOtp); // Save generated OTP
            superAdmin.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_DURATION)); // OTP valid for 5 minutes

            // Save OTP and expiry time
            superAdminRepo.save(superAdmin);

            // Step 5: Send the OTP email
            sendEmail(email, generatedOtp);

            response.setMessege("OTP sent successfully. Please check your email.");
            response.setResult("success");
            response.setStatuscode("200");
            return response;
        }

        // Step 6: OTP is provided, so validate the OTP
        SuperAdmin superAdmin = superAdminRepo.findbyemailofsuoerAdmin(email);

        if (superAdmin != null && superAdmin.getOtp() != null && superAdmin.getOtpExpiry() != null
                && superAdmin.getOtpExpiry().isAfter(LocalDateTime.now()) && superAdmin.getOtp().equals(otp)) {

            // OTP is valid, create session for the user
            superAdmin.setOtp(null);  // Clear OTP after successful validation
            superAdmin.setOtpExpiry(null);  // Clear OTP expiry after successful validation
            superAdminRepo.save(superAdmin);

            // Set the session attribute with the authenticated admin
            session.setAttribute("admininstance", superAdmin);

            response.setMessege("OTP verified successfully. Login successful.");
            response.setResult("success");
            response.setStatuscode("200");
        } else {
            response.setMessege("Invalid or expired OTP.");
            response.setResult("failure");
            response.setStatuscode("401");
        }

        return response;
    }
    
        
    
    //logic of otp without session  handing
    
    @Override
    public JsonResponse login(String email, String otp) {
        JsonResponse response = new JsonResponse();

        // Step 1: If OTP is not provided, send a new OTP
        if (otp == null || otp.isEmpty()) {
            SuperAdmin superAdmin = superAdminRepo.findbyemailofsuoerAdmin(email);

            if (superAdmin == null) {
                response.setMessege("Email not found.");
                response.setResult("failure");
                response.setStatuscode("404");
                return response;
            }

            // Step 2: Check if an OTP was generated and is still valid
            if (superAdmin.getOtp() != null && superAdmin.getOtpExpiry() != null
                    && superAdmin.getOtpExpiry().isAfter(LocalDateTime.now())) {
                response.setMessege("OTP already sent. Please check your email.");
                response.setResult("failure");
                response.setStatuscode("400");
                return response;
            }

            // Step 3: Generate a new OTP if it's expired or doesn't exist
            String generatedOtp = generateOtp();
            superAdmin.setOtp(generatedOtp); // Save generated OTP
            superAdmin.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_DURATION)); // OTP valid for 5 minutes

            // Step 4: Save the OTP and expiry time in the database
            superAdminRepo.save(superAdmin);

            // Step 5: Send the OTP email
            sendEmail(email, generatedOtp);

            // Step 6: Return success response
            response.setMessege("OTP sent successfully. Please check your email.");
            response.setResult("success");
            response.setStatuscode("200");
            return response;
        }

        // Step 2: OTP is provided, so validate the OTP
        SuperAdmin superAdmin = superAdminRepo.validateOtp(email, otp);

        // Step 3: Check if the provided OTP is valid and not expired
        if (superAdmin != null && superAdmin.getOtpExpiry().isAfter(LocalDateTime.now())) {
            // OTP is valid, clear OTP and expiry fields after successful validation
            superAdmin.setOtp(null);
            superAdmin.setOtpExpiry(null);
            superAdminRepo.save(superAdmin);

            // Step 4: Return success response
            response.setMessege("OTP verified successfully. Login successful.");
            response.setResult("success");
            response.setStatuscode("200");
        } else {
            // OTP is invalid or expired
            response.setMessege("Invalid or expired OTP.");
            response.setResult("failure");
            response.setStatuscode("401");
        }

        return response;
    }

    
    //forgot password api for email 
    @Override
    public JsonResponse forgotPasswordRequest(String email) {
        JsonResponse response = new JsonResponse();

        SuperAdmin superAdmin = superAdminRepo.findbyemailofsuoerAdmin(email);
        if (superAdmin == null) {
            response.setMessege("Email not found.");
            response.setStatuscode("404");
            response.setResult("failure");
            return response;
        }

        // Generate OTP and save to database
        String otp = generateOtp();
        superAdmin.setOtp(otp);
        superAdmin.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_DURATION));
        superAdminRepo.save(superAdmin);

        // Send OTP email
        sendOtpEmail(email, otp);

        response.setMessege("OTP sent successfully. Please check your email.");
        response.setStatuscode("200");
        response.setResult("success");
        return response;
    }

    @Override
    public JsonResponse verifyOtpAndResetPassword(String email, String otp, String newPassword, HttpSession session) {
        JsonResponse response = new JsonResponse();

        SuperAdmin superAdmin = superAdminRepo.findbyemailofsuoerAdmin(email);
        if (superAdmin == null) {
            response.setMessege("Email not found.");
            response.setStatuscode("404");
            response.setResult("failure");
            return response;
        }

        // Validate OTP and expiry
        if (superAdmin.getOtp() != null 
                && superAdmin.getOtp().equals(otp) 
                && superAdmin.getOtpExpiry() != null 
                && superAdmin.getOtpExpiry().isAfter(LocalDateTime.now())) {

            // Reset password
            String hashedPassword = bCryptPasswordEncoder.encode(newPassword);
            superAdmin.setPassword(hashedPassword);

            // Clear OTP and expiry after successful reset
            superAdmin.setOtp(null);
            superAdmin.setOtpExpiry(null);

            superAdminRepo.save(superAdmin);

            response.setStatuscode("200");
            response.setMessege("Password reset successfully.");
            response.setResult("success");
        } else {
            response.setStatuscode("400");
            response.setMessege("Invalid or expired OTP.");
            response.setResult("failure");
        }

        return response;
    }

//upload image logic

	@Override
	public JsonResponse uploadImage(MultipartFile image, HttpSession session) {
        JsonResponse response = new JsonResponse();

        // Check if the user is logged in
        Object adminSession = session.getAttribute("admininstance");

        if (adminSession == null) {
            response.setMessege("You must be logged in to upload an image.");
            response.setStatuscode("401");
            response.setResult("failure");
            return response;
        }

        // Get the logged-in SuperAdmin object
        SuperAdmin loggedInAdmin = (SuperAdmin) adminSession;

        try {
            // Define the relative path where images will be stored
            String uploadDirectory = "/Users/mahii/Documents/workspace-spring-tool-suite-4-4.19.0.RELEASE/EcomerceApplicationNew/upload/SuperAdmin/Profileimage";

            // Create directory if it doesn't exist
            File directory = new File(uploadDirectory);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    response.setMessege("Error creating upload directory.");
                    response.setStatuscode("500");
                    response.setResult("failure");
                    return response;
                }
            }

            // Generate a unique file name to avoid overwriting
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path serverFilePath = Paths.get(uploadDirectory + fileName);

            // Save the file to the server
            image.transferTo(serverFilePath.toFile());

            // Check if the file was successfully written
            if (!Files.exists(serverFilePath)) {
                response.setMessege("File not written to disk.");
                response.setStatuscode("500");
                response.setResult("failure");
                return response;
            }

            // Update the profileImage field of the logged-in SuperAdmin
            loggedInAdmin.setProfileImage(uploadDirectory + fileName); // Store the relative path

            // Save the updated SuperAdmin entity to the database
            superAdminRepo.save(loggedInAdmin);

            // Respond with success
            response.setMessege("Image uploaded successfully.");
            response.setStatuscode("200");
            response.setResult("success");

        } catch (IOException e) {
            response.setMessege("Error while uploading the image: " + e.getMessage());
            response.setStatuscode("500");
            response.setResult("failure");
            e.printStackTrace();  // Log the exception stack trace for more details
        }

        return response;
    }


	@Override
	public JsonResponse getImageById(Long id, HttpSession session) {
	    JsonResponse response = new JsonResponse();

	    // Check if the user is logged in
	    Object adminSession = session.getAttribute("admininstance");

	    if (adminSession == null) {
	        response.setMessege("You must be logged in to view the image.");
	        response.setStatuscode("401");
	        response.setResult("failure");
	        return response;
	    }

	    // Fetch the SuperAdmin by ID
	    SuperAdmin superAdmin = superAdminRepo.findById(id).orElse(null);
	    if (superAdmin == null || superAdmin.getProfileImage() == null) {
	        response.setMessege("No profile image found.");
	        response.setStatuscode("404");
	        response.setResult("failure");
	        return response;
	    }

	    // Validate if the logged-in user is authorized to view the image
	    SuperAdmin loggedInAdmin = (SuperAdmin) adminSession;
	    if (!loggedInAdmin.getId().equals(superAdmin.getId())) {
	        response.setMessege("You are not authorized to view this image.");
	        response.setStatuscode("403");
	        response.setResult("failure");
	        return response;
	    }

	    // Path to the image file
	    Path imagePath = Paths.get(superAdmin.getProfileImage()).toAbsolutePath();

	    // Check if the file exists
	    File file = imagePath.toFile();
	    if (!file.exists()) {
	        response.setMessege("Image file not found.");
	        response.setStatuscode("404");
	        response.setResult("failure");
	        return response;
	    }

	    try {
	        // Return the image path and success message in the response
	        response.setMessege(imagePath.toString()); // Set the image path for retrieval
	        response.setStatuscode("200");
	        response.setResult("success");
	    } catch (Exception e) {
	        response.setMessege("Error retrieving the image: " + e.getMessage());
	        response.setStatuscode("500");
	        response.setResult("failure");
	    }

	    return response;
	}

	//geting image by id on the browser
	@Override
	public ResponseEntity<?> getImageByIdd(Long id) {
	    JsonResponse response = new JsonResponse();

	    // Fetch the SuperAdmin by ID
	    SuperAdmin superAdmin = superAdminRepo.findById(id).orElse(null);
	    if (superAdmin == null || superAdmin.getProfileImage() == null) {
	        response.setMessege("No profile image found.");
	        response.setStatuscode("404");
	        response.setResult("failure");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	    }

	    Path imagePath = Paths.get(superAdmin.getProfileImage()).toAbsolutePath();

	    // Check if the file exists
	    File file = imagePath.toFile();
	    if (!file.exists()) {
	        response.setMessege("Image file not found.");
	        response.setStatuscode("404");
	        response.setResult("failure");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	    }

	    try {
	        // Create a resource from the file
	        Resource resource = new UrlResource(imagePath.toUri());

	        // Determine the content type
	        String contentType = Files.probeContentType(imagePath);
	        if (contentType == null) {
	            contentType = "application/octet-stream";
	        }

	        // Return the image as a ResponseEntity
	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(contentType))
	                .body(resource);

	    } catch (IOException e) {
	        response.setMessege("Error retrieving the image: " + e.getMessage());
	        response.setStatuscode("500");
	        response.setResult("failure");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}








	}


    
//    @Override
//    public JsonResponse login(String email, String otp) {
//        JsonResponse response = new JsonResponse();
//
//        // Step 1: If OTP is not provided, send a new OTP
//        if (otp == null || otp.isEmpty()) {
//            SuperAdmin superAdmin = superAdminRepo.findbyemailofsuoerAdmin(email);
//
//            if (superAdmin == null) {
//                response.setMessege("Email not found.");
//                response.setResult("failure");
//                response.setStatuscode("404");
//                return response;
//            }
//
//            // Step 2: Check if an OTP was generated and is still valid
//            if (superAdmin.getOtp() != null && superAdmin.getOtpExpiry() != null
//                    && superAdmin.getOtpExpiry().isAfter(LocalDateTime.now())) {
//                response.setMessege("OTP already sent. Please check your email.");
//                response.setResult("failure");
//                response.setStatuscode("400");
//                return response;
//            }
//
//            // Step 3: Generate a new OTP if it's expired or doesn't exist
//            String generatedOtp = generateOtp();
//            superAdmin.setOtp(generatedOtp);
//            superAdmin.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_DURATION)); // OTP valid for 5 minutes
//            superAdminRepo.save(superAdmin);
//
//            // Step 4: Send the OTP email
//            sendEmail(email, generatedOtp);
//
//            // Step 5: Return success response
//            response.setMessege("OTP sent successfully. Please check your email.");
//            response.setResult("success");
//            response.setStatuscode("200");
//            return response;
//        }
//
//        // Step 2: OTP is provided, so validate the OTP
//        SuperAdmin superAdmin = superAdminRepo.validateOtp(email, otp);
//
//        // Step 3: Check if the provided OTP is valid and not expired
//        if (superAdmin != null && superAdmin.getOtpExpiry().isAfter(LocalDateTime.now())) {
//            // OTP is valid, clear OTP and expiry fields after successful validation
//            superAdmin.setOtp(null);
//            superAdmin.setOtpExpiry(null);
//            superAdminRepo.save(superAdmin);
//
//            // Step 4: Return success response
//            response.setMessege("OTP verified successfully. Login successful.");
//            response.setResult("success");
//            response.setStatuscode("200");
//        } else {
//            // OTP is invalid or expired
//            response.setMessege("Invalid or expired OTP.");
//            response.setResult("failure");
//            response.setStatuscode("401");
//        }
//
//        return response;
//    }

