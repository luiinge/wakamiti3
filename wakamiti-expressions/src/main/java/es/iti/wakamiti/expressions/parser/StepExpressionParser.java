package es.iti.wakamiti.expressions.parser;

import es.iti.wakamiti.api.*;
import es.iti.wakamiti.expressions.Expression;

public class StepExpressionParser {

	private final DataTypes dataTypes;
	private final SubExpressions subExpressions;


	public StepExpressionParser(DataTypes dataTypes, SubExpressions subExpressions) {
		this.dataTypes = dataTypes;
		this.subExpressions = subExpressions;
	}

	public Expression parse(String stepExpression) {
//		FragmentTree ast = new FragmentTreeBuilder(stepExpression).buildTree();
//		var fragments = ast.buildFragments(dataTypes,subExpressions);
//		return new Expression(fragments);
		return null;
	}

}
