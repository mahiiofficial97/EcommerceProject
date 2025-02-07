package com.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.model.Admin;
import com.model.JsonResponse;
import com.model.SuperAdmin;
import com.service.AdminImplService;
import com.service.SuperAdminServiceImpl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/superAdmin")
public class SuperController {

	@Autowired
	SuperAdminServiceImpl superAdminImpl;

	@Autowired
	AdminImplService adminImplService;

	// $2a$10$unwKzrNTbecjI02vPvBd3.UKqIlzQQ5V5eFnFE28jcjSuWIopPM5a
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@PostMapping("/save")
	public JsonResponse saveAdmin(@RequestBody SuperAdmin superadmin) {
		JsonResponse ao = new JsonResponse();

		com.model.SuperAdmin email = superAdminImpl.findByEmail(superadmin.getEmail());

		if (email == null) {

			String hashedpassword = bCryptPasswordEncoder.encode(superadmin.getPassword());
			superadmin.setPassword(hashedpassword);

			superAdminImpl.savesuperAdmin(superadmin);

			ao.setMessege("Superadmin save succesfully");
			ao.setResult("success");
			ao.setStatuscode("200");
		} else

		{
			ao.setStatuscode("400");
			ao.setResult("unsuccessful");
			ao.setMessege("this id is already created please create new id ");
		}
		return ao;

	}

	@DeleteMapping("/delete/{id}")
	public JsonResponse deleteSuperAdmin(@PathVariable Long id) {
		JsonResponse response = new JsonResponse();

		SuperAdmin superAdmin = superAdminImpl.findById(id);
		if (superAdmin != null) {
			superAdminImpl.deleteById(id);

			response.setMessege("SuperAdmin deleted successfully.");
			response.setResult("success");
			response.setStatuscode("200");
		} else {
			response.setMessege("SuperAdmin not found.");
			response.setResult("unsuccessful");
			response.setStatuscode("404");
		}
		return response;
	}

	@PutMapping("/update/{id}")
	public JsonResponse updateSuperAdmin(@PathVariable("id") Long id, @RequestBody SuperAdmin updatedSuperAdmin) {
		JsonResponse response = new JsonResponse();

		SuperAdmin superAdmin = superAdminImpl.findById(id);
		if (superAdmin != null) {
			superAdmin.setName(updatedSuperAdmin.getName());
			superAdmin.setMobileno(updatedSuperAdmin.getMobileno());
			superAdmin.setLastupdate(updatedSuperAdmin.getLastupdate());

			superAdmin.setEmail(updatedSuperAdmin.getEmail());
			if (updatedSuperAdmin.getPassword() != null) {
				String hashedPassword = bCryptPasswordEncoder.encode(updatedSuperAdmin.getPassword());
				superAdmin.setPassword(hashedPassword);
			}

			superAdminImpl.savesuperAdmin(superAdmin);

			response.setMessege("SuperAdmin updated successfully.");
			response.setResult("success");
			response.setStatuscode("200");
		} else {
			response.setMessege("SuperAdmin not found.");
			response.setResult("unsuccessful");
			response.setStatuscode("404");
		}
		return response;
	}

	@GetMapping("/all")
	public JsonResponse getAllSuperAdmins() {
		JsonResponse response = new JsonResponse();

		List<SuperAdmin> superAdmins = superAdminImpl.findAll();
		if (!superAdmins.isEmpty()) {
			response.setMessege("SuperAdmins retrieved successfully.");
			response.setResult("success");
			response.setStatuscode("200");
			response.setMessege(superAdmins.toString());
		} else {
			response.setMessege("No SuperAdmins found.");
			response.setResult("unsuccessful");
			response.setStatuscode("404");
		}
		return response;
	}

