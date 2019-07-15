package frogmodaiGame;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.*;

import com.artemis.*;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import frogmodaiGame.components.*;
import frogmodaiGame.generators.*;
import frogmodaiGame.systems.*;

public class WorldManager {
	ArrayList<Chunk> chunkList;
	public World world;
	CreatureBuilder creatureBuilder;
	ItemBuilder itemBuilder;
	public UIHelper uiHelper;
	public int activeChunk = -1;
	TileRenderingSystem tileRender;
	Screen screen;

	CaveGenerator caveGenerator;

	ComponentMapper<Tile> mTile;
	ComponentMapper<Char> mChar;
	ComponentMapper<Position> mPosition;
	ComponentMapper<ChunkAddress> mChunkAddress;

	public WorldManager(Screen _screen) {
		chunkList = new ArrayList<Chunk>();
		initWorld(_screen);
		creatureBuilder = new CreatureBuilder(world);
		itemBuilder = new ItemBuilder(world);
		uiHelper = new UIHelper(world);
		caveGenerator = new CaveGenerator(world);
	}

	void initWorld(Screen _screen) {
		WorldConfiguration config = new WorldConfigurationBuilder()
				// .dependsOn(MyPlugin.class)
				.with(
						new TimeSystem(), 
						new CharacterMovingSystem(), 
						new CameraMovingSystem(),
						new TileOccupationClearingSystem(), 
						new TileOccupationSystem(), 
						new PickupSystem(),
						new DropSystem(), 
						new ItemRelocatingSystem(), 
						new TileRenderingSystem(_screen),
						new PositionGhostSystem(_screen), 
						new SphereRenderingSystem(_screen))
				.build();
		world = new World(config);
		world.inject(this);
		screen = _screen;
	}

	public void start() {
		int chunk1 = createChunk(12, 12);
		int chunk2 = createChunk(12, 12);
		int chunk3 = createChunk(36, 12);
		setActiveChunk(chunk1);
		// generateTest();
		loadTestWorld(getChunk(chunk1));
		loadTest2(getChunk(chunk2));
		loadTest2(getChunk(chunk3));
		
		getChunk(chunk1).attach(getChunk(chunk2), 2, 0);
		getChunk(chunk1).attach(getChunk(chunk3), 0, 0);
		getChunk(chunk2).attach(getChunk(chunk1), 0, 0);
		getChunk(chunk2).attach(getChunk(chunk3), 2, 0);
		getChunk(chunk3).attach(getChunk(chunk2), 0, 0);
		getChunk(chunk3).attach(getChunk(chunk1), 2, 0);
		
		getChunk(chunk1).attach(getChunk(chunk2), 1, 0);
		getChunk(chunk2).attach(getChunk(chunk1), 3, 0);
		
		getChunk(chunk1).attach(getChunk(chunk2), 3, 0);
		getChunk(chunk2).attach(getChunk(chunk1), 1, 0);
		
		//creatureBuilder.sphere(64+32, 16, 16, 20.0f);
		
		//getChunk(chunk1).attachCorner(getChunk(chunk1), 0);
		//getChunk(chunk1).attachCorner(getChunk(chunk3), 1);
		//getChunk(chunk1).attachCorner(getChunk(chunk3), 2);
		//getChunk(chunk1).attachCorner(getChunk(chunk1), 3);
		
		//getChunk(chunk2).attachCorner(getChunk(chunk3), 0);
		//getChunk(chunk2).attachCorner(getChunk(chunk2), 1);
		//getChunk(chunk2).attachCorner(getChunk(chunk2), 2);
		//getChunk(chunk2).attachCorner(getChunk(chunk3), 3);
		
		//getChunk(chunk3).attachCorner(getChunk(chunk1), 0);
		//getChunk(chunk3).attachCorner(getChunk(chunk2), 1);
		//getChunk(chunk3).attachCorner(getChunk(chunk2), 2);
		//getChunk(chunk3).attachCorner(getChunk(chunk1), 3);
	}

	void queueEdit() { // proposes an edit to the map.
		// to resolve conflicts, edits should have priority? or should I move on to time
		// management
		// because if it's added and handled in order i can have it ignore edits for
		// tiles that
		// already have accepted proposals
	}

	void flushQueue() { // updates the map with all accepted proposed edits from last frame

	}

	void setRenderingPerspective(int e) {
		// world.getSystem(CharacterRenderingSystem.class).perspective = e;
		// world.getSystem(ItemRenderingSystem.class).perspective = e;
		world.getSystem(TileRenderingSystem.class).perspective = e;
	}

