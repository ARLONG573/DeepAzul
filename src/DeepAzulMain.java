import java.util.Scanner;

import state.AzulState;

/**
 * This class contains the main method for the DeepAzul program.
 * 
 * @author Aaron Tetens
 */
public class DeepAzulMain {

	public static void main(final String[] args) {
		final Scanner in = new Scanner(System.in);

		// initialize game state
		int numPlayers = 0;
		AzulState state = null;
		boolean tryAgain = true;

		while (tryAgain) {
			tryAgain = false;
			System.out.print("How many players? ");
			try {
				numPlayers = Integer.parseInt(in.nextLine());
			} catch (final NumberFormatException e) {
				System.out.println("Please enter a number from 2-4");
				tryAgain = true;
				continue;
			}
			try {
				state = new AzulState(numPlayers, in);
			} catch (final IllegalArgumentException e) {
				System.out.println(e.getMessage());
				tryAgain = true;
			}
		}

		// get the ai player number
		tryAgain = true;
		int aiPlayer = 0;

		while (tryAgain) {
			tryAgain = false;
			System.out.print("Which player is the AI? ");
			try {
				aiPlayer = Integer.parseInt(in.nextLine());
			} catch (final NumberFormatException e) {
				System.out.println("Please enter a number from 0-" + (numPlayers - 1));
				tryAgain = true;
				continue;
			}

			if (aiPlayer < 0 || aiPlayer > numPlayers - 1) {
				System.out.println("Please enter a number from 0-" + (numPlayers - 1));
				tryAgain = true;
			}
		}

		// game play loop
		while (state.getWinningPlayers().isEmpty()) {
			if (state.getCurrentPlayer() == aiPlayer) {
				System.out.println("AI is thinking...");
				state = (AzulState) MCTS.search(state, 1000);
				System.out.println(state);

				if (state.getWinningPlayers().isEmpty() && ((AzulState) state).isRoundOver()) {
					((AzulState) state).refillDisplaysFromInput(in);
					System.out.println(state);
				}
			} else {
				// get human move input
				System.out.println("Player " + state.getCurrentPlayer() + "'s turn!");

				int tileLocation = 0;
				String tileChoice = "";
				int rowChoice = 0;
				tryAgain = true;

				while (tryAgain) {
					tryAgain = false;
					System.out.print("Which tile location? Use 0 for the table, or 1-" + (2 * numPlayers + 1)
							+ " for a display: ");
					try {
						tileLocation = Integer.parseInt(in.nextLine());
					} catch (final NumberFormatException e) {
						System.out.println("Please enter a number from 0-" + (2 * numPlayers + 1));
						tryAgain = true;
						continue;
					}

					System.out.print("Which color? Use one of {B, Y, R, K, W}: ");
					tileChoice = in.nextLine().toUpperCase();

					System.out.print(
							"In which row on the player board will you place the tile(s)? Use 0-4, or -1 to put them directly in your floor line: ");
					try {
						rowChoice = Integer.parseInt(in.nextLine());
					} catch (final NumberFormatException e) {
						System.out.println(
								"Please enter a number from 0-4, or -1 to put the tiles directly in your floor line");
						tryAgain = true;
						continue;
					}

					try {
						state.makeMove(tileLocation, tileChoice, rowChoice, true, false, in);
					} catch (final IllegalArgumentException e) {
						System.out.println(e.getMessage());
						tryAgain = true;
						continue;
					}
				}

				System.out.println(state);
			}
		}

		in.close();

		System.out.println("Winning players = " + state.getWinningPlayers());
	}
}
