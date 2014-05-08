package test.integration;

import static org.junit.Assert.*;

import java.io.IOException;

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
import org.junit.Test;

public class StatedInterferenceTest {

	public static void main(String[] args) throws IOException{
		new StatedInterferenceTest().test();
	}
	
	@Test
	public void test() throws IOException {
		EasyParseMachine machine = new EasyParseMachine("45");
		machine.addState(new Start());
		machine.addState(new Digits());
		machine.addState(new DigitsAndFail());
		machine.addState(new DigitsAndAccept());

		ParseTreeModel model = machine.parse("Start");
		ParseTreeNode root = model.getRoot();
		ParseTreeNode daa = root.getChildAt(0);
		ParseTreeNode digits = daa.getChildAt(0);
		
		assertEquals("45", digits.getContent());
	}

	private static class Start extends StateAdapter{
		@Override
		public IStateChange feed(int c) {
			if (c == -1)
				return new Closure();
			return new Guess("DigitsAndFail", "DigitsAndAccept");
		}
	}
	
	private static class DigitsAndFail extends StateAdapter{
		public IStateChange feed(int c) {
			if (Character.isDigit(c))
				return new Goto("Digits");
			return new Fail();
		}
	}
	
	private static class DigitsAndAccept extends StateAdapter{
		public IStateChange feed(int c) {
			if (Character.isDigit(c))
				return new Goto("Digits");
			return new Accept("");
		}
	}
	
	private static class Digits extends StateAdapter{
		private String result = "";
		
		public Digits(){}
		private Digits(String result){
			this.result = result;
		}
		
		public IStateChange feed(int c) {
			if (Character.isDigit(c)){
				result += (char) c;
				return new Consume();
			}
			return new Accept(result);
		}
		
		public void reset(){
			result = "";
		}
		
		public State copy(){
			return new Digits(result);
		}
	}
	
}
