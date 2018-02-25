package nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/*
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

public abstract class Node<T extends Comparable<T>> implements java.io.Serializable {
	
	private static final long serialVersionUID = 2581745483599503855L;
	protected T t_key;
	protected Node<T> n_parent;
	protected List<Node<T>> n_children;
	
	public Node(T t_key)
	{
		setKey(t_key);
		n_children = new ArrayList<>();
	}
	
	public T getKey()
	{
		return t_key;
	}
	
	@SuppressWarnings("unchecked")
	public void setKey(T key)
	{
		if (key instanceof String)
		{
			StringBuilder keyStr = new StringBuilder((String)key);
			char[] disallowedChrs = { ',', '=', '-' };
			int index;
			for (char c: disallowedChrs)
			{
				while ((index = keyStr.indexOf("" + c)) >= 0)
				{
					keyStr.deleteCharAt(index);
				}
			}
			key = (T)keyStr.toString();
		}
		this.t_key = key;
	}
	
	public Node<T> getParent()
	{
		return n_parent;
	}
	
	public void setParent(Node<T> n_parent)
	{
		this.n_parent = n_parent;
	}
	
	public boolean hasParent()
	{
		return n_parent != null;
	}
	
	public List<Node<T>> getChildren()
	{
		return n_children;
	}
	
	public static <T extends Comparable<T>> List<Node<T>> getChildren(Collection<Node<T>> nodes)
	{
		List<Node<T>> successors = new LinkedList<>();
		for (Node<T> n: nodes)
		{
			if (n == null) continue;
			successors.addAll(n.getChildren());
		}
		return successors;
	}
	
	public List<Node<T>> getLeaves()
	{
		ArrayList<Node<T>> leavesList = new ArrayList<>();
		
		leavesList.add(this);
		return getLeavesAux(leavesList);
	}

	private static <T extends Comparable<T>> List<Node<T>> getLeavesAux(ArrayList<Node<T>> leavesList)
	{
		int index = 0;
		Node<T> n;
		while (index < leavesList.size())
		{
			n = leavesList.get(index);
			if (n.hasChildren())
			{
				leavesList.remove(n);
				leavesList.addAll(n.getChildren());
			}
			else {
				index++;
			}
			
		}
		
		
		return leavesList;
	}
	
	public void addChild(Node<T> n_child)
	{
		n_children.add(n_child);
		n_child.n_parent = this;
	}
		
	public boolean hasChildren()
	{
		return !n_children.isEmpty();
	}
}
