package valoeghese.shuttle;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;
import javax.script.Invocable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.fabriccommunity.events.world.EntitySpawnCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import valoeghese.shuttle.api.event.EventResult;
import valoeghese.shuttle.api.event.ShuttleEntitySpawnEvent;
import valoeghese.shuttle.api.event.ShuttleSetupEvent;
import valoeghese.shuttle.impl.Event;
import valoeghese.shuttle.impl.JSConsole;
import valoeghese.shuttle.impl.ScriptManager;
import valoeghese.shuttle.impl.ScriptManager.ScriptContext;

public class Shuttle implements ModInitializer {
	@Override
	public void onInitialize() {
		System.out.println("Loading Shuttle Plugins.");
		AtomicReference<Function<ShuttleSetupEvent, EventResult>> setup = new AtomicReference<>();
		// ~~~GAME LIFECYCLE~~~
		Event.register("setup", ShuttleSetupEvent.class, false, func -> setup.set(func));
		// ~~~ENTTIY~~~
		Event.register("entitySpawning", ShuttleEntitySpawnEvent.class, false, func -> EntitySpawnCallback.PRE.register((original, entity, world, reason) -> func.apply(new ShuttleEntitySpawnEvent(original, entity, world, reason)).toInteractionResult()));

		try {
			ScriptContext context = new ScriptContext();

			JSConsole console = new JSConsole();
			context.addObjectDefinition("console", console);

			context.addClassDefinition("Item", Item.class);
			ScriptManager script = new ScriptManager(context);

			for (Path p : MODS_FOLDER) {
				if (p.getFileName().endsWith(".jar")) {
					@Nullable Invocable ivc = script.apply(new ZipFile(p.toFile()), "main.js");

					if (ivc != null) {
						Event.trySubscribeAll(ivc);
						PLUGINS.add(ivc);
					}
				} else if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS) && p.getFileName().toString().equals("shuttledev")) {
					try (InputStream stream = Files.newInputStream(p.resolve("main.js"))) {
						Invocable ivc = script.apply(stream);
						Event.trySubscribeAll(ivc);
						PLUGINS.add(ivc);
					}
				}
			}

			// Dev
			if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
				System.out.println("Fabric development environment detected. Searching for Shuttle Dev Test plugin.");

				Path binFolder = Paths.get("../bin/test");

				// If no bin folder might be out :: @reason Intellij uses out/ directory by default.
				if (!Files.isDirectory(binFolder, LinkOption.NOFOLLOW_LINKS)) {
					binFolder = Paths.get("../out/test");
				}

				// try other folders lol
				if (!Files.isDirectory(binFolder, LinkOption.NOFOLLOW_LINKS)) {
					binFolder = Paths.get("../bin");
				}

				if (!Files.isDirectory(binFolder, LinkOption.NOFOLLOW_LINKS)) {
					binFolder = Paths.get("../out");
				}

				if (Files.isDirectory(binFolder, LinkOption.NOFOLLOW_LINKS)) {
					System.out.println("Succesfully found Shuttle Dev Test plugin.");

					try (InputStream stream = Files.newInputStream(binFolder.resolve("main.js"))) {
						Invocable ivc = script.apply(stream);
						Event.trySubscribeAll(ivc);
						PLUGINS.add(ivc);
					}
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		System.out.println("Setting up Shuttle Plugins.");
		setup.get().apply(new ShuttleSetupEvent());
	}

	public static final Logger LOGGER = LogManager.getLogger("Shuttle 2");
	public static final Path MODS_FOLDER = FabricLoader.getInstance().getConfigDir().getParent().resolve("mods");
	private static final List<Invocable> PLUGINS = new ArrayList<>();
}
