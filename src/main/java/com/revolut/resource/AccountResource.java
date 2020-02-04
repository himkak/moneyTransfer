package com.revolut.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.AccountService;
import com.revolut.model.AccountResponse;
import com.revolut.model.AddMoneyRequest;
import com.revolut.model.AddMoneyResponse;
import com.revolut.model.CreateAccountRequest;
import com.revolut.model.MoneyTransferResponse;
import com.revolut.model.SendMoneyRequest;

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
	public MoneyTransferResponse sendMoney(SendMoneyRequest request) {
		int txnId= accountService.sendMoney(request);
		return new MoneyTransferResponse(txnId);
	}

	@PUT
	@Path("/money")
	public AddMoneyResponse addMoney(AddMoneyRequest req) {
		boolean isAdded= accountService.addMoney(req);
		return new AddMoneyResponse(isAdded);
	}

}
