package org.digitalmodular.udbconfigreader;

import java.util.regex.Pattern;

import static org.digitalmodular.utilities.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.utilities.ValidatorUtilities.requireStringLengthAtLeast;

/**
 * @author Zom-B
 */
// Created 2021-08-10
public class ConfigurationToken {
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\r\n]");

	/**
	 * @author Zom-B
	 */
	// Created 2021-08-10
	public enum TokenType {
		NEWLINE(true),
		WHITESPACE(true),
		SLASH(false),
		ASTERISK(false),
		STRING_DELIMITER(false),
		FUNCTION_START(false),
		FUNCTION_END(false),
		BLOCK_START(false),
		BLOCK_END(false),
		OPERATOR(true),
		OTHER(true);

		private final boolean mayCombine;

		TokenType(boolean mayCombine) {
			this.mayCombine = mayCombine;
		}

		public boolean isMayCombine() {
			return mayCombine;
		}
	}

	private final String    source;
	private final int       lineNumber;
	private final int       column;
	private final TokenType tokenType;
	private final String    text;

	public ConfigurationToken(String source, int lineNumber, int column, TokenType tokenType, String text) {
		this.text = requireStringLengthAtLeast(1, text, "text");
		this.source = requireNonNull(source, "source");
		this.lineNumber = requireAtLeast(1, lineNumber, "lineNumber");
		this.tokenType = requireNonNull(tokenType, "tokenType");
		this.column = requireAtLeast(1, column, "column");
	}

	public String getSource() {
		return source;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumn() {
		return column;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "ConfigurationToken{" + source + ':' + lineNumber + ':' + column +
		       " (" + tokenType + ") \"" + NEWLINE_PATTERN.matcher(text).replaceAll(" ") + "\"}";
	}
}
