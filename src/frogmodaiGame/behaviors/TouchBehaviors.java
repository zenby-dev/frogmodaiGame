package frogmodaiGame.behaviors;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.artemis.ComponentMapper;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import frogmodaiGame.*;
import frogmodaiGame.commands.*;
import frogmodaiGame.components.*;

public enum TouchBehaviors {
	PlayerTouch(new BiConsumer<Integer, Integer>() {
		ComponentMapper<Tile> mTile;
		ComponentMapper<ChunkAddress> mChunkAddress;
		ComponentMapper<Description> mDescription;
		ComponentMapper<Position> mPosition;
		
		@Override
		public void accept(Integer e, Integer neighbor) {
			Tile tile = mTile.create(neighbor);
			Position tilePos = mPosition.create(neighbor);
			Position ePos = mPosition.create(e);
			//System.out.println(String.format("%d,%d %d,%d", tilePos.x, tilePos.y, ePos.x, ePos.y));
			if (tile.entitiesHere.size() > 0) {
				for (int o : tile.entitiesHere) {
					Description desc = mDescription.create(o);
					System.out.println(desc.name);
					//FFMain.worldManager.world.delete(o);
				}
			}
		}
	}),
	
	GoblinTouch(new BiConsumer<Integer, Integer>() {
		@Override
		public void accept(Integer e, Integer neighbor) {

		}
	});
	
	public BiConsumer<Integer, Integer> act;

	TouchBehaviors(BiConsumer<Integer, Integer> _act) {
		act = _act;
	}
}
