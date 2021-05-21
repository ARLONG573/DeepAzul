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
}
