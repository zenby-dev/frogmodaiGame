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
import frogmodaiGame.events.HPAtZero.After;
import frogmodaiGame.events.HPAtZero.During;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;

public class DeathSystem2 extends IteratingSystem {

	//Runs after everything else to delete dead creatures.
	
	ComponentMapper<Char> mChar;
	ComponentMapper<IsDead> mIsDead;
	
	EventSystem es;

	public DeathSystem2() {
		super(Aspect.all(IsDead.class)); // ??

	}

	@Override
	protected void process(int e) {
		//if (mIsDead.has(e)) {
		//	System.out.println(mChar.get(e).character);
		//}
		FFMain.worldManager.world.delete(e);
	}
}