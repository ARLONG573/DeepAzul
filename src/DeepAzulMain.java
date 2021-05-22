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
