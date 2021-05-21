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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Table: " + this.tiles + ", hasFirstPlayerTile = " + this.hasFirstPlayerTile;
	}
}
