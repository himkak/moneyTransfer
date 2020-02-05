package com.revolut.application;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);
	private static ConfigurationLoader instance = null;

	private ConfigurationLoader() {

	}

	public static ConfigurationLoader getInstance() {
		if (instance == null) {
			synchronized (ConfigurationLoader.class) {
				if (instance == null) {
					instance = new ConfigurationLoader();
					instance.loadProperties();
				}
			}
		}
		return instance;
	}

	private Properties props = null;

	private void loadProperties() {
		props = new Properties();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try {
			props.load(classloader.getResourceAsStream("application.properties"));
		} catch (IOException e) {
			LOGGER.warn("Exception while loading configuration.", e);
		}
		LOGGER.debug("Configuration loaded successfully.");
	}

	public String getProperties(String key, String defaultVal) {
		return props.getProperty(key, defaultVal);
	}
}
