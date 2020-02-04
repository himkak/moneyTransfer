package com.revolut.it;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.application.SendMoneyServer;
import com.revolut.entity.Account;
import com.revolut.entity.UserDetails;
import com.revolut.model.AccountResponse;
import com.revolut.model.CreateAccountRequest;
import com.revolut.util.HibernateUtil;

public class CreateAccountTest {
	@Before
	public void setup() throws Exception {
		SendMoneyServer.main(null);
	}

	@After
	public void tearDown() {
		SendMoneyServer.stop();
	}

	private ObjectMapper objMapper = new ObjectMapper();

	@Test
	public void shouldCreateUserAndAccount_when_newUserProvided() throws IOException, InterruptedException {

		String userName = "Him";
		HttpResponse<String> resp = createAccount(userName);

		Assert.assertEquals(200, resp.statusCode());
		assertAccountAndUserCount(userName, resp, 1);

	}

	private void assertAccountAndUserCount(String userName, HttpResponse<String> resp, int noOfAccounts)
			throws IOException, JsonParseException, JsonMappingException {
		AccountResponse accountInfo = objMapper.readValue(resp.body(), AccountResponse.class);
		int accountId = accountInfo.getAccountId();
		Session session = HibernateUtil.getSessionFactory().openSession();
		Query<Account> queryAccount = session.createQuery("from Account where accountNum=:accountNum", Account.class);
		Query<UserDetails> queryUser = session.createQuery("from UserDetails where userName=:userName",
				UserDetails.class);
		queryAccount.setParameter("accountNum", accountId);
		queryUser.setParameter("userName", userName);
		int fetchSize = queryAccount.list().size();
		Assert.assertEquals(1, fetchSize);
		Assert.assertEquals(1, queryUser.list().size());
	}

	private void assertMultiAccountAndUserCount(String userName, List<HttpResponse<String>> responseList,
			int noOfAccounts) throws IOException, JsonParseException, JsonMappingException {

		for (int i = 0; i < responseList.size(); i++) {
			HttpResponse<String> resp = responseList.get(i);
			AccountResponse accountInfo = objMapper.readValue(resp.body(), AccountResponse.class);
			int accountId = accountInfo.getAccountId();
			Session session = HibernateUtil.getSessionFactory().openSession();
			Query<Account> queryAccount = session.createQuery("from Account where accountNum=:accountNum",
					Account.class);
			Query<UserDetails> queryUser = session.createQuery("from UserDetails where userName=:userName",
					UserDetails.class);
			queryAccount.setParameter("accountNum", accountId);
			queryUser.setParameter("userName", userName);
			int fetchSize = queryAccount.list().size();
			Assert.assertEquals(1, fetchSize);
			Assert.assertEquals(1, queryUser.list().size());
		}
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

	@Test
	public void shouldCreateAccountAndLinkToExistingUser_when_exitingUserProvided()
			throws JsonProcessingException, IOException, InterruptedException {

		// send a create user request
		String userName="Him1";
		HttpResponse<String> resp = createAccount(userName);
		Assert.assertEquals(200, resp.statusCode());

		HttpResponse<String> resp2 = createAccount(userName);
		Assert.assertEquals(200, resp.statusCode());
		// send a create user request

		// fetch all users and assert
		
		assertMultiAccountAndUserCount(userName, Arrays.asList(resp,resp2), 2);
	}

}
