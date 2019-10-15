package frogmodaiGame.systems;

import java.util.ArrayList;
import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.annotations.EntityId;
import com.artemis.systems.IteratingSystem;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenBuffer;

import frogmodaiGame.Chunk;
import frogmodaiGame.FFMain;
import frogmodaiGame.components.*;
import frogmodaiGame.events.CameraShift;
import net.mostlyoriginal.api.event.common.Subscribe;

public class TileRenderingSystem extends IteratingSystem { // This is for terrain only
	ComponentMapper<Tile> mTile;
	ComponentMapper<Position> mPosition;
	ComponentMapper<Char> mChar;
	ComponentMapper<ChunkAddress> mChunkAddress;
	ComponentMapper<CameraWindow> mCameraWindow;
	ComponentMapper<Sight> mSight;
	ComponentMapper<IsItem> mIsItem;
	ComponentMapper<TimedActor> mTimedActor;
	ComponentMapper<IsDead> mIsDead;

	Screen screen;
	@EntityId
	public int perspective = -1;
	boolean fullRedraw = false;
	TextCharacter emptyCharacter;
	double PI = 3.14159265;
	
	public boolean drewThisFrame = false;
	
	ScreenBuffer buffer;

	public TileRenderingSystem(Screen _screen) { // Matches camera, not tiles, for performance
		super(Aspect.all(Position.class, CameraWindow.class));
		screen = _screen;
		System.out.printf("%d, %d\n", FFMain.screenWidth/2, FFMain.screenHeight);
		buffer = new ScreenBuffer(new TerminalSize(FFMain.screenWidth/2, FFMain.screenHeight), new TextCharacter(' ', TextColor.ANSI.BLACK, TextColor.ANSI.BLACK));
		//emptyCharacter = new TextCharacter('X', TextColor.ANSI.YELLOW, TextColor.ANSI.BLUE);
		emptyCharacter = new TextCharacter(' ', TextColor.ANSI.BLACK, TextColor.ANSI.BLACK);
	}
	
	public void triggerRedraw() {
		fullRedraw = true;
	}

	@Override
	protected void process(int e) { //this happens with high frequency
		drewThisFrame = false;
		Position camPos = mPosition.create(e);
		CameraWindow camWindow = mCameraWindow.create(e);
		if (camWindow.focus == -1)
			return;
		Position focusPos = mPosition.create(camWindow.focus); //It's possible for focusPos to be from a different chunk than "chunk"
		Chunk chunk = FFMain.worldManager.getActiveChunk(); //getActiveChunk() sometimes returns a chunk the player is not in
		Sight sight = mSight.create(perspective);

		HashMap<String, RelativePosition> vision = new HashMap<String, RelativePosition>();
		RelativePosition start = new RelativePosition();
		start.x = focusPos.x;
		start.y = focusPos.y;
		start.e = chunk.getTile(focusPos.x, focusPos.y);
		chunk.floodGrab(focusPos, start, sight.distance, start.e, vision);

		// This new method is also not the way
		// This grabs all tiles that could be seen
		// But unseen tiles in the chunk as determined
		// by the camera window should be drawn underneath
		// Also need to remember to accomodate for when perspective is -1
		// TODO: test by making a small chunk that is a torus
		// TODO: change the movement system to be based on local tile neighbors
		// TODO: By the palyer's hanging inTile reference, check if they've moved out of
		// the active chunk, if so, SWTICH active chunk
		
		if (fullRedraw) {
			clearBuffer();

			//memoryDraw(camPos, camWindow);
			//drawLocal(vision, sight.distance, camPos, camWindow);
			olddrawLocal(vision, camPos, camWindow);
			fullRedraw = false;
			drewThisFrame = true;
		}
		
		TextGraphics tg = screen.newTextGraphics();
		tg.fillRectangle(new TerminalPosition(0,0), 
				new TerminalSize(FFMain.screenWidth/2, FFMain.screenHeight), emptyCharacter);
		tg.drawImage(new TerminalPosition(0, 0), buffer);
			
		//FFMain.worldManager.mapLoader.drawMap();

	}
	
	@Subscribe
	public void CameraShiftListener(CameraShift event) {
		//FFMain.sendMessage(event.dx + ", " + event.dy);
	}
	
	private void clearBuffer() {
		buffer.newTextGraphics().fillRectangle(new TerminalPosition(0, 0), buffer.getSize(), emptyCharacter);
	}
	
