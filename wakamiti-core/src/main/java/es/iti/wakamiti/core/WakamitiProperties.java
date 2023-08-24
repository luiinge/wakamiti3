package es.iti.wakamiti.core;

import java.nio.charset.StandardCharsets;

import es.iti.wakamiti.api.contributors.ConfigProvider;
import imconfig.Config;
import jexten.Extension;

@Extension
public class WakamitiProperties implements ConfigProvider {

	private final static Config DEFAULT = Config.factory().accordingDefinitionsFromResource(
		"es_iti_wakamiti_core-config.yaml",
		StandardCharsets.UTF_8,
		WakamitiProperties.class.getModule().getClassLoader()
	);

	public static final String REPOSITORY_PATH = "repository.path";
	public static final String REPOSITORY_TRANSIENT = "repository.transient";
	public static final String RESOURCES_PATH = "resources.path";

	@Override
	public Config config() {
		return DEFAULT;
	}

}
