package com.model;

import lombok.Data;

@Data
public class JsonResponse {
	
	
	public JsonResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public JsonResponse(String messege, String statuscode, String result) {
		super();
		this.messege = messege;
		this.statuscode = statuscode;
		this.result = result;
	}
	private String messege;
	private String statuscode;
	private String result;
	

}