	private void drawLocal(HashMap<String, RelativePosition> vision, int viewDistance, Position camPos, CameraWindow camWindow) {
		//Right now this just goes through ALL the tiles grabbed a relative process
		//So no more relativeness SHOULD NEED TO BE DONE!!!
		//Instead, we should do a traditional LOS kinda
		//but change vision to be a hashmap of tile IDs indexed by relative positions (which include ID anyways)
		//Then have a loop through the edge of a circle of radius=sight.distance
		//and it should draw every character along the way, from center out
		//to avoid retracing rays.
		//Do findLine, 
		
		Position playerPos = mPosition.create(perspective);
		Sight sight = mSight.create(perspective);
		ChunkAddress playerChunkAddress = mChunkAddress.create(perspective);
		Chunk playerChunk = FFMain.worldManager.getChunk(playerChunkAddress.worldID);
		
		ArrayList<String> circle = new ArrayList<String>();
		int angleTicks = 100;
		for (int angle=0; angle<angleTicks; angle++) {
			int x = (int) (viewDistance * Math.cos((angle*1.0/angleTicks)*PI*2));
			int y = (int) (viewDistance * Math.sin((angle*1.0/angleTicks)*PI*2));
			//x and y are circular offsets from the player's position
			//Check if this point in the circle has been looked at already
			if (!circle.contains(x+"|"+y)) {
				circle.add(x+"|"+y);
				
				//&& vision.containsKey((x+playerPos.x)+"|"+(y+playerPos.y))
				
				Position screenPos = new Position();
				screenPos.x = x - camPos.x;
				screenPos.y = y - camPos.y;
				
				//Start is player position, end is player position and the circular offset
				FFMain.worldManager.LOS(playerChunk, playerPos.x, playerPos.y, x+playerPos.x, y+playerPos.y, camPos.x, camPos.y, buffer);
			}
		}
		
		//To fill in gaps
		/*for (int angle=0; angle<angleTicks; angle++) {
			int x = (int) (viewDistance-1 * Math.cos((angle*1.0/angleTicks)*PI*2));
			int y = (int) (viewDistance-1 * Math.sin((angle*1.0/angleTicks)*PI*2));
			//x and y are circular offsets from the player's position
			//Check if this point in the circle has been looked at already
			if (!circle.contains(x+"|"+y)) {
				circle.add(x+"|"+y);
				
				//&& vision.containsKey((x+playerPos.x)+"|"+(y+playerPos.y))
				
				Position screenPos = new Position();
				screenPos.x = x - camPos.x;
				screenPos.y = y - camPos.y;
				
				//Start is player position, end is player position and the circular offset
				FFMain.worldManager.LOS(playerChunk, playerPos.x, playerPos.y, x+playerPos.x, y+playerPos.y, camPos.x, camPos.y, buffer);
			}
		}*/
		
		for (RelativePosition rel : vision.values()) {
			Tile tile = mTile.create(rel.e);
			Char character = mChar.create(rel.e);
			Position screenPos = new Position();
			screenPos.x = rel.x - camPos.x;
			screenPos.y = rel.y - camPos.y;
			if (tile.seen) {
				drawEntity(screenPos, tile, character);
			}
		}
	}

	private void olddrawLocal(HashMap<String, RelativePosition> vision, Position camPos, CameraWindow camWindow) {
		//Right now this just goes through ALL the tiles grabbed a relative process
		//So no more relativeness SHOULD NEED TO BE DONE!!!
		//Instead, we should do a traditional LOS kinda
		//but change vision to be a hashmap of tile IDs indexed by relative positions (which include ID anyways)
		//Then have a loop through the edge of a circle of radius=sight.distance
		//and it should draw every character along the way, from center out
		//to avoid retracing rays.
		//Do findLine, 
		for (RelativePosition rel : vision.values()) {
			int t = rel.e;
			Position pos = new Position();
			pos.x = rel.x;
			pos.y = rel.y;
			// int localX = rel.x;
			// int localY = rel.y
			// Position pos = mPosition.create(t); //TODO: RELATIVE POSITIONS???
			Char character = mChar.create(t);
			ChunkAddress chunkAddress = mChunkAddress.create(t);
			Chunk chunk = FFMain.worldManager.getChunk(chunkAddress.worldID);
			Tile tile = mTile.create(t);
			
			//if (fullRedraw) tile.cachedLOS = false;

			// if (chunkAddress.worldID == FFMain.worldManager.activeChunk) {
			Position screenPos = new Position();
			screenPos.x = pos.x - camPos.x;
			screenPos.y = pos.y - camPos.y;
			if (screenPos.x >= 0 && screenPos.y >= 0 && screenPos.x < camWindow.width
					&& screenPos.y < camWindow.height) { // If this tile is on screen
				if (perspective == -1) {
					if (!drawEntity(screenPos, tile, character))
						buffer.setCharacterAt(screenPos.x, screenPos.y, character.getTextCharacter());
				} else {
					Position playerPos = mPosition.create(perspective);
					Sight sight = mSight.create(perspective);
					ChunkAddress playerChunkAddress = mChunkAddress.create(perspective);
					Chunk playerChunk = FFMain.worldManager.getChunk(playerChunkAddress.worldID);
					// if (true) {//pos.withinDistance(playerPos, sight.distance) ) {
					// && FFMain.worldManager.getActiveChunk().LOS(playerPos.x, playerPos.y, pos.x,
					// pos.y)) {
					//TODO: this is significantly broken
					//RESOLUTION(?): parameter "start" was the chunk of the destination tile, not the player's chunk.
					//if (playerChunkAddress.worldID != chunkAddress.worldID) continue;
					//FFMain.worldManager.LOS(playerChunk, playerPos.x, playerPos.y, pos.x, pos.y, buffer);
					if (FFMain.worldManager.badLOS(playerChunk, playerPos.x, playerPos.y, pos.x, pos.y)) { //TODO: CROSSING CHUNKS IS FUCKING BROKEN
						if (!drawEntity(screenPos, tile, character)) {
							buffer.setCharacterAt(screenPos.x, screenPos.y, character.getTextCharacter());
						}
						tile.seen = true;
						//tile.cachedLOS = true;
					}
					// } //Don't draw anything not in your line of sight
				}
			}
			// }
		}
		fullRedraw = false;
	}

