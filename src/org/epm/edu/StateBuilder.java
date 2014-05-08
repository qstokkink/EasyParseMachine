package org.epm.edu;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import org.epm.edu.statechanges.Accept;
import org.epm.edu.statechanges.Closure;
import org.epm.edu.statechanges.Fail;
import org.epm.edu.statechanges.Goto;
import org.epm.edu.statechanges.IStateChange;

/**
 * Parse a state and following states and construct
 * a parse tree in the mean time.
 * 
 * A single StateBuilder takes care of a single parse
 * chain. For instance when we have two states that
 * accept a character, for both states we create a 
 * StateBuilder to handle the possible resulting tree.
 * 
 * The StateBuilder maintains a subtree that is attached
 * bottom-up to the non-ambiguous part of the tree.
 */
public class StateBuilder {

	private State current;
	private ParseTreeNode root;
	private ParseTreeNode node = null;
	
	private static AtomicLong ids = new AtomicLong(0);
	private long myid = 0;
	
	/**
	 * Create a new StateBuilder with a certain starting
	 * state.
	 * 
	 * @param start the start state
	 */
	public StateBuilder(State start, String startName){
		this.current = start;
		this.root = new ParseTreeNode(startName);
		this.node = root;
		
		myid = ids.incrementAndGet();
	}
	
	/**
	 * Create a new StateBuilder with a certain starting
	 * state and a given subtree root.
	 * 
	 * @param current The start state
	 * @param root The root to use for this tree
	 */
	protected StateBuilder(State current, String currentName, ParseTreeNode root){
		this.root = root;
		this.node = new ParseTreeNode(currentName);
		node.setParent(this.root);
		this.current = current;
		
		myid = ids.incrementAndGet();
	}
	
	/**
	 * Handle reading a character and update the tree
	 * according to the state change the user requires.
	 * 
	 * @param f The read character (or -1 if EOF)
	 * @return The statechange to feed to the machine
	 */
	public IStateChange feed(int f){
		IStateChange isc = current.feed(f);

		/*
		 * Update the tree
		 */
		if (isc instanceof Accept || isc instanceof Closure){
			/*
			 * If the user accepts the current state,
			 * we update the subtree by linking its parent
			 * to our root. Making the top-down tree match
			 * up with our bottom-up representation. 
			 */
			if (isc instanceof Accept){
				node.setContent(((Accept) isc).getContent());
			} else {
				node.setContent(((Closure) isc).getContent());
			}
			ParseTreeNode parent = (ParseTreeNode) node.getParent();
			if (parent != null){
				if (!parent.hasChild(node))
					parent.addChild(node);
			}
			node = parent;
		} else if (isc instanceof Goto){
			/*
			 * In case of a goto we move into a new node
			 */
			Goto got = (Goto) isc;
			ParseTreeNode child = new ParseTreeNode(got.getNextState());
			child.setParent(node);
			node = child;
		} 

		/*
		 * Reset the State's internal state upon finishing
		 * with the current input
		 */
		if (isc instanceof Accept || isc instanceof Fail)
			current.reset();
		
		return isc;
	}
	
	/**
	 * Get the registered root of our subtree
	 * 
	 * @return Our root (can be null if we are top root)
	 */
	public ParseTreeNode getSubtreeRoot(){
		return root;
	}
	
	/**
	 * Get the top root node (not null)
	 * 
	 * @return The top most node in the full tree (not just the subtree)
	 */
	public ParseTreeNode getRealRoot(){
		ParseTreeNode out = root;
		while (out.getParent() != null)
			out = (ParseTreeNode) out.getParent();
		return out;
	}
	
	/**
	 * Set the state of this builder
	 * 
	 * @param s The new state
	 */
	public void setState(State s){
		current = s;
	}
	
	protected State getState(){
		return current;
	}
	
	/**
	 * Get the name of the current node we are
	 * building in the tree
	 * 
	 * @return The name of the node being handled
	 */
	public String getCurrentNodeName(){
		if (node != null)
			return node.getName();
		return null;
	}
	
	/**
	 * Get the name of the parent of the current 
	 * node we are building in the tree
	 * 
	 * @return The name of the parent of the node being handled
	 */
	public String getNodeParentName(){
		if (node != null && node.getParent() != null)
			return ((ParseTreeNode) this.node.getParent()).getName();
		return null;
	}
	
	/**
	 * Get info on the tree chain of the current node.
	 * May return $EPM_NO_STATE if the current node is null.
	 * 
	 * @return Tree info on the current node
	 */
	public String currentNodeInfo(){
		if (node == null)
			return "$EPM_NO_STATE";
		return node.debugInfo();
	}
	
	/**
	 * Branch the current node off into several states
	 * Used for handling ambiguity
	 * 
	 * @param nameOverrides The states of the (detached) child nodes 
	 * @return A new set of StateBuilders for the new child nodes
	 */
	public Collection<StateBuilder> split(Collection<String> nameOverrides){
		HashSet<StateBuilder> sbs = new HashSet<StateBuilder>();
		if (nameOverrides.size() == 0)
			return sbs;
		for (String state : nameOverrides)
			sbs.add(new StateBuilder(null, state, node.copy()));
		return sbs;
	}
	
	public long getBuilderId(){
		return myid;
	}
}
