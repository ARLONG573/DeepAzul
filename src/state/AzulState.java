package state;

import java.util.List;
import java.util.Scanner;

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

	/**
	 * @param numPlayers
	 *            The number of players to create the game for
	 * @param in
	 *            The Scanner used to read in the user input to refill the game
	 *            displays
	 * @throws IllegalArgumentException
	 */
	public AzulState(final int numPlayers, final Scanner in) throws IllegalArgumentException {
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

		this.refillDisplaysFromInput(in);
	}

	/**
	 * This method applies the given move to the current state. If the move takes
	 * the last tiles for the round, this method will update the game state as much
	 * as possible via scoring, moving the necessary tiles to the lid of the game
	 * box, and moving the first player tile back to the table (and setting the new
	 * current player based on who had the first player tile).
	 * 
	 * @param tileLocation
	 *            Number of the tile location from where the player is taking tiles
	 *            (use 0 if taking from the table)
	 * @param tileChoice
	 *            The color of tiles that the player has decided to take from the
	 *            given location
	 * @param rowChoice
	 *            The index of the pattern line that the player will add the
	 *            selected tiles to (use -1 to add directly to the floor line)
	 * @throws IllegalArgumentException
	 *             If the chosen tile location has no tiles to take, if tileLocation
	 *             does not have the chosen tile, or if the chosen row is not legal
	 */
	public void makeMove(final int tileLocation, final String tileChoice, final int rowChoice)
			throws IllegalArgumentException {

	}

	/**
	 * This method takes in user input to draw tiles from the bag and put them in
	 * the displays
	 * 
	 * @param in
	 *            The Scanner used to read in the user input
	 */
	private void refillDisplaysFromInput(final Scanner in) {
		System.out.println("Refilling displays from input...");

		// check that all tile locations are empty before taking in user input
		for (final TileLocation tileLocation : this.tileLocations) {
			if (!tileLocation.isEmpty()) {
				throw new IllegalStateException("Tried to refill displays while tiles are still in play");
			}
		}

		// add the lid tiles back into the bag if there are not enough tiles in the bag
		// to fill all of the displays - we can only do this ahead of time because we
		// are receiving user input to determine which tiles are drawn instead of
		// drawing them ourselves randomly
		if (this.tileBag.getNumTilesRemaining() < 4 * (this.tileLocations.length - 1)) {
			this.tileBag.addLidTilesToBag();
		}

		// read in user input and add to the displays from the bag
		for (int i = 1; i < this.tileLocations.length; i++) {
			String newTiles = null;
			boolean tryAgain = true;

			while (tryAgain) {
				tryAgain = false;
				System.out.print("Display " + i + ": ");
				newTiles = in.nextLine().toUpperCase();
				try {
					this.tileBag.removeTilesFromInput(newTiles);
					this.tileLocations[i].addTilesFromInput(newTiles);
				} catch (final IllegalArgumentException | IllegalStateException e) {
					System.out.println(e.getMessage());
					tryAgain = true;
				}
			}

		}
	}

	/**
	 * @return The player who makes the next move from this state
	 */
	public int getCurrentPlayer() {
		return this.currentPlayer;
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
		sb.append("===================================================================\n");
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
