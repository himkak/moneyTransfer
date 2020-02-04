package com.revolut.test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.test.model.AccountResponse;
import com.revolut.test.model.AddMoneyRequest;
import com.revolut.test.model.CreateAccountRequest;
import com.revolut.test.model.SendMoneyRequest;

@Path("/accounts/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource extends ServerResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);
	private final AccountService accountService = AccountService.getInstance();

	@POST
	public AccountResponse createAccount(CreateAccountRequest userDetails) {
		LOGGER.info("Request received to create account");
		int accNum = accountService.createAccount(userDetails);
		LOGGER.debug("Account created " + accNum);
		return new AccountResponse(accNum);
	}

	@PUT
	@Path("/transfer")
	public int sendMoney(SendMoneyRequest request) {
		return accountService.sendMoney(request);
	}

	@PUT
	@Path("/money")
	public void addMoney(AddMoneyRequest req) {
		accountService.addMoney(req);
	}

}
