package es.iti.wakamiti.expressions.parser;

import es.iti.wakamiti.api.DataType;



public class ArgumentFragment extends Fragment {

	private final String name;
	private final DataType dataType;


	ArgumentFragment(String name, DataType dataType) {
		this.name = name;
		this.dataType = dataType;
	}


	@Override
	public boolean consume(MatcherState state) {
		return false;
	}


//	@Override
//	boolean consume(ExpressionMatchState state) {
//		Pattern regex = Regex.of(dataType.regex(state.locale()));
//		Matcher regexMatcher = regex.matcher(state.pendingChars());
//		if (regexMatcher.find() && regexMatcher.start() == 0) {
//			String matchValue = regexMatcher.group();
//			state.addArgument(new ExpressionArgument(name,matchValue,dataType,state.locale()));
//			state.consume(regexMatcher.end());
//			return true;
//		} else {
//			state.reject();
//			return false;
//		}
//	}

}
