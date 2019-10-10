package frogmodaiGame.events;

import frogmodaiGame.CancellableEvent;
import net.mostlyoriginal.api.event.common.Cancellable;
import net.mostlyoriginal.api.event.common.Event;

public class ChangeStat {
	public static class Before extends CancellableEvent {
		public String name;
		public int entity;
		public int amount;

		public Before(String _name, int _entity, int _amount) {
			name = _name;
			entity = _entity;
			amount = _amount;
		}
	}
	
	public static class During extends CancellableEvent {
		public String name;
		public int entity;
		public int amount;

		public During(String _name, int _entity, int _amount) {
			name = _name;
			entity = _entity;
			amount = _amount;
		}
	}
	
	public static class After implements Event {
		public String name;
		public int entity;
		public int amount;

		public After(String _name, int _entity, int _amount) {
			name = _name;
			entity = _entity;
			amount = _amount;
		}
	}

}