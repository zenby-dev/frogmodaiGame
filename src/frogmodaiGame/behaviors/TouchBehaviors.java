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
					
					//TODO: Check if this is something we should be doing some activation method on,
					//	or if it's something we should be hitting (faction system?)
					//TODO: Send an event to try to hit the goblin/object (that gives them a chance to dodge)
					
					//FFMain.sendMessage(desc.name + ": " + desc.getDescription());
					//FFMain.sendMessage("    This method does the reverse of getColumnIndex, given a String and imagining it has been printed out to the top-left corner of a terminal, in the column specified by columnIndex, what is the index of that character in the string.");
					//System.out.println(desc.name);
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
