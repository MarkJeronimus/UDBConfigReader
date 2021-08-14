package org.digitalmodular.udbconfigreader.lexer;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.OTHER;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.SLASH;

/**
 * @author Zom-B
 */
// Created date
@UtilityClass
public final class KeywordLexer {
	private KeywordLexer() {
		throw new AssertionError();
	}

	public static List<ConfigToken> process(List<ConfigToken> tokens) {
		List<ConfigToken> processedTokens = new ArrayList<>(tokens.size());

		StringBuilder         sb                = new StringBuilder(40);
		@Nullable ConfigToken firstKeywordToken = null;

		for (int i = 0; i < tokens.size(); i++) {
			ConfigToken           token     = tokens.get(i);
			ConfigToken.TokenType tokenType = token.getTokenType();

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
