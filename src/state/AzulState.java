package state;

import java.util.List;

import api.GameState;

/**
 * This class defines a single game state for Azul. Implementing the GameState
 * interface allows this class to use my MCTS implementation. Azul is unique in
 * that we cannot perform node expansion once we reach the end of a single
 * round, since the branching factor to the start of the next round is too large
 * due to the number of possible tile combinations that may emerge from the bag.
 * As a result, this implemention will treat the ends of rounds as leaf nodes in
 * the MCTS tree and randomly play out one such tile combination for each
 * successive round during the simulation phase. Also,
 * {@link AzulState#getWinningPlayers()} may only return a non-empty list if we
 * are in the final round of the game.
 * 
 * @author Aaron Tetens
 */
public class AzulState implements GameState {

	private final TileBag tileBag;
	private final TileLocation[] tileLocations;
	private final PlayerBoard[] playerBoards;

	private int lastPlayer;
	private int currentPlayer;
	private int currentRound;

	public AzulState(final int numPlayers) {
		if (numPlayers < 2 || numPlayers > 4) {
			throw new IllegalArgumentException("Tried to start a game with " + numPlayers + " players (2-4 required)");
		}

		this.tileBag = new TileBag();

		this.tileLocations = new TileLocation[2 * numPlayers + 2]; // 2n + 1 displays, plus one for the table
		this.tileLocations[0] = new Table();
		for (int i = 1; i < this.tileLocations.length; i++) {
			this.tileLocations[i] = new Display();
		}

		this.playerBoards = new PlayerBoard[numPlayers];
		for (int i = 0; i < this.playerBoards.length; i++) {
			this.playerBoards[i] = new PlayerBoard();
		}

		this.lastPlayer = -1;
		this.currentPlayer = 0;
		this.currentRound = 1;
	}

	/**
	 * This method applies the given move to the current state. If the move takes
	 * the last tiles for the round, this method will update the game state as much
	 * as possible via scoring, moving the necessary tiles to the lid of the game
	 * box, and moving the first player tile back to the table (and setting the new
	 * current player based on who had the first player tile).
	 * 
	 * @param tileLocation
	 *            From where the player is taking tiles
	 * @param tileChoice
	 *            The color of tiles that the player has decided to take from the
	 *            given location
	 * @param rowChoice
	 *            The index of the pattern line that the player will add the
	 *            selected tiles to (use -1 to add directly to the floor line)
	 */
	public void makeMove(final TileLocation tileLocation, final String tileChoice, final int rowChoice) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLastPlayer() {
		return this.lastPlayer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GameState> getNextStates() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GameState getRandomNextState() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getWinningPlayers() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Last Player = " + this.lastPlayer + "\n");
		sb.append("Current Player = " + this.currentPlayer + "\n");
		sb.append("Current Round = " + this.currentRound + "\n");
		sb.append(this.tileBag);
		for (final TileLocation tileLocation : this.tileLocations) {
			sb.append("\n" + tileLocation);
		}
		for (int i = 0; i < this.playerBoards.length; i++) {
			sb.append("\n===================================================================");
			sb.append("\nPlayer " + i + "\n" + this.playerBoards[i]);
		}
		return sb.toString();
	}
}
