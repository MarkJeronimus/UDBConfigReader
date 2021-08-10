package org.digitalmodular.udbconfigreader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Zom-B
 */
// Created 2021-08-09
public class GameConfigurationIO {
	public static void main(String... args) throws IOException {
		loadGameConfiguration(Paths.get("Configurations/Doom_DoomDoom.cfg"));
	}

	public static ConfigStruct loadGameConfiguration(Path file) throws IOException {
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
		List<ConfigurationToken> tokens = ConfigurationTokenizer.tokenize(reader);
		tokens.forEach(System.out::println);
	}
}
