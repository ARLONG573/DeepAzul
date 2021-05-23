package state;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

	/**
	 * @param tilesToAdd
	 *            A string representing the tiles to add to this location -
	 *            tilesToAdd should match the regex pattern [BYRKW]{4}
	 * @throws IllegalArgumentException
	 *             If tilesToAdd is not formatted correctly
	 * @throws IllegalStateException
	 *             If this tile location is non-empty
	 */
	void addTilesFromInput(final String tilesToAdd) throws IllegalArgumentException, IllegalStateException {
		if (!Pattern.matches("[BYRKW]{4}", tilesToAdd)) {
			throw new IllegalArgumentException("Tiles must be 4 of {BYRKW}");
		}

		if (!this.isEmpty()) {
			throw new IllegalStateException("Attempted to add tiles to a non-empty tile location");
		}

		for (int i = 0; i < 4; i++) {
			final String tileToAdd = tilesToAdd.substring(i, i + 1);

			this.tiles.putIfAbsent(tileToAdd, 0);
			this.tiles.put(tileToAdd, this.tiles.get(tileToAdd) + 1);
		}
	}

	/**
	 * @return Whether or not this tile location has no tiles
	 */
	boolean isEmpty() {
		return this.tiles.isEmpty();
	}
}
