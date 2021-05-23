import java.util.Scanner;

import state.AzulState;

/**
 * This class contains the main method for the DeepAzul program.
 * 
 * @author Aaron Tetens
 */
public class DeepAzulMain {

	private static final Scanner in = new Scanner(System.in);

	public static void main(final String[] args) {
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
				state = (AzulState) MCTS.search(state, 1000);
			} else {
				// TODO input human move
			}
			System.out.println(state);
		}

		in.close();
	}
}
