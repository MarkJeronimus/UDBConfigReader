package org.digitalmodular.udbconfigreader.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.OTHER;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.SLASH;

/**
 * Combines tokens into keywords
 * <p>
 * Tokens that can be part of a keyword are alphanumeric ({@link TokenType#OTHER OTHER})
 * and {@link TokenType#SLASH slashes}.
 *
 * @author Zom-B
 */
// Created date
@UtilityClass
public final class KeywordLexer {
	private KeywordLexer() {
		throw new AssertionError();
	}

	public static List<ConfigToken> process(Collection<ConfigToken> tokens) {
		List<ConfigToken> processedTokens = new ArrayList<>(tokens.size());

		StringBuilder         sb                = new StringBuilder(40);
		@Nullable ConfigToken firstKeywordToken = null;

		for (ConfigToken token : tokens) {
			TokenType tokenType = token.getTokenType();

			if (tokenType == OTHER || tokenType == SLASH && sb.length() > 0) {
				if (firstKeywordToken == null)
					firstKeywordToken = token;
				sb.append(token.getText());
				continue;
			}

			if (firstKeywordToken != null) {
				processedTokens.add(firstKeywordToken.replace(OTHER, sb.toString()));
				sb.setLength(0);
				firstKeywordToken = null;
			}

			processedTokens.add(token);
		}

		return processedTokens;
	}
}
