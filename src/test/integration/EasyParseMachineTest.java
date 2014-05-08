package test.integration;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.swing.tree.TreeModel;

import org.epm.edu.EasyParseMachine;
import org.epm.edu.ParseTreeModel;
import org.epm.edu.ParseTreeNode;
import org.epm.edu.State;
import org.epm.edu.StateAdapter;
import org.epm.edu.statechanges.Accept;
import org.epm.edu.statechanges.Closure;
import org.epm.edu.statechanges.Consume;
import org.epm.edu.statechanges.Fail;
import org.epm.edu.statechanges.Goto;
import org.epm.edu.statechanges.Guess;
import org.epm.edu.statechanges.IStateChange;
import org.epm.edu.statechanges.UnknownStateChangeException;
import org.epm.edu.statechanges.Split;
import org.junit.Test;

public class EasyParseMachineTest {
	
	/**
	 * Consume the "1" with the "test" state, 
	 * Guess "test2" or "fail"
	 * Produce
	 *   -> test : 
	 *      -> test2 :
	 * Produce
	 *   -> test : 
	 *      -> fail :
	 *   
	 * Closure on EOF with the "test2" state
	 * Fail on EOF with the "fail" state
	 * Produce
	 *   -> test : 
	 *      -> test2 : 
	 */
	@Test
	public void testSplit() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("1");
		
		machine.addState(new SomethingOrOtherOnNumber(new Split("test2", "fail"), new Fail()),	"test");
		machine.addState(new SomethingOrGotoOnEmpty(new Closure(), "fail"), 					"test2");
		machine.addState(new ConsumeOrSomethingOnNumber(new Fail()),							"fail");
		
		//When
		ParseTreeModel model = machine.parse("test");
		ParseTreeNode root = model.getRoot();
		
