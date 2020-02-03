package com.revolut.test;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.revolut.test.model.UserDetails;

@Path("/accounts/v1")
//@Produces(MediaType.APPLICATION_JSON)

public class AccountResource extends ServerResource {

	private final AccountService accountService = AccountService.getInstance();

	@Post
	@Consumes(MediaType.APPLICATION_JSON)
	public String createAccount(UserDetails userDetails) {
		// UserDetails userDetails=new UserDetails();
		System.out.println("Request received to create account");
		accountService.createAccount(userDetails);
		System.out.println("Account created");
		return "Account created";
	}

	@Put
	@Path("/transfer")
	public void sendMoney() {
		System.out.println("server delete method called.");
	}

	@Get
	public List<UserDetails> getAllUsers() {
		return accountService.getAllUsers();
	}
}
