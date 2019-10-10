package frogmodaiGame.behaviors;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.artemis.ComponentMapper;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import frogmodaiGame.*;
import frogmodaiGame.commands.*;
import frogmodaiGame.components.*;
import frogmodaiGame.events.ChangeStat;
import net.mostlyoriginal.api.event.common.EventSystem;

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
		ComponentMapper<StatHP> mStatHP;
		
		EventSystem es;

		@Override
		public void accept(Integer e, Integer toucher) {
			if (toucher == FFMain.playerID) {
				//FFMain.sendMessage("Hey buddy");
				Paragraph para = new Paragraph();
				para.add("This is what a ");
				para.add("keyword", TextColor.ANSI.RED);
				para.add(" should look like!");
				//FFMain.sendMessage(para);
				
				//System.out.println(mStatHP.has(e));
				/*ChangeStat.Before before = new ChangeStat.Before("HP", e, -1);
				es.dispatch(before);
				if (!before.isCancelled()) {
					ChangeStat.During during = new ChangeStat.During("HP", e, -1);
					es.dispatch(during);
					if (!during.isCancelled()) {
						ChangeStat.After after = new ChangeStat.After("HP", e, -1);
						es.dispatch(after);
					}
				}*/
				FFMain.worldManager.runEventSet(new ChangeStat.Before("HP", e, -1),
						new ChangeStat.During("HP", e, -1),
						new ChangeStat.After("HP", e, -1));
				//FFMain.sendMessage("    This method does the reverse of getColumnIndex, given a String and imagining it has been printed out to the top-left corner of a terminal, in the column specified by columnIndex, what is the index of that character in the string.");
			}
		}
	});

	public BiConsumer<Integer, Integer> act;

	TouchedBehaviors(BiConsumer<Integer, Integer> _act) {
		act = _act;
	}
}
