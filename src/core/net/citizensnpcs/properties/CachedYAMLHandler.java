package net.citizensnpcs.properties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.citizensnpcs.utils.Messaging;
import net.citizensnpcs.utils.StringUtils;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Lists;

public class CachedYAMLHandler extends AbstractStorage {
	private final FileConfiguration config;
	private final File file;
	private final SettingsTree tree = new SettingsTree();

	public CachedYAMLHandler(String fileName) {
		this.file = new File(fileName);
		this.config = new YamlConfiguration();
		if (!file.exists()) {
			create();
			save();
		} else {
			load();
		}
	}

	private void clear() {
		for (String path : config.getKeys(true)) {
			config.set(path, null);
		}
	}

	private void create() {
		try {
			Messaging
					.log("Creating new config file at " + file.getName() + ".");
			file.getParentFile().mkdirs();
			file.createNewFile();
		} catch (IOException ex) {
			Messaging.log("Unable to create " + file.getPath() + ".",
					Level.SEVERE);
		}
	}

	private String get(String path) {
		return this.tree.get(path);
	}

	@Override
	public boolean getBoolean(String path) {
		return pathExists(path) && Boolean.parseBoolean(get(path));
	}

	@Override
	public boolean getBoolean(String path, boolean value) {
		if (valueExists(path)) {
			return getBoolean(path);
		} else {
			setBoolean(path, value);
		}
		return value;
	}

	@Override
	public double getDouble(String path) {
		if (valueExists(path)) {
			return Double.parseDouble(get(path));
		}
		return 0;
	}

	@Override
	public double getDouble(String path, double value) {
		if (valueExists(path)) {
			return getDouble(path);
		} else {
			setDouble(path, value);
		}
		return value;
	}

	@Override
	public int getInt(String path) {
		if (valueExists(path)) {
			return Integer.parseInt(get(path));
		}
		return 0;
	}

	@Override
	public int getInt(String path, int value) {
		if (valueExists(path)) {
			return getInt(path);
		} else {
			setInt(path, value);
		}
		return value;
	}

	@Override
	public List<Integer> getIntegerKeys(String path) {
		List<Integer> ret = new ArrayList<Integer>();
		for (String str : getKeys(path)) {
			try {
				ret.add(Integer.parseInt(str));
			} catch (NumberFormatException ex) {
			}
		}
		Collections.sort(ret);
		return ret;
	}

	@Override
	public List<String> getKeys(String path) {
		if (path == null)
			path = "";
		else
			path += ".";
		List<String> keys = Lists.newArrayList();
		for (String key : this.tree.getTree().keySet()) {
			if (key.startsWith(path) && key.length() > path.length()) {
				key = key.replace(path, "");
				int index = key.contains(".") ? key.indexOf('.') : key.length();
				key = key.substring(0, index);
				if (!keys.contains(key))
					keys.add(key);
			}
		}
		return keys;
	}

	@Override
	public long getLong(String path) {
		if (valueExists(path)) {
			return Long.parseLong(get(path));
		}
		return 0;
	}

	@Override
	public long getLong(String path, long value) {
		if (valueExists(path)) {
			return getLong(path);
		} else {
			setLong(path, value);
		}
		return value;
	}

	@Override
	public Object getRaw(String string) {
		return config.get(string);
	}

	@Override
	public String getString(String path) {
		if (valueExists(path)) {
			return get(path);
		}
		return "";
	}

	@Override
	public String getString(String path, String value) {
		if (valueExists(path)) {
			return get(path);
		} else {
			setString(path, value);
		}
		return value;
	}

	@Override
	public boolean keyExists(String path) {
		return pathExists(path);
	}

	@Override
	public void load() {
		clear();
		try {
			config.load(file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		for (String entry : this.config.getKeys(true)) {
			if (!(config.get(entry) instanceof MemorySection))
				tree.set(entry, config.get(entry).toString());
		}
		clear();
	}

	public boolean pathExists(String path) {
		return this.tree.get(path) != null;
	}

	@Override
	public void removeKey(String path) {
		this.tree.remove(path);
	}

	@Override
	public void save() {
		clear();
		for (Entry<String, String> entry : tree.getTree().entrySet()) {
			if (entry.getValue() != null && !entry.getValue().isEmpty()
					&& !StringUtils.isNumber(entry.getKey())) {
				this.config.set(entry.getKey(), entry.getValue());
			}
		}
		try {
			this.config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		clear();
	}

	@Override
	public void setBoolean(String path, boolean value) {
		this.tree.set(path, String.valueOf(value));
	}

	@Override
	public void setDouble(String path, double value) {
		this.tree.set(path, String.valueOf(value));
	}

	@Override
	public void setInt(String path, int value) {
		this.tree.set(path, String.valueOf(value));
	}

	@Override
	public void setLong(String path, long value) {
		this.tree.set(path, String.valueOf(value));
	}

	@Override
	public void setRaw(String path, Object value) {
		config.set(path, value);
	}

	@Override
	public void setString(String path, String value) {
		this.tree.set(path, value);
	}

	public boolean valueExists(String path) {
		return pathExists(path) && !this.tree.get(path).isEmpty();
	}
}