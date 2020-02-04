package com.revolut.it;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.test.application.SendMoneyServer;
import com.revolut.test.exception.ErrorResponse;
import com.revolut.test.model.SendMoneyRequest;

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
	public void shouldFailSendMoney_when_SenderDoesntHaveAdequateBalance() {

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

}
