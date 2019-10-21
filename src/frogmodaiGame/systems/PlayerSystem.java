package frogmodaiGame.systems;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.artemis.Aspect;
import com.artemis.Aspect.Builder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import frogmodaiGame.Chunk;
import frogmodaiGame.FFMain;
import frogmodaiGame.commands.DropCommand;
import frogmodaiGame.commands.PickupCommand;
import frogmodaiGame.components.*;
import frogmodaiGame.events.ActorDied;
import frogmodaiGame.events.ActorTakeTurn;
import frogmodaiGame.events.ChangeStat;
import frogmodaiGame.events.HPAtZero;
import frogmodaiGame.events.MoveAttempt;
import frogmodaiGame.events.PostTileRendering;
import frogmodaiGame.events.ScreenRefreshRequest;
import frogmodaiGame.events.TryToHit;
import frogmodaiGame.events.HPAtZero.After;
import frogmodaiGame.events.HPAtZero.During;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;

public class PlayerSystem extends BaseSystem {

	ComponentMapper<IsPlayer> mIsPlayer;
	ComponentMapper<IsFaction> mIsFaction;
	ComponentMapper<TimedActor> mTimedActor;
	ComponentMapper<ChunkAddress> mChunkAddress;
	ComponentMapper<Position> mPosition;

	EventSystem es;

	public PlayerSystem() {
		super(); // ??

	}

	@Override
	protected void processSystem() {
		
	}
	
	public void astarTest() {
		int _player = FFMain.playerID;
		ChunkAddress ca = mChunkAddress.get(_player);
		Chunk chunk = FFMain.worldManager.getChunk(ca.worldID);
		Position pos = mPosition.get(_player);
		ArrayList<Integer> path = chunk.findPath(pos.x, pos.y, 5, 5);
		
		Screen screen = FFMain.screen;
		Position camPos = mPosition.get(FFMain.cameraID);
		
		if (path != null && camPos != null) {
			for (int e : path) {
				Position tilePos = mPosition.get(e);
				int screenX = tilePos.x - camPos.x;
				int screenY = tilePos.y - camPos.y;
				//System.out.println(screenX + " " + screenY);
				screen.setCharacter(screenX, screenY, new TextCharacter('X', TextColor.ANSI.YELLOW, TextColor.ANSI.BLUE));
			}
		}
	}
	
	@Subscribe 
	public void PostTileRenderingListener(PostTileRendering event) {
		astarTest();
	}
	
	@Subscribe
	public void ActorTakeTurnListener(ActorTakeTurn event) {
		if (!mIsPlayer.has(event.entity))
			return;
		
		KeyStroke keystroke = FFMain.keystroke;
		int e = event.entity;
		TimedActor timedActor = mTimedActor.create(e);
		int MOVE_COST = -1; //The default is to wait another cycle
		//Until pre-requisites are met
		//TODO: Other actors having limited number of cycles to make a decision???

		// virtualController.moveX = 0.0f;
		// virtualController.moveY = 0.0f;

		if (keystroke != null) {
			KeyType keytype = keystroke.getKeyType();
			boolean moving = true;
			if (keytype == KeyType.ArrowRight)
				MoveAttempt.run(e, 1, 0);
				//virtualController.addAction(new MoveCommand(1, 0));
			else if (keytype == KeyType.ArrowLeft)
				MoveAttempt.run(e, -1, 0);
				//virtualController.addAction(new MoveCommand(-1, 0));
			else if (keytype == KeyType.ArrowUp)
				MoveAttempt.run(e, 0, -1);
				//virtualController.addAction(new MoveCommand(0, -1));
			else if (keytype == KeyType.ArrowDown)
				MoveAttempt.run(e, 0, 1);
				//virtualController.addAction(new MoveCommand(0, 1));
			else if (keytype == KeyType.Character && keystroke.getCharacter() == '1') {
				MoveAttempt.run(e, -1, 1);
				//virtualController.addAction(new MoveCommand(-1, 1));
			} else if (keytype == KeyType.Character && keystroke.getCharacter() == '3') {
				MoveAttempt.run(e, 1, 1);
				//virtualController.addAction(new MoveCommand(1, 1));
			} else if (keytype == KeyType.Character && keystroke.getCharacter() == '7') {
				MoveAttempt.run(e, -1, -1);
				//virtualController.addAction(new MoveCommand(-1, -1));
			} else if (keytype == KeyType.Character && keystroke.getCharacter() == '9') {
				MoveAttempt.run(e, 1, -1);
				//virtualController.addAction(new MoveCommand(1, -1));
			} else {
				moving = false;
			}
			if (moving) {
				MOVE_COST = (int) (timedActor.speed * 1.0f); // moving should take a majority of your energy
				//I guess just send a draw request every time the player's turn ends???
				es.dispatch(new ScreenRefreshRequest());
			}
			
			if (keytype == KeyType.Character) {
				char c = keystroke.getCharacter();
				if (c == 'p') {
					//virtualController.addAction(new PickupCommand());
					MOVE_COST = (int) (timedActor.speed * -0.1f);
					//Pause to open menu
					//Except I don't want to add the menu yet, just checking the local tile for objects
				}
				if (c == 'd') {
					//virtualController.addAction(new DropCommand());
					MOVE_COST = (int) (timedActor.speed * -0.1f);
					//Pause to open menu
					//Except I don't want to add the menu yet, just checking the local tile for objects
				}
			}
			
			FFMain.keystroke = null; // KEYSTROKES SHOULD NOT COUNT MORE THAN ONCE
		}

		event.actionCost += MOVE_COST;
		event.passing = false;
	}

	@Subscribe
	public void OnTouchListener(frogmodaiGame.events.OnTouch event) {
		//System.out.println(event.entity + ", " + mIsPlayer.has(event.entity) + ", " + event.neighbor + ", " + mIsPlayer.has(event.neighbor));
		if (!mIsPlayer.has(event.entity))
			return;
		
		if (mIsFaction.has(event.neighbor)) {
			IsFaction faction = mIsFaction.get(event.neighbor);
			if (faction.name.equals("MONSTERS")) {
				// falcon PUNCH (try to hit)
				TryToHit.run(event.entity, event.neighbor);
			}
		}
	}

}

/*
 * ChangeStat.Before before = new ChangeStat.Before("HP", e, -1);
 * es.dispatch(before); if (!before.isCancelled()) { ChangeStat.During during =
 * new ChangeStat.During("HP", e, -1); es.dispatch(during); if
 * (!during.isCancelled()) { ChangeStat.After after = new ChangeStat.After("HP",
 * e, -1); es.dispatch(after); } }
 */