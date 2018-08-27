package frogmodaiGame.components;

import com.artemis.annotations.EntityId;

public class RelativePosition {
	@EntityId public int e = -1;
	public int x = 0;
	public int y = 0;
	public int dx = 0;
	public int dy = 0;
	public float pathLength = 0;
	public boolean withinDistance(Position p, float d) { //squared
		//I'm pretty sure this is wrong
		return distanceSquared(p) < d*d;
	}
	public float distanceSquared(Position p) { //squared
		//I'm pretty sure this is wrong
		return (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y);
	}
}
