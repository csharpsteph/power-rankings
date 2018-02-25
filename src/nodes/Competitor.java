package nodes;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

// General function for participants in games.
public abstract class Competitor extends Node<String> implements Comparable<Competitor> {

	private static final long serialVersionUID = -7333953472615249059L;
	
	public Competitor(String name)
	{
		super("COMP_" + name);
	}
	
	public abstract String getName();
	
	@Override
	public abstract String toString();
	
	@Override
	public abstract int compareTo(Competitor comp);
	

}
