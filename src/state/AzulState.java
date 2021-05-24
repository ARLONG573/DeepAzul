package state;

import java.util.ArrayList;
import java.util.HashMap;
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
		this.nextRoundFirstPlayer = -1;

		this.refillDisplaysFromInput(in);
	}

	private AzulState(final AzulState state) {
		this.tileBag = new TileBag(state.tileBag);

		this.tileLocations = new TileLocation[state.tileLocations.length];
		for (int i = 0; i < this.tileLocations.length; i++) {
			if (i == 0) {
				this.tileLocations[i] = new Table((Table) state.tileLocations[i]);
			} else {
				this.tileLocations[i] = new Display((Display) state.tileLocations[i]);
			}
		}

		this.playerBoards = new PlayerBoard[state.playerBoards.length];
		for (int i = 0; i < this.playerBoards.length; i++) {
			this.playerBoards[i] = new PlayerBoard(state.playerBoards[i]);
		}

		this.lastPlayer = state.lastPlayer;
		this.currentPlayer = state.currentPlayer;
		this.nextRoundFirstPlayer = state.nextRoundFirstPlayer;
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
	 * @param performRefill
	 *            Whether or not a potentially necessary display refill will be
	 *            performed (set to false when simulating)
	 * @param randomRefill
	 *            Whether or not any performed refills will be done randomly (as
	 *            opposed to through user input) - this value does not matter if
	 *            performRefill is false
	 * @param in
	 *            The Scanner used to read in the user input to refill the game
	 *            displays - only necessary to define if performRefill is true and
	 *            randomRefill is false
	 * @throws IllegalArgumentException
	 *             If the chosen tile location has no tiles to take, if tileLocation
	 *             does not have the chosen tile, or if the chosen row is not legal
	 */
	public void makeMove(final int tileLocation, final String tileChoice, final int rowChoice,
			final boolean performRefill, final boolean randomRefill, final Scanner in) throws IllegalArgumentException {

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
		if (!this.isRoundOver()) {
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
		if (this.nextRoundFirstPlayer != -1) {
			this.currentPlayer = this.nextRoundFirstPlayer;
		} else {
			this.currentPlayer = (this.currentPlayer < (this.tileLocations.length - 2) / 2 - 1) ? this.currentPlayer + 1
					: 0;
		}

		if (this.getWinningPlayers().isEmpty()) {
			if (performRefill) {
				if (randomRefill) {
					this.refillDisplaysRandomly();
				} else {
					this.refillDisplaysFromInput(in);
				}
			}

			((Table) this.tileLocations[0]).addFirstPlayerTile();
			this.nextRoundFirstPlayer = -1;
		}
	}

	/**
	 * Refills the displays randomly. This method assumes that all displays are
	 * empty and that the table has no tiles on it.
	 */
	private void refillDisplaysRandomly() {
		for (int i = 1; i < this.tileLocations.length; i++) {
			for (int count = 0; count < 4; count++) {
				final String tile = this.tileBag.drawRandomTile();
				final Map<String, Integer> tilesToAdd = new HashMap<>();
				tilesToAdd.put(tile, 1);
				this.tileLocations[i].addTiles(tilesToAdd);
			}
		}
	}

	/**
	 * This method takes in user input to draw tiles from the bag and put them in
	 * the displays
	 * 
	 * @param in
	 *            The Scanner used to read in the user input
	 */
	public void refillDisplaysFromInput(final Scanner in) {
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
	 * @return Whether or not the current round is over
	 */
	public boolean isRoundOver() {
		boolean isRoundOver = true;
		for (final TileLocation location : this.tileLocations) {
			if (!location.isEmpty()) {
				isRoundOver = false;
				break;
			}
		}

		return isRoundOver;
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
		final List<GameState> nextStates = new ArrayList<>();

		// if the round is over, then this state is not expandable
		if (this.isRoundOver()) {
			return nextStates;
		}

		// try every combination of tile location, tile choice, and row choice and keep
		// the ones that are valid
		final String tileChoices = "BYRKW";

		for (int tileLocation = 0; tileLocation < this.tileLocations.length; tileLocation++) {
			for (int tileChoiceIndex = 0; tileChoiceIndex < tileChoices.length(); tileChoiceIndex++) {
				final String tileChoice = tileChoices.substring(tileChoiceIndex, tileChoiceIndex + 1);

				// prefer moves that put tiles into rows instead of the floor line
				boolean madeLegalMove = false;
				for (int rowChoice = 0; rowChoice < 5; rowChoice++) {
					final AzulState nextState = new AzulState(this);

					try {
						nextState.makeMove(tileLocation, tileChoice, rowChoice, false, false, null);
					} catch (final IllegalArgumentException e) {
						continue;
					}

					nextStates.add(nextState);
					madeLegalMove = true;
				}

				if (!madeLegalMove) {
					final AzulState nextState = new AzulState(this);

					try {
						nextState.makeMove(tileLocation, tileChoice, -1, false, false, null);
					} catch (final IllegalArgumentException e) {
						continue;
					}

					nextStates.add(nextState);
				}
			}
		}

		return nextStates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GameState getRandomNextState() {
		final AzulState copy = new AzulState(this);

		if (copy.isRoundOver()) {
			copy.refillDisplaysRandomly();
		}

		final List<GameState> nextStates = copy.getNextStates();
		final int randomIndex = (int) (Math.random() * nextStates.size());
		return nextStates.get(randomIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getWinningPlayers() {
		final List<Integer> winningPlayers = new ArrayList<>();

		// check if game is over
		boolean isGameOver = false;
		for (final PlayerBoard playerBoard : this.playerBoards) {
			if (playerBoard.hasCompletedWallRow()) {
				isGameOver = true;
				break;
			}
		}

		// if game is not over, there are no winners to report
		if (!isGameOver) {
			return winningPlayers;
		}

		// if game is over, winner is decided by final score, then by most completed
		// rows
		int finalScoreOfBest = -1;
		int numCompletedWallRowsOfBest = -1;

		for (int i = 0; i < this.playerBoards.length; i++) {
			final int finalScore = this.playerBoards[i].getFinalScore();
			final int numCompletedWallRows = this.playerBoards[i].getNumCompletedWallRows();

			if (finalScore > finalScoreOfBest) {
				winningPlayers.clear();
				winningPlayers.add(i);

				finalScoreOfBest = finalScore;
				numCompletedWallRowsOfBest = numCompletedWallRows;
			} else if (finalScore == finalScoreOfBest) {
				if (numCompletedWallRows > numCompletedWallRowsOfBest) {
					winningPlayers.clear();
					winningPlayers.add(i);

					numCompletedWallRowsOfBest = numCompletedWallRows;
				} else if (numCompletedWallRows == numCompletedWallRowsOfBest) {
					winningPlayers.add(i);
				}
			}
		}

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
