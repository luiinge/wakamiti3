package es.iti.wakamiti.core;

import es.iti.wakamiti.api.ContentType;
import es.iti.wakamiti.api.contributors.ContentTypeProvider;
import es.iti.wakamiti.core.contenttypes.TextContentType;
import java.util.stream.Stream;
import jexten.Extension;


@Extension
public class BasicContentTypes implements ContentTypeProvider {

	public static final ContentType PLAIN_TEXT = new TextContentType("text/plain");


	@Override
	public Stream<ContentType> contentTypes() {
		return Stream.of(
			PLAIN_TEXT
		);
	}

}
