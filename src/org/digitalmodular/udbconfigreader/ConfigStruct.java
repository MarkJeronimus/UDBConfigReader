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

		if (value instanceof ConfigStruct) {
			String name = ((ConfigStruct)value).getName();
			if (!key.equals(name)) {
				throw new IllegalArgumentException(
						"specified 'key' and the 'name' of the specified ConfigurationStruct don't match:" + key +
						", " + name);
			}

			merge((ConfigStruct)value);
			return;
		}

		if (value == null)
			values.remove(key);
		else
			values.put(key, value);
	}

	private void merge(ConfigStruct struct) {
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(16384);
		prettyPrint(this, sb, 0);
		return sb.toString();
	}

	private static void prettyPrint(ConfigStruct struct, StringBuilder sb, int indentation) {
		sb.append("\t".repeat(Math.max(0, indentation)));
		sb.append(struct.getName()).append(" {\n");
		indentation++;

		for (Entry<String, Object> entry : struct) {
			@Nullable Object value = entry.getValue();
			if (value instanceof ConfigStruct)
				prettyPrint((ConfigStruct)value, sb, indentation);
			else if (value == null)
				sb.append("\t".repeat(Math.max(0, indentation)))
				  .append(entry.getKey()).append(";\n");
			else if (value instanceof String)
				sb.append("\t".repeat(Math.max(0, indentation)))
				  .append(entry.getKey()).append(" = \"").append(value).append("\";\n");
			else
				sb.append("\t".repeat(Math.max(0, indentation)))
				  .append(entry.getKey()).append(" = ").append(value).append(";\n");
		}

		indentation--;
		sb.append("\t".repeat(Math.max(0, indentation))).append("}\n");
	}

}
