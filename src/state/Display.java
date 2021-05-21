package state;

/**
 * This class represents one of the factory displays during the game.
 * 
 * @author Aaron Tetens
 */
class Display extends TileLocation {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Display: " + this.tiles;
	}
}
