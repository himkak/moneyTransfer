package com.revolut.it;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.application.SendMoneyServer;
import com.revolut.entity.Account;
import com.revolut.exception.ErrorResponse;
import com.revolut.model.AccountResponse;
import com.revolut.model.AddMoneyRequest;
import com.revolut.model.CreateAccountRequest;
import com.revolut.model.SendMoneyRequest;
import com.revolut.util.HibernateUtil;

public class SendMoneyTest {

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
	public void shouldFailSendMoney_when_SenderAccountNumberInvalid()
			throws JsonProcessingException, IOException, InterruptedException {

		HttpResponse response = sendMoney("0", "1", 10);

		Assert.assertEquals(500, response.getStatusLine().getStatusCode());
		ErrorResponse errResp = objMapper.readValue(EntityUtils.toString(response.getEntity()), ErrorResponse.class);
		Assert.assertEquals("Sender account doesnt exists.", errResp.getMessgae());

	}

	@Test
	public void shouldFailSendMoney_when_ReceiverAccountNumberInvalid()
			throws JsonProcessingException, IOException, InterruptedException {

		String resp = createAccount("him");
		AccountResponse accountInfo = objMapper.readValue(resp, AccountResponse.class);
		HttpResponse response = sendMoney(accountInfo.getAccountId(), "1", 10);

		Assert.assertEquals(500, response.getStatusLine().getStatusCode());
		ErrorResponse errResp = objMapper.readValue(EntityUtils.toString(response.getEntity()), ErrorResponse.class);
		Assert.assertEquals("Receiver account doesnt exists.", errResp.getMessgae());

	}

	@Test
	public void shouldFailSendMoney_when_SenderDoesntHaveAdequateBalance()
			throws JsonProcessingException, IOException, InterruptedException {
		String resp = createAccount("him");
		AccountResponse senderAccountInfo = objMapper.readValue(resp, AccountResponse.class);

		String resp1 = createAccount("him1");
		AccountResponse receiverAccountInfo = objMapper.readValue(resp1, AccountResponse.class);

		HttpResponse response = sendMoney(senderAccountInfo.getAccountId(), receiverAccountInfo.getAccountId(), 10);
		Assert.assertEquals(500, response.getStatusLine().getStatusCode());
		ErrorResponse errResp = objMapper.readValue(EntityUtils.toString(response.getEntity()), ErrorResponse.class);
		Assert.assertEquals("Insufficient balance in sender's account.", errResp.getMessgae());
	}

	@Test
	public void shouldSendMoney_when_SenderHavingAdequateBalanceAndReceiverExists()
			throws JsonProcessingException, IOException, InterruptedException {
		AccountResponse senderAccountInfo = objMapper.readValue(createAccount("him"), AccountResponse.class);

		addMoney(senderAccountInfo.getAccountId(), 100);
		AccountResponse receiverAccountInfo = objMapper.readValue(createAccount("him1"), AccountResponse.class);
		HttpResponse response = sendMoney(senderAccountInfo.getAccountId(), receiverAccountInfo.getAccountId(), 10);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Account senderAccInfo = fetchAccountInfo(senderAccountInfo.getAccountId());
		Assert.assertEquals(90, senderAccInfo.getBalance());
	}
	
	@Test
	public void print(){
		int a='A';
		System.out.println(a);
	}

	@Test
	public void shouldSendMoneyConcurrently_when_SenderHavingAdequateBalanceAndReceiverExists()
			throws JsonProcessingException, IOException, InterruptedException {
		int totalAmtOfSender = 100;
		AccountResponse senderAccountInfo = objMapper.readValue(createAccount("A"), AccountResponse.class);
		addMoney(senderAccountInfo.getAccountId(), totalAmtOfSender);

		List<HttpUriRequest> allRequests = new ArrayList<>();

		for (int i = 65, j=0; j < 40; i++,j++) {
			allRequests.add(getSendMoneyReq(senderAccountInfo, objMapper.readValue(createAccount(Character.toString((char)i)), AccountResponse.class), 10));
		}
		
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(100);
		connectionManager.setDefaultMaxPerRoute(50);

		HttpClient client = HttpClients.custom().setConnectionManager(connectionManager).build();

		List<CompletableFuture<HttpResponse>> futures = allRequests.parallelStream()
				.map(req -> CompletableFuture.supplyAsync(() -> {
					try {
						return client.execute(req);
					} catch (ClientProtocolException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return null;
				})).collect(Collectors.toList());

		int successOps = 0;

		for (Future<HttpResponse> future : futures) {
			try {
				if (future.get().getStatusLine().getStatusCode() == 200) {
					successOps++;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		int currBalanceOfSender = fetchAccountInfo(senderAccountInfo.getAccountId()).getBalance();
		Assert.assertEquals(totalAmtOfSender, successOps * 10 + currBalanceOfSender);

	}

	private HttpUriRequest getSendMoneyReq(AccountResponse senderAccountInfo, AccountResponse receiverAccountInfo,
			int amt) throws JsonProcessingException, UnsupportedEncodingException {
		SendMoneyRequest req = new SendMoneyRequest(senderAccountInfo.getAccountId(),
				receiverAccountInfo.getAccountId(), amt);

		String requestStr = objMapper.writeValueAsString(req);

		HttpPut httpPost = new HttpPut("http://localhost:8080/accounts/v1/transfer");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(requestStr));
		return httpPost;
	}

	private HttpResponse sendMoney(String fromAccountId, String toAccountId, int amount)
			throws JsonProcessingException, IOException, InterruptedException {
		SendMoneyRequest req = new SendMoneyRequest(fromAccountId, toAccountId, amount);
		String requestStr = objMapper.writeValueAsString(req);

		HttpClient client = HttpClients.createDefault();
		HttpPut httpPost = new HttpPut("http://localhost:8080/accounts/v1/transfer");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(requestStr));
		HttpResponse resp = client.execute(httpPost);

		return resp;
	}

	private Account fetchAccountInfo(String accountId) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Query<Account> query = session.createQuery("from Account where accountNum=:accountNum", Account.class);
		query.setParameter("accountNum", accountId);
		Account senderAccInfo = query.getSingleResult();
		session.close();
		return senderAccInfo;
	}

	private void addMoney(String accountId, int amt) throws IOException, InterruptedException {
		AddMoneyRequest req = new AddMoneyRequest(accountId, amt);
		String requestStr = objMapper.writeValueAsString(req);

		HttpClient client = HttpClients.createDefault();
		HttpPut httpPost = new HttpPut("http://localhost:8080/accounts/v1/money");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(requestStr));
		HttpResponse resp = client.execute(httpPost);

		Assert.assertEquals(200, resp.getStatusLine().getStatusCode());
	}

	private String createAccount(String userName) throws JsonProcessingException, IOException, InterruptedException {

		CreateAccountRequest req = new CreateAccountRequest(userName);
		String requestStr = objMapper.writeValueAsString(req);

		HttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://localhost:8080/accounts/v1");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(requestStr));
		HttpResponse resp = client.execute(httpPost);
		return EntityUtils.toString(resp.getEntity());
	}

}
