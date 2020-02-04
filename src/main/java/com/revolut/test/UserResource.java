package com.revolut.test;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.revolut.test.model.UserAccountsInfo;

@Path("/user/v1")
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource  extends ServerResource  {

	private final AccountService accountService = AccountService.getInstance();

	@Get
	@Path("/user")
	public List<UserAccountsInfo> getAllUsers() {
		return accountService.getAllUsers();
	}

}
