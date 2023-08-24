package es.iti.wakamiti.expressions.parser;

import java.util.regex.Pattern;

import es.iti.wakamiti.expressions.Regex;


public class RegexFragment extends Fragment {

	private final Pattern pattern;
	private final String literal;



	RegexFragment(String regex, String literal) {
		this.pattern = Regex.of(regex);
		this.literal = literal;
	}


	public String literal() {
		return literal;
	}





	//	@Override
//    boolean consume(ExpressionMatchState state) {
//        var regexMatcher = pattern.matcher(state.pendingChars());
//        if (regexMatcher.find() && regexMatcher.start() == 0) {
//            state.consume(regexMatcher.end());
//            return true;
//        } else {
//            state.reject();
//            return false;
//        }
//    }


	public Pattern pattern() {
		return pattern;
	}


	@Override
	public boolean consume(MatcherState state) {
		return false;
	}

}
