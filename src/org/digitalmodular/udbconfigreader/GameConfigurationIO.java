package org.digitalmodular.udbconfigreader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * @author Zom-B
 */
// Created 2021-08-09
public class GameConfigurationIO {
	public static void main(String... args) throws IOException {
		loadGameConfiguration(Paths.get("Configurations/Doom_DoomDoom.cfg"));
	}

	private static final String ERROR_KEYMISSING = "Missing key name in assignment or scope."; /// 154
	private static final String ERROR_KEYSPACES  = "Spaces not allowed in key names.";
	//	private static final String ERROR_ASSIGNINVALID = "Invalid assignment. Missing a previous terminator symbol?";
//	private static final String ERROR_VALUEINVALID = "Invalid value in assignment. Missing a previous terminator symbol?";
//	private static final String ERROR_VALUETOOBIG = "Value too big.";
//	private static final String ERROR_KEYNOTUNQIUE = "Key is not unique within scope.";
//	private static final String ERROR_KEYWORDUNKNOWN = "Unknown keyword in assignment. Missing a previous terminator symbol?";
//	private static final String ERROR_UNEXPECTED_END = "Unexpected end of data. Missing a previous terminator symbol?";
//	private static final String ERROR_UNKNOWN_FUNCTION = "Unknown function call.";
//	private static final String ERROR_INVALID_ARGS = "Invalid function arguments.";
//	private static final String ERROR_INCLUDE_UNSUPPORTED = "Include function is not supported in data parsed from stream.";

	public static @Nullable ConfigStruct loadGameConfiguration(Path file) throws IOException {
		try {
			List<String>    lines  = Files.readAllLines(file);
			CharacterReader reader = new CharacterReader(file.getFileName().toString(), lines);

			ConfigStruct configuration = new ConfigStruct(file.getFileName().toString(), true, 64);
			parseConfiguration(reader, configuration);

			return configuration;
		} catch (IOException ex) {
			// Unable to load configuration
			throw new IOException("Unable to load the game configuration file: " + file, ex);
		}
	}

	private static void parseConfiguration(CharacterReader reader, ConfigStruct configuration) {
		while (true) {
			int c = reader.nextChar();
			if (c < 0)
				return;

			System.out.print((char)c);
		}
	}
}
