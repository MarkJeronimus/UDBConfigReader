package org.digitalmodular.udbconfigreader;

import java.util.List;

import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

/**
 * @author Zom-B
 */
// Created 2021-08-09
public class CharacterReader {
	private final String   sourceName;
	private final String[] lines;

	private int lineNumber = 0;
	private int column     = 0;

	private int storedLineNumber = 0;
	private int storedColumn     = 0;

	public CharacterReader(String sourceName, List<String> lines) {
		this.sourceName = requireNonNull(sourceName, "sourceName");
		requireNonNull(lines, "lines");
		this.lines = lines.toArray(new String[0]);
	}

	/**
	 * Returns the next char, or {@code -1} if there are none
	 */
	public int nextChar() {
		if (lineNumber >= lines.length)
			return -1;

		if (column >= lines[lineNumber].length()) {
			lineNumber++;
			column = 0;
			return '\n';
		}

		char ch = lines[lineNumber].charAt(column++);
		column++;
		return ch;
	}

	public String getSourceName() {
		return sourceName;
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
