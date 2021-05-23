package state;

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
