package org.digitalmodular.udbconfigreader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

	public static void main(String... args) throws IOException {
//		parseDirectory(Paths.get("Configurations/"));

		System.out.println(loadGameConfiguration(Paths.get("Configurations/Doom_DoomDoom.cfg")));
//		System.out.println(loadGameConfiguration(Paths.get("Configurations/Includes/Test_params.cfg")));
//		System.out.println(loadGameConfiguration(Paths.get("Configurations/Includes/Boom_linedefs.cfg")));
//		System.out.println(loadGameConfiguration(Paths.get("Configurations/Includes/Heretic_misc.cfg")));
	}

	public static void parseDirectory(Path path) throws IOException {
		for (Path file : Files.newDirectoryStream(path)) {
			if (Files.isDirectory(file)) {
				parseDirectory(file);
				continue;
			}

			String filename = file.getFileName().toString();
			if (filename.startsWith("valid") || filename.startsWith("corrupt"))
				continue;

			loadGameConfiguration(file);
		}
	}

	public static ConfigStruct loadGameConfiguration(Path file) throws IOException {
		List<Path> fileStack = new ArrayList<>(4);
		fileStack.add(file);
		return loadConfigurationFile(fileStack);
	}

	static ConfigStruct loadConfigurationFile(List<Path> fileStack) throws IOException {
		assert !fileStack.isEmpty();
		Path file = fileStack.get(fileStack.size() - 1);

		@Nullable ConfigStruct gameConfiguration = ConfigFileCache.INSTANCE.get(file);
		if (gameConfiguration != null)
			return gameConfiguration;

		try {
			List<String>    lines  = Files.readAllLines(file);
			CharacterReader reader = new CharacterReader(file.getFileName().toString(), lines);

			gameConfiguration = new ConfigStruct(file.toString(), true, 16);

			parseConfiguration(fileStack, reader, gameConfiguration);

			ConfigFileCache.INSTANCE.add(file, gameConfiguration);
			return gameConfiguration;
		} catch (IOException ex) {
			// Unable to load configuration
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

//		tokens.forEach(System.out::println);
		ConfigParser.parse(fileStack, tokens, gameConfiguration);
	}
}