	// @Override
	private void memoryDraw(Position camPos, CameraWindow camWindow) {
		for (int y = 0; y < camWindow.height; y++) {
			for (int x = 0; x < camWindow.width; x++) {
				int tx = camPos.x + x;
				int ty = camPos.y + y;

				// For all spots in the camera window,
				// Check the ACTIVE CHUNK for a tile.

				Chunk chunk = FFMain.worldManager.getActiveChunk();
				int i = tx + ty * chunk.width;
				if (tx < 0 || ty < 0 || tx >= chunk.width || ty >= chunk.height) {
					buffer.setCharacterAt(x, y, emptyCharacter);
					continue;
				}
				int t = chunk.tiles[i];

				Position pos = mPosition.create(t);
				Position screenPos = new Position();
				screenPos.x = pos.x - camPos.x;
				screenPos.y = pos.y - camPos.y;
				Char character = mChar.create(t);
				ChunkAddress chunkAddress = mChunkAddress.create(t);
				Tile tile = mTile.create(t);

				Position playerPos = mPosition.create(perspective);
				Sight sight = mSight.create(perspective);

				if (pos.withinDistance(playerPos, sight.distance))
					continue;

				if (chunkAddress.worldID == FFMain.worldManager.activeChunk && FFMain.cameraID != -1) {

					if (screenPos.x >= 0 && screenPos.y >= 0 && screenPos.x < camWindow.width
							&& screenPos.y < camWindow.height) {
						if (perspective == -1) { // If no perspective, draw everything as-is
							if (!drawEntity(screenPos, tile, character))
								buffer.setCharacterAt(screenPos.x, screenPos.y, character.getTextCharacter());
						} else {
							if (tile.seen) { // Draw memory
								buffer.setCharacterAt(screenPos.x, screenPos.y, character.getTextCharacter(8, 0, false));
							} else { // Black out unknown areas
								buffer.setCharacterAt(screenPos.x, screenPos.y, character.getTextCharacter(0, 0, false));
							}
						}
					}
				}

				// TODO: render neighboring chunks
			}
		}
	}

	private boolean drawEntity(Position pos, Tile tile, Char tileChar) {
		if (tile.entitiesHere.size() == 0)
			return false;

		int winner = -1;
		for (int e : tile.entitiesHere) {
			if (mChar.has(e) && !mIsDead.has(e)) { // don't bother with anything that doesn't draw a character
				if (winner == -1)
					winner = e;
				else {
					if (mIsItem.has(e) && !mIsItem.has(winner) && !mTimedActor.has(winner))
						winner = e;
					if (mTimedActor.has(e) && !mTimedActor.has(winner))
						winner = e;
				}
			}
		}

		if (winner == -1)
			return false;
		
		//System.out.println(winner + ", " + mChar.get(winner).character + ", " + mIsDead.has(winner));

		TextCharacter ct = mChar.get(winner).getTextCharacter();
		buffer.setCharacterAt(pos.x, pos.y, ct.withBackgroundColor(TextColor.ANSI.values()[tileChar.bgc]));

		return true;
	}
}
