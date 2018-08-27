package frogmodaiGame.components;

import java.util.ArrayList;

import com.artemis.Component;
import com.artemis.annotations.EntityId;

public class Tile extends Component {
	public byte type;
	public boolean solid;
	public boolean occupied;
	public boolean willBeOccupied;
	public boolean seen = false;
	@EntityId public ArrayList<Integer> entitiesHere;
	@EntityId public int[] neighbors;
	//This field is going to be really important
	//Entities shouldn't be stored at positions, they should be stored based on what Tile they're on
	//Maybe.. if possible... we'll see
	
	public Tile() {
		entitiesHere = new ArrayList<Integer>();
		neighbors = new int[8]; //top left is 0, right then down a rown and right again and so on
		for (int i = 0; i < 8; i++) {
			neighbors[i] = -1;
		}
	}
}
