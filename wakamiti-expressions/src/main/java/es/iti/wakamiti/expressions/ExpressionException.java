package es.iti.wakamiti.expressions;

public class ExpressionException extends RuntimeException {

	public ExpressionException(Exception e, String message, Object... args) {
		super(format(message,args),e);
	}

	public ExpressionException(String message, Object... args) {
		super(format(message,args));
	}


	public ExpressionException(String text, int position, String error) {
		super(
			"Invalid Wakamiti expression parsing character %s: %s\n\n%s\n%s\n".formatted(
				position,
				error,
				text,
				marker(text, position)
			));
	}


	private static String marker(String text, int position) {
		if (position <= text.length() - 1) {
			return "%s^".formatted(" ".repeat(position));
		} else {
			return text;
		}
	}


	private static String format(String message, Object... args) {
		return message.replace("{}","%s").formatted(args);
	}

}
