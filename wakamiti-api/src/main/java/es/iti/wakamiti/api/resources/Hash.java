package es.iti.wakamiti.api.resources;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import es.iti.wakamiti.api.WakamitiException;
import lombok.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode
public class Hash implements Comparable<Hash> {

	private static final Base64.Encoder encoder = Base64.getEncoder();

	private static MessageDigest newDigest() {
		try {
			return MessageDigest.getInstance("SHA3-256");
		} catch (NoSuchAlgorithmException e) {
			throw new WakamitiException(e, "Error obtaining hash algorithm");
		}
	}

	public static Hash of(String content) {
		var bytes = newDigest().digest(content.getBytes(StandardCharsets.UTF_8));
		return new Hash(encoder.encodeToString(bytes));
	}

	public static Hash of(URI uri) {
		try (var stream = new DigestInputStream(uri.toURL().openStream(),newDigest())) {
			return new Hash(encoder.encodeToString(stream.getMessageDigest().digest()));
		} catch (IOException e) {
			throw new WakamitiException(e,"Cannot calculate hash of {resource}",uri);
		}
	}

	private final String value;


	@Override
	public int compareTo(Hash other) {
		return value.compareTo(other.value);
	}

}
