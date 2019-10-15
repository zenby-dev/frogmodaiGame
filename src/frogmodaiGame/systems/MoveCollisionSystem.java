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
import frogmodaiGame.events.MoveCollision;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;

public class MoveCollisionSystem extends BaseSystem {
	
	ComponentMapper<IsDead> mIsDead;
	ComponentMapper<Tile> mTile;
	ComponentMapper<IsPlayer> mIsPlayer;
	ComponentMapper<OnTouch> mOnTouch;
	ComponentMapper<OnTouched> mOnTouched;

	EventSystem es;

	public MoveCollisionSystem() {
		super(); // ??

	}

	@Override
	protected void processSystem() {

	}
	
	@Subscribe
	public void MoveCollisionListener(MoveCollision event) {
		int e = event.entity;
		int neighbor = event.neighborTile;
		//System.out.println(e + ", " + neighbor);
		
		OnTouch onTouch = mOnTouch.create(e);
		onTouch.act.accept(e, neighbor);

		Tile tile = mTile.create(neighbor);
		if (tile.entitiesHere.size() > 0) {
			for (int o : tile.entitiesHere) {
				if (mOnTouched.has(o)) {
					OnTouched onTouched = mOnTouched.create(o);
					onTouched.act.accept(o, e);
				}
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