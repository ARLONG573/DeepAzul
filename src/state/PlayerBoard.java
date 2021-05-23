package state;

import java.util.Map;

/**
 * This class stores all of the information about a player's board, which
 * contains the tiles in their rows, floor line, and wall, as well as their
 * current score.
 * 
 * Tiles are stored as strings of length one: B = Blue, Y = Yellow, R = Red, K =
 * Black, W = White, 1 = First Player Tile, _ = No Tile, and X = Invalid
 * Placement
 * 
 * @author Aaron Tetens
 */
class PlayerBoard {

	private static final String[][] WALL_PLACEMENTS = { { "B", "Y", "R", "K", "W" }, { "W", "B", "Y", "R", "K" },
			{ "K", "W", "B", "Y", "R" }, { "R", "K", "W", "B", "Y" }, { "Y", "R", "K", "W", "B" } };
	private static final int[] FLOOR_LINE_VALUES = { -1, -1, -2, -2, -2, -3, -3 };

	private final String[][] patternLines;
	private final String[][] wall;
	private final String[] floorLine;

	private int score;

	PlayerBoard() {
		this.patternLines = new String[5][5];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (j < 4 - i) {
					this.patternLines[i][j] = "X";
				} else {
					this.patternLines[i][j] = "_";
				}
			}
		}

		this.wall = new String[5][5];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				this.wall[i][j] = "_";
			}
		}

		this.floorLine = new String[7];
		for (int i = 0; i < 7; i++) {
			this.floorLine[i] = "_";
		}

		this.score = 0;
	}

	/**
	 * This method fills in wall tiles for completed pattern lines, removes tiles
	 * from the floor line, and updates the score.
	 * 
	 * @return A map containing the leftover tiles from completed pattern lines and
	 *         the floor line that need to be sent to the lid of the box
	 */
	Map<String, Integer> doScoring() {
		return null;
	}

	/**
	 * Adds the first player tile to the floor line, if possible
	 */
	void addFirstPlayerTile() {
		for (int i = 0; i < 7; i++) {
			if (this.floorLine[i].equals("_")) {
				this.floorLine[i] = "1";
				return;
			}
		}
	}

	/**
	 * Adds the given number of tiles of the given color to the given row, with any
	 * excess going into the floor line. Parameters are assumed to represent a legal
	 * move.
	 * 
	 * @param numTiles
	 *            The number of tiles to place
	 * @param color
	 *            Is assumed to be one of {B, Y, R, K, W}
	 * @param row
	 *            Is assumed to be from -1-4
	 * @return The number of tiles that need to be send to the lid of the game box
	 *         due to floor line overflow
	 */
	int addTiles(final int numTiles, final String color, final int row) {
		int numToFloorLine = numTiles;

		// place tiles from right to left in pattern line
		if (row != -1) {
			int i = 4;
			int numPlaced = 0;

			while (numPlaced < numTiles && i >= 0 && !this.patternLines[row][i].equals("X")) {
				if (this.patternLines[row][i].equals("_")) {
					this.patternLines[row][i] = color;
					numPlaced++;
					numToFloorLine--;
				}

				i--;
			}
		}

		// place tiles from left to right in floor line
		for (int i = 0; i < 7; i++) {
			if (numToFloorLine == 0) {
				return 0;
			}

			if (this.floorLine[i].equals("_")) {
				this.floorLine[i] = color;
				numToFloorLine--;
			}
		}

		// return the amount of tiles leftover from placing in the floor line
		return numToFloorLine;
	}

	/**
	 * @param color
	 *            Is assumed to be one of {B, Y, R, K, W}
	 * @param row
	 *            Is assumed to be 0-4
	 * @return Whether or not it is legal to place tiles of the given color into the
	 *         given row
	 */
	boolean isLegalPlacement(final String color, final int row) {
		// check if our wall already has the tile of the given color
		for (int i = 0; i < 5; i++) {
			if (this.wall[row][i].equals(color)) {
				return false;
			}
		}

		// check if another color is already in the pattern line
		return this.patternLines[row][4].equals("_") || this.patternLines[row][4].equals(color);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Score = " + this.score + "\n");
		sb.append("Pattern lines = \n");
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				sb.append(this.patternLines[i][j]);
			}
			sb.append("\n");
		}
		sb.append("Wall = \n");
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				sb.append(this.wall[i][j]);
			}
			sb.append("\n");
		}
		sb.append("Floor line = \n");
		for (int i = 0; i < 7; i++) {
			sb.append(this.floorLine[i]);
		}
		return sb.toString();
	}
}
