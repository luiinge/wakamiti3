package es.iti.wakamiti.api.resources;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.function.Supplier;

import es.iti.wakamiti.api.lang.Lazy;

public class Resource {

	private final String contentType;
	private final URI URI;
	private final Path relativePath;
	private final Supplier<InputStream> reader;
	private final Lazy<Hash> hash;


	public Resource(
		String contentType,
		URI URI,
		Path relativePath,
		Supplier<InputStream> reader
	) {
		this.contentType = contentType;
		this.URI = URI;
		this.relativePath = relativePath;
		this.reader = reader;
		this.hash = Lazy.of(() -> Hash.of(URI));
	}


	public String contentType() {
		return contentType;
	}


	public InputStream open() {
		return reader.get();
	}

	public Hash hash() {
		return hash.get();
	}


	public URI URI() {
		return URI;
	}


	public Path relativePath() {
		return relativePath;
	}

}
