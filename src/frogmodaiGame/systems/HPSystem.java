package frogmodaiGame.systems;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.artemis.Aspect;
import com.artemis.Aspect.Builder;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import frogmodaiGame.FFMain;
import frogmodaiGame.components.*;
import frogmodaiGame.events.ActorDied;
import frogmodaiGame.events.ChangeStat;
import frogmodaiGame.events.HPAtZero;
import frogmodaiGame.events.HPAtZero.After;
import frogmodaiGame.events.HPAtZero.During;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;

public class HPSystem extends IteratingSystem {

	EventSystem es;

	ComponentMapper<StatHP> mStatHP;

	public HPSystem() {
		super(Aspect.one(Stat.class)); // ??

	}

	@Override
	protected void process(int e) {

	}

	@Subscribe(ignoreCancelledEvents = true)
	public void BeforeChangeStatListener(ChangeStat.Before event) {
		//System.out.println(event.entity);
		StatHP statHP = mStatHP.get(event.entity);
		//System.out.println(statHP);
	}

	@Subscribe(ignoreCancelledEvents = true)
	public void DuringChangeStatListener(ChangeStat.During event) {
		StatHP statHP = mStatHP.get(event.entity);
		//FFMain.sendMessage("OW!");
		statHP.changeValue(event.amount);
	}

	@Subscribe
	public void AfterChangeStatListener(ChangeStat.After event) {
		StatHP statHP = mStatHP.get(event.entity);
		//System.out.println(statHP.currentValue);
		if (statHP.currentValue < 1) {
			/*HPAtZero.Before before = new HPAtZero.Before(event.entity);
			es.dispatch(before);
			if (!before.isCancelled()) {
				HPAtZero.During during = new HPAtZero.During(event.entity);
				es.dispatch(during);
				if (!during.isCancelled()) {
					HPAtZero.After after = new HPAtZero.After(event.entity);
					es.dispatch(after);
				}
			}*/
			HPAtZero.run(event.entity);
		}
	}
	
	@Subscribe(ignoreCancelledEvents=true)
	public void BeforeHPAtZero(HPAtZero.Before event) {
		//I guess like a zelda fairy would kick in around here
	}
	
	@Subscribe(ignoreCancelledEvents=true)
	public void DuringHPAtZero(HPAtZero.During event) {
		
	}
	
	@Subscribe
	public void AfterHPAtZero(HPAtZero.After event) {
		//TRIGGER DEATH!!
		es.dispatch(new ActorDied(event.entity));
		//FFMain.sendMessage("GAAAAAAAAAAHH!!!!");
		//FFMain.worldManager.world.delete(event.entity);
	}
}

/*
 * ChangeStat.Before before = new ChangeStat.Before("HP", e, -1);
 * es.dispatch(before); if (!before.isCancelled()) { ChangeStat.During during =
 * new ChangeStat.During("HP", e, -1); es.dispatch(during); if
 * (!during.isCancelled()) { ChangeStat.After after = new ChangeStat.After("HP",
 * e, -1); es.dispatch(after); } }
 */