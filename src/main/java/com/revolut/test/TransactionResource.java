package com.revolut.test;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.test.model.TransactionHistoryResponse;

@Path("/transactions/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource extends ServerResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);
	private final AccountService accountService = AccountService.getInstance();
	
	@GET
	public List<TransactionHistoryResponse> getAllTransactions() {
		return accountService.getAllTransactions();
	}

}
