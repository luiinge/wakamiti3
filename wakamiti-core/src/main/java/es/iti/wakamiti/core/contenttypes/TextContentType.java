package es.iti.wakamiti.core.contenttypes;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.*;
import es.iti.wakamiti.api.*;


public class TextContentType implements ContentType {

	private final String name;


	public TextContentType(String name) {
		this.name = name;
	}


	@Override
	public String name() {
		return name;
	}


	protected String readUTF8(InputStream inputStream) {
		try (var reader = new BufferedReader(new InputStreamReader(inputStream,UTF_8))) {
			StringBuilder string = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				string.append(line).append("\n");
			}
			return string.toString();
		} catch (IOException e) {
			throw new WakamitiException(e,"Error reading text input");
		}
	}

}
