package org.digitalmodular.udbconfigreader.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import static java.util.logging.Level.WARNING;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.STRING;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.STRING_DELIMITER;

/**
 * @author Zom-B
 */
// Created 2021-08-10
@UtilityClass
public final class StringsLexer {
	private StringsLexer() {
		throw new AssertionError();
	}

	public static List<ConfigToken> process(Collection<ConfigToken> tokens) {
		List<ConfigToken> processedTokens = new ArrayList<>(tokens.size());

		@Nullable ConfigToken firstStringToken = null;
		StringBuilder         stringContents   = new StringBuilder(80);

		for (ConfigToken token : tokens) {
			if (firstStringToken != null) {
				if (token.getTokenType() == STRING_DELIMITER) {
					unEscape(stringContents, firstStringToken);
					processedTokens.add(firstStringToken.replace(STRING, stringContents.toString()));
					stringContents.setLength(0);
					firstStringToken = null;
				} else {
					stringContents.append(token.getText());
				}
			} else {
				if (token.getTokenType() == STRING_DELIMITER)
					firstStringToken = token;
				else
					processedTokens.add(token);
			}
		}

		if (firstStringToken != null)
			throw new IllegalArgumentException("Unclosed string literal at " + firstStringToken.getLocationString());

		return processedTokens;
	}

	private static void unEscape(StringBuilder sb, ConfigToken token) {
		for (int i = 0; i < sb.length(); i++) {
			char ch = sb.charAt(i);

			if (ch == '\\') {
				int remaining = sb.length() - i - 1;
				if (remaining == 0)
					continue;

				char nextCh = sb.charAt(i + 1);

				int escapeLen = 1; // unless overridden below
				switch (nextCh) {
					case '\\':
						ch = '\\';
						break;
					case 'n':
						ch = '\n';
						break;
					default:
						Logger.getGlobal()
						      .log(WARNING, "Probable unimplemented escape in " + token.getLocationString(i) +
						                    ": " + sb.substring(i));
						escapeLen = 0;
				}

				if (escapeLen > 0) {
					sb.setCharAt(i, ch);
					sb.delete(i + 1, i + escapeLen + 1);
				}

				if (escapeLen > 1)
					i -= escapeLen;
			}
		}
	}
}
