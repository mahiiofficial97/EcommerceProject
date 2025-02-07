package com.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.model.Admin;
import com.model.JsonResponse;
import com.service.AdminImplService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminController {

	BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

	
	
	
	@Autowired
	AdminImplService adminImplService;

	@PostMapping("/update/{id}")
	public JsonResponse updateAdmin(@RequestBody Admin admin, @PathVariable("id") Long id) {
		JsonResponse response = new JsonResponse();

		Optional<Admin> admins = adminImplService.findByIdd(id);

		if (admins.isPresent()) {
			Admin update = admins.get();

			// update fields of the Exitinf entry with data from Request body

			update.setEmail(admin.getEmail());
			update.setLastupdate(admin.getLastupdate());
			update.setName(admin.getName());
			update.setStatus(admin.getStatus());

			// check if password is provide and bcrypt before uodating

			if (admin.getPassword() != null) {
				String bcryptpassword = bCryptPasswordEncoder.encode(admin.getPassword());
				update.setPassword(bcryptpassword);

				// or

				// update.setPassword(bCryptPasswordEncoder.encode(admin.getPassword()));

			}
			adminImplService.saveAdmin(update);
			response.setMessege("Admin update successfully");
			response.setResult("success");
			response.setStatuscode("200");
		} else {
			response.setMessege("This id is not present in the System!!");
			response.setResult("Unsuccessful");
			response.setStatuscode("404");
		}

		return response;

	}
	
	
//updatebysession

	    @PutMapping   ("/update")
	 
	    public JsonResponse updateAdmin(@RequestBody Admin updatedAdmin, HttpSession session) {
	        JsonResponse response = new JsonResponse();

	        // Retrieve the currently logged-in Admin from the session
	        Admin currentAdmin = (Admin) session.getAttribute("admininstance");

	        if (currentAdmin == null) {
	            response.setMessege("No admin is logged in");
	            response.setResult("failure");
	            response.setStatuscode("401");
	            return response;
	        }

	        // Fetch the existing Admin details from the database
	        Admin existingAdmin = adminImplService.findById(currentAdmin.getId());
	        if (existingAdmin == null) {
	            response.setMessege("Admin not found");
	            response.setResult("failure");
	            response.setStatuscode("404");
	            return response;
	        }

	        // Update the fields as per the request
	        if (updatedAdmin.getName() != null) {
	            existingAdmin.setName(updatedAdmin.getName());
	        }
	        if (updatedAdmin.getEmail() != null) {
	            existingAdmin.setEmail(updatedAdmin.getEmail());
	        }
	        if (updatedAdmin.getPassword() != null) {
	            String hashedPassword = bCryptPasswordEncoder.encode(updatedAdmin.getPassword());
	            existingAdmin.setPassword(hashedPassword);
	        }

	        // Save the updated Admin to the database
	        adminImplService.saveAdmin(existingAdmin);

	        response.setMessege("Admin updated successfully");
	        response.setResult("success");
	        response.setStatuscode("200");
	        return response;
	    }
	    
	    
	    
	    @PostMapping("/login")
	    public JsonResponse login(@RequestBody Admin admin, HttpSession session) {
	        JsonResponse response = new JsonResponse();
	        Admin existingAdmin = adminImplService.findByEmail(admin.getEmail());

	        if (existingAdmin == null) {
	            response.setMessege("Admin not found");
	            response.setResult("failure");
	            response.setStatuscode("404"); // Not Found
	            return response;
	        }

	        boolean passwordMatches;
	        if (existingAdmin.getPassword().startsWith("$2a$")) {
	            // Password is bcrypt-ed
	            passwordMatches = bCryptPasswordEncoder.matches(admin.getPassword(), existingAdmin.getPassword());
	        } else {
	            // Password is plain text
	            passwordMatches = admin.getPassword().equals(existingAdmin.getPassword());
	        }

	        if (passwordMatches) {
	            // Set the logged-in Admin in the session
	            session.setAttribute("admininstance", existingAdmin);

	            response.setMessege("Login successful");
	            response.setResult("success");
	            response.setStatuscode("200"); // OK
	        } else {
	            response.setMessege("Invalid credentials");
	            response.setResult("failure");
	            response.setStatuscode("401"); // Unauthorized
	        }

	        return response;
	    }

	    
//	    
//	    //new login 
//	    @PostMapping("/loginn")
//	    public JsonResponse loginAdmin(@RequestBody Admin admin, HttpSession session) {
//	        JsonResponse response = new JsonResponse();
//
//	        // Check if an Admin is already logged in
//	        Object adminSession = session.getAttribute("admininstance");
//	        if (adminSession != null) {
//	            response.setMessege("You are already logged in!");
//	            response.setResult("success");
//	            response.setStatuscode("200");
//	            return response;
//	        }
//
//	        // Fetch the Admin details by email
//	        Admin adminDetails = adminImplService.findByEmail(admin.getEmail());
//	        if (adminDetails != null && adminDetails.getStatus().equals("Active")) {
//	            // Verify password
//	            boolean passwordMatches = bCryptPasswordEncoder.matches(admin.getPassword(), adminDetails.getPassword());
//
//	            if (passwordMatches) {
//	                // Set session attribute
//	                session.setAttribute("adminInstance", adminDetails);
//	                response.setMessege("Login successful!");
//	                response.setResult("success");
//	                response.setStatuscode("200");
//	            } else {
//	                response.setMessege("Invalid password. Please try again.");
//	                response.setResult("failure");
//	                response.setStatuscode("401"); // Unauthorized
//	            }
//	        } else {
//	            response.setMessege("Email does not exist or account is not active.");
//	            response.setResult("failure");
//	            response.setStatuscode("404"); // Not Found
//	        }
//
//	        return response;
//	    }

	}