	@PostMapping("/login")
	public JsonResponse loginAdmin(@RequestBody SuperAdmin superadmin, HttpSession session) {
		JsonResponse response = new JsonResponse();

		// Check if the admin is already logged in
		Object adminSession = session.getAttribute("admininstance");

		if (adminSession != null) {
			response.setMessege("You are already logged in.");
			response.setStatuscode("200");
			response.setResult("success");
			return response;
		}
		session.setMaxInactiveInterval(200000);
		// Fetch existing admin by email
		SuperAdmin existingAdmin = superAdminImpl.findByEmail(superadmin.getEmail());

		
		
		if (existingAdmin != null && existingAdmin.getStatus().equals("Active")) {
			// Verify password
			boolean passwordMatches = bCryptPasswordEncoder.matches(superadmin.getPassword(),
					existingAdmin.getPassword());

			if (passwordMatches) {
				// Set session attribute
				session.setAttribute("admininstance", existingAdmin);
				response.setStatuscode("200");
				response.setMessege("Login successful.");
				response.setResult("success");
			} else {
				response.setStatuscode("300");
				response.setMessege("Invalid password.");
				response.setResult("failure");
			}
		} else {
			response.setStatuscode("400");
			response.setMessege("Email does not exist or account is not active.");
			response.setResult("failure");
		}

		return response;
	}

	@PostMapping("/logout")
	public JsonResponse logout(HttpSession session) {
		JsonResponse response = new JsonResponse();

		// Check if the user is already logged out (session doesn't exist)
		Object adminSession = session.getAttribute("admininstance");

		if (adminSession == null) {
			response.setStatuscode("200");
			response.setMessege("You are already logged out.");
			response.setResult("success");
		} else {
			// Invalidate the session to log out
	        session.removeAttribute("admininstance");  // Remove the attribute before invalidating

			session.invalidate();

			response.setStatuscode("200");
			response.setMessege("Logged out successfully.");
			response.setResult("success");
		}

		return response;
	}

	// creating Admin By LoggedBy SuperAdmin

	@RequestMapping(value = "/createnewadmin", method = RequestMethod.POST)
	public JsonResponse craeteNewAdmin(@RequestBody Admin admin, HttpSession session) {
		JsonResponse response = new JsonResponse();

		// Fetch The SuperAdmin instance from the session
		Object isSuperAdminloggedOrNot = session.getAttribute("admininstance");

		if (isSuperAdminloggedOrNot == null) {
			response.setMessege("No SuperAdmin Is Logged In The Application. Please Login First!!!!");
			response.setResult("failure");
			response.setStatuscode("401"); // Unauthorized
			return response;
		}

		// Check if Admin already exists by email
		Admin exitingAdmin = adminImplService.findByEmail(admin.getEmail());
		if (exitingAdmin != null) {
			response.setMessege("Admin already exists. Please create a new Admin.");
			response.setResult("Unsuccess");
			response.setStatuscode("409"); // Conflict
			return response; // Return immediately after setting the conflict response
		}

		// Set The Logged-In SuperAdmin To the admin instance
		admin.setSuperAdmin((SuperAdmin) isSuperAdminloggedOrNot);

		// Save the new Admin
		adminImplService.saveAdmin(admin);

		response.setMessege("Admin saved successfully!");
		response.setResult("Success");
		response.setStatuscode("201"); // Created
		return response;
	}

	@PostMapping("/updateStatus/{id}")
	public JsonResponse UpdateSuperAdmin(@PathVariable("id") Long adminid, HttpSession session) {
		JsonResponse response = new JsonResponse();

		// check if the SuperAdmin is logged in

		Object superadminsession = session.getAttribute("superadminInstance");

		if (superadminsession == null) {
			response.setMessege("session Expired.Please Login As SuperAdmin");
			response.setStatuscode("401");
			response.setResult("failure");

		}

		// Fetch the SuperAdminById

		SuperAdmin superAdmin = superAdminImpl.findById(adminid);

		if (superAdmin != null) {
			// Toggle the SuoerAsdmin status between Actve And Inactive

			if ("Active".equals(superAdmin.getStatus())) {
				superAdmin.setStatus("InActive");
			} else {
				superAdmin.setStatus("Active");
			}

			superAdminImpl.savesuperAdmin(superAdmin);
			response.setMessege("SuperAdmin Status Update Successfully with id =" + adminid);
			response.setResult("succces");
			response.setStatuscode("200");
			response.setStatuscode(superAdmin.getStatus());

		} else {
			response.setMessege("SuperAdmin Not found with id =" + adminid);
			response.setResult("failure");
			response.setStatuscode("404");
		}

		return response;

	}

