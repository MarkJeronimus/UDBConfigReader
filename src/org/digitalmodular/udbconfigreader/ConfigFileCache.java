package org.digitalmodular.udbconfigreader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.utilities.annotation.Singleton;

/**
 * Caches parsed config files, so multiple calls to {@code include()} with
 * the same filename won't cause that file to be parsed multiple times.
 *
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
