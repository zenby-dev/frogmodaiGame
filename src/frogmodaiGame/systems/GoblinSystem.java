package frogmodaiGame.systems;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.artemis.Aspect;
import com.artemis.Aspect.Builder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import frogmodaiGame.FFMain;
import frogmodaiGame.components.*;
import frogmodaiGame.events.ActorDied;
import frogmodaiGame.events.ActorTakeTurn;
import frogmodaiGame.events.ChangeStat;
import frogmodaiGame.events.HPAtZero;
import frogmodaiGame.events.MoveAttempt;
import frogmodaiGame.events.ScreenRefreshRequest;
import frogmodaiGame.events.TryToHit;
import frogmodaiGame.events.HPAtZero.After;
import frogmodaiGame.events.HPAtZero.During;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;

public class GoblinSystem extends BaseSystem {

	ComponentMapper<IsGoblin> mIsGoblin;
	ComponentMapper<IsFaction> mIsFaction;
	ComponentMapper<TimedActor> mTimedActor;

	EventSystem es;

	public GoblinSystem() {
		super(); // ??

	}

	@Override
	protected void processSystem() {

	}

	@Subscribe(ignoreCancelledEvents=true)
	public void TryToHitDuringListener(TryToHit.During event) {
		if (!mIsGoblin.has(event.target))
			return;
		
		FFMain.sendMessage("The goblin squeals as you punch it!");
		
		ChangeStat.run("HP", event.target, -1);
	}
	
	@Subscribe
	public void ActorTakeTurnListener(ActorTakeTurn event) {
		if (!mIsGoblin.has(event.entity))
			return;
		
		KeyStroke keystroke = FFMain.keystroke;
		int e = event.entity;
		TimedActor timedActor = mTimedActor.create(e);
		int MOVE_COST = -1; //The default is to wait another cycle
		//Until pre-requisites are met
		//TODO: Other actors having limited number of cycles to make a decision???

		// virtualController.moveX = 0.0f;
		// virtualController.moveY = 0.0f;

		boolean moving = true;
		int r = FFMain.random.nextInt(9);
		if (r == 0)
			MoveAttempt.run(e, 1, 0);
			//virtualController.addAction(new MoveCommand(1, 0));
		else if (r == 1)
			MoveAttempt.run(e, -1, 0);
			//virtualController.addAction(new MoveCommand(-1, 0));
		else if (r == 2)
			MoveAttempt.run(e, 0, -1);
			//virtualController.addAction(new MoveCommand(0, -1));
		else if (r == 3)
			MoveAttempt.run(e, 0, 1);
			//virtualController.addAction(new MoveCommand(0, 1));
		else if (r == 4) {
			MoveAttempt.run(e, -1, 1);
			//virtualController.addAction(new MoveCommand(-1, 1));
		} else if (r == 5) {
			MoveAttempt.run(e, 1, 1);
			//virtualController.addAction(new MoveCommand(1, 1));
		} else if (r == 6) {
			MoveAttempt.run(e, -1, -1);
			//virtualController.addAction(new MoveCommand(-1, -1));
		} else if (r == 7) {
			MoveAttempt.run(e, 1, -1);
			//virtualController.addAction(new MoveCommand(1, -1));
		} else {
			moving = false;
			MOVE_COST = 1;
		}
		if (moving) {
			MOVE_COST = (int) (timedActor.speed * 1.0f); // goblin should move ridiculously fast
		}

		event.actionCost += MOVE_COST;
		event.passing = false;
	}

}