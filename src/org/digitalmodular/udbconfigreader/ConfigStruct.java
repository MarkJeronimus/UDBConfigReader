package org.digitalmodular.udbconfigreader;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.utilities.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.utilities.ValidatorUtilities.requireStringLengthAtLeast;

/**
 * A structure with a name and holding key-value pairs, where value can be a literal value or another structure.
 * <p>
 * During construction, the choice can be made to keep the entries in order of being added.
 *
 * @author Zom-B
 */
// Created 2021-08-09
public class ConfigStruct implements Iterable<Entry<String, Object>> {
	private final String              name;
	private final Map<String, Object> values;

	public ConfigStruct(String name, int initialCapacity) {
		this.name = requireStringLengthAtLeast(1, name, "name");
		values = new LinkedHashMap<>(initialCapacity);
	}

	public String getName() {
		return name;
	}

	public @Nullable Object get(String key) {
		return values.get(key);
	}

	/**
	 * Stores or overwrites the specified value at the specified key.
	 * <p>
	 * If the value is not another {@code ConfigStruct}, it stores the value, overwriting any previous value.
	 * <p>
	 * If the value is a {@code ConfigStruct}, special logic is applied:
	 * <ul><li>If the value stored for this key is another {@code ConfigStruct}, it merges the two structs,</li>
	 * <li>Otherwise it directly stores the value, overwriting any previous value.</li></ul>
	 */
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

	/**
	 * Adds all elements in the specified struct to this struct.
	 * <p>
	 * Entries with a key that already exists are overwritten.
	 */
	public void putAll(ConfigStruct struct) {
		for (Entry<String, Object> entry : struct)
			put(entry.getKey(), entry.getValue());
	}

	public @Nullable ConfigStruct getStruct(String key) {
		@Nullable Object value = values.get(requireNonNull(key, "key"));

		if (!(value instanceof ConfigStruct))
			return null;

		return (ConfigStruct)value;
	}

	@Contract("_, null -> null; _, _ -> !null")
	public String getString(String key, String fallbackValue) {
		@Nullable Object value = values.get(requireNonNull(key, "key"));

		if (value == null)
			return fallbackValue;

		return value.toString();
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