	// DeacticateSuperAdmin
	@PostMapping("/deactivate/{id}")
	public JsonResponse deActivateSuperAdmin(@PathVariable("id") Long adminid, HttpSession session) {
		JsonResponse response = new JsonResponse();

		// check if the SuperAdmin is logged in

		Object superadminsession = session.getAttribute("superadminInstance");

		if (superadminsession == null) {
			response.setMessege("session Expired.Please Login As SuperAdmin");
			response.setStatuscode("401");
			response.setResult("failure");

		}

		// Fetch the SuperAdminById

		SuperAdmin SuperAdmin = superAdminImpl.findById(adminid);

		if (SuperAdmin != null) {

			SuperAdmin.setStatus("Inactive");
			superAdminImpl.savesuperAdmin(SuperAdmin);
			response.setMessege("SuperAdmin Status Deactivated Successfully with id =" + adminid);
			response.setResult("succces");
			response.setStatuscode("200");

		} else {
			response.setMessege("SuperAdmin Not found with id =" + adminid);
			response.setResult("failure");
			response.setStatuscode("404");
		}

		return response;

	}

	// Activate SuoerAdmin
	@PostMapping("/active/{id}")
	public JsonResponse ActiveSuperAdmin(@PathVariable("id") Long adminid, HttpSession session) {
		JsonResponse response = new JsonResponse();

		// check if the SuperAdmin is logged in

		Object superadminsession = session.getAttribute("superadminInstance");

		if (superadminsession == null) {
			response.setMessege("session Expired.Please Login As SuperAdmin");
			response.setStatuscode("401");
			response.setResult("failure");

		}

		// Fetch the SuperAdminById

		SuperAdmin SuperAdmin = superAdminImpl.findById(adminid);

		if (SuperAdmin != null) {

			SuperAdmin.setStatus("Active");
			superAdminImpl.savesuperAdmin(SuperAdmin);
			response.setMessege("SuperAdmin Status Ativated Successfully with id =" + adminid);
			response.setResult("succces");
			response.setStatuscode("200");

		} else {
			response.setMessege("SuperAdmin Not found with id =" + adminid);
			response.setResult("failure");
			response.setStatuscode("404");
		}

		return response;

	}

	// viewStatusApi
	@PostMapping("/viewStatus/{id}")
	public JsonResponse statusuperAdmin(@PathVariable("id") Long id, HttpSession session) {
		JsonResponse response = new JsonResponse();

		Object superadminsession = session.getAttribute("superadminInstance");

		if (superadminsession == null) {
			response.setMessege("session Expired.Please Login As SuperAdmin");
			response.setStatuscode("401");
			response.setResult("failure");

		}

		SuperAdmin SuperAdmin = superAdminImpl.findById(id);

		if (SuperAdmin != null) {

			response.setMessege("SuperAdmin Status Fetached Successfully with id =" + id);
			response.setResult("succces");
			response.setStatuscode("200");
			response.setStatuscode(SuperAdmin.getStatus());

		} else {
			response.setMessege("SuperAdmin Not found with id =" + id);
			response.setResult("failure");
			response.setStatuscode("404");
		}

		return response;

	}

	
	//Automatically SuperAdmin DEactivate if he not login from last 1 month 
	
	 @PostMapping("/autoDeactivate")
	    public JsonResponse autoDeactivateInactiveSuperAdmins() {
	        JsonResponse response = new JsonResponse();

	        int updatedCount = superAdminImpl.deactivateInactiveSuperAdmins();
	        if (updatedCount > 0) {
	            response.setStatuscode("200");
	            response.setMessege(updatedCount + " SuperAdmins were deactivated successfully.");
	            response.setResult("success");
	        } else {
	            response.setStatuscode("204");
	            response.setMessege("No inactive SuperAdmins found.");
	            response.setResult("success");
	        }

	        return response;
	    }
	
	 
	 
	
	// View Stat

