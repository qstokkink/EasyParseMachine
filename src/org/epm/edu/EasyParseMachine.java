package org.epm.edu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import org.epm.edu.statechanges.*;

/**
 * The main class to keep track of parse states
 */
public class EasyParseMachine {

	//Attributes
	private InputStream feeder;
	private HashMap<Long, Stack<State>> states = new HashMap<Long, Stack<State>>();
	private HashMap<String, State> backupStates = new HashMap<String, State>();
	
	//Settings
	private boolean compressWhitespace = false;
	private boolean cWKeepNewlines = false;
	private PrintStream debugStream;
	
	//States
	private boolean isAmbiguous = false;
	private LinkedList<String> deletionSnapshot = new LinkedList<String>();
	
	/**
	 * Create a new EPM without an InputStream,
	 * set this later, it's kind of important.
	 */
	public EasyParseMachine(){
	}
	
	/**
	 * Create a new EPM with a given character InputStream
	 * 
	 * @param is The InputStream to read characters from
	 */
	public EasyParseMachine(InputStream is){
		feeder = is;
	}
	
	/**
	 * Create a new EPM with a given input String
	 * 
	 * @param s The String to construct an InputStream from
	 */
	public EasyParseMachine(String s){
		feeder = new ByteArrayInputStream(s.getBytes());
	}
	
	/**
	 * Set the character InputStream for this EPM
	 * 
	 * @param is The InputStream to use
	 */
	public void setInput(InputStream is){
		feeder = is;
	}
	
	/**
	 * Set the character input String
	 * 
	 * @param s The String to use
	 */
	public void setInput(String s){
		feeder = new ByteArrayInputStream(s.getBytes());
	}
	
	/**
	 * If set, this will provide debug messages
	 * to the given PrintStream.
	 * 
	 * @param out The PrintStream to print debug messages to
	 */
	public void setVerbose(PrintStream out){
		debugStream = out;
	}
	
	/**
	 * Register a State instance for use by the EPM
	 * 
	 * @param s The State to register
	 */
	public void addState(State s){
		addState(s, s.getName());
	}
	
	/**
	 * Replace all whitespace blocks with a single space.
	 * You can also choose not to wrap these over newline characters.
	 * Ergo: <br>
	 * keepNewlines == false: "a\t\n\tb" -> "a b" <br> 
	 * keepNewlines == true: "a\t\n\tb" -> "a \n b"
	 * 
	 * @param compress Whether or not to compress
	 * @param keepNewlines Whether or not to compress over newlines
	 */
	public void setCompressWhitespace(boolean compress, boolean keepNewlines){
		compressWhitespace = compress;
		cWKeepNewlines = keepNewlines;
	}
	
	/**
	 * Register a State instance for use by the EPM,
	 * bind it to a name that is different than the
	 * one specified by the s.getName()
	 * 
	 * This allows a State instance to be reused
	 * 
	 * @param s The State to register
	 * @param override The name override for this state
	 */
	public void addState(State s, String override){
		backupStates.put(override, s);
	}

	/**
	 * Remove a State instance from our state change
	 * handlers
	 * 
	 * @param s The State to remove from our handlers
	 */
	public void removeState(String s){
		backupStates.remove(s);
	}
	
	/**
	 * Reset the Machine to the initial state before
	 * any character was read
	 */
	public void resetMachine(){
		states.clear();
	}
	
	/**
	 * Get all the registered state names
	 * 
	 * @return All of the state names in use
	 */
	public Collection<String> getStateNames(){
		return backupStates.keySet();
	}
	
	/**
	 * Get all the state instances in use
	 * 
	 * @return All of the States in use
	 */
	public Collection<State> getStates(){
		return backupStates.values();
	}
	
	/**
	 * Retrieve a localized State for a certain context
	 * 
	 * @param sb The state building context
	 * @param state The state to retrieve
	 * @return The State belonging to the given StateBuilder or null if state == null
	 */
	private State getNewState(StateBuilder sb, String state){
		if (state == null)
			return null;
		if (!states.containsKey(sb.getBuilderId()))
			states.put(sb.getBuilderId(), new Stack<State>());
		if (!backupStates.containsKey(state))
			throw new UnknownStateChangeException("Unknown state: " + state);
		states.get(sb.getBuilderId()).push(backupStates.get(state).copy());
		return states.get(sb.getBuilderId()).peek();
	}
	
	private State getNewState(StateBuilder parent, StateBuilder sb, String state){
		Stack<State> stack = states.get(parent.getBuilderId());
		Stack<State> child = new Stack<State>();
		Iterator<State> oldies = stack.iterator();
		while (oldies.hasNext()){
			child.add(oldies.next().copy());
		}
		states.put(sb.getBuilderId(), child); 
		return getNewState(sb, state);
	}
	
