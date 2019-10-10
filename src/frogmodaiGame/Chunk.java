package frogmodaiGame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.systems.IteratingSystem;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.screen.Screen;

import frogmodaiGame.components.*;

public class Chunk { // SHOULD NOT CONTAIN ANY GENERATION CODE
	int worldID = -1;
	public int width = 96;
	public int height = 32;
	// non-fixed chunk sizes
	public int[] tiles; // TODO: convert this to be a couple systems?
	ArrayList<Chunk> neighbors;
	Screen screen;
	// non-linear chunk connection (have to manually define teleports off the
	// screen)
	// this was you could have a building with a door that transports you to another
	// screen
	// but also building interiors will all be active
	// so characters (at most) one screen away can enter the activeChunk

	ComponentMapper<Position> mPosition;
	ComponentMapper<Char> mChar;
	ComponentMapper<Tile> mTile;
	ComponentMapper<ChunkAddress> mChunkAddress;

	// This class is only for creating and holding the map representation
	// Other classes should be used to populate it

	public Chunk(int _width, int _height, Screen _screen) {
		width = _width;
		height = _height;
		tiles = new int[width * height];
		neighbors = new ArrayList<Chunk>();
		screen = _screen;
	}
	
	public Chunk(int _width, int _height) {
		width = _width;
		height = _height;
		tiles = new int[width * height];
		neighbors = new ArrayList<Chunk>();
	}

	public Chunk() {
		tiles = new int[width * height];
		neighbors = new ArrayList<Chunk>();
	}

	public int getTile(int x, int y) {
		int i = XYToi(x, y);
		/*if (!posInChunk(x, y)) {
			return -1; //FAILURE. no tile
		}*/
		return tiles[i];
	}
	
	public boolean posInChunk(int x, int y) {
		int i = XYToi(x, y);
		return x >= 0 && y >= 0 && x < width && y < height;//i >= 0 && i < width*height;
	}

