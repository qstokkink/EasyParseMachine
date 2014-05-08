package org.epm.edu;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * A TreeModel for ParseTreeNodes.
 * Compatible with JTree.
 */
public class ParseTreeModel extends DefaultTreeModel{

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new ParseTreeModel with a certain root
	 * 
	 * @param root The root
	 */
	public ParseTreeModel(TreeNode root) {
		super(root);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParseTreeNode getChild(Object parent, int index) {
		return (ParseTreeNode) super.getChild(parent, index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParseTreeNode getRoot() {
		return (ParseTreeNode) super.getRoot();
	}

}
