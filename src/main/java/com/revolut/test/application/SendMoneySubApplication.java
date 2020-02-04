package com.revolut.test.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.revolut.resource.AccountResource;
import com.revolut.resource.TransactionResource;
import com.revolut.resource.UserResource;

public class SendMoneySubApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> resources = new HashSet<Class<?>>();
		resources.add(AccountResource.class);
		resources.add(UserResource.class);
		resources.add(TransactionResource.class);
		return resources;
	}
}
