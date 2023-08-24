package es.iti.wakamiti.expressions;

import static es.iti.wakamiti.expressions.MatchResult.*;

import java.util.*;

import es.iti.wakamiti.expressions.parser.*;
import es.iti.wakamiti.expressions.parser.Fragment;

public class Expression {

	private final List<es.iti.wakamiti.expressions.parser.Fragment> fragments;


	public Expression(List<es.iti.wakamiti.expressions.parser.Fragment> fragments) {
		this.fragments = fragments;
	}


	public ExpressionMatch match(String step, Locale locale) {
		var state = new MatcherState(step.strip(), locale);
		int fragmentsCount = fragments.size()-1;
		int lastFragmentMatched = -1;
		for (int i = 0; i <= fragmentsCount; i++) {
			if (fragments.get(i).consume(state)) {
				lastFragmentMatched = i;
			} else {
				break;
			}
		}
		boolean allFragmentsMatched = (lastFragmentMatched == fragmentsCount);
		boolean totallyConsumed = state.totallyConsumed();

		MatchResult matchResult;
		if (totallyConsumed) {
			matchResult = allFragmentsMatched ? EXACT_MATCH : FRAGMENT_MATCH;
		} else {
			matchResult = allFragmentsMatched ? LEADING_MATCH : NO_MATCH;
		}

		return new ExpressionMatch(
			matchResult,
			state.position(),
			state.remainingCharacters(),
			lastFragmentMatched+1
		);
	}


	public List<Fragment> fragments() {
		return List.copyOf(fragments);
	}

}
