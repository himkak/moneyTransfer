package com.revolut.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.ServerResource;

import com.revolut.AccountService;
import com.revolut.model.UserAccountsInfo;

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
