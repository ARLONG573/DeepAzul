package state;

/**
 * This class represents the table during the game (tiles that are not taken
 * from the factory display are moved here throughout the game).
 * 
 * @author Aaron Tetens
 */
class Table extends TileLocation {

	private boolean hasFirstPlayerTile;

	Table() {
		super();

		this.hasFirstPlayerTile = true;
	}

	Table(final Table table) {
		super(table);

		this.hasFirstPlayerTile = table.hasFirstPlayerTile;
	}

	/**
	 * @return Whether or not the table still has the first player tile
	 */
	boolean hasFirstPlayerTile() {
		return this.hasFirstPlayerTile;
	}

	/**
	 * Removes the first player tile to the table
	 */
	void removeFirstPlayerTile() {
		this.hasFirstPlayerTile = false;
	}

	/**
	 * Adds the first player tile to the table
	 */
	void addFirstPlayerTile() {
		this.hasFirstPlayerTile = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Table: " + this.tiles + ", hasFirstPlayerTile = " + this.hasFirstPlayerTile;
	}
}
