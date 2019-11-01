package frogmodaiGame.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import frogmodaiGame.Chunk;
import frogmodaiGame.FFMain;
import frogmodaiGame.components.ChunkAddress;
import frogmodaiGame.components.IsInContainer;
import frogmodaiGame.components.Position;
import frogmodaiGame.components.Tile;
import frogmodaiGame.events.TriggerTileOccupation;
import frogmodaiGame.events.TurnCycle;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;

public class TileOccupationClearingSystem extends IteratingSystem {
	ComponentMapper<Tile> mTile;
	
	boolean needsProcessing = false;
	
	EventSystem es;
	
	//This system helps keep a handy list of entities refreshed on each tile
	
	public TileOccupationClearingSystem() {
		super(Aspect.all(Tile.class));
	}
	
	@Override
	protected boolean checkProcessing() {
		//System.out.println("checkProcessing() " + needsProcessing);
		return needsProcessing;
	}

	@Override
	protected void process(int e) {
		Tile tile = mTile.get(e);
		tile.entitiesHere.clear();
		tile.occupied = false;
	}
	
	@Override
	protected void end() {
		//System.out.println("end() " + needsProcessing);
		needsProcessing = false;
		
		es.dispatch(new TriggerTileOccupation());
	}
	
	@Subscribe
	void TurnCycleAfterListener(TurnCycle.After event) {
		//System.out.println("Listener() " + needsProcessing);
		needsProcessing = true;
	}
}
