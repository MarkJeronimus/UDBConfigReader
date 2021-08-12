package org.digitalmodular.udbconfigreader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.digitalmodular.udbconfigreader.lexer.CommentsLexer;
import org.digitalmodular.udbconfigreader.lexer.ConfigToken;
import org.digitalmodular.udbconfigreader.lexer.ConfigTokenizer;
import org.digitalmodular.udbconfigreader.lexer.StringsLexer;

/**
 * @author Zom-B
 */
// Created 2021-08-09
public class GameConfigurationIO {
	public static void main(String... args) throws IOException {
		parseDirectory(Paths.get("Configurations/"));

//		loadGameConfiguration(Paths.get("Configurations/Doom_DoomDoom.cfg"));
//		loadGameConfiguration(Paths.get("Configurations/valid1.cfg"));
//		loadGameConfiguration(Paths.get("Configurations/valid2.cfg"));
//		loadGameConfiguration(Paths.get("Configurations/corrupt1.cfg"));
//		loadGameConfiguration(Paths.get("Configurations/corrupt2.cfg"));
	}

	private static void parseDirectory(Path path) throws IOException {
		for (Path file : Files.newDirectoryStream(path)) {
			if (Files.isDirectory(file))
				parseDirectory(file);
			else
				loadGameConfiguration(file);
		}
	}

	public static ConfigStruct loadGameConfiguration(Path file) throws IOException {
		System.out.println("### " + file);

		try {
			List<String>    lines  = Files.readAllLines(file);
			CharacterReader reader = new CharacterReader(file.getFileName().toString(), lines);

			parseConfiguration(reader);

			return null;
		} catch (IOException ex) {
			// Unable to load configuration
			throw new IOException("Unable to load the game configuration file: " + file, ex);
		}
	}

	private static void parseConfiguration(CharacterReader reader) {
		List<ConfigToken> tokens = ConfigTokenizer.tokenize(reader);
		tokens = StringsLexer.process(tokens);
		tokens = CommentsLexer.process(tokens);
		tokens.forEach(System.out::println);
	}
}
