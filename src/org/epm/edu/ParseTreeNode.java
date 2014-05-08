package org.epm.edu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

/**
 * A node in the inferred parse tree,
 * Linked up and down in the hierarchy.
 */
public class ParseTreeNode implements TreeNode{

	private String content;
	private ArrayList<ParseTreeNode> children;
	private ParseTreeNode parent;
	private String name;
	
	/**
	 * Create a new node in the parse tree with 
	 * a certain name (usually just the state name)
	 * 
	 * @param name The name of this node
	 */
	public ParseTreeNode(String name) {
		this.parent = null;
		this.children = new ArrayList<ParseTreeNode>();
		this.name = name;
	}

	/**
	 * Set the parent of this node.
	 * Does not set this node as a child of the parent.
	 * 
	 * @param parent The new node to consider as our parent
	 */
	public void setParent(ParseTreeNode parent) {
		this.parent = parent;
	}

	/**
	 * Get our node name
	 * 
	 * @return The name of the node
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of this node
	 * 
	 * @param name The new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieve all children of this node in enumeration form
	 * 
	 * @return All the children of this node
	 */
	@Override
	public Enumeration<ParseTreeNode> children() {
		return Collections.enumeration(children);
	}
	
	/**
	 * Retrieve all children of this node in list form
	 * 
	 * @return All the children of this node
	 */
	public List<ParseTreeNode> childList(){
		return children;
	}

	/**
	 * Whether or not this node allows children.
	 * Always true
	 * 
	 * @return true
	 */
	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	/**
	 * Get the n'th child
	 * 
	 * @param index The index of the child to retrieve
	 * @return The child at the given index 
	 */
	@Override
	public ParseTreeNode getChildAt(int index) {
		return children.get(index);
	}

	/**
	 * Get the amount of children hung on this node
	 * 
	 * @return The amount of children
	 */
	@Override
	public int getChildCount() {
		return children.size();
	}

	/**
	 * Get the index of a given child node
	 * 
	 * @return The index corresponding to the given node
	 */
	@Override
	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	/**
	 * Get the our specified parent node
	 * 
	 * @return The parent tree node
	 */
	@Override
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * Whether or not this node is a leaf in the tree
	 * 
	 * @return True iff this node has no children
	 */
	@Override
	public boolean isLeaf() {
		return getChildCount() == 0;
	}

	/**
	 * Get the content (String) attached to this node
	 * 
	 * @return The contents of this node
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Set the content to associate with this node 
	 * 
	 * @param content The new content
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * Add a child to our children
	 * Automatically calls setParent() in the child
	 * 
	 * @param node The new child
	 */
	public void addChild(ParseTreeNode node){
		node.setParent(this);
		children.add(node);
	}
	
	/**
	 * Check whether or not a certain node is added
	 * as our child.
	 * 
	 * @param node The node to check
	 * @return True iff the node is registered as our child
	 */
	public boolean hasChild(ParseTreeNode node){
		return children.contains(node);
	}
	
	/**
	 * Remove a child from our children
	 * 
	 * @param node The node to decouple
	 */
	public void removeChild(ParseTreeNode node){
		children.remove(node);
	}
	
	/**
	 * Copy the subtree specified by this node.
	 * Will copy this node and hook up copies of all
	 * its children
	 * 
	 * @return The copied subtree
	 */
	public ParseTreeNode copy(){
		ParseTreeNode out = new ParseTreeNode(name);
		out.setParent(parent);
		out.setContent(content);
		for (ParseTreeNode child : children)
			out.addChild(child.copy());
		return out;
	}
	
	/**
	 * Get a pretty representation of this node
	 * 
	 * @return The string representation of this node
	 */
	public String toString(){
		if (content != null)
			return name + " : " + content;
		return name;
	}
	
	/**
	 * Get a chain representation of this node
	 * 
	 * @return A string representation of all parents linking to this node
	 */
	public String debugInfo(){
		String out = getName();
		ParseTreeNode parent = (ParseTreeNode) getParent();
		while (parent != null){
			out = parent.getName() + " -> " + out;
			parent = (ParseTreeNode) parent.getParent();
		}
		return out;
	}
}
