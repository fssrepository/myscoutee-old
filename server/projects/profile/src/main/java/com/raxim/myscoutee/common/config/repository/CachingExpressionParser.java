package com.raxim.myscoutee.common.config.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;

public class CachingExpressionParser implements ExpressionParser {

	private final ExpressionParser delegate;
	private final Map<String, Expression> cache = new ConcurrentHashMap<>();

	public CachingExpressionParser(ExpressionParser delegate) {
		this.delegate = delegate;
	}

	@Override
	public Expression parseExpression(String expressionString) throws ParseException {
		return cache.computeIfAbsent(expressionString, delegate::parseExpression);
	}

	@Override
	public Expression parseExpression(String expressionString, ParserContext context) throws ParseException {
		throw new UnsupportedOperationException("Parsing using ParserContext is not supported");
	}
}
