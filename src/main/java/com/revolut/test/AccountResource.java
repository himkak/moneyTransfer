package com.revolut.test;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.test.model.AccountResponse;
import com.revolut.test.model.AddMoneyRequest;
import com.revolut.test.model.CreateAccountRequest;
import com.revolut.test.model.SendMoneyRequest;
import com.revolut.test.model.UserAccountsInfo;

@Path("/accounts/v1")
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource extends ServerResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);
	private final AccountService accountService = AccountService.getInstance();


	//TODO remove
	@Get
	@Path("/user")
	public List<UserAccountsInfo> getAllUsers() {
		return accountService.getAllUsers();
	}
	
	@Post
	public AccountResponse createAccount(CreateAccountRequest userDetails) {
		LOGGER.info("Request received to create account");
		int accNum = accountService.createAccount(userDetails);
		LOGGER.debug("Account created " + accNum);
		return new AccountResponse(accNum);
	}

	/*
	 * @Put
	 * 
	 * @Path("/transfer") public int sendMoney(SendMoneyRequest request) { return
	 * accountService.sendMoney(request); }
	 */


	
	@Put
	@Path("/money")
	public void addMoney(AddMoneyRequest req) {
		accountService.addMoney(req);
		
	}
	

}
