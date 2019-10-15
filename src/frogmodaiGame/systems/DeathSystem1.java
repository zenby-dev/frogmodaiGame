package frogmodaiGame.systems;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.artemis.Aspect;
import com.artemis.Aspect.Builder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import frogmodaiGame.FFMain;
import frogmodaiGame.components.*;
import frogmodaiGame.events.ActorDied;
import frogmodaiGame.events.ChangeStat;
import frogmodaiGame.events.HPAtZero;
import frogmodaiGame.events.ScreenRefreshRequest;
import frogmodaiGame.events.HPAtZero.After;
import frogmodaiGame.events.HPAtZero.During;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;

public class DeathSystem1 extends BaseSystem {
	
	ComponentMapper<IsDead> mIsDead;

	EventSystem es;

	public DeathSystem1() {
		super(); // ??

	}

	@Override
	protected void processSystem() {

	}
	
	@Subscribe
	public void ActorDiedListener(ActorDied event) {
		//Turn into corpse or something
		//The easiest and least flexible answer is every creature drops the same "corpse" item
		//The second easiest is to create an item that pulls some general attributes from the creature if they're there
		//Another option is for entities to have components that carry info about what should happen upon their death??
		//Removing components directly from the entity to reduce it down to just the ones for a corpse would 
		//	require knowing what components they have (bad idea)
		//FFMain.worldManager.world.delete(event.entity);
		mIsDead.create(event.entity);
		FFMain.sendMessage("Entity " + event.entity + " has died");
		es.dispatch(new ScreenRefreshRequest());
	}
	
}

/*
 * ChangeStat.Before before = new ChangeStat.Before("HP", e, -1);
 * es.dispatch(before); if (!before.isCancelled()) { ChangeStat.During during =
 * new ChangeStat.During("HP", e, -1); es.dispatch(during); if
 * (!during.isCancelled()) { ChangeStat.After after = new ChangeStat.After("HP",
 * e, -1); es.dispatch(after); } }
 */