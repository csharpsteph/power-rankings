package metrics;

import nodes.Competitor;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* A convenience class, pairing a Competitor and a corresponding Comparable value, usually a Record
 * or a power rating. */
public class CompetitorComparablePair<C extends Competitor, T extends Comparable<T>>
{
	C competitor;
	T value;
	
	CompetitorComparablePair(C competitor, T value)
	{
		this.competitor = competitor;
		this.value = value;
	}
	
	public C competitor()
	{
		return competitor;
	}
	
	public String competitorName()
	{
		return competitor.getName();
	}
	
	public T value()
	{
		return value;
	}
	
	public String toString()
	{
		return String.format("%s: %s", competitor, value);
	}
}