	// super Admin Login Validation
	// 1 st validatio Check is Logged in or not
	// 2nd validation check Status DeActive
	// email id correct
	// paswword correct
	// mobile no is correct
	 
	 
	 //email login api

	 
	 @PostMapping("/loginemail")
	    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
	        String email = request.get("email");
	        String otp = request.get("otp"); // OTP can be null for the first step

	        JsonResponse response = superAdminImpl.login(email, otp);
	        return ResponseEntity.status(Integer.parseInt(response.getStatuscode())).body(response);
	    }
	 
	 
	 @PostMapping("/loginsessionemail")
	    public ResponseEntity<?> loginWithHttpSession(@RequestBody Map<String, String> request, HttpSession session) {
	        String email = request.get("email");
	        String otp = request.get("otp"); // OTP can be null for the first step

	        // Call the service to handle OTP login and session creation
	        JsonResponse response = superAdminImpl.loginwithsession(email, otp, session);

	        // Return the appropriate response
	        return ResponseEntity.status(Integer.parseInt(response.getStatuscode())).body(response);
	    }
	        

   // email forgot password api
	 
	 @PostMapping("/forgotpassword")
	    public ResponseEntity<JsonResponse> forgotPassword(@RequestBody Map<String, String> request) {
	        String email = request.get("email");
	        JsonResponse response = superAdminImpl.forgotPasswordRequest(email);
	        return ResponseEntity.status(Integer.parseInt(response.getStatuscode())).body(response);
	    }

	    // Step 2: Verify OTP and Reset Password
	    @PostMapping("/reset-password")
	    public ResponseEntity<JsonResponse> resetPassword(@RequestBody Map<String, String> request, HttpSession session) {
	        String email = request.get("email");
	        String otp = request.get("otp");
	        String newPassword = request.get("newPassword");

	        JsonResponse response = superAdminImpl.verifyOtpAndResetPassword(email, otp, newPassword, session);
	        return ResponseEntity.status(Integer.parseInt(response.getStatuscode())).body(response);
	    }	
	    
	    
	    //upload image APi
	    
	    
	    @PostMapping("/uploadImage")
	    public ResponseEntity<JsonResponse> uploadImage(@RequestParam("image") MultipartFile image, HttpSession session) {
	        JsonResponse response = superAdminImpl.uploadImage(image, session);
	        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatuscode())));
	    }
	    
	    
	    
	    //retruiver image
	    
	    @GetMapping("/getImage/{id}")
	    public ResponseEntity<?> getImageById(@PathVariable("id") Long id, HttpSession session) {
	        // Get the response from the service layer
	        JsonResponse response = superAdminImpl.getImageById(id, session);

	        // If status code is 200, proceed to return the image as the response
	        if ("200".equals(response.getStatuscode())) {
	            // Extract the image path from the response message
	            String imagePathStr = response.getMessege();
	            Path imagePath = Paths.get(imagePathStr);

	            try {
	                // Create resource from the image path
	                Resource resource = new UrlResource(imagePath.toUri());

	                // Check if content type is null, and set a fallback
	                String contentType = Files.probeContentType(imagePath);
	                if (contentType == null) {
	                    contentType = "application/octet-stream"; // Fallback content type
	                }

	                // Return the image as a response entity with the appropriate content type
	                return ResponseEntity.ok()
	                        .contentType(MediaType.parseMediaType(contentType))  // Set content type
	                        .body(resource);  // Return the image directly
	            } catch (IOException e) {
	                // Handle IO exceptions when fetching the image
	                response.setMessege("Error retrieving the image: " + e.getMessage());
	                response.setStatuscode("500");
	                response.setResult("failure");
	                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	            }
	        }

	        // If the response from the service layer isn't a success (statuscode not 200), return the response
	        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.getStatuscode())));
	    }
	

	    @GetMapping("/getImagebyid/{id}")
	    public ResponseEntity<?> getImageById(@PathVariable("id") Long id) {
	        return superAdminImpl.getImageByIdd(id);
	    }
 
	    
}