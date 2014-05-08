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
import org.epm.edu.statechanges.Guess;
import org.epm.edu.statechanges.IStateChange;

/**
 * JSONReader - A JSON reading EPM implementation you can play 
 * around with in the Visual Debugger.
 * 
 * See http://json.org/ for the JSON format specification.
 * 
 * This is not the prettiest implementation of a JSON parser ever, 
 * but it will give you an idea of the capabilities and use of
 * the EasyParseMachine framework.
 *
 * Also, I apologize for cramming all of the states into a
 * single class :)
 */

public class JSONReader {

	public static void main (String[] args) throws IOException{
		EasyParseMachine epm = new EasyParseMachine();
		
		// The start node
		epm.addState(new Start(), "root");
		
		// A number in JSON format
		epm.addState(new JSONNumber());
		
		// A constant (true, false, null) in JSON format
		epm.addState(new JSONConstant());
		
		// A string in JSON format
		epm.addState(new JSONString());
		
		// An array in JSON format
		epm.addState(new JSONArray());
		
		// An object in JSON format
		epm.addState(new JSONObject());
		
		// A key value pair in a JSON format
		epm.addState(new Pair());
		
		// A key in a key value pair
		epm.addState(new JSONString(), "Key");	// Alias a string as a key
		
		new EPMVisualDebugger(epm, "root");
	}
	
	/**
	 * The start state for the JSONReader
	 */
	private static class Start extends StateAdapter{
		public IStateChange feed(int c) {
			// If we have not begun matching, just consume
			if (Character.isWhitespace(c))
				return new Consume();
			
			// If we have read all characters, stop
			if (c == -1)
				return new Closure();
			
			// Try to match content
			return new GotoJSONValue();
			
		}
	}
	
	/**
	 * A state transition to hide any JSON value
	 */
	private static class GotoJSONValue extends Guess{
		public GotoJSONValue(){
			super("JSONNumber", "JSONConstant", "JSONString", "JSONArray", "JSONObject");
		}
	}
	
	/**
	 * A state that matches objects in JSON format 
	 */
	private static class JSONObject extends StateAdapter{

		private boolean opened = false;
		private boolean expectvalue = false;
		private boolean first = true;
		private boolean expectcomma = false;

		@Override
		public IStateChange feed(int c) {
			// Consume whitespace
			if (Character.isWhitespace(c))
				return new Consume();
			
			// We must start with an opening bracket
			if (!opened && c != '{')
				return new Fail();
			if (!opened && c == '{'){
				opened = true;
				return new Consume();
			}
			
			// End with a closing bracket
			if (!expectvalue && '}' == (char) c)
				return new Closure();
			
			// If we read a comma start reading a new value
			if (!expectvalue && !first && ',' == (char) c){
				expectvalue = true;
				expectcomma = false;
				return new Consume();
			}
			
			// We were expecting a separator
			if (expectcomma)
				return new Fail();

			// If we need to read a value, start reading it
			expectvalue = false;
			first = false;
			expectcomma = true;
			return new Goto("Pair"); 
		}
		
		@Override
		public void reset() {
			opened = false;
			expectvalue = false;
			first = false;
			expectcomma = false;
		}

	}
	
	/**
	 * A state that matches JSON Object key-value pairs
	 */
	private static class Pair extends StateAdapter{

		private boolean expectvalue = false;
		private boolean readvalue = false;
		private boolean expectstring = true;
		
		@Override
		public IStateChange feed(int c) {
			// Consume whitespace
			if (Character.isWhitespace(c))
				return new Consume();
			
			// We just read a : and will have to read a value
			if (expectvalue && ':' == c){
				readvalue = true;
				expectvalue = false;
				return new Consume();
			}
			
			// If we need to read a value, start reading it
			if (readvalue){
				readvalue = false;
				return new GotoJSONValue();
			} else if (expectstring){
				expectstring = false;
				expectvalue = true;
				return new Goto("Key");
			}

			// If we matched everything, we are done
			if (!expectstring && !expectvalue && !readvalue)
				return new Accept();
			else
				return new Fail();
		}
		
