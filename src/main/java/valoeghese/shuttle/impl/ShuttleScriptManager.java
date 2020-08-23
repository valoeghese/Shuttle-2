package valoeghese.shuttle.impl;

import java.io.IOException;
import java.util.zip.ZipFile;

import net.minecraft.world.item.Item;

public class ShuttleScriptManager extends ScriptManager {
	public ShuttleScriptManager(ZipFile file) throws IOException {
		super(file, "main.js");
	}

	private static final ScriptContext CONTEXT;

	static {
		CONTEXT = new ScriptContext();

		CONTEXT.addClassDefinition("Item", Item.class);
	}
}
