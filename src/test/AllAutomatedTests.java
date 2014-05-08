package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.integration.EasyParseMachineTest;
import test.integration.StatedInterferenceTest;
import test.unit.EPMDebugStreamTest;
import test.unit.ParseTreeNodeTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	EasyParseMachineTest.class, 
	EPMDebugStreamTest.class,
	ParseTreeNodeTest.class,
	StatedInterferenceTest.class
	})
public class AllAutomatedTests {

}
