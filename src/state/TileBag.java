package state;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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

	// it is vital that entries with value=0 are removed from the maps so that
	// methods such as isEmpty() behave correctly
	private final Map<String, Integer> tilesInBag;
	private final Map<String, Integer> tilesInLid;

	TileBag() {
		this.tilesInBag = new HashMap<>();
		this.tilesInBag.put("B", 20);
		this.tilesInBag.put("Y", 20);
		this.tilesInBag.put("R", 20);
		this.tilesInBag.put("K", 20);
		this.tilesInBag.put("W", 20);

		this.tilesInLid = new HashMap<>();
	}

	TileBag(final TileBag bag) {
		this.tilesInBag = new HashMap<>(bag.tilesInBag);
		this.tilesInLid = new HashMap<>(bag.tilesInLid);
	}

	/**
	 * Draw a random tile and refill the bag with the tiles in the lid if necessary.
	 * 
	 * @return One of {B, Y, R, K, W}, which represents the tile that was drawn
	 */
	String drawRandomTile() {
		if (this.tilesInBag.isEmpty()) {
			this.addLidTilesToBag();
		}

		int numTilesInBag = 0;
		for (final Integer count : this.tilesInBag.values()) {
			numTilesInBag += count;
		}

		final String[] tiles = new String[numTilesInBag];
		int index = 0;

		for (final String color : this.tilesInBag.keySet()) {
			for (int i = 0; i < this.tilesInBag.get(color); i++) {
				tiles[index] = color;
				index++;
			}
		}

		final int randomIndex = (int) (Math.random() * numTilesInBag);
		final String removedTile = tiles[randomIndex];

		this.tilesInBag.put(removedTile, this.tilesInBag.get(removedTile) - 1);
		if (this.tilesInBag.get(removedTile) == 0) {
			this.tilesInBag.remove(removedTile);
		}

		return removedTile;
	}

	/**
	 * @param numTiles
	 *            The number of tiles to add
	 * @param color
	 *            Is assumed to be one of {B, Y, R, K, W}
	 */
	void addTilesToLid(final int numTiles, final String color) {
		this.tilesInLid.putIfAbsent(color, 0);
		this.tilesInLid.put(color, this.tilesInLid.get(color) + numTiles);
	}

	/**
	 * @param tilesToAdd
	 *            Is assumed to have keys in the set {B, Y, R, K, W} and values
	 *            greater than zero
	 */
	void addTilesToLid(final Map<String, Integer> tilesToAdd) {
		for (final Map.Entry<String, Integer> entry : tilesToAdd.entrySet()) {
			final String color = entry.getKey();
			final Integer count = entry.getValue();

			this.addTilesToLid(count, color);
		}
	}

	/**
	 * @param tilesToRemove
	 *            A string representing the tiles that should be removed -
	 *            tilesToRemove should match the regex pattern [BYRKW]{4}
	 * @throws IllegalArgumentException
	 *             If tilesToRemove is not formatted correctly or if tilesToRemove
	 *             is formatted correctly, but the removal is not possible given the
	 *             state of the bag
	 */
	void removeTilesFromInput(final String tilesToRemove) throws IllegalArgumentException {
		if (!Pattern.matches("[BYRKW]{4}", tilesToRemove)) {
			throw new IllegalArgumentException("Tiles must be 4 of {BYRKW}");
		}

		// check that the removal is possible before attempting it
		final Map<String, Integer> removals = new HashMap<>();
		for (int i = 0; i < 4; i++) {
			final String tileToRemove = tilesToRemove.substring(i, i + 1);

			removals.putIfAbsent(tileToRemove, 0);
			removals.put(tileToRemove, removals.get(tileToRemove) + 1);
		}

		for (final Map.Entry<String, Integer> removalEntry : removals.entrySet()) {
			final String removalColor = removalEntry.getKey();
			final Integer removalCount = removalEntry.getValue();

			if (!this.tilesInBag.containsKey(removalColor) || this.tilesInBag.get(removalColor) < removalCount) {
				throw new IllegalArgumentException("Tried to make an impossible removal: " + tilesToRemove);
			}
		}

		for (final Map.Entry<String, Integer> removalEntry : removals.entrySet()) {
			final String removalColor = removalEntry.getKey();
			final Integer removalCount = removalEntry.getValue();

			this.tilesInBag.put(removalColor, this.tilesInBag.get(removalColor) - removalCount);

			// remove entries for colors that are no longer in the bag
			if (this.tilesInBag.get(removalColor) == 0) {
				this.tilesInBag.remove(removalColor);
			}
		}
	}

	/**
	 * @return The number of tiles left in the bag
	 */
	int getNumTilesRemaining() {
		int numTilesRemaining = 0;

		for (final Integer numOfColorRemaining : this.tilesInBag.values()) {
			numTilesRemaining += numOfColorRemaining;
		}

		return numTilesRemaining;
	}

	/**
	 * This method takes the tiles that are in the lid of the game box and moves
	 * them to the bag.
	 */
	void addLidTilesToBag() {
		for (final Map.Entry<String, Integer> lidEntry : this.tilesInLid.entrySet()) {
			final String color = lidEntry.getKey();
			final Integer numOfColor = lidEntry.getValue();

			this.tilesInBag.putIfAbsent(color, 0);
			this.tilesInBag.put(color, this.tilesInBag.get(color) + numOfColor);
		}

		this.tilesInLid.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Tiles in bag = " + this.tilesInBag + "\n");
		sb.append("Tiles in lid = " + this.tilesInLid);
		return sb.toString();
	}
}
