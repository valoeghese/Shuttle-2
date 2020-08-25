package valoeghese.shuttle;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;
import javax.script.Invocable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import valoeghese.shuttle.api.event.EventResult;
import valoeghese.shuttle.api.event.ShuttleSetupEvent;
import valoeghese.shuttle.impl.Event;
import valoeghese.shuttle.impl.ScriptManager;
import valoeghese.shuttle.impl.ScriptManager.ScriptContext;

public class Shuttle implements ModInitializer {
	@Override
	public void onInitialize() {
		AtomicReference<Function<ShuttleSetupEvent, EventResult>> setup = new AtomicReference<>();
		Event.register("setup", ShuttleSetupEvent.class, false, func -> setup.set(func));

		try {
			ScriptContext context = new ScriptContext();
			context.addClassDefinition("Item", Item.class);
			ScriptManager script = new ScriptManager(context);

			for (Path p : MODS_FOLDER) {
				if (p.getFileName().endsWith(".jar")) {
					@Nullable Invocable ivc = script.apply(new ZipFile(p.toFile()), "main.js");

					if (ivc != null) {
						PLUGINS.add(ivc);
					}
				} else if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS) && p.getFileName().toString().equals("shuttledev")) {
					try (InputStream stream = Files.newInputStream(p.resolve("main.js"))) {
						PLUGINS.add(script.apply(stream));
					}
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static final Logger LOGGER = LogManager.getLogger("Shuttle 2");
	public static final Path MODS_FOLDER = FabricLoader.getInstance().getConfigDir().getParent().resolve("mods");
	private static final List<Invocable> PLUGINS = new ArrayList<>();
}
