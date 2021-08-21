package org.digitalmodular.udbconfigreader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.udbconfigreader.lexer.CleaningLexer;
import org.digitalmodular.udbconfigreader.lexer.CommentsLexer;
import org.digitalmodular.udbconfigreader.lexer.ConfigToken;
import org.digitalmodular.udbconfigreader.lexer.ConfigTokenizer;
import org.digitalmodular.udbconfigreader.lexer.KeywordLexer;
import org.digitalmodular.udbconfigreader.lexer.StringsLexer;

/**
 * @author Zom-B
 */
// Created 2021-08-09
public final class GameConfigurationIO {
	private GameConfigurationIO() {
		throw new AssertionError();
	}

	/**
	 * Load a configuration file as a {@code ConfigStruct} structure.
	 */
	public static ConfigStruct loadGameConfiguration(Path file) throws IOException {
		List<Path> fileStack = new ArrayList<>(4);
		fileStack.add(file);
		return loadConfigurationFile(fileStack);
	}

	/**
	 * Internal method to load configuration files recursively, via
	 *
	 * @param fileStack The list of files which are currently being parsed.
	 *                  Each subsequent element is a file included by the previous element.
	 *                  The last element is the one to parse next.
	 */
	static ConfigStruct loadConfigurationFile(List<Path> fileStack) throws IOException {
		assert !fileStack.isEmpty();
		Path file = fileStack.get(fileStack.size() - 1);

		@Nullable ConfigStruct gameConfiguration = ConfigFileCache.INSTANCE.get(file);
		if (gameConfiguration != null)
			return gameConfiguration;

		if (Logger.getGlobal().isLoggable(FINER))
			Logger.getGlobal().log(INFO, "Loading configuration file: " + file.toAbsolutePath());
		else
			Logger.getGlobal().log(INFO, "Loading configuration file: " + file);

		try {
			List<String>    lines  = Files.readAllLines(file);
			CharacterReader reader = new CharacterReader(file.getFileName().toString(), lines);

			gameConfiguration = new ConfigStruct(file.toString(), 16);

			parseConfiguration(fileStack, reader, gameConfiguration);

			ConfigFileCache.INSTANCE.add(file, gameConfiguration);
			return gameConfiguration;
		} catch (IOException ex) {
			throw new IOException("Unable to load the game configuration file: " + file, ex);
		}
	}

	private static void parseConfiguration(
			List<Path> fileStack, CharacterReader reader, ConfigStruct gameConfiguration) {
		List<ConfigToken> tokens = ConfigTokenizer.tokenize(reader);

		tokens = StringsLexer.process(tokens);
		tokens = CommentsLexer.process(tokens);
		tokens = KeywordLexer.process(tokens);
		tokens = CleaningLexer.process(tokens);

		ConfigParser.parse(fileStack, tokens, gameConfiguration);
	}
}
