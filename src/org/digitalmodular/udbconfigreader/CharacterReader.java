package org.digitalmodular.udbconfigreader;

import java.util.List;

import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

/**
 * Groups runs of characters that perform the same grammatical function into tokens.
 *
 * @author Zom-B
 */
// Created 2021-08-09
public class CharacterReader {
	private final String   source;
	private final String[] lines;

	private int lineNumber = 0;
	private int column     = -1;

	private int storedLineNumber = 0;
	private int storedColumn     = 0;

	public CharacterReader(String source, List<String> lines) {
		this.source = requireNonNull(source, "sourceName");
		requireNonNull(lines, "lines");
		this.lines = lines.toArray(new String[0]);
	}

	/**
	 * Returns the next char, or {@code -1} if there are none
	 */
	public int nextChar() {
		if (lineNumber >= lines.length)
			return -1;

		column++;
		if (column > lines[lineNumber].length()) {
			lineNumber++;
			column = 0;
		}

		if (lineNumber >= lines.length)
			return -1; // End of everything
		else if (lineNumber == lines.length - 1 && column == lines[lineNumber].length())
			return -1; // End of last line
		else if (column == lines[lineNumber].length())
			return '\n'; // End of any line that's not the last
		else
			return lines[lineNumber].charAt(column);
	}

	public String getSource() {
		return source;
	}

	public int getLineNumber() {
		return lineNumber + 1;
	}

	public int getColumn() {
		return column + 1;
	}

	public void storeLocation() {
		storedLineNumber = lineNumber;
		storedColumn = column;
	}

	public int getStoredLineNumber() {
		return storedLineNumber;
	}

	public int getStoredColumn() {
		return storedColumn;
	}
}
