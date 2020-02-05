package com.revolut.it;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
		createAccount("Him11");
		createAccount("Him11");
		createAccount("Him12");

		List<UserAccountsInfo> allAccountsInfo = fetchAccountsInfo();

		Assert.assertEquals(2, allAccountsInfo.stream().filter(acc -> acc.getUserName().equals("Him11")).findFirst()
				.get().getUserAccount().size());
		Assert.assertEquals(1, allAccountsInfo.stream().filter(acc -> acc.getUserName().equals("Him12")).findFirst()
				.get().getUserAccount().size());

	}

	private HttpResponse createAccount(String userName)
			throws JsonProcessingException, IOException, InterruptedException {
		CreateAccountRequest req = new CreateAccountRequest(userName);
		String requestStr = objMapper.writeValueAsString(req);

		HttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://localhost:8080/accounts/v1");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(requestStr));
		HttpResponse resp = client.execute(httpPost);

		Assert.assertEquals(200, resp.getStatusLine().getStatusCode());

		return resp;

	}

	private List<UserAccountsInfo> fetchAccountsInfo()
			throws JsonParseException, JsonMappingException, IOException, InterruptedException {

		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://localhost:8080/users/v1");
		request.setHeader("Content-Type", "application/json");
		CloseableHttpResponse response = client.execute(request);
		String respStr = EntityUtils.toString(response.getEntity());
		return objMapper.readValue(respStr, new TypeReference<List<UserAccountsInfo>>() {
		});
	}
}
