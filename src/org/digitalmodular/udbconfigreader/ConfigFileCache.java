package org.digitalmodular.udbconfigreader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.Singleton;

/**
 * @author Zom-B
 */
// Created 2021-08-15
@Singleton
public enum ConfigFileCache {
	INSTANCE;

	private final Map<Path, ConfigStruct> cache = new HashMap<>(256);

	public void add(Path file, ConfigStruct configStruct) {
		cache.put(file, configStruct);
	}

	public @Nullable ConfigStruct get(Path file) {
		return cache.get(file);
	}
}
