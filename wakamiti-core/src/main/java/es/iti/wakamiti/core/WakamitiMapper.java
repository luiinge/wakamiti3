package es.iti.wakamiti.core;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;

import es.iti.wakamiti.api.plan.PlanNodeID;
import es.iti.wakamiti.api.resources.Hash;

public class WakamitiMapper extends ObjectMapper {

	public WakamitiMapper() {
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		enable(SerializationFeature.INDENT_OUTPUT);
		setAccessorNaming(new DefaultAccessorNamingStrategy.Provider().withGetterPrefix("").withSetterPrefix(""));
		SimpleModule wakamitiModule = new SimpleModule();
		wakamitiModule.addSerializer(PlanNodeID.class, new SimpleSerializer<>(PlanNodeID::value));
		wakamitiModule.addDeserializer(PlanNodeID.class, new SimpleDeserializer<>(PlanNodeID::new));
		wakamitiModule.addSerializer(Hash.class, new SimpleSerializer<>(Hash::value));
		wakamitiModule.addDeserializer(Hash.class, new SimpleDeserializer<>(Hash::new));
		registerModule(wakamitiModule);
	}



	private static class SimpleSerializer<T> extends JsonSerializer<T> {
		private final Function<T,String> serializer;
		private SimpleSerializer(Function<T, String> serializer) {
			this.serializer = serializer;
		}
		@Override
		public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(serializer.apply(value));
		}
	}


	private static class SimpleDeserializer<T> extends JsonDeserializer<T> {
		private final Function<String,T> deserializer;
		private SimpleDeserializer(Function<String,T> deserializer) {
			this.deserializer = deserializer;
		}
		@Override
		public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
			return deserializer.apply(p.getValueAsString());
		}
	}


}
