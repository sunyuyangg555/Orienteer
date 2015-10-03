package org.orienteer.core.service;

import java.util.Random;

import com.google.inject.AbstractModule;

public class CustomTestModule extends AbstractModule {
	
	public static interface ITestInterface {
		public String getKey();
	}
	
	public static class TestInterface implements ITestInterface {

		private String key = RANDOM_STRING;
		
		@Override
		public String getKey() {
			return key;
		}
		
	}

	public static final String RANDOM_STRING = "Random"+new Random().nextLong();
	@Override
	protected void configure() {
		bind(ITestInterface.class).to(TestInterface.class).asEagerSingleton();
	}

}
