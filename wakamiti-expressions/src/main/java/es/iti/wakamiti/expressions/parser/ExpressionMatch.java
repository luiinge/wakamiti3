package es.iti.wakamiti.expressions.parser;

import es.iti.wakamiti.expressions.MatchResult;

public record ExpressionMatch(
	MatchResult result,
	int matchEndPosition,
	int remainingCharacters,
	int matchedFragments
) { }
