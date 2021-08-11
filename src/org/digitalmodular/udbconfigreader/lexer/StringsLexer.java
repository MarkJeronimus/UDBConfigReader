package org.digitalmodular.udbconfigreader.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import static java.util.logging.Level.WARNING;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

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

		boolean               inString         = false;
		@Nullable ConfigToken firstStringToken = null;
		StringBuilder         stringContents   = new StringBuilder(80);

		for (ConfigToken token : tokens) {
			if (inString) {
				if (token.getTokenType() == ConfigToken.TokenType.STRING_DELIMITER) {
					if (firstStringToken != null) {
						assert stringContents.length() > 0;
						unEscape(stringContents);
						processedTokens.add(new ConfigToken(firstStringToken.getSource(),
						                                    firstStringToken.getLineNumber(),
						                                    firstStringToken.getColumn(),
						                                    ConfigToken.TokenType.STRING,
						                                    stringContents.toString()));
						stringContents.setLength(0);
						firstStringToken = null;
					}

					inString = false;
				} else {
					stringContents.append(token.getText());
					if (firstStringToken == null)
						firstStringToken = token;
				}
			} else {
				if (token.getTokenType() == ConfigToken.TokenType.STRING_DELIMITER)
					inString = true;
				else
					processedTokens.add(token);
			}
		}

		return processedTokens;
	}

	private static void unEscape(StringBuilder sb) {
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
					default:
						Logger.getGlobal().log(WARNING, "WARNING: Probably unimplemented escape: " + sb.substring(i));
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
