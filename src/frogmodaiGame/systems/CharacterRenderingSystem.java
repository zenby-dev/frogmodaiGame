package frogmodaiGame.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;

import frogmodaiGame.*;
import frogmodaiGame.components.*;

public class CharacterRenderingSystem extends IteratingSystem { // Pretty loose dependencies
	// Should run after items and other things are drawn, so that this includes
	// characters and misc. stuff left behind
	ComponentMapper<Position> mPosition;
	ComponentMapper<Char> mChar;
	ComponentMapper<ChunkAddress> mChunkAddress;
	ComponentMapper<CameraWindow> mCameraWindow;
	ComponentMapper<Sight> mSight;
	ComponentMapper<Tile> mTile;

	//static final int PLAYER_VIEW_DISTANCE = 10; // TODO: MAKE THIS DYNAMIC
	public int perspective = -1;

	Screen screen;

	public CharacterRenderingSystem(Screen _screen) {
		super(Aspect.all(Position.class, Char.class, ChunkAddress.class).exclude(Tile.class, IsItem.class,
				IsInContainer.class));
		// Include anything that has a position and char,
		// and exclude anything that has a Tile/Terrain component
		screen = _screen;
	}

	@Override
	protected void process(int e) {
		Position pos = mPosition.create(e);
		Char character = mChar.create(e);
		ChunkAddress chunkAddress = mChunkAddress.create(e);
		Chunk chunk = FFMain.worldManager.getChunk(chunkAddress.worldID);

		Position camPos = mPosition.create(FFMain.cameraID);
		CameraWindow camWindow = mCameraWindow.create(FFMain.cameraID);

		if (chunkAddress.worldID == FFMain.worldManager.activeChunk) {
			Position screenPos = new Position();
			screenPos.x = pos.x - camPos.x;
			screenPos.y = pos.y - camPos.y;
			if (screenPos.x >= 0 && screenPos.y >= 0 && screenPos.x < camWindow.width && screenPos.y < camWindow.height) {
				//Only draw inside the camera window
				//if (FFMain.playerID == -1 || FFMain.playerID == e) { // rendering without a player...
				System.out.println("HELP");
				if (perspective == -1) {
					screen.setCharacter(screenPos.x, screenPos.y, character.getTextCharacter());
				} else {
					Position playerPos = mPosition.create(perspective);
					Sight sight = mSight.create(perspective);
					if (pos.withinDistance(playerPos, sight.distance)) {
						if (FFMain.worldManager.badLOS(chunk, playerPos.x, playerPos.y, pos.x, pos.y)) {
							TextCharacter ct = character.getTextCharacter();
							int tile = chunk.getTile(playerPos.x, playerPos.y);
							Char tileChar = mChar.create(tile);
							screen.setCharacter(screenPos.x, screenPos.y, ct.withBackgroundColor(TextColor.ANSI.values()[tileChar.bgc]));
						}
					}
				}
			}
		}
	}
}
