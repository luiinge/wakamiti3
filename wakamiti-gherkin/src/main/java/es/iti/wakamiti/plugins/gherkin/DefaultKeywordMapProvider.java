package es.iti.wakamiti.plugins.gherkin;

import es.iti.wakamiti.api.Log;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import es.iti.wakamiti.plugins.gherkin.parser.*;

public class DefaultKeywordMapProvider implements KeywordMapProvider {

	private static final Log log = Log.of("gherkin");

	private final Map<Locale,Optional<KeywordMap>> cache = new HashMap<>();

	@Override
	public Optional<KeywordMap> keywordMap(Locale locale) {
		return cache.computeIfAbsent(locale, this::readKeywordMap);
	}


	private Optional<KeywordMap> readKeywordMap(Locale locale) {
		var resourceFile = "gherkin_"+locale.getLanguage()+".properties";
		var url = getClass().getClassLoader().getResource(resourceFile);
		if (url == null) {
			return Optional.empty();
		}
		try (var reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			var map = new EnumMap<KeywordType,List<String>>(KeywordType.class);
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("=");
				String keyword = parts[0];
				String[] values = parts[1].split(",");
				map.put(KeywordType.of(keyword), List.of(values));
			}
			return Optional.of(map::get);
		} catch (IOException e) {
			log.error(e,"Cannot read {url}",url);
			return Optional.empty();
		}
	}
}
