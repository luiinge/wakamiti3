package es.iti.wakamiti.core;

import es.iti.wakamiti.api.Wakamiti;
import es.iti.wakamiti.api.lang.Functions;
import imconfig.Config;
import jexten.ExtensionManager;

public class WakamitiEngineBuilder implements Wakamiti.Builder {

	private Config config;
	private ExtensionManager extensionManager;


	@Override
	public Wakamiti.Builder config(Config config) {
		this.config = config;
		return this;
	}


	@Override
	public Wakamiti.Builder extensionManager(ExtensionManager extensionManager) {
		this.extensionManager = extensionManager;
		return this;
	}


	@Override
	public Wakamiti build() {
		return new WakamitiEngine(
			config,
			Functions.or(extensionManager, ExtensionManager::create)
		);
	}

}
