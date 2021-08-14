package org.digitalmodular.udbconfigreader.lexer;

import java.util.regex.Pattern;

import static org.digitalmodular.utilities.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

/**
 * @author Zom-B
 */
// Created 2021-08-10
public class ConfigToken {
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");
	private static final Pattern TAB_PATTERN     = Pattern.compile("\t");
	private static final Pattern QUOTE_PATTERN   = Pattern.compile("\"");

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
		ASSIGNMENT(false),
		LIST_SEPARATOR(false),
		STATEMENT_SEPARATOR(true),
		OTHER(true),

		// Don't include this token; it's redundant or erroneous (e.g. Carriage-return)
		SKIP,

		// Lexed tokens (not produced by the tokenizer)
		STRING,
		COMMENT;

		private final boolean mayCombine;

		TokenType() {
			this(true);
		}

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

	public ConfigToken(String source, int lineNumber, int column, TokenType tokenType, String text) {
		this.text = requireNonNull(text, "text");
		this.source = requireNonNull(source, "source");
		this.lineNumber = requireAtLeast(1, lineNumber, "lineNumber");
		this.tokenType = requireNonNull(tokenType, "tokenType");
		this.column = requireAtLeast(1, column, "column");
	}

	/**
	 * Replaces the {@code tokenType} and {@code text} while preserving the source coordinates.
	 */
	public ConfigToken replace(TokenType tokenType, String text) {
		return new ConfigToken(getSource(), getLineNumber(), getColumn(), tokenType, text);
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

	public String getLocationString() {
		return getLocationString(0);
	}

	public String getLocationString(int columnOffset) {
		return getSource() + ':' + getLineNumber() + ':' + (getColumn() + columnOffset);
	}

	public String getEscapedText() {
		String text = NEWLINE_PATTERN.matcher(this.text).replaceAll("\\\\n");
		text = TAB_PATTERN.matcher(text).replaceAll("\\\\t");
		text = QUOTE_PATTERN.matcher(text).replaceAll("\\\\\"");
		return text;
	}

	@Override
	public String toString() {
		return "ConfigToken{" + getLocationString() + " (" + getTokenType() + ") \"" + getEscapedText() + "\"}";
	}
}
