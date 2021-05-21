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

		AzulState state = null;
		boolean tryAgain = true;

		while (tryAgain) {
			tryAgain = false;
			System.out.print("How many players? ");
			final int numPlayers = in.nextInt();
			try {
				state = new AzulState(numPlayers);
			} catch (final IllegalArgumentException e) {
				System.out.println(e.getMessage());
				tryAgain = true;
			}
		}

		System.out.println(state);
		in.close();
	}
}