	/**
	 * Re-enter a previous state
	 * 
	 * @param sb The state building context
	 * @return The State the StateBuilder was previously in or null if on top
	 */
	private State getPreviousState(StateBuilder sb){
		if (!states.containsKey(sb.getBuilderId()))
			return null;
		Stack<State> stack = states.get(sb.getBuilderId());
		if (stack.isEmpty())
			return null;
		stack.pop();
		if (stack.isEmpty())
			return null;
		return stack.peek();
	}
	
	/**
	 * Start reading the input from the previously specified
	 * InputStream given a certain starting State with a certain
	 * state name override. 
	 * 
	 * @param sName The starting state name (override)
	 * @return The TreeModel of the parsed input
	 * @throws IOException If the InputStream failed
	 * @throws NullPointerException If someone forgot to set the InputStream 
	 * @throws UnknownStateChangeException If we were directed to an unregistered state
	 */
	public ParseTreeModel parse(String sName) throws IOException, UnknownStateChangeException{
		if (!backupStates.containsKey(sName))
			throw new UnknownStateChangeException("Unknown start state: " + sName);
		
		Set<StateBuilder> builders = new LinkedHashSet<StateBuilder>();
		StateBuilder startBuilder = new StateBuilder(null, sName);
		State startState = getNewState(startBuilder, sName);
		startBuilder.setState(startState);
		builders.add(startBuilder);
		
		isAmbiguous = false;
		boolean justReadWhitespace = false;
		
		int f = -1;
		do {
			f = feeder.read();
			
			if (compressWhitespace && f!=-1 && Character.isWhitespace((char) f) && !(cWKeepNewlines && (char) f == '\n')){
				if (justReadWhitespace){
					continue;
				} else {
					f = ' ';
					justReadWhitespace = true;
				}
			} else {
				justReadWhitespace = false;
			}
			
			debugln("[EPM] FEED: "+(char)f);
			
			Set<StateBuilder> deletions = new LinkedHashSet<StateBuilder>();
			Set<StateBuilder> additions = new LinkedHashSet<StateBuilder>();
			Stack<StateBuilder> gotos = new Stack<StateBuilder>();
			
			//Give everyone a new character
			for (StateBuilder sb : builders){
				IStateChange sc = sb.feed(f);
				handleChange(sb, sc, additions, deletions, gotos);
			}
			
			debugln("[EPM] SECONDARY FEED: "+(char)f);
			
			while (!additions.isEmpty() || !deletions.isEmpty() || !gotos.isEmpty()){
				//Add requested new builders
				for (StateBuilder sb : additions)
					builders.add(sb);
				additions.clear();
		
				//Delegate the character to the next consumer for all 
				//builders
				while (!gotos.isEmpty()){
					StateBuilder sb = gotos.pop();
					IStateChange sc = sb.feed(f);
					handleChange(sb, sc, additions, deletions, gotos);
				}
				
				//Remove all requested builder removals
				deletionSnapshot.clear();
				for (StateBuilder sb : deletions){
					builders.remove(sb);
					deletionSnapshot.add(sb.getCurrentNodeName());
					states.remove(sb.getBuilderId());
				}
				deletions.clear();
			}

			if (debugStream != null){
				debugln("[EPM] FEED DONE: # builders left: " + builders.size());
				for (StateBuilder sb : builders){
					if (sb.getCurrentNodeName() != null){
						debuglnt("[" + sb.getBuilderId() + "]: "+sb.currentNodeInfo());
					} else
						debuglnt("$EPM_END_OF_INPUT");
				}
				debugln("");
			}
		} while (f!=-1&&builders.size()>0);

		if (builders.size() == 0){
			return null;
		}
		
		if (builders.size() > 1)
			isAmbiguous = true;
		
		return new ParseTreeModel(builders.iterator().next().getRealRoot());
	}
	
	/**
	 * Has the previous parse() call left the machine
	 * in an ambiguous state
	 * 
	 * @return If there are multiple parse trees possible
	 */
	public boolean isAmbiguous(){
		return isAmbiguous;
	}
	
	/**
	 * Retrieve the last set of removed states before ending parsing.
	 * Useful to retrieve expected states after failing.
	 * 
	 * @return The collection of state names we removed last
	 */
	public Collection<String> getDeletionSnapshot(){
		return deletionSnapshot;
	}
	
	/**
	 * Delegate a StateChange provided by a StateBuilder
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The change in state we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 */
	private void handleChange(StateBuilder sb, IStateChange isc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		if (isc instanceof Accept)
			handleChange(sb, (Accept) isc, add, del, gotos);
		else if (isc instanceof Closure)
			handleChange(sb, (Closure) isc, add, del, gotos);
		else if (isc instanceof Consume)
			handleChange(sb, (Consume) isc, add, del, gotos);
		else if (isc instanceof Fail)
			handleChange(sb, (Fail) isc, add, del, gotos);
		else if (isc instanceof Goto)
			handleChange(sb, (Goto) isc, add, del, gotos);
		else if (isc instanceof Guess)
			handleChange(sb, (Guess) isc, add, del, gotos);
		else if (isc instanceof Split)
			handleChange(sb, (Split) isc, add, del, gotos);
	}
	
