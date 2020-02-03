package com.revolut.test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.ServerResource;

import com.revolut.test.model.UserDetails;

@Path("/accounts/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource extends ServerResource {

	private final AccountService accountService = AccountService.getInstance();

	@POST
	public String createAccount(UserDetails userDetails) {
		
		accountService.createAccount(userDetails);

		return "Account created";
	}

	@PUT
	@Path("/transfer")
	public void sendMoney() {
		System.out.println("server delete method called.");
	}
}
