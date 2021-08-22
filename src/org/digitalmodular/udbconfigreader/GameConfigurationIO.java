package org.digitalmodular.udbconfigreader;

import java.io.IOException;
import java.nio.file.Path;

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
		RecursiveConfigFileLoader parser = new RecursiveConfigFileLoader();

		return parser.loadConfigurationFile(file);
	}
}
