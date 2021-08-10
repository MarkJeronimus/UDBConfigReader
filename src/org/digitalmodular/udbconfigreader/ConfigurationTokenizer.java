package org.digitalmodular.udbconfigreader;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.ASTERISK;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.BLOCK_END;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.BLOCK_START;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.FUNCTION_END;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.FUNCTION_START;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.NEWLINE;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.OPERATOR;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.OTHER;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.SLASH;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.STRING_DELIMITER;
import static org.digitalmodular.udbconfigreader.ConfigurationToken.TokenType.WHITESPACE;

/**
 * @author Zom-B
 */
// Created 2021-08-10
@UtilityClass
public final class ConfigurationTokenizer {
	private ConfigurationTokenizer() {
		throw new AssertionError();
	}

	public static List<ConfigurationToken> tokenize(CharacterReader reader) {
		List<ConfigurationToken> tokens = new ArrayList<>(1024);

		StringBuilder       sb        = new StringBuilder(40);
		@Nullable TokenType tokenType = null;

		while (true) {
			int ch = reader.nextChar();
			if (ch < 0)
				break;

			TokenType detectedType = detectTokenType(ch);
			if (detectedType == tokenType && tokenType.isMayCombine()) {
				sb.append((char)ch);
				continue;
			}

			if (tokenType != null) {
				tokens.add(new ConfigurationToken(reader.getSource(),
				                                  reader.getLineNumber(),
				                                  reader.getColumn(),
				                                  tokenType,
				                                  sb.toString()));
				sb.setLength(0);
			}

			sb.append((char)ch);
			reader.storeLocation();
			tokenType = detectedType;
		}

		return tokens;
	}

	private static TokenType detectTokenType(int ch) {
		if (ch == '\n' || ch == '\r')
			return NEWLINE;
		else if (ch <= ' ' || ch == 127)
			return WHITESPACE;
		else if (ch == '/')
			return SLASH;
		else if (ch == '*')
			return ASTERISK;
		else if (ch == '"')
			return STRING_DELIMITER;
		else if (ch == '(')
			return FUNCTION_START;
		else if (ch == ')')
			return FUNCTION_END;
		else if (ch == '{')
			return BLOCK_START;
		else if (ch == '}')
			return BLOCK_END;
		else if (ch == ';' || ch == '=' || ch == ',')
			return OPERATOR;
		else
			return OTHER;
	}
}
