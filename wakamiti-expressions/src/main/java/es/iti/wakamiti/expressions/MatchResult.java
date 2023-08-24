package es.iti.wakamiti.expressions;

public enum MatchResult {
	/**
	 * The provided step matches all the step expression fragments,
	 * and there are no trailing unmatched characters in the step
	 */
	EXACT_MATCH,
	/**
	 * The leading characters of the provided input match all the step expression fragments,
	 * but there are trailing unmatched characters in the input
	 */
	LEADING_MATCH,
	/**
	 * The provided input matches one or more of the expression fragments,
	 * but there are unmatched remaining fragments in the expression
	 */
	FRAGMENT_MATCH,
	/**
	 * The provided input does not match even the first fragment of the expression
	 */
	NO_MATCH
}
