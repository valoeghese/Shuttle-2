package valoeghese.shuttle.api.event;

import net.minecraft.world.InteractionResult;

/**
 * Enum representing the result of an event which specifies a result. Details on the meaning of each is described in the particular implementing event.
 * @author Valoeghese
 */
public enum EventResult {
	FAIL,
	PASS,
	CONSUME, // Behaves as SUCCESS in normal circumstances, but may provide an alternate form of "success", often not fully successful, but not fully failing.
	SUCCESS;

	/**
	 * @return whether the event should cancel further processing of an event, under normal event behaviour.
	 */
	public boolean isCancellable() {
		return this != PASS;
	}

	public InteractionResult toInteractionResult() {
		switch (this) {
		case CONSUME:
			return InteractionResult.CONSUME;
		case FAIL:
			return InteractionResult.FAIL;
		case PASS:
			return InteractionResult.PASS;
		case SUCCESS:
			return InteractionResult.SUCCESS;
		default:
			return null;
		}
	}
}
