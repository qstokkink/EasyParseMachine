package test.unit;

import static org.junit.Assert.*;

import org.epm.edu.ParseTreeNode;
import org.junit.Before;
import org.junit.Test;

public class ParseTreeNodeTest {

	private ParseTreeNode node;
	
	@Test
	public void testCopy() {
		//Given
		ParseTreeNode child = new ParseTreeNode("child");
		child.setContent("hat");
		node.addChild(child);
		ParseTreeNode parent = new ParseTreeNode("parent");
		node.setParent(parent);
		node.setContent("magic");
		
		//When
		ParseTreeNode copy = node.copy();
		
		//Then
		assertEquals("magic", copy.getContent());
		assertEquals(parent, copy.getParent());
		assertEquals(1, copy.getChildCount());
		assertNotEquals(child, copy.getChildAt(0));
		assertEquals("hat", copy.getChildAt(0).getContent());
	}
	
	@Test
	public void testContent() {
		//When
		node.setContent("magic");
		
		//Then
		assertEquals("magic", node.getContent());
	}
	
	@Test
	public void testChild() {
		//Given
		ParseTreeNode child = new ParseTreeNode("child");
		
		//When
		node.addChild(child);
		
		//Then
		assertEquals(1, node.getChildCount());
		assertTrue(node.childList().contains(child));
		assertEquals(node.children().nextElement(), child);
		assertEquals(node.getChildAt(0), child);
		assertEquals(0, node.getIndex(child));
		assertFalse(node.isLeaf());
	}
	
	@Test
	public void testRemoveChild() {
		//Given
		ParseTreeNode child = new ParseTreeNode("child");
		node.addChild(child);
		
		//When
		node.removeChild(child);
		
		//Then
		assertEquals(0, node.getChildCount());
		assertFalse(node.childList().contains(child));
		assertTrue(node.isLeaf());
	}
	
	@Test
	public void testSetName() {
		//When
		node.setName("hat");
		
		//Then
		assertEquals("hat", node.getName());
	}
	
	@Test
	public void testSetParent() {
		//Given
		ParseTreeNode parent = new ParseTreeNode("parent");
		
		//When
		node.setParent(parent);
		
		//Then
		assertEquals(parent, node.getParent());
		assertTrue(node.isLeaf());
	}
	
	@Test
	public void testCreateWithName() {
		assertEquals("magic", node.getName());
		assertTrue(node.getAllowsChildren());
		assertEquals(0, node.getChildCount());
		assertNull(node.getParent());
		assertTrue(node.isLeaf());
		assertNull(node.getContent());
	}
	
	@Before
	public void setUp(){
		node = new ParseTreeNode("magic");
	}

}
