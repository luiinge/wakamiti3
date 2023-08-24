package es.iti.wakamiti.api;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import imconfig.Config;
import jexten.ExtensionManager;

public interface Wakamiti {

	interface Builder {

		static Builder get() {
			return ServiceLoader.load(Wakamiti.Builder.class).findFirst().orElseThrow(
				()->new WakamitiException("There is no Wakamiti implementation found!")
			);
		}

		Builder config(Config config);
		Builder extensionManager(ExtensionManager extensionManager);
		Wakamiti build();

	}

	static Wakamiti of() {
		return Builder.get().build();
	}

	static Wakamiti of(Config config) {
		return Builder.get().config(config).build();
	}

	static Wakamiti of(Config config, ExtensionManager extensionManager) {
		return Builder.get().config(config).extensionManager(extensionManager).build();
	}

	UUID buildPlan();

	void serialize(UUID planNodeID, Writer writer) throws IOException;

	ExtensionManager extensionManager();

	Stream<DataType> dataTypes();

	Stream<ContentType> contentTypes();


}
