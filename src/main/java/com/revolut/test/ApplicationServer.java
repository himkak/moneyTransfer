package com.revolut.test;

import org.restlet.Server;
import org.restlet.data.Protocol;

public class ApplicationServer {

	public static void main(String[] args) throws Exception{
		
		Server server = new Server(Protocol.HTTP,8080,AccountResource.class);
		server.start();
	}
}
