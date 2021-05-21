package state;

import java.util.List;

import api.GameState;

/**
 * This class defines a single game state for Azul. Implementing the GameState
 * interface allows this class to use my MCTS implementation.
 * 
 * @author Aaron Tetens
 */
public class AzulState implements GameState {

	@Override
	public int getLastPlayer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<GameState> getNextStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GameState getRandomNextState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getWinningPlayers() {
		// TODO Auto-generated method stub
		return null;
	}

}
