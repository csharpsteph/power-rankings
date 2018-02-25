package nodes;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* 
 * This graph class represents a graph whose nodes can take any number of children.
 */

public class Graph<T extends Comparable<T>> implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2887166626086791276L;
	protected Node<T> root;
	
	public Graph()
	{
		setRoot(null);
	}
	
	public Node<T> getRoot()
	{
		return root;
	}
	
	public void setRoot(Node<T> node)
	{
		if (node != null) node.setParent(null);
		root = node;
	}
	
	public boolean isRoot(Node<T> node)
	{
		return node == root;
	}
	
	public Node<T> findNode(T key)
	{
		return findNode(root, key);
	}
	
	private Node<T> findNode(Node<T> root, T key)
	{
		if (root == null) return null;
		Collection<Node<T>> forest = new ArrayDeque<>();
		forest.add(root);
		while (!forest.isEmpty())
		{
			for (Node<T> node: forest)
			{
				if (node.getKey().equals(key))
				{
					return node;
				}
			}
			forest = Node.getChildren(forest);
		}
		
		return null;
	}
	
	public List<Node<T>> getLeaves()
	{
		return root.getLeaves();
	}
	
	public List<Node<T>> getNodesAtLevel(int level)
	{
		if (root == null) 
		{
			throw new RuntimeException("Root is null");
		}
		
		List<Node<T>> forest = new LinkedList<>();
		forest.add(root);
		int currentLevel = 0;
		
		while (currentLevel++ < level)
		{
			forest = Node.getChildren(forest);
			if (forest.isEmpty())
			{
				throw new RuntimeException(String.format(
					"Graph does not go up to level %d (stops at %d).", level, currentLevel-1));
			}
		}
		
		return forest;
	}
}
