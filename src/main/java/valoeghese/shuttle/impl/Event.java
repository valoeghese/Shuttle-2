package valoeghese.shuttle.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptException;

import valoeghese.shuttle.api.event.EventResult;

/**
 * Shuttle Events: provides a javascript interface for other event implementations.
 * @author Valoeghese
 */
public final class Event<T> {
	private static final List<Event<?>> EVENTS = new ArrayList<>();

	private Event(String name, Class<T> event, boolean complex, Consumer<Function<T, EventResult>> subscriber) {
		this.name = name;

		if (complex) {
			subscriber.accept(evt -> {
				for (int i = this.listeners.size() - 1; i >= 0; --i) {
					Invocable listener = this.listeners.get(i);

					try {
						EventResult result = (EventResult) listener.invokeFunction(this.name, evt);

						if (result.isCancellable()) {
							return result;
						}
					} catch (NoSuchMethodException e) {
						// Unsubscribe
						this.listeners.remove(i);
					} catch (ScriptException e) {
						throw new RuntimeException("Exception executing script in event " + this.name, e);
					}
				}

				return EventResult.PASS;
			});
		} else {
			subscriber.accept(evt -> {
				for (int i = this.listeners.size() - 1; i >= 0; --i) {
					Invocable listener = this.listeners.get(i);

					try {
						listener.invokeFunction(this.name, evt);
					} catch (NoSuchMethodException e) {
						// Unsubscribe
						this.listeners.remove(i);
					} catch (ScriptException e) {
						throw new RuntimeException("Exception executing script in event " + this.name, e);
					}
				}

				return EventResult.PASS;
			});
		}
	}

	private final String name;
	//	private final Queue<Invocable> waitList = new LinkedList<>();
	private final List<Invocable> listeners = new ArrayList<>();

	/**
	 * Subscribes a listener to the event, if the function of the event exists.
	 * @param listener the event listener.
	 */
	public void trySubscribe(Invocable listener) {
		// this.waitList.add(listener);
		this.listeners.add(listener);
	}

	/**
	 * Create and register a new shuttle event.
	 * @param name the method name for the event.
	 * @param event the class of the event parameter.
	 * @param complex whether the event is a complex cancellable event (i.e. returns an {@link EventResult}).
	 * @param subscriber the callback which takes the waiting list function and subscribes
	 * @return the event created.
	 */
	public static <T> Event<T> register(String name, Class<T> event, boolean complex, Consumer<Function<T, EventResult>> subscriber) {
		Event<T> result = new Event<>(name, event, complex, subscriber);
		EVENTS.add(result);
		return result;
	}

	public static void trySubscribeAll(Invocable listener) {
		for (Event<?> e : EVENTS) {
			e.trySubscribe(listener);
		}
	}

	// OLD: @param subscriber callback which takes the Invocable and subscribes it to the event implementation (usually a fabric event).
	// Perhaps there's still a way to subscribe it to the implementation directly?
}
