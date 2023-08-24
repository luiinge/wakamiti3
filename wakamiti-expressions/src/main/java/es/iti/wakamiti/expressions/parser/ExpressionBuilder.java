package es.iti.wakamiti.expressions.parser;

import es.iti.wakamiti.api.*;
import es.iti.wakamiti.expressions.*;
import java.nio.CharBuffer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExpressionBuilder {


	private static final Pattern regexSymbols = CharBuffer.wrap(Tokenizer.symbols)
		.chars()
		.mapToObj(n -> "\\"+Character.toString(n))
		.reduce((a,b)->a+b)
		.map(it -> Pattern.compile("["+it+"]"))
		.orElseThrow();


	private final Locale locale;
	private final DataTypes dataTypes;
	private final SubExpressions subExpressions;


	public ExpressionBuilder(Locale locale, DataTypes dataTypes, SubExpressions subExpressions) {
		this.locale = locale;
		this.dataTypes = dataTypes;
		this.subExpressions = subExpressions;
	}



	public Expression buildExpression(String expression) {
		var tree = new FragmentTreeBuilder(expression).buildTree();
		List<Fragment> fragments = buildFragments(tree);
		return new Expression(fragments);
	}



	private String regex(FragmentTree tree) {
		return switch (tree.type) {
			case WILDCARD -> "(.*)";
			case NEGATION -> "(?!"+regex(tree.firstChild())+")" + (isWord(tree.firstChild()) ? "\\S+" : ".*");
			case LITERAL -> "("+ regexSymbols.matcher(tree.value).replaceAll("\\\\$0").replace(" ","\\s+")+")";
			case OPTIONAL -> regex(tree.firstChild())+"?";
			case CHOICE -> tree.children.stream().map(this::regex).collect(Collectors.joining("|","(",")"));
			case SEQUENCE -> tree.children.stream().map(this::regex).collect(Collectors.joining());
			case ARGUMENT, SUBEXPRESSION -> null;
		};
	}

	private String literal(FragmentTree tree) {
		var child = tree.firstChild();
		return switch (tree.type) {
			case WILDCARD -> " * ";
			case NEGATION -> isWord(child) ? "^"+literal(child) : "^["+literal(child)+"]";
			case LITERAL -> escape(tree.value);
			case OPTIONAL -> "("+literal(child)+")";
			case CHOICE -> tree.children.stream().map(this::regex).collect(Collectors.joining("|"));
			case SEQUENCE -> tree.children.stream().map(this::regex).collect(Collectors.joining());
			case ARGUMENT, SUBEXPRESSION -> null;
		};
	}


	private String joinRegex(RegexFragment regex1, RegexFragment regex2) {
		var regex = regex1.pattern().pattern() + regex2.pattern().pattern();
		// temporary replacement required to proper match of the following expression
		regex = regex.replace("\\)",">>>>>");
		regex = Regex.replace(
			regex,
			"\\\\s\\+\\)\\(([^)]+)\\)\\?\\(\\\\s\\+",
			"\\\\s+)($1\\\\s+)?("
		);
		regex = regex.replace(">>>>>","\\)");
		return regex;
	}


	private String joinLiteral(RegexFragment regex1, RegexFragment regex2) {
		return regex1.literal()+regex2.literal();
	}


	private Fragment buildSingleFragment(FragmentTree tree) {
		String regex = regex(tree);
		String literal = literal(tree);
		if (regex != null) {
			return new RegexFragment(regex,literal);
		} else if (tree.type == FragmentTree.Type.ARGUMENT) {
			String[] valueParts = tree.value.split(":");
			if (valueParts.length == 1) {
				return new ArgumentFragment(tree.value,dataTypes.byName(tree.value));
			} else {
				return new ArgumentFragment(valueParts[0],dataTypes.byName(valueParts[1]));
			}
		} else if (tree.type == FragmentTree.Type.SUBEXPRESSION) {
			return new SubexpressionFragment(subExpressions.byName(tree.value));
		}
		throw new ExpressionException("cannot build a single fragment for {}", tree);
	}



	private List<Fragment> buildFragments(FragmentTree tree) {
		if (tree.type == FragmentTree.Type.SEQUENCE) {
			LinkedList<Fragment> fragments = new LinkedList<>();
			Fragment lastFragment = null;
			for (FragmentTree child : tree.children) {
				Fragment childFragment = buildSingleFragment(child);
				if (lastFragment instanceof RegexFragment regex1 &&
					childFragment instanceof RegexFragment regex2
				) {
					fragments.removeLast();
					fragments.add(new RegexFragment(
						joinRegex(regex1, regex2),
						joinLiteral(regex1,regex2)
					));
				} else {
					fragments.add(childFragment);
				}
				lastFragment = fragments.getLast();
			}
			return fragments;
		} else {
			return List.of(buildSingleFragment(tree));
		}
	}



	private static boolean isWord(FragmentTree tree) {
		return tree.type == FragmentTree.Type.LITERAL && !tree.value.strip().contains(" ");
	}


	private static String escape(String text) {
		return regexSymbols.matcher(text).replaceAll("\\\\$0");
	}

}
