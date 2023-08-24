package es.iti.wakamiti.expressions;


import java.util.*;
import java.util.regex.*;

public final class Regex {

	private static final int FLAGS = Pattern.CASE_INSENSITIVE;
	private Regex() { }

	private static final Map<String, Pattern> patterns = new HashMap<>();


	public static Pattern of(String regex) {
		return patterns.computeIfAbsent(regex, it -> Pattern.compile(regex, FLAGS));
	}

	public static Matcher match(String value, String regex) {
		return of(regex).matcher(value);
	}

	public static String replace(String value, String regex, String replacement) {
		return match(value, regex).replaceAll(replacement);
	}

}
