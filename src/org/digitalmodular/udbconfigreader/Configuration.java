package org.digitalmodular.udbconfigreader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.utilities.ValidatorUtilities.requireStringLengthAtLeast;

/**
 * @author zom-b
 */
// Created 2021-08-09
public class Configuration {
	private final           String              key;
	private final @Nullable Object              value;
	private final @Nullable Map<Object, String> children;
	private final           boolean             sorted;

	public Configuration(String key, @Nullable Object value) {
		this.key = requireStringLengthAtLeast(1, key, "key");
		this.value = requireNonNull(value, "value");
		children = null;
		sorted = false;
	}

	public Configuration(String key, boolean sorted, int initialCapacity) {
		this.key = requireStringLengthAtLeast(1, key, "key");
		value = null;
		children = sorted ? new LinkedHashMap<>(initialCapacity) : new HashMap<>(initialCapacity);
		this.sorted = sorted;
	}

	public String getKey() {
		return key;
	}

	/**
	 * If this is {@code null}, this node contains children instead.
	 */
	public @Nullable Object getValue() {
		return value;
	}

	/**
	 * If this is {@code null}, this node contains a direct value instead.
	 */
	public @Nullable Map<Object, String> getChildren() {
		return children;
	}
}