	int createChunk() {
		Chunk chunk = new Chunk();
		return addChunk(chunk);
	}

	int createChunk(int w, int h) {
		Chunk chunk = new Chunk(w, h, screen);
		return addChunk(chunk);
	}

	int addChunk(Chunk chunk) {
		chunk.worldID = chunkList.size();
		chunk.initTiles(world);
		// System.out.println(chunk.worldID);
		if (activeChunk == -1)
			activeChunk = chunk.worldID;
		chunkList.add(chunk);
		return chunk.worldID;
	}

	void setActiveChunk(int i) {
		if (i < 0 || i >= chunkList.size())
			return;
		activeChunk = i;
	}

	void setActiveChunk(Chunk chunk) {
		if (chunk.worldID < 0)
			return;
		activeChunk = chunk.worldID;
	}

	public void shiftChunks(Chunk newChunk) { //Does the "activeChunk != playerChunk" issue arise here?
		Chunk oldChunk = getActiveChunk();
		for (Chunk chunk : oldChunk.neighbors) {
			chunk.unload();
		}
		setActiveChunk(newChunk);
		newChunk.load();
		oldChunk.unload(); // idk this just seems right LOL
		for (Chunk chunk : newChunk.neighbors) {
			chunk.load();
		}
	}

	public Chunk getChunk(int i) {
		try {
			return chunkList.get(i);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Chunk getActiveChunk() {
		try {
			return chunkList.get(activeChunk);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}

	void process() {
		getActiveChunk().update();
		TimeSystem timeSystem = world.getSystem(TimeSystem.class);
		int actorsPerUpdate = timeSystem.getNumActors();
		for (int i = 0; i < actorsPerUpdate; i++) { // ACTORS PROPOSE ACTIONS
			if (!timeSystem.tick())
				break;
		}
		world.process();
	}

	void loadTestWorld(Chunk chunk) {
		Random r = new Random();
		int numRocks = 0;
		for (int i = 0; i < numRocks; i++) { // make boulders
			int q = r.nextInt(chunk.width * chunk.height);
			int e = chunk.tiles[q];
			Tile tile = mTile.create(e);
			Char character = mChar.create(e);
			tile.solid = true;
			character.character = '0';
			character.fgc = TextColor.ANSI.DEFAULT.ordinal();
		}

		creatureBuilder.player(chunk, 3, 3);
		setRenderingPerspective(FFMain.playerID);
		creatureBuilder.camera(FFMain.playerID, 0, 0, 64, 32, 14);
		int numGoblins = 3;
		for (int i = 0; i < numGoblins; i++) {
			creatureBuilder.goblin(chunk, r.nextInt(chunk.width), r.nextInt(chunk.height));
		}

		// itemBuilder.createTest(chunk, 12, 6);
		// itemBuilder.createTest(chunk, 12, 6);
		// itemBuilder.createTest(chunk, 12, 6);

		// creatureBuilder.sphere(64+32, 16);
	}
	
	void loadTest2(Chunk chunk) {
		chunk.setGroundColor(TextColor.ANSI.YELLOW.ordinal());
		Random r = new Random();
		int numRocks = 0;
		for (int i = 0; i < numRocks; i++) { // make boulders
			int q = r.nextInt(chunk.width * chunk.height);
			int e = chunk.tiles[q];
			Tile tile = mTile.create(e);
			Char character = mChar.create(e);
			tile.solid = true;
			character.character = '0';
			character.fgc = TextColor.ANSI.RED.ordinal();
		}

		int numGoblins = 3;
		for (int i = 0; i < numGoblins; i++) {
			creatureBuilder.goblin(chunk, r.nextInt(chunk.width), r.nextInt(chunk.height));
		}
		
		// itemBuilder.createTest(chunk, 12, 6);
	}

	void generateTest() {
		Chunk chunk = getActiveChunk();
		chunk = caveGenerator.generate(chunk);
		// int ghost = creatureBuilder.positionGhost(10, 10);
		// creatureBuilder.camera(ghost, 0, 0, 64, 32, 14);
	}

	public int[] findLine(int x0, int y0, int x1, int y1) {
		int length = (int) Math.ceil(Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1)));
		//System.out.println(length);
		if (length == 0) return null;
		int[] line = new int[(length) * 2 + 4]; // allocate extra space
		//for (int i = 0; i < line.length; i++)
		//	line[i] = -1;
		int c = 0;

		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);

		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;

		int err = dx - dy;
		int e2;

		while (true) { // TODO: adjust to tile.neighbor based
			if (c < line.length) {
				line[c] = x0;
				line[c+1] = y0;
				//System.out.println(String.format("%d,%d", x0, y0));
				c+=2;
				//if (c >= line.length) break;
			}
			// line.add(XYToi(x0, y0));

			if (x0 == x1 && y0 == y1)
				break;

			e2 = 2 * err;
			if (e2 > -dy) {
				err = err - dy;
				x0 = x0 + sx;
			}

			if (e2 < dx) {
				err = err + dx;
				y0 = y0 + sy;
			}
		}
		
		if (c < line.length) { //Mark points beyond end, if they exist (should there be a -1?)
			line[c] = -9999;
			line[c+1] = -9999;
		}
		//System.out.println("");
		return line;
	}