		//Then
		assertNotEquals(null, root);
		assertFalse(machine.isAmbiguous());
		assertEquals(1, root.getChildCount());
		assertEquals("test", root.getName());
		assertEquals("test2", root.getChildAt(0).getName());
	}
	
	/**
	 * Guess on EOF with the "test" state
	 * Produce
	 *   -> test :
	 *      -> test2 : 
	 * Produce
	 *   -> test :
	 *      -> test3 : 
	 *      
	 * Closure on EOF with "test2" and "test3" state => ambiguous
	 */
	@Test
	public void testAmbiguous() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		machine.addState(new SomethingOrFailOnEmpty(new Guess("test2", "test3")), 	"test");
		machine.addState(new SomethingOrFailOnEmpty(new Closure()), 				"test2");
		machine.addState(new SomethingOrFailOnEmpty(new Closure()), 				"test3");
		
		//When
		ParseTreeModel model = machine.parse("test");
		ParseTreeNode root = model.getRoot();
		
		//Then
		assertNotEquals(null, root);
		assertTrue(machine.isAmbiguous());
		assertEquals(1, root.getChildCount());
		assertEquals("test", root.getName());
	}
	
	/**
	 * Throw exception on invalid goto state
	 */
	@Test(expected=UnknownStateChangeException.class)
	public void testInvalidGuess() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		machine.addState(new SomethingOrFailOnEmpty(new Guess("magic")), "test");
		
		//Then
		machine.parse("test");
	}
	
	/**
	 * Throw exception on invalid goto state
	 */
	@Test(expected=UnknownStateChangeException.class)
	public void testInvalidSplit() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		machine.addState(new SomethingOrFailOnEmpty(new Split("magic")), "test");
		
		//Then
		machine.parse("test");
	}
	
	/**
	 * Throw exception on invalid goto state
	 */
	@Test(expected=UnknownStateChangeException.class)
	public void testInvalidGoto() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		machine.addState(new SomethingOrFailOnEmpty(new Goto("magic")), "test");
		
		//Then
		machine.parse("test");
	}
	
	/**
	 * Throw exception on invalid start state
	 */
	@Test(expected=UnknownStateChangeException.class)
	public void testInvalidStart() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		
		//Then
		machine.parse("test");
	}
	
	/**
	 * Fail on EOF with the "test" state
	 * Produce
	 *   null
	 */
	@Test
	public void testFail() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		machine.addState(new SomethingOrFailOnEmpty(new Fail()), "test");
		
		//When
		TreeModel model = machine.parse("test");
		
		//Then
		assertNull(model);
	}
	
	/**
	 * Do not consume the "1" with the "test2" state
	 * Goto "test"
	 * Produce
	 *   -> test2 : 
	 *   
	 * Consume the "1" with the "test" state
	 * Reenter "test"
	 * Produce
	 *   -> test2 : 
	 *      -> test : "magic"
	 *      
	 * Cannot consume EOF with the "test" state
	 * Return to "test2"
	 * Produce
	 *   -> test2 : 
	 *      -> test : "magic"
	 *      
	 * Closure on EOF with the "test2" state
	 * Produce
	 *   -> test2 : 
	 *      -> test : "magic"
	 */
	@Test
	public void testGotoAccept() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("1");
		machine.addState(new ConsumeOrSomethingOnNumber(new Accept("magic")),	"test");
		machine.addState(new SomethingOrGotoOnEmpty(new Closure(), "test"), 	"test2");
		
		//When
		ParseTreeModel model = machine.parse("test2");
		ParseTreeNode root = model.getRoot();
		
		//Then
		assertNotEquals(null, root);
		assertFalse(machine.isAmbiguous());
		assertEquals(1, root.getChildCount());
		assertEquals("magic", root.getChildAt(0).getContent());
	}
	
	/**
	 * Do not consume the "1" with the "test2" state
	 * Goto "test"
	 * Produce
	 *   -> test2 : 
	 *   
	 * Consume the "1" with the "test" state
	 * Reenter "test"
	 * Produce
	 *   -> test2 : 
	 *      -> test :
	 *      
	 * Cannot consume EOF with the "test" state
	 * Return to "test2"
	 * Produce
	 *   -> test2 : 
	 *      -> test :
	 *      
	 * Closure on EOF with the "test2" state
	 * Produce
	 *   -> test2 : 
	 *      -> test :
	 */
	@Test
	public void testGotoAcceptWOContent() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("1");
		machine.addState(new ConsumeOrSomethingOnNumber(new Accept()),	"test");
		machine.addState(new SomethingOrGotoOnEmpty(new Closure(), "test"), 	"test2");
		
		//When
		ParseTreeModel model = machine.parse("test2");
		ParseTreeNode root = model.getRoot();
		
		//Then
		assertNotEquals(null, root);
		assertFalse(machine.isAmbiguous());
		assertEquals(1, root.getChildCount());
		assertEquals(null, root.getChildAt(0).getContent());
	}
	
	/**
	 * Closure on EOF with the "test" state
	 * Produce
	 *   -> test : 
	 */
	@Test
	public void testClosure() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		machine.addState(new SomethingOrFailOnEmpty(new Closure()), "test");

		//When
		ParseTreeModel model = machine.parse("test");
		ParseTreeNode root = model.getRoot();
		
		//Then
		assertNotEquals(null, root);
		assertFalse(machine.isAmbiguous());
		assertEquals(0, root.getChildCount());
		assertEquals("test", root.getName());
		assertEquals(null, root.getContent());
	}
	
	/**
	 * Closure on EOF with the "test" state
	 * Produce
	 *   -> test : "magic"
	 */
	@Test
	public void testClosureWContent() throws IOException{
		//Given
		EasyParseMachine machine = new EasyParseMachine("");
		machine.addState(new SomethingOrFailOnEmpty(new Closure("magic")), "test");

		//When
		ParseTreeModel model = machine.parse("test");
		ParseTreeNode root = model.getRoot();
		
		//Then
		assertNotEquals(null, root);
		assertFalse(machine.isAmbiguous());
		assertEquals(0, root.getChildCount());
		assertEquals("test", root.getName());
		assertEquals("magic", root.getContent());
	}
	
	/******
	 ****** END OF TEST CASES, START OF HELPER CLASSES
	 ******/

	/**
	 * If EOF is read: return response
	 * Else: return fail
	 */
	private class SomethingOrFailOnEmpty extends StateAdapter{

		private final IStateChange response;
		private boolean visited = false;
		
		public SomethingOrFailOnEmpty(IStateChange change){
			response = change;
		}
		
		public SomethingOrFailOnEmpty(IStateChange change, boolean visited){
			this(change);
			this.visited = visited;
		} 
		
		@Override
		public IStateChange feed(int c) {
			if (visited)
				return new Closure();
			visited = true;
			if (c == -1)
				return response;
			else
				return new Fail();
		}
		
		@Override
		public State copy() {
			return new SomethingOrFailOnEmpty(response, visited);
		}
		
	}
	
	/**
	 * If digit is read: consume
	 * Else: return response
	 */
	private class ConsumeOrSomethingOnNumber extends StateAdapter{

		private final IStateChange response;
		private boolean visited = false;
		
		public ConsumeOrSomethingOnNumber(IStateChange change){
			response = change;
		}
		
		public ConsumeOrSomethingOnNumber(IStateChange change, boolean visited){
			this(change);
			this.visited = visited;
		}
		
		@Override
		public IStateChange feed(int c) {
			if (Character.isDigit(c))
				return new Consume();
			else
				return response;
		}
		
		@Override
		public State copy() {
			return new ConsumeOrSomethingOnNumber(response, visited);
		}
		
	}
	
	/**
	 * If digit is read: consume
	 * Else: return response
	 */
	private class SomethingOrOtherOnNumber extends StateAdapter{

		private final IStateChange response;
		private final IStateChange response2;
		private boolean visited = false;
		
		public SomethingOrOtherOnNumber(IStateChange change, IStateChange change2){
			response = change;
			response2 = change2;
		}
		
		public SomethingOrOtherOnNumber(IStateChange change, IStateChange change2, boolean visited){
			this(change, change2);
			this.visited = visited;
		}
		
		@Override
		public IStateChange feed(int c) {
			if (Character.isDigit(c))
				return response;
			else
				return response2;
		}
		
		@Override
		public State copy() {
			return new SomethingOrOtherOnNumber(response, response2, visited);
		}
		
	}
	
	/**
	 * If EOF is read: return response
	 * Else: goto other state
	 */
	private class SomethingOrGotoOnEmpty extends StateAdapter{

		private final IStateChange response;
		private final String otherState;
		private boolean visited = false;
		
		public SomethingOrGotoOnEmpty(IStateChange change, String other){
			response = change;
			otherState = other;
		}
		
		public SomethingOrGotoOnEmpty(IStateChange change, String other, boolean visited){
			this(change, other);
			this.visited = visited;
		}
		
		@Override
		public IStateChange feed(int c) {
			if (c == -1)
				return response;
			else
				return new Goto(otherState);
		}

		@Override
		public State copy() {
			return new SomethingOrGotoOnEmpty(response, otherState, visited);
		}

	}
	
}
