package com.revolut.it;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
		HttpResponse resp = createAccount(userName);

		Assert.assertEquals(200, resp.getStatusLine().getStatusCode());
		assertAccountAndUserCount(userName, resp, 1);

	}

	private void assertAccountAndUserCount(String userName, HttpResponse resp, int noOfAccounts)
			throws IOException, JsonParseException, JsonMappingException {
		AccountResponse accountInfo = objMapper.readValue(EntityUtils.toString(resp.getEntity()), AccountResponse.class);
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

	private void assertMultiAccountAndUserCount(String userName, List<HttpResponse> responseList,
			int noOfAccounts) throws IOException, JsonParseException, JsonMappingException {

		for (int i = 0; i < responseList.size(); i++) {
			HttpResponse resp = responseList.get(i);
			AccountResponse accountInfo = objMapper.readValue(EntityUtils.toString(resp.getEntity()), AccountResponse.class);
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

	private HttpResponse createAccount(String userName)
			throws JsonProcessingException, IOException, InterruptedException {
		CreateAccountRequest req = new CreateAccountRequest(userName);
		String requestStr = objMapper.writeValueAsString(req);
		
		HttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://localhost:8080/accounts/v1");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(requestStr));
		HttpResponse resp = client.execute(httpPost);
		
		return resp;
	}

	@Test
	public void shouldCreateAccountAndLinkToExistingUser_when_exitingUserProvided()
			throws JsonProcessingException, IOException, InterruptedException {

		String userName = "Him1";
		HttpResponse resp = createAccount(userName);
		Assert.assertEquals(200, resp.getStatusLine().getStatusCode());

		HttpResponse resp2 = createAccount(userName);
		Assert.assertEquals(200, resp.getStatusLine().getStatusCode());

		assertMultiAccountAndUserCount(userName, Arrays.asList(resp, resp2), 2);
	}

}