	//This function directly crawls through the tiles.neighbors array.
	public boolean LOS(Chunk start, int x0, int y0, int x1, int y1) {
		int t = start.getTile(x0, y0);
		int[] line = findLine(x0, y0, x1, y1);
		if (line == null) return true; //making this false makes the player unseeable
		//int lastX = x0;
		//int lastY = y0;
		//if (line.length <= 2) return true; //very short line
		//screen.setCharacter(x0, y0, new TextCharacter('@', TextColor.ANSI.CYAN, TextColor.ANSI.RED));
		for (int i = 2; i < line.length; i += 2) {
			int x = line[i];
			int y = line[i + 1];
			//if (x == -1 && y == -1) break; 
			Position d = new Position();
			d.x = x - line[i-2];
			d.y = y - line[i-1];
			
			//screen.setCharacter(x, y, new TextCharacter((char)((i/2)+48), TextColor.ANSI.RED, TextColor.ANSI.CYAN));
			//System.out.println(String.format("%d,%d %d,%d", x, y, d.x, d.y));
			//if (x == 0 && y == 0 && (Math.abs(d.x) > 1 || Math.abs(d.y) > 1)) return true; //passed end of the line
			if (x == -9999 && y == -9999) {
				//screen.setCharacter(line[i-2], line[i-1], new TextCharacter((char)((i/2)+48), TextColor.ANSI.RED, TextColor.ANSI.CYAN));
				return true; //if false, there diagonal directions cannot be seen
			}//doesn't affect tunnel issue
			
			
			
			Tile oldTile = mTile.create(t);
			ChunkAddress tileChunkAddress = mChunkAddress.create(t);
			
			//screen.setCharacter(line[i-2], line[i-1], new TextCharacter((char)((i/2)+48), TextColor.ANSI.GREEN, TextColor.ANSI.MAGENTA));

			int dir = DirectionConverter.toInt(d);
			//screen.setCharacter(x, y, new TextCharacter((char)((dir)+48), TextColor.ANSI.RED, TextColor.ANSI.CYAN));
			//There's an extra row present when the bleed is present!!!!
			
			//if (dir == -1) System.out.println(String.format("%d,%d %d,%d", x, y, d.x, d.y));
			if (dir == -1) return false; //TODO: why is this returning -1 ever?
			int neighbor = oldTile.neighbors[dir];
			if (neighbor == -1) return false; //THE BLEED IN THE TUNNEL
			t = neighbor;
			
			//screen.setCharacter(x, y, new TextCharacter((char)((dir)+48), TextColor.ANSI.RED, TextColor.ANSI.CYAN));
			
			
			Tile newTile = mTile.create(t);
			ChunkAddress newTileChunkAddress = mChunkAddress.create(t);
			
			
			
			//if (x != x0 && x != x1 && y != y0 && y != y1) { //don't check solidity of start/end tiles
			//if (!((x == x0 && y == y0) || (x == x1 && y == y1))) {
			if (!((x == x0 && y == y0) || (x == x1 && y == y1))) {
				if (newTile.solid) {
					//screen.setCharacter(x, y, new TextCharacter('X', TextColor.ANSI.CYAN, TextColor.ANSI.RED));
					return false;
				}
			}
		}
		//screen.setCharacter(x1, y1, new TextCharacter('X', TextColor.ANSI.GREEN, TextColor.ANSI.YELLOW));
		// for (int i : line) { // doesn't count solids at the beginning/end
		// if (i != XYToi(x0, y0) && i != XYToi(x1, y1) && i != -1) {
		// // if (x0 != x1 && y0 != y1)
		// // System.out.println(String.format("%d %b", i, isSolid(i)));
		// if (isSolid(i))
		// return false;
		// }
		// }
		// if (x0 != x1 && y0 != y1) System.out.println("AAA");
		return true;
	}
}