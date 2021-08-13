package org.digitalmodular.udbconfigreader.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.digitalmodular.utilities.annotation.UtilityClass;

import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.COMMENT;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.NEWLINE;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.WHITESPACE;

/**
 * @author Zom-B
 */
// Created 2021-08-12
@UtilityClass
public final class CleaningLexer {
	private CleaningLexer() {
		throw new AssertionError();
	}

	public static List<ConfigToken> process(Collection<ConfigToken> tokens) {
		List<ConfigToken> processedTokens = new ArrayList<>(tokens.size());

		for (ConfigToken token : tokens) {
			ConfigToken.TokenType tokenType = token.getTokenType();

			if (tokenType == WHITESPACE ||
			    tokenType == COMMENT ||
			    tokenType == NEWLINE)
				continue;

			processedTokens.add(token);
		}

		return processedTokens;
	}
}
