package valoeghese.shuttle.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public final class JSConsole {
	private static final Logger LOGGER = LogManager.getLogger("Plugin");
	private static final Object2IntMap<String> COUNT = new Object2IntArrayMap<>();

	public void log(Object msg) {
		LOGGER.info(msg);
	}

	public void count() {
		count("default");
	}

	public void count(String label) {
		LOGGER.info(COUNT.compute(label, (lbl, val) -> val == null ? 1 : val.intValue() + 1));
	}

	public void info(Object msg) {
		LOGGER.info(msg);
	}

	public void error(Object msg) {
		LOGGER.error(msg);
	}

	public void warn(Object msg) {
		LOGGER.warn(msg);
	}

	public void clear() {
		throw new UnsupportedOperationException("console.clear is not supported by Shuttle's JS Environment!");
	}
}
