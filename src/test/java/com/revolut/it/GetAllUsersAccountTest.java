package com.revolut.it;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.application.SendMoneyServer;
import com.revolut.model.CreateAccountRequest;
import com.revolut.model.UserAccountsInfo;

public class GetAllUsersAccountTest {

	private ObjectMapper objMapper = new ObjectMapper();

	@Before
	public void setup() throws Exception {
		SendMoneyServer.main(null);
	}

	@After
	public void tearDown() {
		SendMoneyServer.stop();
	}
	
	@Test
	public void shouldReturnAllAccountsOfAllUsers_when_AccountsCreatedForUsers()
			throws JsonProcessingException, IOException, InterruptedException {
		createAccount("Him1");
		createAccount("Him1");
		createAccount("Him2");

		List<UserAccountsInfo> allAccountsInfo = fetchAccountsInfo();
		
		Assert.assertEquals(2, allAccountsInfo.stream().filter(acc->acc.getUserName().equals("Him1")).findFirst().get().getUserAccount().size());
		Assert.assertEquals(1, allAccountsInfo.stream().filter(acc->acc.getUserName().equals("Him2")).findFirst().get().getUserAccount().size());

	}

	private HttpResponse<String> createAccount(String userName)
			throws JsonProcessingException, IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().build();
		CreateAccountRequest req = new CreateAccountRequest(userName);
		String requestStr = objMapper.writeValueAsString(req);
		HttpRequest request = HttpRequest.newBuilder().header("Content-Type", "application/json")
				.uri(URI.create("http://localhost:8080/accounts/v1")).POST(BodyPublishers.ofString(requestStr)).build();
		HttpResponse<String> resp = client.send(request, BodyHandlers.ofString());
		return resp;
	}

	private List<UserAccountsInfo> fetchAccountsInfo()
			throws JsonParseException, JsonMappingException, IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().build();
		HttpRequest request = HttpRequest.newBuilder().header("Content-Type", "application/json")
				.uri(URI.create("http://localhost:8080/users/v1")).GET().build();
		HttpResponse<String> resp = client.send(request, BodyHandlers.ofString());
		return objMapper.readValue(resp.body(), new TypeReference<List<UserAccountsInfo>>() {
		});
	}
}
