package es.iti.wakamiti.expressions.parser;

import java.util.*;
import java.util.stream.Stream;

import es.iti.wakamiti.expressions.*;


class FragmentTree {


	enum Type {

		SEQUENCE,
		LITERAL,
		CHOICE,
		WILDCARD,
		SUBEXPRESSION,
		ARGUMENT,
		OPTIONAL,
		NEGATION;


		FragmentTree empty() {
			return new FragmentTree(this,null);
		}

		FragmentTree of(Token token, FragmentTree... children) {
			return new FragmentTree(this, token == null ? null : token.value() ,children);
		}

		FragmentTree of(String value, FragmentTree... children) {
			return new FragmentTree(this, value ,children);
		}

		FragmentTree of(FragmentTree... children) {
			return new FragmentTree(this,null,children);
		}

	}

	final Type type;
	String value;
	final List<FragmentTree> children = new ArrayList<>();



	private FragmentTree(Type type, String value, FragmentTree... children) {
		this.type = type;
		this.value = value;
		Stream.of(children).map(FragmentTree::reduced).forEach(this::add);
	}


	void add(FragmentTree child) {
		children.add(child.reduced());
	}


	public FragmentTree firstChild() {
		return children.isEmpty() ? null : children.get(0);
	}


	public FragmentTree lastChild() {
		return children.isEmpty() ? null : children.get(children.size()-1);
	}


	public void remove(FragmentTree child) {
		children.remove(child);
	}


	FragmentTree reduced() {
		if (type == Type.SEQUENCE && children.size() == 1) {
			return firstChild();
		} else {
			return this;
		}
	}


	FragmentTree assertType(Type expected) {
		if (this.type != expected)
			throw new ExpressionException("Unexpected node {}; expected {}", this.type, expected);
		return this;
	}



	@Override
	public String toString() {
		return toString(new StringBuilder(),0).toString();
	}


	protected StringBuilder toString(StringBuilder string, int level) {
		String margin = "  ".repeat(level);
		string.append(margin);
		string.append(type);
		if (value != null) {
			string.append("<").append(value).append(">");
		}
		if (!children.isEmpty()) {
			string.append(" [\n");
			children.forEach(child -> child.toString(string, level+1));
			string.append(margin).append("]\n");
		} else {
			string.append("\n");
		}
		return string;
	}



	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FragmentTree astNode = (FragmentTree) o;
		return type == astNode.type
			&& Objects.equals(value, astNode.value)
			&& children.equals(astNode.children);
	}


	@Override
	public int hashCode() {
		return Objects.hash(type, value, children);
	}

}
