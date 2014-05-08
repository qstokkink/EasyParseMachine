package test.unit;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.epm.edu.EPMDebugStream;
import org.junit.Before;
import org.junit.Test;

public class EPMDebugStreamTest {

	private EPMDebugStream stream;
	
	@Test
	public void testHighlightPointer() throws IOException {
		for (int i = 0; i < 5; i++)
			stream.read();
		
		assertEquals("     ^", stream.getLineHLLastPointer());
	}
	
	@Test
	public void testPointer() throws IOException {
		for (int i = 0; i < 5; i++)
			stream.read();
		
		assertEquals("    ^", stream.getLineLastPointer());
	}
	
	@Test
	public void testHighlight() throws IOException {
		for (int i = 0; i < 5; i++)
			stream.read();
		
		assertEquals("test 0", stream.getCurrentPartialLineHLLast());
	}
	
	@Test
	public void testMultiLineTerminated() throws IOException {
		for (int i = 0; i < 12; i++)
			stream.read();
		
		assertEquals("", stream.getCurrentPartialLine());
		assertEquals(2, stream.getCurrentLine());
	}
	
	@Test
	public void testMultiLine() throws IOException {
		for (int i = 0; i < 11; i++)
			stream.read();
		
		assertEquals("test1", stream.getCurrentPartialLine());
		assertEquals(1, stream.getCurrentLine());
	}
	
	@Test
	public void testSingleLineTerminated() throws IOException {
		for (int i = 0; i < 6; i++)
			stream.read();
		
		assertEquals("", stream.getCurrentPartialLine());
		assertEquals(1, stream.getCurrentLine());
	}
	
	@Test
	public void testSingleLine() throws IOException {
		for (int i = 0; i < 5; i++)
			stream.read();
		
		assertEquals("test0", stream.getCurrentPartialLine());
		assertEquals(0, stream.getCurrentLine());
	}
	
	@Before
	public void setUp(){
		stream = new EPMDebugStream(new ByteArrayInputStream("test0\ntest1\ntest2".getBytes()));
	}

}
