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

public class TileOccupationClearingSystem extends IteratingSystem {
	ComponentMapper<Tile> mTile;
	
	//This system helps keep a handy list of entities refreshed on each tile
	
	public TileOccupationClearingSystem() {
		super(Aspect.all(Tile.class));
	}

	@Override
	protected void process(int e) {
		Tile tile = mTile.create(e);
		tile.entitiesHere.clear();
		tile.occupied = false;
	}
}
