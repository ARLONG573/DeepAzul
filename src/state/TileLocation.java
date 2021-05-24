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

	// it is vital that entries with value=0 are removed from the map so that
	// methods such as isEmpty() behave correctly
	protected final Map<String, Integer> tiles;

	TileLocation() {
		this.tiles = new HashMap<>();
	}

	TileLocation(final TileLocation location) {
		this.tiles = new HashMap<>(location.tiles);
	}

	/**
	 * @return One of each tile that there is at least one of in this location
	 */
	String getTileChoices() {
		final StringBuilder sb = new StringBuilder();
		for (final String color : this.tiles.keySet()) {
			sb.append(color);
		}
		return sb.toString();
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
	 * @return The tiles that were removed
	 */
	Map<String, Integer> removeAllTiles() {
		final Map<String, Integer> removedTiles = new HashMap<>(this.tiles);
		this.tiles.clear();
		return removedTiles;
	}

	/**
	 * @param tilesToAdd
	 *            A map that is assumed to have keys in the set {B, Y, R, K, W} and
	 *            values greater than zero
	 */
	void addTiles(final Map<String, Integer> tilesToAdd) {
		for (final Map.Entry<String, Integer> addEntry : tilesToAdd.entrySet()) {
			final String color = addEntry.getKey();
			final Integer count = addEntry.getValue();

			this.tiles.putIfAbsent(color, 0);
			this.tiles.put(color, this.tiles.get(color) + count);
		}
	}

	/**
	 * Removes all tiles of the given color from this tile location.
	 * 
	 * @param color
	 *            Is assumed to be one of {B, Y, R, K, W}
	 * @return The number of tiles that were removed
	 */
	int removeAll(final String color) {
		return this.tiles.remove(color);
	}

	/**
	 * @param tile
	 *            Is assumed to be one of {B, Y, R, K, W}
	 * @return Whether or not this tile location has at least one of the specified
	 *         tile
	 */
	boolean hasTile(final String tile) {
		return this.tiles.containsKey(tile);
	}

	/**
	 * @return Whether or not this tile location has no tiles
	 */
	boolean isEmpty() {
		return this.tiles.isEmpty();
	}
}
