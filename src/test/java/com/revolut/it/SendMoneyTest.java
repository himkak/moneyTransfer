package com.revolut.it;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.test.application.SendMoneyServer;
import com.revolut.test.entity.Account;
import com.revolut.test.exception.ErrorResponse;
import com.revolut.test.model.AccountResponse;
import com.revolut.test.model.AddMoneyRequest;
import com.revolut.test.model.CreateAccountRequest;
import com.revolut.test.model.SendMoneyRequest;
import com.revolut.test.util.HibernateUtil;

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

		HttpResponse<String> response = sendMoney(0, 1, 10);

		Assert.assertEquals(500, response.statusCode());
		ErrorResponse errResp = objMapper.readValue(response.body(), ErrorResponse.class);
		Assert.assertEquals("Sender account doesnt exists.", errResp.getMessgae());

	}

	@Test
	public void shouldFailSendMoney_when_ReceiverAccountNumberInvalid()
			throws JsonProcessingException, IOException, InterruptedException {

		HttpResponse<String> resp = createAccount("him");
		AccountResponse accountInfo = objMapper.readValue(resp.body(), AccountResponse.class);
		HttpResponse<String> response = sendMoney(accountInfo.getAccountId(), 1, 10);

		Assert.assertEquals(500, response.statusCode());
		ErrorResponse errResp = objMapper.readValue(response.body(), ErrorResponse.class);
		Assert.assertEquals("Receiver account doesnt exists.", errResp.getMessgae());

	}

	@Test
	public void shouldFailSendMoney_when_SenderDoesntHaveAdequateBalance()
			throws JsonProcessingException, IOException, InterruptedException {
		HttpResponse<String> resp = createAccount("him");
		AccountResponse senderAccountInfo = objMapper.readValue(resp.body(), AccountResponse.class);

		HttpResponse<String> resp1 = createAccount("him1");
		AccountResponse receiverAccountInfo = objMapper.readValue(resp1.body(), AccountResponse.class);

		HttpResponse<String> response = sendMoney(senderAccountInfo.getAccountId(), receiverAccountInfo.getAccountId(),
				10);
		Assert.assertEquals(500, response.statusCode());
		ErrorResponse errResp = objMapper.readValue(response.body(), ErrorResponse.class);
		Assert.assertEquals("Insufficient balance in sender's account.", errResp.getMessgae());
	}

	@Test
	public void shouldSendMoney_when_SenderHavingAdequateBalanceAndReceiverExists()
			throws JsonProcessingException, IOException, InterruptedException {
		AccountResponse senderAccountInfo = objMapper.readValue(createAccount("him").body(), AccountResponse.class);

		addMoney(senderAccountInfo.getAccountId(), 100);
		AccountResponse receiverAccountInfo = objMapper.readValue(createAccount("him1").body(), AccountResponse.class);
		HttpResponse<String> response = sendMoney(senderAccountInfo.getAccountId(), receiverAccountInfo.getAccountId(),
				10);
		Assert.assertEquals(200, response.statusCode());
		Account senderAccInfo = fetchAccountInfo(senderAccountInfo.getAccountId());
		Assert.assertEquals(90, senderAccInfo.getBalance());
	}

	@Test
	public void shouldSendMoneyConcurrently_when_SenderHavingAdequateBalanceAndReceiverExists()
			throws JsonProcessingException, IOException, InterruptedException {
		int totalAmtOfSender = 100;
		AccountResponse senderAccountInfo = objMapper.readValue(createAccount("A").body(), AccountResponse.class);
		AccountResponse receiver1AccountInfo = objMapper.readValue(createAccount("B").body(), AccountResponse.class);
		AccountResponse receiver2AccountInfo = objMapper.readValue(createAccount("C").body(), AccountResponse.class);
		AccountResponse receiver3AccountInfo = objMapper.readValue(createAccount("D").body(), AccountResponse.class);
		addMoney(senderAccountInfo.getAccountId(), totalAmtOfSender);

		List<HttpRequest> allRequests = new ArrayList<>(3);

		allRequests.add(getSendMoneyReq(senderAccountInfo, receiver1AccountInfo, 10));
		allRequests.add(getSendMoneyReq(senderAccountInfo, receiver2AccountInfo, 10));
		allRequests.add(getSendMoneyReq(senderAccountInfo, receiver3AccountInfo, 10));

		HttpClient client = HttpClient.newBuilder().build();
		List<CompletableFuture<HttpResponse<String>>> futures = allRequests.stream()
				.map(req -> client.sendAsync(req, BodyHandlers.ofString())).collect(Collectors.toList());

		int successOps = 0;

		for (Future<HttpResponse<String>> future : futures) {
			try {
				if (future.get().statusCode() == 200) {
					successOps++;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		int currBalanceOfSender = fetchAccountInfo(senderAccountInfo.getAccountId()).getBalance();
		Assert.assertEquals(totalAmtOfSender, successOps * 10 + currBalanceOfSender);

	}

	private HttpRequest getSendMoneyReq(AccountResponse senderAccountInfo, AccountResponse receiverAccountInfo, int amt)
			throws JsonProcessingException {
		SendMoneyRequest req = new SendMoneyRequest(senderAccountInfo.getAccountId(),
				receiverAccountInfo.getAccountId(), amt);

		String requestStr = objMapper.writeValueAsString(req);
		return HttpRequest.newBuilder().header("Content-Type", "application/json")
				.uri(URI.create("http://localhost:8080/accounts/v1/transfer")).PUT(BodyPublishers.ofString(requestStr))
				.build();
	}

	private HttpResponse<String> sendMoney(int fromAccountId, int toAccountId, int amount)
			throws JsonProcessingException, IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().build();
		SendMoneyRequest req = new SendMoneyRequest(fromAccountId, toAccountId, amount);
		String requestStr = objMapper.writeValueAsString(req);
		HttpRequest request = HttpRequest.newBuilder().header("Content-Type", "application/json")
				.uri(URI.create("http://localhost:8080/accounts/v1/transfer")).PUT(BodyPublishers.ofString(requestStr))
				.build();
		HttpResponse<String> resp = client.send(request, BodyHandlers.ofString());
		return resp;
	}

	private Account fetchAccountInfo(int accountId) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Query<Account> query = session.createQuery("from Account where accountNum=:accountNum", Account.class);
		query.setParameter("accountNum", accountId);
		Account senderAccInfo = query.getSingleResult();
		session.close();
		return senderAccInfo;
	}

	private void addMoney(int accountId, int amt) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().build();
		AddMoneyRequest req = new AddMoneyRequest(accountId, amt);
		String requestStr = objMapper.writeValueAsString(req);
		HttpRequest request = HttpRequest.newBuilder().header("Content-Type", "application/json")
				.uri(URI.create("http://localhost:8080/accounts/v1/money")).PUT(BodyPublishers.ofString(requestStr))
				.build();
		HttpResponse<String> resp = client.send(request, BodyHandlers.ofString());
		Assert.assertEquals(200, resp.statusCode());
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

}
