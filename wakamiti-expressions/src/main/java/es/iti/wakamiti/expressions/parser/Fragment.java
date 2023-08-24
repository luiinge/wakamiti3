package es.iti.wakamiti.expressions.parser;

public abstract class Fragment {

	/**
	 * @return <code>true</code> if the fragment was totally matched when consuming the
	 * matcher state
	 */
	public abstract boolean consume(MatcherState state);

}