	/**
	 * Handle an Accept update.
	 * We have to revisit the current character.
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The Accept we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 */
	private void handleChange(StateBuilder sb, Accept sc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		sb.setState(getPreviousState(sb));
		debugln("[EPM] [" + sb.getBuilderId() + "]: " + sb.currentNodeInfo() + " -> ACCEPT(" + sc.getContent() + ")");
		gotos.add(sb);
	}
	
	/**
	 * Handle a Closure update.
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The Closure we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 */
	private void handleChange(StateBuilder sb, Closure sc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		sb.setState(getPreviousState(sb));
		debugln("[EPM] [" + sb.getBuilderId() + "]: " + sb.currentNodeInfo() + " CLOSURE(" + sc.getContent() + ")");
	}
	
	/**
	 * Handle a Consume update. We don't need to do anything.
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The Consume we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 */
	private void handleChange(StateBuilder sb, Consume sc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		debugln("[EPM] [" + sb.getBuilderId() + "]: " + sb.currentNodeInfo() + " CONSUME");
	}
	
	/**
	 * Handle a Fail update. Remove this StateBuilder.
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The Fail we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 */
	private void handleChange(StateBuilder sb, Fail sc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		debugln("[EPM] [" + sb.getBuilderId() + "]: " + sb.currentNodeInfo() + " FAIL");
		del.add(sb);
	}
	
	/**
	 * Handle a Goto update. We revisit this builder and feed the same character
	 * to the goto state.
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The Goto we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 * @throws UnknownStateChangeException If the specified state name is not registered
	 */
	private void handleChange(StateBuilder sb, Goto sc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		debugln("[EPM] [" + sb.getBuilderId() + "]: " + sb.currentNodeInfo() + " GOTO " + sc.getNextState());
		if (!backupStates.containsKey(sc.getNextState()))
			throw new UnknownStateChangeException("Unknown state: " + sc.getNextState());
		
		sb.setState(getNewState(sb, (sc.getNextState())));
		gotos.add(sb);
	}
	
	/**
	 * Handle a Guess update. We revisit the ambiguous builders and feed the same character
	 * to these.
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The Guess we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 * @throws UnknownStateChangeException If a specified state name is not registered, or if there are no states to split into
	 */
	private void handleChange(StateBuilder sb, Guess sc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		debugln("[EPM] [" + sb.getBuilderId() + "]: " + sb.currentNodeInfo() + " GUESS " + toString(sc.getNextStates()));
		Collection<StateBuilder> nbuilders = sb.split(sc.getNextStates());
		for (StateBuilder nsb : nbuilders)
			nsb.setState(getNewState(sb, nsb, nsb.getCurrentNodeName()));
		add.addAll(nbuilders);
		gotos.addAll(nbuilders);
		del.add(sb);
	}
	
	/**
	 * Handle a Split update. We do not revisit the new ambiguous StateBuilders. 
	 * 
	 * @param sb The source StateBuilder of this state change
	 * @param isc The Split we are to handle
	 * @param add The Set of additions we can add to if we need to add StateBuilders
	 * @param del The Set of deletions we can add to if we need to remove StateBuilders
	 * @param gotos The Set of StateBuilders that needs to be revisited given the current input
	 * @throws UnknownStateChangeException If a specified state name is not registered, or if there are no states to split into
	 */
	private void handleChange(StateBuilder sb, Split sc, Set<StateBuilder> add, Set<StateBuilder> del, Stack<StateBuilder> gotos){
		debugln("[EPM] [" + sb.getBuilderId() + "]: " + sb.currentNodeInfo() + " SPLIT " + toString(sc.getNextStates()));
		Collection<StateBuilder> nbuilders = sb.split(sc.getNextStates());
		for (StateBuilder nsb : nbuilders)
			nsb.setState(getNewState(sb, nsb, nsb.getCurrentNodeName()));
		add.addAll(nbuilders);
		del.add(sb);
	}
	
	/**
	 * Convert a collection of Strings to a single String
	 * 
	 * @param states The String Collection to implode
	 * @return The String representation
	 */
	private String toString(Collection<String> states){
		String out = "[";
		for (String s : states){
			if ("[".equals(out)){
				out += s;
			} else {
				out += ", " + s;
			}
		}
		return out + "]";
	}
	
	/**
	 * Print a line to the debug stream
	 * 
	 * @param message The message to print
	 */
	private void debugln(String message){
		if (debugStream != null)
			debugStream.println(message.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r"));
	}
	
	/**
	 * Print a line preceded by a tab-character to the debug stream 
	 * 
	 * @param message The message to print
	 */
	private void debuglnt(String message){
		if (debugStream != null)
			debugStream.println("\t" + message.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r"));
	}
}
