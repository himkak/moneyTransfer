package com.revolut.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.ServerResource;

import com.revolut.test.AccountService;
import com.revolut.test.model.UserAccountsInfo;

@Path("/users/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource extends ServerResource {

	private final AccountService accountService = AccountService.getInstance();

	@GET
	public List<UserAccountsInfo> getAllUsers() {
		return accountService.getAllUsers();
	}

}
