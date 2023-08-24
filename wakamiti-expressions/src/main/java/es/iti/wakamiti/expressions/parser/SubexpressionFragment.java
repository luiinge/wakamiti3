package es.iti.wakamiti.expressions.parser;


import es.iti.wakamiti.api.SubExpression;

public class SubexpressionFragment extends Fragment {

	private final SubExpression subexpression;

	SubexpressionFragment(SubExpression subexpression) {
		this.subexpression = subexpression;
	}


	@Override
	public boolean consume(MatcherState state) {
		return false;
	}

}
