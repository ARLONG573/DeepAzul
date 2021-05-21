package state;

import java.util.HashMap;
import java.util.Map;

/**
 * A TileLocation refers to a location in the game from which a player may take
 * or move tiles. The two subclasses of this class are Display and Table, which
 * are the two areas from which players take or move tiles throughout the game.
 * 
 * Tiles are stored as strings of length one: B = Blue, Y = Yellow, R = Red, K =
 * Black, and W = White
 * 
 * @author Aaron Tetens
 */
abstract class TileLocation {

	protected final Map<String, Integer> tiles;

	TileLocation() {
		this.tiles = new HashMap<>();
	}
}
