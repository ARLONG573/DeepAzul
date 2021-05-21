package state;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the bag of tiles in the game and keeps track of the
 * tiles that enter and exit the bag as the game progresses. This class also
 * keeps track of which tiles are waiting in the lid of the game box to be put
 * back in the bag once the bag runs out of tiles.
 * 
 * Tiles are stored as strings of length one: B = Blue, Y = Yellow, R = Red, K =
 * Black, and W = White
 * 
 * @author Aaron Tetens
 */
class TileBag {

	private final Map<String, Integer> tilesInBag;
	private final Map<String, Integer> tilesInLid;

	TileBag() {
		this.tilesInBag = new HashMap<>();
		this.tilesInLid = new HashMap<>();

		this.tilesInBag.put("B", 20);
		this.tilesInBag.put("Y", 20);
		this.tilesInBag.put("R", 20);
		this.tilesInBag.put("K", 20);
		this.tilesInBag.put("W", 20);
	}
}
