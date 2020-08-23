package valoeghese.shuttle.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.zip.ZipFile;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

// Adapted from Epic Valo Mod's ScriptManager
public abstract class ScriptManager {
	private static final ScriptEngineManager ENGINE_MANAGER = new ScriptEngineManager();
	private static final Supplier<ScriptEngine> ENGINE_SOURCE = () -> ENGINE_MANAGER.getEngineByName("nashorn");
	protected final String source;

	public ScriptManager(ZipFile file, String entry) throws IOException {
		InputStream is = file.getInputStream(file.getEntry(entry));

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nBytesRead;
		byte[] bufferBuffer = new byte[0x4000];

		while ((nBytesRead = is.read(bufferBuffer, 0, bufferBuffer.length)) != -1) {
			buffer.write(bufferBuffer, 0, nBytesRead);
		}

		is.close();
		this.source = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
	}

	public static class ScriptContext {
		protected ScriptContext() {
		}

		final Set<String> definitions = new HashSet<>();
		final StringBuilder classDefinitions = new StringBuilder();
		final Map<String, Object> objectDefinitions = new HashMap<>();
		final Map<String, Triple<String, String, Integer>> methodDefinitions = new HashMap<>();

		private void checkDef(String def) {
			if (!definitions.add(def)) {
				throw new RuntimeException("Definition " + def + " already exists in this ScriptContext!");
			}
		}

		public void addClassDefinition(String def, Class<?> clazz) {
			checkDef(def);
			this.classDefinitions.append("var " + def + " = Java.type(\"" + clazz.getName() + "\");\n");
		}

		public void addFunctionDefinition(String def, Class<?> clazz, String methodName, int parameterCount) {
			if (parameterCount > 26) {
				throw new RuntimeException("Too many parameters! " + parameterCount);
			}

			checkDef(def);
			String classDef = "generated_" + Math.abs(def.hashCode()) + def.substring(0, 3);
			this.addClassDefinition(classDef, clazz);
			this.methodDefinitions.put(def, new ImmutableTriple<>(classDef, methodName, parameterCount));
		}

		public void addObjectDefinition(String def, Object object) {
			checkDef(def);
			this.objectDefinitions.put(def, object);
		}

		public Invocable runScript(File file) throws ScriptException, IOException {
			ScriptEngine engine = ENGINE_SOURCE.get();
			// add objects
			engine.getBindings(javax.script.ScriptContext.ENGINE_SCOPE).putAll(this.objectDefinitions);
			// add classes
			engine.eval(this.classDefinitions.toString());

			// add methods
			for (Map.Entry<String, Triple<String, String, Integer>> function : this.methodDefinitions.entrySet()) {
				StringBuilder params = new StringBuilder();
				final int max = 97 + function.getValue().getRight();

				for (int i = 97; i < max; ++i) {
					params.append((char) i);

					if (i < max - 1) {
						params.append(',');
					}
				}

				String paramString = params.toString();

				engine.eval(
						new StringBuilder("function ").append(function.getKey()).append('(').append(paramString).append("){")
						.append(function.getValue().getLeft()).append('.')
						.append(function.getValue().getMiddle()).append('(').append(paramString).append(");")
						.append("}\n")
						.toString());
			}

			// eval
			try (FileReader reader = new FileReader(file)) {
				engine.eval(reader);
			}

			return (Invocable) engine;
		}
	}
}