	public void initTiles(World world) {
		world.inject(this);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = XYToi(x, y);
				tiles[i] = world.create(ArchetypeBuilders.Tile.archetype);
				Position pos = mPosition.create(tiles[i]);
				Tile tile = mTile.create(tiles[i]);
				Char character = mChar.create(tiles[i]);
				ChunkAddress chunkAddress = mChunkAddress.create(tiles[i]);
				pos.x = x;
				pos.y = y;
				chunkAddress.worldID = worldID;
				tile.occupied = false;
				tile.willBeOccupied = false;
			}
		}

		linkLocal(false);
	}

	public void update() {
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				int i = XYToi(x, y);
//				Position pos = mPosition.create(tiles[i]);
//				Tile tile = mTile.create(tiles[i]);
//				Char character = mChar.create(tiles[i]);
//				ChunkAddress chunkAddress = mChunkAddress.create(tiles[i]);
//				// tile.occupied = false;
//				//tile.willBeOccupied = false;
//				//tile.entitiesHere.clear();
//			}
//		}
	}

	public void entityOnTile(int x, int y, int e) {
		int i = XYToi(x, y);
		if (i >= tiles.length)
			return; // TODO: ERROR
		Tile tile = mTile.create(tiles[i]);
		tile.entitiesHere.add(e);
	}

	public ArrayList<Integer> getEntitiesAtPos(int x, int y) {
		int i = XYToi(x, y);
		Tile tile = mTile.create(tiles[i]);
		return tile.entitiesHere;
	}
	
	//IF the path crawled is longer than a direct line, toss it out?????
	//To prevent leakage around corners.
	
	//TODO: Convert "tocuhed" to a dictionary that store relativepositions.
	//Swap in multiplicities with shorter path length.
	//use this as final list of points, as it will only contain shortest paths.
	
	public void floodGrab(Position startPos, RelativePosition rel, int d, int e, final HashMap<String, RelativePosition> list) {
		//"list" is the final list of included RelativePositions.
		//"touched" is all RelativePositions that have already been explored.
		//"todo" is the list of all neighbors flagged for checking on the previous cycle.
		HashMap<String, RelativePosition> touched = new HashMap<String, RelativePosition>();
		ArrayList<RelativePosition> todo = new ArrayList<RelativePosition>();
		ArrayList<RelativePosition> newTodo = new ArrayList<RelativePosition>();
		Random random = new Random();
		
		todo.add(rel);
		
		float depth = 0;
		
		//screen.clear();
		
		while(todo.size() > 0) {
			//Pop a random element of todo.
			int r = random.nextInt(todo.size());
			RelativePosition randPos = todo.get(r);
			floodGrabCheckTile(startPos, randPos, d, depth, randPos.e, list, touched, newTodo, random);
			todo.remove(r);
			if (todo.isEmpty() && !newTodo.isEmpty()) {
				todo.addAll(newTodo); //begin new cycle
				newTodo.clear();
				depth++;
			}
		}
		
		list.clear();
		for (RelativePosition p : touched.values()) {
			if (p.withinDistance(startPos, d))
				//System.out.println((p.x-startPos.x)+"|"+(p.y-startPos.y));
				//System.out.println((p.x)+"|"+(p.y));
				//list.put(p.x+"|"+p.y, p);
				//p.x -= startPos.x;
				//p.y -= startPos.y;
				list.put(p.x+"|"+p.y, p);
		}
	}

	public void floodGrabCheckTile(Position startPos, RelativePosition rel, int d, float _depth, int e, final HashMap<String, RelativePosition> list,
			final HashMap<String, RelativePosition> touched, final ArrayList<RelativePosition> todo, Random random) {
		Tile tile = mTile.create(e); // These tiles aren't necessarily in the same chunk
		Position tilePos = mPosition.create(e);

		// if (list.size() > 100) return;
		
		//float depth = _depth + ((Math.abs(rel.dx) == 1 && Math.abs(rel.dy) == 1) ? 0.25f : 0);

		if (!rel.withinDistance(startPos, d))
			return;
		
		//if (depth > d) return;
		
		//if (rel.distanceSquared(startPos) < rel.pathLength) return;
		
		//Char chara = mChar.create(rel.e);
		//screen.setCharacter(rel.x, rel.y, new TextCharacter((char)(depth+48), TextColor.ANSI.RED, TextColor.ANSI.CYAN));
		
		// if (!FFMain.worldManager.getActiveChunk().LOS(rel.x, rel.y, tilePos.x,
		// tilePos.y)) return;

		list.put(rel.x+"|"+rel.y, rel);
		
		if (tile.solid) return;
		
		ArrayList<Integer> neighbors = new ArrayList<Integer>();

		// FFMain.worldManager.getActiveChunk().LOS(playerPos.x, playerPos.y, pos.x,
		// pos.y)

		for (int i = 0; i < 8; i++) {
			int j = tile.neighbors[i];
			RelativePosition newrel = new RelativePosition();
			Position dir = DirectionConverter.toPosition(i);
			newrel.x = rel.x + dir.x;
			newrel.y = rel.y + dir.y;
			newrel.dx = dir.x;
			newrel.dy = dir.y;
			newrel.pathLength = rel.pathLength + ((Math.abs(rel.dx) == 1 && Math.abs(rel.dy) == 1) ? (float)Math.sqrt(2.0) : 1f);
			newrel.e = j;
			//Flooding stops locally when a tile with the same relative position has already been touched.
			if (j != -1 && (!touched.containsKey(newrel.x + "|" + newrel.y) 
					|| touched.get(newrel.x + "|" + newrel.y).pathLength > newrel.pathLength)) {// !list.contains((Integer) j)) {
				neighbors.add(i); // neighbors is a direction! not entID!!!
				touched.put(newrel.x + "|" + newrel.y, newrel);
			}
		}
		
		//If this was to be breadth-first, I might collect all the bundles in a "final Queue"
		//And then surrender back to top-level function
		//Top-level empties current todo-list, and in turn fills a new todo-list
		//Boundary conditions of "no repeats" and "max distance" will prevent addition to todo-list
		//When the new todo-list has 0 length after the old todo-list is completed, return final list
		
		while (neighbors.size() > 0) {
			int r = random.nextInt(neighbors.size());
			int i = neighbors.get(r); // direction
			int j = tile.neighbors[i]; // ENT ID
			if (j == -1) continue;
			neighbors.remove(r);
			RelativePosition newrel = new RelativePosition();
			Position dir = DirectionConverter.toPosition(i);
			newrel.x = rel.x + dir.x;
			newrel.y = rel.y + dir.y;
			newrel.dx = dir.x;
			newrel.dy = dir.y;
			newrel.pathLength = rel.pathLength + ((Math.abs(rel.dx) == 1 && Math.abs(rel.dy) == 1) ? (float)Math.sqrt(2.0) : 1f);
			newrel.e = j;
			todo.add(newrel);
			//Char chara = mChar.create(newrel.e);
			//screen.setCharacter(newrel.x+10, newrel.y+10, new TextCharacter(chara.character, TextColor.ANSI.RED, TextColor.ANSI.CYAN));
			//floodGrab(startPos, newrel, d, j, list, touched);
		}
	}

	public void floodGrabOLD(Position startPos, RelativePosition rel, int d, int e, final ArrayList<RelativePosition> list,
			final ArrayList<String> touched) {
		Tile tile = mTile.create(e); // These tiles aren't necessarily in the same chunk
		Position tilePos = mPosition.create(e);

		// if (list.size() > 100) return;

		if (!rel.withinDistance(startPos, d))
			return;
		// if (!FFMain.worldManager.getActiveChunk().LOS(rel.x, rel.y, tilePos.x,
		// tilePos.y)) return;

		list.add(rel);

		ArrayList<Integer> neighbors = new ArrayList<Integer>();
		Random random = new Random();

		// FFMain.worldManager.getActiveChunk().LOS(playerPos.x, playerPos.y, pos.x,
		// pos.y)

		for (int i = 0; i < 8; i++) {
			int j = tile.neighbors[i];
			RelativePosition newrel = new RelativePosition();
			Position dir = DirectionConverter.toPosition(i);
			newrel.x = rel.x + dir.x;
			newrel.y = rel.y + dir.y;
			//Flooding stops locally when a tile with the same relative position has already been touched.
			if (j != -1 && !touched.contains(newrel.x + "|" + newrel.y)) {// !list.contains((Integer) j)) {
				neighbors.add(i); // neighbors is a direction! not entID!!!
				touched.add(newrel.x + "|" + newrel.y);
			}
		}

		// To keep everything processing evenly
		//While there are unchecked neighbors,
		//Pick a random neighbor direction.
		//Bundle that tile's relative position and ID
		//Pass the bundle back into floodGrab and immediately move to search it's neighbors 
		//  until they're all recursively done
		//This is not ideal
		//ISSUE: This is depth first, whereas breadth-first seems to be a better fit for this task.
		
		//If this was to be breadth-first, I might collect all the bundles in a "final Queue"
		//And then surrender back to top-level function
		//Top-level empties current todo-list, and in turn fills a new todo-list
		//Boundary conditions of "no repeats" and "max distance" will prevent addition to todo-list
		//When the new todo-list has 0 length after the old todo-list is completed, return final list
		
		while (neighbors.size() != 0) {
			int r = random.nextInt(neighbors.size());
			int i = neighbors.get(r); // direction
			int j = tile.neighbors[i]; // ENT ID
			neighbors.remove(r);
			RelativePosition newrel = new RelativePosition();
			Position dir = DirectionConverter.toPosition(i);
			newrel.x = rel.x + dir.x;
			newrel.y = rel.y + dir.y;
			newrel.e = j;
			//floodGrab(startPos, newrel, d, j, list, touched);
		}
	}

	public boolean isSolid(int i) {
		if (i < 0 || i >= width * height)
			return true;
		Tile tile = mTile.create(tiles[i]);
		return tile.solid;
	}

	public boolean isSolid(int x, int y) {
		int i = XYToi(x, y);
		if (i < 0 || i >= width * height)
			return true;
		Tile tile = mTile.create(tiles[i]);
		return tile.solid;
	}

	public boolean isOccupied(int i) {
		if (i < 0 || i >= width * height)
			return true;
		Tile tile = mTile.create(tiles[i]);
		return tile.occupied;
	}

	public boolean isOccupied(int x, int y) {
		int i = XYToi(x, y);
		if (i < 0 || i >= width * height)
			return false;
		Tile tile = mTile.create(tiles[i]);
		return tile.occupied;
	}

	public void setOccupied(int x, int y, boolean o) {
		int i = XYToi(x, y);
		if (i < 0 || i >= width * height)
			return;
		Tile tile = mTile.create(tiles[i]);
		tile.occupied = o;
	}

	private int XYToi(int x, int y) {
		return x + y * width;
	}

	private int iToX(int i) {
		return i % width;
	}

	private int iToY(int i) {
		return (i - iToX(i)) / width;
	}

	public void linkLocal(boolean wrap) {
		// Link to neighbors
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = XYToi(x, y);
				Tile localTile = mTile.create(tiles[i]);
				int c = 0;
				for (int dy = -1; dy < 2; dy++) {
					for (int dx = -1; dx < 2; dx++) {
						if (!(dx == 0 && dy == 0)) { // It's not the local tile
							int nx = x+dx;
							int ny = y+dy;
							if (wrap) {
								nx = (nx + width) % width;
								ny = (ny + height) % height;
							}
							if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
								// It's within the local chunk
								int j = XYToi(nx, ny);
								// Tile otherTile = mTile.create(tiles[j]);
								localTile.neighbors[c] = tiles[j];
							} else {
								localTile.neighbors[c] = -1;
							}
							c++; // Count up even if the tile isn't there (except center)
						}
					}
				}
			}
		}
	}
	
	public boolean attachCorner(Chunk chunk, int a) { //I fucked something up in here
		int me;
		int them;
		switch(a) {
			case 0: me = getTile(0, 0); them = chunk.getTile(chunk.width-1, chunk.height-1); break;
			case 1: me = getTile(width-1, 0); them = chunk.getTile(0, chunk.height-1); break;
			case 2: me = getTile(width-1, height-1); them = chunk.getTile(0, 0); break;
			case 3: me = getTile(0, height-1); them = chunk.getTile(chunk.width-1, 0); break;
			default: me = -1; them = -1; break;
		}
		/*switch(b) {
			case 0: them = chunk.getTile(0, 0); break;
			case 1: them = chunk.getTile(chunk.width-1, 0); break;
			case 2: them = chunk.getTile(chunk.width-1, chunk.height-1); break;
			case 3: them = chunk.getTile(0, chunk.height-1); break;
			default: them = -1;
		}*/
		
		//if (me == -1 || them == -1) return false; //OOPS
		if (me == -1) return false;
		
		Tile meTile = mTile.create(me);
		
		//meTile.neighbors[3] = them;
		switch(a) {
		case 0: meTile.neighbors[0] = them; break;
		case 1: meTile.neighbors[2] = them; break;
		case 2: meTile.neighbors[7] = them; break;
		case 3: meTile.neighbors[5] = them; break;
		default: me = -1; them = -1; break;
		}
		
		return true;
	}
	
	public boolean attach(Chunk chunk, int side, int alignment) {
		if (side == 0) { //left
			for (int y = 0; y < height; y++) {
				int me = getTile(0, y);
				if (chunk.posInChunk(chunk.width-1, y + alignment)) {
					int them = chunk.getTile(chunk.width-1, y + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[3] = them;
					//themTile.neighbors[4] = me;
				}
				if (chunk.posInChunk(chunk.width-1, y+1 + alignment)) {
					int them = chunk.getTile(chunk.width-1, y+1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them;
					//themTile.neighbors[2] = me;
				}
				if (chunk.posInChunk(chunk.width-1, y-1 + alignment)) {
					int them = chunk.getTile(chunk.width-1, y-1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them;
					//themTile.neighbors[7] = me;
				}
			}
		} else if (side == 1) {//up
			for (int x = 0; x < width; x++) {
				int me = getTile(x, 0);
				if (chunk.posInChunk(x + alignment, chunk.height-1)) {
					int them = chunk.getTile(x + alignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[1] = them;
					//themTile.neighbors[6] = me;
				}
				if (chunk.posInChunk(x+1 + alignment, chunk.height-1)) {
					int them = chunk.getTile(x+1 + alignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them;
					//themTile.neighbors[5] = me;
				}
				if (chunk.posInChunk(x-1 + alignment, chunk.height-1)) {
					int them = chunk.getTile(x-1 + alignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them;
					//themTile.neighbors[7] = me;
				}
			}
		} else if (side == 2) {//right
			for (int y = 0; y < height; y++) {
				int me = getTile(width-1, y);
				if (chunk.posInChunk(0, y + alignment)) {
					int them = chunk.getTile(0, y + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[4] = them;
					//themTile.neighbors[3] = me;
				}
				if (chunk.posInChunk(0, y+1 + alignment)) {
					int them = chunk.getTile(0, y+1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them;
					//themTile.neighbors[0] = me;
				}
				if (chunk.posInChunk(0, y-1 + alignment)) {
					int them = chunk.getTile(0, y-1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them;
					//themTile.neighbors[5] = me;
				}
			}
		} else if (side == 3) {//down
			for (int x = 0; x < width; x++) {
				int me = getTile(x, height-1);
				if (chunk.posInChunk(x + alignment, 0)) {
					int them = chunk.getTile(x + alignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[6] = them;
					//themTile.neighbors[1] = me;
				}
				if (chunk.posInChunk(x+1 + alignment, 0)) {
					int them = chunk.getTile(x+1 + alignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them;
					//themTile.neighbors[0] = me;
				}
				if (chunk.posInChunk(x-1 + alignment, 0)) {
					int them = chunk.getTile(x-1 + alignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them;
					//themTile.neighbors[2] = me;
				}
			}
		}
		
		return true;
	}

	public boolean portal(Chunk chunk, int w, int dir, int _ox, int _oy, int _dx, int _dy, boolean wall) {
		for (int i = 0; i < w; i++) { //width of opening
			if (dir == 0) {//portal opens left, extends down
				int ox = _ox;
				int oy = _oy + i;
				int dx = _dx;
				int dy = _dy + i;
				
				int me = getTile(ox, oy); //portal in
				if (chunk.posInChunk(dx, dy)) {
					int them = chunk.getTile(dx, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[4] = them; //portal's rightward neighbor
					//themTile.neighbors[3] = me; //destination's leftward neighbor
					if (i == 0 && posInChunk(ox,oy-1)) {
						int me2 = getTile(ox,oy-1);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[7] = them;
					}
					if (i == w-1 && posInChunk(ox,oy+1)) {
						int me2 = getTile(ox,oy+1);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[2] = them;
					}
				}
				if (chunk.posInChunk(dx, dy-1)) {
					int them = chunk.getTile(dx, dy-1); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them; //portal's rightward neighbor
					//themTile.neighbors[5] = me; //destination's leftward neighbor
				}
				if (chunk.posInChunk(dx, dy+1)) {
					int them = chunk.getTile(dx, dy+1); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them; //portal's rightward neighbor
					//themTile.neighbors[0] = me; //destination's leftward neighbor
				}
				
				if (wall) {
					if (posInChunk(ox+1, oy)) {
						int mewall = getTile(ox+1, oy);
						Tile tile = mTile.create(mewall);
						Char character = mChar.create(mewall);
						tile.solid = true;
						character.character = '#';
						character.fgc = TextColor.ANSI.DEFAULT.ordinal();
						character.bold = false;
					}
				}
			} else if (dir == 1) { //portal opens up, extends right
				int ox = _ox + i;
				int oy = _oy;
				int dx = _dx + i;
				int dy = _dy;
				
				int me = getTile(ox, oy); //portal in
				if (chunk.posInChunk(dx, dy)) {
					int them = chunk.getTile(dx, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[6] = them; //portal's downward neighbor
					//themTile.neighbors[1] = me; //destination's upward neighbor
					
					if (i == 0 && posInChunk(ox-1,oy)) {
						int me2 = getTile(ox-1,oy);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[7] = them;
					}
					if (i == w-1 && posInChunk(ox+1,oy)) {
						int me2 = getTile(ox+1,oy);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[5] = them;
					}
				}
				if (chunk.posInChunk(dx-1, dy)) {
					int them = chunk.getTile(dx-1, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them; //portal's downward neighbor
					//themTile.neighbors[2] = me; //destination's upward neighbor
				}
				if (chunk.posInChunk(dx+1, dy)) {
					int them = chunk.getTile(dx+1, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them; //portal's downward neighbor
					//themTile.neighbors[0] = me; //destination's upward neighbor
				}
				
				if (wall) {
					if (posInChunk(ox, oy+1)) {
						int mewall = getTile(ox, oy+1);
						Tile tile = mTile.create(mewall);
						Char character = mChar.create(mewall);
						tile.solid = true;
						character.character = '#';
						character.fgc = TextColor.ANSI.DEFAULT.ordinal();
						character.bold = false;
					}
				}
			} else if (dir == 2) { //portal opens right, extends down
				int ox = _ox;
				int oy = _oy + i;
				int dx = _dx;
				int dy = _dy + i;
				
				int me = getTile(ox, oy); //portal in
				if (chunk.posInChunk(dx, dy)) {
					int them = chunk.getTile(dx, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[3] = them; //portal's leftward neighbor
					//themTile.neighbors[4] = me; //destination's rightward neighbor
					if (i == 0 && posInChunk(ox,oy-1)) {
						int me2 = getTile(ox,oy-1);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[5] = them;
					}
					if (i == w-1 && posInChunk(ox,oy+1)) {
						int me2 = getTile(ox,oy+1);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[0] = them;
					}
				}
				if (chunk.posInChunk(dx, dy-1)) {
					int them = chunk.getTile(dx, dy-1); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them; //portal's leftward neighbor
					//themTile.neighbors[7] = me; //destination's rightward neighbor
				}
				if (chunk.posInChunk(dx, dy+1)) {
					int them = chunk.getTile(dx, dy+1); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them; //portal's leftward neighbor
					//themTile.neighbors[2] = me; //destination's rightward neighbor
				}
				
				if (wall) {
					if (posInChunk(ox-1, oy)) {
						int mewall = getTile(ox-1, oy);
						Tile tile = mTile.create(mewall);
						Char character = mChar.create(mewall);
						tile.solid = true;
						character.character = '#';
						character.fgc = TextColor.ANSI.DEFAULT.ordinal();
						character.bold = false;
					}
				}
			} else if (dir == 3) { //portal opens down, extends right
				int ox = _ox + i;
				int oy = _oy;
				int dx = _dx + i;
				int dy = _dy;
				
				int me = getTile(ox, oy); //portal in
				if (chunk.posInChunk(dx, dy)) {
					int them = chunk.getTile(dx, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[1] = them; //portal's upward neighbor
					//themTile.neighbors[6] = me; //destination's downward neighbor
					if (i == 0 && posInChunk(ox-1,oy)) {
						int me2 = getTile(ox-1,oy);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[2] = them;
					}
					if (i == w-1 && posInChunk(ox+1,oy)) {
						int me2 = getTile(ox+1,oy);
						Tile meTile2 = mTile.create(me2);
						meTile2.neighbors[0] = them;
					}
				}
				if (chunk.posInChunk(dx-1, dy)) {
					int them = chunk.getTile(dx-1, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them; //portal's upward neighbor
					//themTile.neighbors[7] = me; //destination's downward neighbor
				}
				if (chunk.posInChunk(dx+1, dy)) {
					int them = chunk.getTile(dx+1, dy); //portal out
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them; //portal's upward neighbor
					//themTile.neighbors[5] = me; //destination's downward neighbor
				}
				
				if (wall) {
					if (posInChunk(ox, oy-1)) {
						int mewall = getTile(ox, oy-1);
						Tile tile = mTile.create(mewall);
						Char character = mChar.create(mewall);
						tile.solid = true;
						character.character = '#';
						character.fgc = TextColor.ANSI.DEFAULT.ordinal();
						character.bold = false;
					}
				}
			}
		}
		/*if (side == 0) { //left
			for (int y = 0; y < height; y++) {
				int me = getTile(0, y);
				if (chunk.posInChunk(chunk.width-1, y + parallelAlignment)) {
					int them = chunk.getTile(chunk.width-1, y + parallelAlignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[3] = them;
					//themTile.neighbors[4] = me;
				}
				if (chunk.posInChunk(chunk.width-1, y+1 + parallelAlignment)) {
					int them = chunk.getTile(chunk.width-1, y+1 + parallelAlignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them;
					//themTile.neighbors[2] = me;
				}
				if (chunk.posInChunk(chunk.width-1, y-1 + parallelAlignment)) {
					int them = chunk.getTile(chunk.width-1, y-1 + parallelAlignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them;
					//themTile.neighbors[7] = me;
				}
			}
		} else if (side == 1) {//up
			for (int x = 0; x < width; x++) {
				int me = getTile(x, 0);
				if (chunk.posInChunk(x + parallelAlignment, chunk.height-1)) {
					int them = chunk.getTile(x + parallelAlignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[1] = them;
					//themTile.neighbors[6] = me;
				}
				if (chunk.posInChunk(x+1 + parallelAlignment, chunk.height-1)) {
					int them = chunk.getTile(x+1 + parallelAlignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them;
					//themTile.neighbors[5] = me;
				}
				if (chunk.posInChunk(x-1 + parallelAlignment, chunk.height-1)) {
					int them = chunk.getTile(x-1 + parallelAlignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them;
					//themTile.neighbors[7] = me;
				}
			}
		} else if (side == 2) {//right
			for (int y = 0; y < height; y++) {
				int me = getTile(width-1, y);
				if (chunk.posInChunk(0, y + parallelAlignment)) {
					int them = chunk.getTile(0, y + parallelAlignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[4] = them;
					//themTile.neighbors[3] = me;
				}
				if (chunk.posInChunk(0, y+1 + parallelAlignment)) {
					int them = chunk.getTile(0, y+1 + parallelAlignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them;
					//themTile.neighbors[0] = me;
				}
				if (chunk.posInChunk(0, y-1 + parallelAlignment)) {
					int them = chunk.getTile(0, y-1 + parallelAlignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them;
					//themTile.neighbors[5] = me;
				}
			}
		} else if (side == 3) {//down
			for (int x = 0; x < width; x++) {
				int me = getTile(x, height-1);
				if (chunk.posInChunk(x + parallelAlignment, 0)) {
					int them = chunk.getTile(x + parallelAlignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[6] = them;
					//themTile.neighbors[1] = me;
				}
				if (chunk.posInChunk(x+1 + parallelAlignment, 0)) {
					int them = chunk.getTile(x+1 + parallelAlignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them;
					//themTile.neighbors[0] = me;
				}
				if (chunk.posInChunk(x-1 + parallelAlignment, 0)) {
					int them = chunk.getTile(x-1 + parallelAlignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them;
					//themTile.neighbors[2] = me;
				}
			}
		}*/
		
		return true;
	}
	
	public boolean attachSingleTile(Chunk chunk, int side, int alignment, int indexA, int indexB) {
		if (side == 0) { //left
			//for (int y = 0; y < height; y++) {
			int y1 = indexA;
			int y2 = indexB; {
				int me = getTile(0, y1);
				if (chunk.posInChunk(chunk.width-1, y2 + alignment)) {
					int them = chunk.getTile(chunk.width-1, y2 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[3] = them;
					//themTile.neighbors[4] = me;
				}
				if (chunk.posInChunk(chunk.width-1, y2+1 + alignment)) {
					int them = chunk.getTile(chunk.width-1, y2+1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them;
					//themTile.neighbors[2] = me;
				}
				if (chunk.posInChunk(chunk.width-1, y2-1 + alignment)) {
					int them = chunk.getTile(chunk.width-1, y2-1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them;
					//themTile.neighbors[7] = me;
				}
			}
		} else if (side == 1) {//up
			//for (int x = 0; x < width; x++) {
			int x1 = indexA;
			int x2 = indexB; {
				int me = getTile(x1, 0);
				if (chunk.posInChunk(x2 + alignment, chunk.height-1)) {
					int them = chunk.getTile(x2 + alignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[1] = them;
					//themTile.neighbors[6] = me;
				}
				if (chunk.posInChunk(x2+1 + alignment, chunk.height-1)) {
					int them = chunk.getTile(x2+1 + alignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them;
					//themTile.neighbors[5] = me;
				}
				if (chunk.posInChunk(x2-1 + alignment, chunk.height-1)) {
					int them = chunk.getTile(x2-1 + alignment, chunk.height-1);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[0] = them;
					//themTile.neighbors[7] = me;
				}
			}
		} else if (side == 2) {//right
			//for (int y = 0; y < height; y++) {
			int y1 = indexA;
			int y2 = indexB; {
				int me = getTile(width-1, y1);
				if (chunk.posInChunk(0, y2 + alignment)) {
					int them = chunk.getTile(0, y2 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[4] = them;
					//themTile.neighbors[3] = me;
				}
				if (chunk.posInChunk(0, y2+1 + alignment)) {
					int them = chunk.getTile(0, y2+1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them;
					//themTile.neighbors[0] = me;
				}
				if (chunk.posInChunk(0, y2-1 + alignment)) {
					int them = chunk.getTile(0, y2-1 + alignment);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[2] = them;
					//themTile.neighbors[5] = me;
				}
			}
		} else if (side == 3) {//down
			//for (int x = 0; x < width; x++) {
			int x1 = indexA;
			int x2 = indexB;{
				int me = getTile(x1, height-1);
				if (chunk.posInChunk(x2 + alignment, 0)) {
					int them = chunk.getTile(x2 + alignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[6] = them;
					//themTile.neighbors[1] = me;
				}
				if (chunk.posInChunk(x2+1 + alignment, 0)) {
					int them = chunk.getTile(x2+1 + alignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[7] = them;
					//themTile.neighbors[0] = me;
				}
				if (chunk.posInChunk(x2-1 + alignment, 0)) {
					int them = chunk.getTile(x2-1 + alignment, 0);
					Tile meTile = mTile.create(me);
					Tile themTile = mTile.create(them);
					meTile.neighbors[5] = them;
					//themTile.neighbors[2] = me;
				}
			}
		}
		
		return true;
	}
	
	//UTILITY SHIT
	public void setGroundColor(int j) {
		for (int i = 0; i < tiles.length; i++) {
			Char chara = mChar.create(tiles[i]);
			chara.fgc = j;
		}
	}

	//SHIT DAWG SERIALIZATION HERE
	public void unload() { //TODO: Called every move?????
		//System.out.println("TODO: Chunk.unload");
	}

	public void load() {
		//System.out.println("TODO: Chunk.load");
		FFMain.sendMessage(worldID + "");
	}
}

/////////////////
// legacy
/////////////
/*
 * public void update() { //Random random = new Random(); for (int y = 0; y <
 * height; y++) { for (int x = 0; x < width; x++) { int i = XYToi(x, y);
 * //FFTile tile = tiles[i]; //tile.fgc =
 * (byte)random.nextInt(TextColor.ANSI.values().length); //tile.bgc =
 * (byte)random.nextInt(TextColor.ANSI.values().length); } } } public void
 * draw(Screen screen) { /*for (int y = 0; y < height; y++) { for (int x = 0; x
 * < width; x++) { int i = XYToi(x, y); FFTile tile = tiles[i];
 * screen.setCharacter(x, y, tile.getTextCharacter()); } } }
 */
//////////////////////