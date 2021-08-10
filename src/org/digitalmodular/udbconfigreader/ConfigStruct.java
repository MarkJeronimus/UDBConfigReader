package org.digitalmodular.udbconfigreader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.utilities.ValidatorUtilities.requireStringLengthAtLeast;

/**
 * @author Zom-B
 */
// Created 2021-08-09
public class ConfigStruct implements Iterable<Entry<String, Object>> {
	private final String              name;
	private final Map<String, Object> values;
	private final boolean             sorted;

	public ConfigStruct(String name, boolean sorted, int initialCapacity) {
		this.name = requireStringLengthAtLeast(1, name, "name");
		values = sorted ? new LinkedHashMap<>(initialCapacity) : new HashMap<>(initialCapacity);
		this.sorted = sorted;
	}

	public String getName() {
		return name;
	}

	public @Nullable Object get(String key) {
		return values.get(key);
	}

	public void put(String key, @Nullable Object value) {
		requireStringLengthAtLeast(1, key, "key");

		if (value instanceof ConfigStruct)
			throw new IllegalArgumentException("'value' cannot be ConfigurationStruct");

		if (value == null)
			values.remove(key);
		else
			values.put(key, value);
	}

	public void put(ConfigStruct struct) {
		String key = struct.getName();

		@Nullable Object oldValue = values.get(key);
		if (oldValue instanceof ConfigStruct) {
			ConfigStruct oldStruct = (ConfigStruct)oldValue;

			for (Entry<String, Object> entry : struct)
				oldStruct.put(entry.getKey(), entry.getValue());
		} else {
			values.put(key, struct);
		}
	}

	public boolean isSorted() {
		return sorted;
	}

	@Override
	public Iterator<Map.Entry<String, Object>> iterator() {
		return values.entrySet().iterator();
	}
}
