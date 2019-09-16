package frogmodaiGame.behaviors;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.artemis.ComponentMapper;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import frogmodaiGame.*;
import frogmodaiGame.commands.*;
import frogmodaiGame.components.*;

public enum TouchedBehaviors {
	PlayerTouched(new BiConsumer<Integer, Integer>() {
		ComponentMapper<Tile> mTile;
		ComponentMapper<ChunkAddress> mChunkAddress;
		ComponentMapper<Description> mDescription;
		ComponentMapper<Position> mPosition;

		@Override
		public void accept(Integer e, Integer toucher) {

		}
	}),

	GoblinTouched(new BiConsumer<Integer, Integer>() {
		ComponentMapper<Tile> mTile;
		ComponentMapper<ChunkAddress> mChunkAddress;
		ComponentMapper<Description> mDescription;
		ComponentMapper<Position> mPosition;

		@Override
		public void accept(Integer e, Integer toucher) {
			if (toucher == FFMain.playerID) {
				FFMain.sendMessage("Hey buddy");
			}
		}
	});

	public BiConsumer<Integer, Integer> act;

	TouchedBehaviors(BiConsumer<Integer, Integer> _act) {
		act = _act;
	}
}
