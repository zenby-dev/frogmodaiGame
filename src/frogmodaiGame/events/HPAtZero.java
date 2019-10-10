package frogmodaiGame.events;

import frogmodaiGame.CancellableEvent;
import net.mostlyoriginal.api.event.common.Cancellable;
import net.mostlyoriginal.api.event.common.Event;

public class HPAtZero {
	public static class Before extends CancellableEvent {
		public int entity;

		public Before(int _entity) {
			entity = _entity;
		}
	}
	
	public static class During extends CancellableEvent {
		public int entity;

		public During(int _entity) {
			entity = _entity;
		}
	}
	
	public static class After implements Event {
		public int entity;

		public After(int _entity) {
			entity = _entity;
		}
	}

}