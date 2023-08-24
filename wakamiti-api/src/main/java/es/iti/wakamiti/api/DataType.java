package es.iti.wakamiti.api;

import java.util.*;
import java.util.regex.Matcher;


public interface DataType {

	String name();
	Class<?> javaType();
	String regex(Locale locale);
	List<String> hints(Locale locale);
	Object parse (Locale locale, String value);
	Matcher matcher(Locale locale, String value);

}
