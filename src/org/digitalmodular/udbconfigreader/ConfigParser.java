package org.digitalmodular.udbconfigreader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

import org.digitalmodular.udbconfigreader.lexer.ConfigToken;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.STATEMENT_SEPARATOR;

/**
 * @author Zom-B
 */
// Created 2021-08-13
@UtilityClass
public final class ConfigParser {
	private ConfigParser() {
		throw new AssertionError();
	}

	public static void parse(Iterable<ConfigToken> tokens, ConfigStruct destination) {
		Iterator<ConfigToken> iter = tokens.iterator();

		while (iter.hasNext()) {
			ConfigToken token = iter.next();
			switch (token.getTokenType()) {
				case STATEMENT_SEPARATOR:
					continue;
				case OTHER:
					parseEntry(token, iter, destination);
					break;
				default:
					throwSyntaxError(token, "identifier");
			}
		}
	}

	private static void parseEntry(
			ConfigToken firstToken, Iterator<ConfigToken> iter, ConfigStruct destination) {
		ConfigToken token = iter.next();

		switch (token.getTokenType()) {
			case ASSIGNMENT:
				parseAssignment(firstToken, iter, destination);
				return;
			case FUNCTION_START:
				parseFunction(firstToken, iter, destination);
				return;
			case BLOCK_START:
				parseBlock(firstToken, iter, destination);
				return;
			case STATEMENT_SEPARATOR:
				destination.put(firstToken.getText(), null);
				return;
			default:
				throwSyntaxError(token, "\"=\", \"{\", or \"(\" after an identifier");
		}
	}

	private static void parseAssignment(
			ConfigToken firstToken, Iterator<ConfigToken> iter, ConfigStruct destination) {
		requireHasNextToken(iter, firstToken, "a variable assignment");
		ConfigToken token = iter.next();

		switch (token.getTokenType()) {
			case STRING:
				destination.put(firstToken.getText(), token.getText());
				break;
			case OTHER:
				if (token.getText().equalsIgnoreCase("null")) {
					destination.put(firstToken.getText(), null);
					break;
				}

				@Nullable Object value = parseValue(token.getText());
				if (value != null) {
					destination.put(firstToken.getText(), value);
					break;
				}

				// fall-through
			default:
				throwSyntaxError(token, "a string, a number, a boolean, or null");
		}

		requireNextTokenOfType(STATEMENT_SEPARATOR, iter, token);
	}

	private static @Nullable Object parseValue(String text) {
		if (text.isEmpty())
			return null;
		else if (text.equalsIgnoreCase("false"))
			return Boolean.FALSE;
		else if (text.equalsIgnoreCase("true"))
			return Boolean.TRUE;

		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException ignored) {
		}

		char lastChar = text.charAt(text.length() - 1);
		if (lastChar == 'f' || lastChar == 'F') {
			try {
				return Float.parseFloat(text);
			} catch (NumberFormatException ignored) {
			}
		} else {
			try {
				return Double.parseDouble(text);
			} catch (NumberFormatException ignored) {
			}
		}

		return null;
	}

	private static void parseFunction(
			ConfigToken firstToken, Iterator<ConfigToken> iter, ConfigStruct destination) {
		Collection<Object> parameters = new ArrayList<>(8);

		boolean     requireValue = true;
		ConfigToken token;
		while (true) {
			requireHasNextToken(iter, firstToken, "a function call");

			token = iter.next();

			switch (token.getTokenType()) {
				case STRING:
					if (!requireValue)
						throwSyntaxError(token, "\",\" or \")\"");

					parameters.add(token.getText());
					break;
				case LIST_SEPARATOR:
					if (requireValue)
						throwSyntaxError(token, "a literal value");

					break;
				case FUNCTION_END:
					// TODO handle function
					System.out.println(firstToken.getText() + parameters);
					return;
				default:
					throwSyntaxError(token, "nothing");
			}

			requireValue = !requireValue;
		}
	}

	private static void parseBlock(
			ConfigToken firstToken, Iterator<ConfigToken> iter, ConfigStruct destination) {
		requireHasNextToken(iter, firstToken, "a block");

		ConfigStruct block = new ConfigStruct(firstToken.getText(), destination.isSorted(), 16);

		ConfigToken token;
		while (true) {
			token = iter.next();
			switch (token.getTokenType()) {
				case STATEMENT_SEPARATOR:
					continue;
				case OTHER:
					parseEntry(token, iter, block);
					break;
				case BLOCK_END:
					destination.put(firstToken.getText(), block);
					return;
				default:
					throwSyntaxError(token, "identifier");
			}
		}
	}

	private static void requireNextTokenOfType(TokenType tokenType, Iterator<ConfigToken> iter, ConfigToken lastToken) {
		requireHasNextToken(iter, lastToken, "a statement");

		ConfigToken token = iter.next();
		if (token.getTokenType() == tokenType)
			return;

		throwSyntaxError(token, "\";\"");
	}

	private static void requireHasNextToken(Iterator<ConfigToken> iter, ConfigToken lastToken, String parsingThing) {
		if (iter.hasNext())
			return;

		throw new IllegalArgumentException("Unexpected End of file while parsing " + parsingThing +
		                                   " at " + lastToken.getLocationString());
	}

	private static void throwSyntaxError(ConfigToken token, String expectedThing) {
		throw new IllegalArgumentException("Syntax error at " + token.getLocationString() +
		                                   ". Expecting " + expectedThing +
		                                   ", but found: \"" + token.getEscapedText() +
		                                   "\" (" + token.getTokenType() + ')');
	}
}