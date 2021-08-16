package org.digitalmodular.udbconfigreader;

import java.util.List;

import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;

/**
 * Turns a list of strings (for example, read from a file) into a stream of characters.
 * <p>
 * Between lines, newline characters will be inserted into the stream.
 * After the last line, no newline is inserted.
 * <p>
 * It also facilitates retrieving the location (source name, line number, and column number)
 * of the most recent character, as well as from a previously marked location.
 * Before the first character is read, the location is indeterminate.
 * If the provided list of lines is empty, the location remains indeterminate,
 * even after calling {@link #nextChar()}.
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
	 * Returns the next char, or {@code -1} if there are none.
	 */
	public int nextChar() {
		if (lineNumber >= lines.length) {
			return -1;
		}

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
		if (column < 0)
			throw new IllegalStateException("No character has been read yet.");

		return lineNumber + 1;
	}

	public int getColumn() {
		if (column < 0)
			throw new IllegalStateException("No character has been read yet.");

		return column + 1;
	}

	public void markLocation() {
		storedLineNumber = lineNumber;
		storedColumn = column;
	}

	public int getMarkedLineNumber() {
		if (column < 0)
			throw new IllegalStateException("No character has been read yet.");

		return storedLineNumber + 1;
	}

	public int getMarkedColumn() {
		if (column < 0)
			throw new IllegalStateException("No character has been read yet.");

		return storedColumn + 1;
	}
}