		@Override
		public void reset(){
			expectvalue = false;
			readvalue = false;
			expectstring = true;
		}
		
	}
	
	/**
	 * A state that matches arrays in JSON format 
	 */
	private static class JSONArray extends StateAdapter{

		private boolean opened = false;
		private boolean expectvalue = false;
		private boolean first = true;
		private boolean expectcomma = false;

		@Override
		public IStateChange feed(int c) {
			// Consume whitespace
			if (Character.isWhitespace(c))
				return new Consume();
			
			// We must start with an opening bracket
			if (!opened && c != '[')
				return new Fail();
			if (!opened && c == '['){
				opened = true;
				return new Consume();
			}
			
			// End with a closing bracket
			if (!expectvalue && ']' == (char) c)
				return new Closure();
			
			// If we read a comma start reading a new value
			if (!expectvalue && !first && ',' == (char) c){
				expectvalue = true;
				expectcomma = false;
				return new Consume();
			}
			
			// We were expecting a separator
			if (expectcomma)
				return new Fail();

			// If we need to read a value, start reading it
			expectvalue = false;
			first = false;
			expectcomma = true;
			return new GotoJSONValue(); 
		}
		
		@Override
		public void reset() {
			opened = false;
			expectvalue = false;
			first = true;
			expectcomma = false;
		}

	}
	
	/**
	 * A state that matches strings in JSON format 
	 */
	private static class JSONString extends StateAdapter{

		private String content = "";
		private boolean escaped = false;
		private boolean valid = false;
		private boolean inunichar = false;
		private String unichar = "";

		@Override
		public IStateChange feed(int c) {
			// If we have not begun matching anything, just consume
			if ("".equals(content) && Character.isWhitespace(c))
				return new Consume();
			
			// We must first read an opening quotation
			if ("".equals(content) && '"' != c)
				return new Fail();
			
			// We must first read an opening quotation
			if ("".equals(content) && '"' == c){
				content += (char) c;
				return new Consume();
			}
			
			// We are reading a unicode character
			if (inunichar){
				if (!isHexDigit(c))
					return new Fail();
				unichar += (char) c;
				if (unichar.length() == 4){
					inunichar = false;
					content += unichar;
					unichar = "";
				}
				return new Consume();
			}
			
			// Only accept if we ended with an unescaped "
			if (valid && content.endsWith("\""))
				return new Accept(content);
			
			// Control characters are never allowed
			if (isControl(c))
				return new Fail();
			
			// If we are not being escaped and we are an escape, escape the next char
			if (!escaped && '\\' == c){
				escaped = true;
				valid = false;
				content += (char) c;
				return new Consume();
			}
			
			// If we are not escaped and not an escape character, just add us
			if (!escaped){
				content += (char) c;
				valid = '"' == c;
				return new Consume();
			}
			
			// If our character is not escapable, fail
			if (!isEscapable((char) c) && !inunichar)
				return new Fail();
			
			// Start a unicode block
			if ('u' == (char) c)
				inunichar = true;
			
			// If we are a normal escaped character, just add us
			content += (char) c;
			valid = false;
			escaped = false;
			return new Consume();
		}
		
		/**
		 * Whether this integer is a control character
		 */
		private boolean isControl(int c){
			return c <= 0x1F || c == 0x7F;
		}
		
		/**
		 * Whether this is an escapable character
		 */
		private boolean isEscapable(char c){
			return '"' == c ||
					'\\' == c ||
					'/' == c ||
					'b' == c ||
					'f' == c ||
					'n' == c ||
					'r' == c ||
					't' == c ||
					'u' == c;
		}
		
		/**
		 * Whether c is a hexadecimal digit
		 */
		private boolean isHexDigit(int c){
			return Character.isDigit((char) c) || 
					(65 <= c && c <= 70) ||			// A-F
					(97 <= c && c <= 102);			// a-f
		}
		
		@Override
		public void reset() {
			content = "";
			escaped = false;
			valid = false;
			inunichar = false;
			unichar = "";
		}

	}
	
