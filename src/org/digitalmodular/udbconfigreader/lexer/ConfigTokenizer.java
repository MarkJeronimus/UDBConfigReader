package org.digitalmodular.udbconfigreader.lexer;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.UtilityClass;

import org.digitalmodular.udbconfigreader.CharacterReader;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.ASSIGNMENT;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.ASTERISK;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.BLOCK_END;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.BLOCK_START;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.FUNCTION_END;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.FUNCTION_START;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.LIST_SEPARATOR;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.NEWLINE;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.OTHER;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.SKIP;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.SLASH;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.STATEMENT_SEPARATOR;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.STRING_DELIMITER;
import static org.digitalmodular.udbconfigreader.lexer.ConfigToken.TokenType.WHITESPACE;

/**
 * @author Zom-B
 */
// Created 2021-08-10
@UtilityClass
public final class ConfigTokenizer {
	private ConfigTokenizer() {
		throw new AssertionError();
	}

	public static List<ConfigToken> tokenize(CharacterReader reader) {
		List<ConfigToken> tokens = new ArrayList<>(1024);

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
				tokens.add(new ConfigToken(reader.getSource(),
				                           reader.getStoredLineNumber() + 1,
				                           reader.getStoredColumn() + 1,
				                           tokenType,
				                           sb.toString()));
				sb.setLength(0);
			}

			sb.append((char)ch);
			reader.storeLocation();
			tokenType = detectedType;
		}

		if (tokenType != null) {
			tokens.add(new ConfigToken(reader.getSource(),
			                           reader.getStoredLineNumber() + 1,
			                           reader.getStoredColumn() + 1,
			                           tokenType,
			                           sb.toString()));
		}

		return tokens;
	}

	private static TokenType detectTokenType(int ch) {
		if (ch == '\n')
			return NEWLINE;
		else if (ch == ' ' || ch == '\t')
			return WHITESPACE;
		else if (ch < ' ' || ch >= 127)
			return SKIP;
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
		else if (ch == '=')
			return ASSIGNMENT;
		else if (ch == ',')
			return LIST_SEPARATOR;
		else if (ch == ';')
			return STATEMENT_SEPARATOR;
		else
			return OTHER;
	}
}
