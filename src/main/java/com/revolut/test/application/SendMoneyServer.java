package com.revolut.test.application;

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SendMoneyServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendMoneyServer.class);

	private static Component comp;

	private SendMoneyServer() {

	}

	public static void main(final String[] args) throws Exception {
		comp = new Component();
		final Server server = comp.getServers().add(Protocol.HTTP, 8080);
		final JaxRsApplication application = new SendMoneyApplication(comp.getContext());
		application.add(new SendMoneySubApplication());
		comp.getDefaultHost().attach(application);
		comp.start();
		LOGGER.debug("Server started on port " + server.getPort());
	}

	public static void stop() {
		try {
			comp.stop();
		} catch (Exception e) {
			LOGGER.error("Exception while stopping the server",e);
		}
	}

}
