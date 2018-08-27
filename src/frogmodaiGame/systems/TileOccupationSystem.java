package frogmodaiGame.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Aspect.Builder;
import com.artemis.systems.IteratingSystem;

import frogmodaiGame.*;
import frogmodaiGame.components.*;

public class TileOccupationSystem extends IteratingSystem {
	ComponentMapper<Position> mPosition;
	ComponentMapper<ChunkAddress> mChunkAddress;
	ComponentMapper<Tile> mTile;
	ComponentMapper<TimedActor> mTimedActor;
	
	//This system helps keep a handy list of entities refreshed on each tile
	
	public TileOccupationSystem() {
		super(Aspect.all(Position.class, ChunkAddress.class).exclude(IsInContainer.class, Tile.class));
	}

	@Override
	protected void process(int e) {
		Position pos = mPosition.create(e);
		ChunkAddress chunkAddress = mChunkAddress.create(e);
		Chunk chunk = FFMain.worldManager.getChunk(chunkAddress.worldID);
		chunk.entityOnTile(pos.x, pos.y, e);
		int t = chunk.getTile(pos.x, pos.y); //THIS SHIT IS BROKEN. OW. Chunk attaching is issue.
		if (t == -1) System.out.println(String.format("%d, %d", pos.x, pos.y));
		if (mTimedActor.has(e) && t != -1) {
			Tile tile = mTile.create(t);
			tile.occupied = true;
		}
	}
}