	/**
	 * A state that matches (floating point) numbers in JSON format 
	 */
	private static class JSONNumber extends StateAdapter{

		private String content = "";
		private boolean predecimalpoint = true;
		private boolean preexponent = true;
		
		@Override
		public IStateChange feed(int c){
			// If we have not begun matching anything, just consume
			if ("".equals(content) && Character.isWhitespace(c))
				return new Consume();
			
			// If we read a minus as the first character
			if ('-' == (char) c && "".equals(content)){
				content += (char) c;
				return new Consume();
			}
			
			// If we read anything but a digit to start the number, fail
			if (("".equals(content)||"-".equals(content)) 
					&& !Character.isDigit(c)){
				return new Fail();
			}
			
			// If we have not started and have a digit
			if (predecimalpoint && preexponent){
				// We are matching the pre-decimal point component
				if (Character.isDigit(c)){
					// We cannot have two leading zeros
					if ('0' == (char) c && ("0".equals(content)||"-0".equals(content))){
						return new Fail();
					}
					content += (char) c;
					return new Consume();
				}
				// We are going to match digits after the decimal point
				if ('.' == (char) c){
					predecimalpoint = false;
					content += (char) c;
					return new Consume();
				}
				// We are going to match an exponent
				if ('e' == (char) c || 'E' == (char) c){
					preexponent = false;
					content += (char) c;
					return new Consume();
				}
				// If we read anything that does not extend our number, finish
				return new Accept(content);
			}
			
			// If we are matching the digits behind a decimal point
			if (!predecimalpoint && preexponent){
				// We need at least one digit after a decimal point
				if (!Character.isDigit(c) && content.endsWith(".")){
					return new Fail();
				}
				// Consume any digits
				if (Character.isDigit(c)){
					content += (char) c;
					return new Consume();
				}
				// We are going to match an exponent
				if ('e' == (char) c || 'E' == (char) c){
					preexponent = false;
					content += (char) c;
					return new Consume();
				}
				// If we read anything that does not extend our number, finish
				return new Accept(content);
			}
			
			// We are matching an exponent (e has already been read)
			if (!preexponent){
				// An exponent can start with a '+' or a '-'
				if (('+' == (char) c ||
					'-' == (char) c) &&
					(content.endsWith("e") ||
					content.endsWith("E"))){
					content += (char) c;
					return new Consume();
				}
				// Match any digits after the E (or e)
				if (Character.isDigit(c)){
					content += (char) c;
					return new Consume();
				}
				// If we have a valid exponent
				if (Character.isDigit(content.charAt(content.length()-1))){
					// If we read anything that does not extend our number, finish
					return new Accept(content);
				}
			}
			
			// If there is no way this can become a number, fail
			return new Fail();
		}
		
		@Override
		public void reset(){
			content = "";
			predecimalpoint = true;
			preexponent = true;
		}
		
	}
	
	/**
	 * A state that matches "true", "false" or "null"
	 */
	private static class JSONConstant extends StateAdapter{

		private String content = "";
		
		public IStateChange feed(int c) {
			// If we have not begun matching anything, just consume
			if ("".equals(content) && Character.isWhitespace(c))
				return new Consume();
			
			// If we are done, accept
			if (validContent())
				return new Accept(content);
			
			// If what we are reading can match either "true", "false" or "null", consume
			if (c != -1 && canComplete((char)c)){
				content += (char) c;
				return new Consume();
			}
			
			// If there is no way this can become either "true", "false" or "null", fail
			return new Fail();
		}
		
		/**
		 * @return Whether the currently read content matches "true", "false" or "null"
		 */
		private boolean validContent(){
			return "true".equals(content) || 
					"false".equals(content) || 
					"null".equals(content);
		}
		
		/**
		 * @return Whether the currently read content with 'c' appended can 
		 * still produce "true" or "false"
		 */
		private boolean canComplete(char c){
			return "true".startsWith(content+c) || 
					"false".startsWith(content+c) || 
					"null".startsWith(content+c);
		}

		@Override
		public void reset(){
			content = "";
		}

	}
}
