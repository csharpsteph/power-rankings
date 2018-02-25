package metrics;

import java.util.ArrayList;
import java.util.List;

import parse.AbstractGame;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/*
 * An alias for an ArrayList of a particular sub-type of AbstractGame.
 */
public class GameList<GameType extends AbstractGame<?>> extends ArrayList<GameType> {
	
	private static final long serialVersionUID = 4985063645306951804L;

	public GameList(List<GameType> list)
	{
		this.addAll(list);
	}
	
	public GameList(GameList<GameType> list)
	{
		this.addAll(list);
	}
	
}
