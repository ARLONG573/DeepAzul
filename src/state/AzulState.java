package state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

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
	private int nextRoundFirstPlayer;

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
		this.nextRoundFirstPlayer = -1;

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
	 * @param in
	 *            The Scanner used to read in the user input to refill the game
	 *            displays
	 * @throws IllegalArgumentException
	 *             If the chosen tile location has no tiles to take, if tileLocation
	 *             does not have the chosen tile, or if the chosen row is not legal
	 */
	public void makeMove(final int tileLocation, final String tileChoice, final int rowChoice, final Scanner in)
			throws IllegalArgumentException {

		// check that the tile location is a valid number
		if (tileLocation < 0 || tileLocation > this.tileLocations.length - 1) {
			throw new IllegalArgumentException("Tried to make a move from tile location " + tileLocation + " (0-"
					+ (this.tileLocations.length - 1) + " required");
		}

		// check that tile location is not empty
		if (this.tileLocations[tileLocation].isEmpty()) {
			throw new IllegalArgumentException(
					"Tried to take a tile from an empty tile location (" + tileLocation + ")");
		}

		// check that tileChoice is formatted correctly
		if (!Pattern.matches("[BYRKW]", tileChoice)) {
			throw new IllegalArgumentException(
					"Tried to take an invalid tile (" + tileChoice + ", one of {B, Y, R, K, W} required)");
		}

		// check that the tile location has the chosen tile
		if (!this.tileLocations[tileLocation].hasTile(tileChoice)) {
			throw new IllegalArgumentException("Tried to take " + tileChoice + " from tile location " + tileLocation
					+ ", but there is no such tile there");
		}

		// check that the row choice is a valid number
		if (rowChoice < -1 || rowChoice > 4) {
			throw new IllegalArgumentException("Tried to add tiles to row " + rowChoice
					+ " (0-4 required, or -1 to add tiles directly to your floor line");
		}

		// check that the chosen color is allowed to be placed in the chosen row
		if (rowChoice != -1 && !this.playerBoards[currentPlayer].isLegalPlacement(tileChoice, rowChoice)) {
			throw new IllegalArgumentException(
					"Tried to add the color " + tileChoice + " to row " + rowChoice + ", but it is not legal to do so");
		}

		// if we haven't thrown an exception yet, then the turn is valid

		// remove all of the chosen color from the chosen location
		final int numRemoved = this.tileLocations[tileLocation].removeAll(tileChoice);

		// add the tiles to the given row and save the amount of tiles that need to be
		// sent to the box due to floor line overflow
		final int numExcess = this.playerBoards[currentPlayer].addTiles(numRemoved, tileChoice, rowChoice);

		// add excess tiles to the lid of the box
		if (numExcess > 0) {
			this.tileBag.addTilesToLid(numExcess, tileChoice);
		}

		// if we took from the table, add the first player tile to the floor line if no
		// one has taken it yet
		if (tileLocation == 0) {
			final Table table = (Table) this.tileLocations[0];

			if (table.hasFirstPlayerTile()) {
				table.removeFirstPlayerTile();
				this.playerBoards[currentPlayer].addFirstPlayerTile();
				this.nextRoundFirstPlayer = this.currentPlayer;
			}
		} else {
			// if we took from a display, move the remaining tiles onto the table
			final Map<String, Integer> remainingTiles = this.tileLocations[tileLocation].removeAllTiles();
			this.tileLocations[0].addTiles(remainingTiles);
		}

		// the turn is over, so update the last player
		this.lastPlayer = this.currentPlayer;

		// if the round is not over, update current player and return
		boolean roundOver = true;
		for (final TileLocation location : this.tileLocations) {
			if (!location.isEmpty()) {
				roundOver = false;
				break;
			}
		}

		if (!roundOver) {
			this.currentPlayer = (this.currentPlayer < (this.tileLocations.length - 2) / 2 - 1) ? this.currentPlayer + 1
					: 0;
			return;
		}

		// if the round is over, proceed to the scoring phase and set up next round if
		// the game is not over

		// scoring
		for (final PlayerBoard playerBoard : this.playerBoards) {
			final Map<String, Integer> tilesToLid = playerBoard.doScoring();
			this.tileBag.addTilesToLid(tilesToLid);
		}

		// next round setup
		this.currentRound++;
		this.currentPlayer = this.nextRoundFirstPlayer;

		if (this.currentRound <= 3) {
			this.refillDisplaysFromInput(in);
			((Table) this.tileLocations[0]).addFirstPlayerTile();
			this.nextRoundFirstPlayer = -1;
		}
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
		final List<Integer> winningPlayers = new ArrayList<>();

		// TODO implement this method

		return winningPlayers;
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
		sb.append("Player to start next round = " + this.nextRoundFirstPlayer + "\n");
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
