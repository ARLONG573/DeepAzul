package state;

import java.util.HashMap;
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

	PlayerBoard(final PlayerBoard board) {
		this.patternLines = new String[5][5];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				this.patternLines[i][j] = board.patternLines[i][j];
			}
		}

		this.wall = new String[5][5];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				this.wall[i][j] = board.wall[i][j];
			}
		}

		this.floorLine = new String[7];
		for (int i = 0; i < 7; i++) {
			this.floorLine[i] = board.floorLine[i];
		}

		this.score = board.score;
	}

	/**
	 * @return The number of rows that this player board has completed on its wall
	 */
	int getNumCompletedWallRows() {
		int numCompletedWallRows = 0;

		for (int i = 0; i < 5; i++) {
			boolean isCompletedRow = true;

			for (int j = 0; j < 5; j++) {
				if (this.wall[i][j].equals("_")) {
					isCompletedRow = false;
					break;
				}
			}

			if (isCompletedRow) {
				numCompletedWallRows++;
			}
		}

		return numCompletedWallRows;
	}

	/**
	 * @return The final score for this player board (score plus end-of-game
	 *         bonuses)
	 */
	int getFinalScore() {
		return this.score + 2 * this.getNumCompletedWallRows() + 7 * this.getNumCompletedWallColumns()
				+ 10 * this.getNumCompletedColorSets();
	}

	/**
	 * @return The number of columns that this player board has completed on its
	 *         wall
	 */
	private int getNumCompletedWallColumns() {
		int numCompletedWallColumns = 0;

		for (int j = 0; j < 5; j++) {
			boolean isCompletedColumn = true;

			for (int i = 0; i < 5; i++) {
				if (this.wall[i][j].equals("_")) {
					isCompletedColumn = false;
					break;
				}
			}

			if (isCompletedColumn) {
				numCompletedWallColumns++;
			}
		}

		return numCompletedWallColumns;
	}

	/**
	 * @return The number of colors on this player board that have all five of its
	 *         tiles placed on the wall
	 */
	int getNumCompletedColorSets() {
		final Map<String, Integer> colorFreqs = new HashMap<>();
		colorFreqs.put("B", 0);
		colorFreqs.put("Y", 0);
		colorFreqs.put("R", 0);
		colorFreqs.put("K", 0);
		colorFreqs.put("W", 0);

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (!this.wall[i][j].equals("_")) {
					final String color = this.wall[i][j];
					colorFreqs.put(color, colorFreqs.get(color) + 1);
				}
			}
		}

		int numCompletedColorSets = 0;
		for (final String color : colorFreqs.keySet()) {
			if (colorFreqs.get(color) == 5) {
				numCompletedColorSets++;
			}
		}

		return numCompletedColorSets;
	}

	/**
	 * @return Whether or not this player has completed a wall row
	 */
	boolean hasCompletedWallRow() {
		return this.getNumCompletedWallRows() > 0;
	}

	/**
	 * This method fills in wall tiles for completed pattern lines, removes tiles
	 * from the floor line, and updates the score. This method assumes that tile
	 * placements have been legal up to this point.
	 * 
	 * @return A map containing the leftover tiles from completed pattern lines and
	 *         the floor line that need to be sent to the lid of the box
	 */
	Map<String, Integer> doScoring() {
		final Map<String, Integer> tilesToLid = new HashMap<>();

		for (int i = 0; i < 5; i++) {
			// determine if row is ready to be scored
			boolean isRowFull = true;
			for (int j = 4; j >= 4 - i; j--) {
				if (this.patternLines[i][j].equals("_")) {
					isRowFull = false;
					break;
				}
			}

			// if the row is ready to be scored, score it and add extra tiles to the lid
			if (isRowFull) {
				final String color = this.patternLines[i][4];
				final int wallIndex = this.getIndexOfColorInWall(i, color);
				this.wall[i][wallIndex] = color;

				int tileScore = 0;

				// horizontal
				int rowLength = 1;
				for (int left = wallIndex - 1; left >= 0 && !this.wall[i][left].equals("_"); left--) {
					rowLength++;
				}
				for (int right = wallIndex + 1; right < 5 && !this.wall[i][right].equals("_"); right++) {
					rowLength++;
				}
				tileScore += (rowLength == 1) ? 0 : rowLength;

				// vertical
				int colLength = 1;
				for (int up = i - 1; up >= 0 && !this.wall[up][wallIndex].equals("_"); up--) {
					colLength++;
				}
				for (int down = i + 1; down < 5 && !this.wall[down][wallIndex].equals("_"); down++) {
					colLength++;
				}
				tileScore += (colLength == 1) ? 0 : colLength;

				// if the tile is standalone, it is worth exactly one point
				tileScore = Math.max(tileScore, 1);

				// increment player score
				this.score += tileScore;

				// clear the row
				for (int j = 4 - i; j < 5; j++) {
					this.patternLines[i][j] = "_";
				}

				// add necessary tiles to the lid
				if (i > 0) {
					tilesToLid.putIfAbsent(color, 0);
					tilesToLid.put(color, tilesToLid.get(color) + i);
				}
			}
		}

		// handle floor line
		for (int i = 0; i < 7 && !this.floorLine[i].equals("_"); i++) {
			this.score += FLOOR_LINE_VALUES[i];

			if (!this.floorLine[i].equals("1")) {
				final String color = this.floorLine[i];

				tilesToLid.putIfAbsent(color, 0);
				tilesToLid.put(color, tilesToLid.get(color) + 1);
			}

			this.floorLine[i] = "_";
		}

		// score cannot go below zero
		this.score = Math.max(this.score, 0);

		return tilesToLid;
	}

	/**
	 * @param row
	 *            Is assumed to be 0-4
	 * @param color
	 *            Is assumed to be in the set {B, Y, R, K, W}
	 * @return The index at which the given color occurs in the wall in the given
	 *         row
	 */
	private int getIndexOfColorInWall(final int row, final String color) {
		for (int i = 0; i < 5; i++) {
			if (WALL_PLACEMENTS[row][i].equals(color)) {
				return i;
			}
		}

		// this method should never reach this point
		return -1;
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
		sb.append("Score = " + (this.getNumCompletedWallRows() > 0 ? this.getFinalScore() : this.score) + "\n");
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
