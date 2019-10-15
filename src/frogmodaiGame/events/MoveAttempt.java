package frogmodaiGame.events;

import frogmodaiGame.CancellableEvent;
import frogmodaiGame.FFMain;
import net.mostlyoriginal.api.event.common.Cancellable;
import net.mostlyoriginal.api.event.common.Event;

public class MoveAttempt {
	public static void run(int entity, int dx, int dy) {
		FFMain.worldManager.runEventSet(new MoveAttempt.Before(entity, dx, dy),
				new MoveAttempt.During(entity, dx, dy),
				new MoveAttempt.After(entity, dx, dy));
	}
	
	public static class Before extends CancellableEvent {
		public int entity;
		public int dx;
		public int dy;

		public Before(int _entity, int _dx, int _dy) {
			entity = _entity;
			dx = _dx;
			dy = _dy;
		}
	}
	
	public static class During extends CancellableEvent {
		public int entity;
		public int dx;
		public int dy;

		public During(int _entity, int _dx, int _dy) {
			entity = _entity;
			dx = _dx;
			dy = _dy;
		}
	}
	
	public static class After implements Event {
		public int entity;
		public int dx;
		public int dy;

		public After(int _entity, int _dx, int _dy) {
			entity = _entity;
			dx = _dx;
			dy = _dy;
		}
	}

}