package valoeghese.shuttle.api.event;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;

public class ShuttleEntitySpawnEvent {
	public ShuttleEntitySpawnEvent(Entity original, AtomicReference<Entity> entity, ServerLevelAccessor world, MobSpawnType reason) {
	}
}
