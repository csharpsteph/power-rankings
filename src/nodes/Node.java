package nodes;

import java.util.ArrayList;
import java.util.List;

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
	
	public void setKey(T key)
	{
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
