package test;

import java.io.IOException;

import org.epm.edu.EPMVisualDebugger;
import org.epm.edu.EasyParseMachine;
import org.epm.edu.StateAdapter;
import org.epm.edu.statechanges.Accept;
import org.epm.edu.statechanges.Closure;
import org.epm.edu.statechanges.Consume;
import org.epm.edu.statechanges.Fail;
import org.epm.edu.statechanges.Goto;
import org.epm.edu.statechanges.IStateChange;
import org.epm.edu.statechanges.Split;

public class EPMManualTest {

	/**
	 * Launch the VisualDebugger for the following Grammar:								<br>
	 *																					<br>
	 *	Start -> NumOrParentheses*														<br>
	 *	NumOrParentheses -> Parentheses													<br>
	 *	NumOrParentheses -> Numbers														<br>
	 *	Parentheses -> ‘(’ Parentheses* ‘)’												<br>
	 *	Parentheses -> ‘(’ Letters ‘)’													<br>
	 *	Parentheses -> ‘(’ Numbers ‘)’													<br>
	 *	Parentheses -> ‘(’ LettersAndNumbers ‘)’										<br>
	 *																					<br>
	 *	Letters -> [a-zA-Z]+															<br>
	 *	Numbers -> [0-9]+																<br>
	 *	LettersAndNumbers -> [a-zA-Z0-9]*[a-zA-Z]+[a-zA-Z0-9]*[0-9]+[a-zA-Z0-9]*		<br>
	 *	LettersAndNumbers -> [a-zA-Z0-9]*[0-9]+[a-zA-Z0-9]*[a-zA-Z]+[a-zA-Z0-9]*		<br>
	 */
	public static void main (String[] args) throws IOException{
		EasyParseMachine epm = new EasyParseMachine();
		
		epm.addState(new Start());
		epm.addState(new Parentheses());
		epm.addState(new Numbers());
		epm.addState(new Letters());
		epm.addState(new LettersAndNumbers());

		epm.setCompressWhitespace(true, false);
		
		new EPMVisualDebugger(epm, "Start");
	}
	
	/**
	 * - Ignore whitespace
	 * - Finish on reading an EOF
	 * - Upon reading an opening parenthesis, goto the Parentheses state
	 * - Upon reading a digit, goto the Numbers state
	 */
	private static class Start extends StateAdapter{

		public IStateChange feed(int c) {
			if (Character.isWhitespace(c))
				return new Consume();
			if (c == -1)
				return new Closure();
			else if (c == '('){
				return new Goto("Parentheses");
			} else if (Character.isDigit(c)){
				return new Goto("Numbers");
			} else {
				return new Fail();
			}
		}
		
	}
	
	/**
	 * - Ignore whitespace
	 * - Upon reading a closing parenthesis, finish
	 * - If we have just moved up to our parent and read another opening parenthesis, create a new child
	 * - Otherwise, upon reading an opening parenthesis split into 4 possible content types
	 * - If none of the other cases apply, Fail
	 */
	private static class Parentheses extends StateAdapter{
		
		private boolean closed = false;
		
		public IStateChange feed(int c) {
			if (Character.isWhitespace(c))
				return new Consume();
			if (c == ')'){
				return new Closure();
			} else if (c == '(' && !closed){
				closed = true;
				return new Split("Numbers", "Letters", "LettersAndNumbers", "Parentheses");
			} else if (c == '('){
				return new Goto("Parentheses");
			} else {
				return new Fail();
			}
		}
		
		public void reset() {
			closed = false;
		}
	}
	
	/**
	 * - Ignore whitespace, until we read numbers
	 * - Once we start reading numbers, consume until we run into non-digits
	 */
	private static class Numbers extends StateAdapter{

		private String content = "";
		
		public IStateChange feed(int c) {
			if (Character.isWhitespace(c) && "".equals(content))
				return new Consume();
			if (Character.isDigit(c)){
				content += (char) c;
				return new Consume();
			} else if (!"".equals(content) && !Character.isLetter((char)c)) {
				return new Accept(content);
			} else {
				return new Fail();
			}
		}

		public void reset() {
			content = "";
		}
		
	}
	
	/**
	 * - Ignore whitespace, until we read letters
	 * - Once we start reading letters, consume until we run into non-letters
	 */
	private static class Letters extends StateAdapter{

		private String content = "";
		
		public IStateChange feed(int c) {
			if (Character.isWhitespace(c) && "".equals(content))
				return new Consume();
			if (Character.isLetter(c)){
				content += (char) c;
				return new Consume();
			} else if (!"".equals(content) && !Character.isDigit((char)c)) {
				return new Accept(content);
			} else {
				return new Fail();
			}
		}

		public void reset() {
			content = "";
		}
		
	}
	
	/**
	 * - Ignore whitespace, until we read numbers or letters
	 * - Once we start reading numbers, consume until we run into whitespace
	 * - We cannot accept if we have just numbers or letters
	 */
	private static class LettersAndNumbers extends StateAdapter{

		private String content = "";
		private boolean hasletters = false;
		private boolean hasnumbers = false;
		
		public IStateChange feed(int c) {
			if (Character.isWhitespace(c) && "".equals(content))
				return new Consume();
			if (Character.isLetter(c)){
				content += (char) c;
				hasletters = true;
				return new Consume();
			} else if (Character.isDigit(c)){
				content += (char) c;
				hasnumbers = true;
				return new Consume();
			} else if (hasletters && hasnumbers){
				return new Accept(content);
			} else {
				return new Fail();
			}
		}

		public void reset() {
			content = "";
			hasletters = false;
			hasnumbers = false;
		}
		
	}
	
}
