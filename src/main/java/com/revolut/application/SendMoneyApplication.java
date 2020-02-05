package com.revolut.application;

import org.restlet.Context;
import org.restlet.ext.jaxrs.JaxRsApplication;

import com.revolut.exception.ExceptionHandler;
import com.revolut.util.HibernateUtil;

public class SendMoneyApplication extends JaxRsApplication {

	public SendMoneyApplication(final Context context) {
		super(context);
		 setStatusService(new ExceptionHandler());
		this.add(new SendMoneySubApplication());
		initializeLoading();
	}

	private void initializeLoading() {
		HibernateUtil.getSessionFactory();
		ConfigurationLoader.getInstance();
	}
}
