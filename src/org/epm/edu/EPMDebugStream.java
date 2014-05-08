package org.epm.edu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A convenience class for tracking when 
 * InputStream reading goes sour.
 * 
 * This is handy for debugging, but can be left
 * out to gain a small speed increase.
 */
public class EPMDebugStream extends InputStream{

	private InputStream feeder;
	private String currentLine = "";
	private int line = 0;
	
	/**
	 * Wrap a given InputStream
	 * 
	 * @param is The InputStream to wrap
	 */
	public EPMDebugStream(InputStream is){
		feeder = is;
	}
	
	/**
	 * Wrap a given String
	 * 
	 * @param s The String to wrap
	 */
	public EPMDebugStream(String s){
		feeder = new ByteArrayInputStream(s.getBytes());
	}

	/**
	 * Forward a read to our wrapped InputStream.
	 * Keep track of newlines and the current line
	 * being read.
	 * 
	 * @return The read character by our wrapped InputStream
	 */
	@Override
	public int read() throws IOException {
		int feed = feeder.read();
		if (feed == (int) '\n'){
			line++;
			currentLine = "";
		} else if (feed != -1){
			currentLine += (char) feed;
		}
		return feed;
	}
	
	/**
	 * Get the current line being read (starting at 0)
	 * 
	 * @return The current line number
	 */
	public int getCurrentLine(){
		return line;
	}
	
	/**
	 * Get the current line's String contents
	 * 
	 * @return The line that was read so far
	 */
	public String getCurrentPartialLine(){
		return currentLine.replace("\t"," ");
	}
	
	/**
	 * Get the current line's String contents, but insert
	 * a space before the last character.
	 * 
	 * @return The line that was read so far, with the last character highlighted
	 */
	public String getCurrentPartialLineHLLast(){
		return (currentLine.substring(0,currentLine.length()-1) + " " + currentLine.charAt(currentLine.length()-1)).replace("\t"," ");
	}
	
	/**
	 * Get a String with an up arrow (^) at the character position
	 * of the last character of getCurrentPartialLineHLLast()
	 * 
	 * NOTE: This is only useful for monospaced fonts
	 * 
	 * @return The String with the up-arrow
	 */
	public String getLineHLLastPointer(){
		return produceSpace(currentLine.length()) + "^";
	}
	
	/**
	 * Get a String with an up arrow (^) at the character position
	 * of the last character of getCurrentPartialLine()
	 * 
	 * NOTE: This is only useful for monospaced fonts
	 * 
	 * @return The String with the up-arrow
	 */
	public String getLineLastPointer(){
		return produceSpace(currentLine.length()-1) + "^";
	}
	
	/**
	 * Produce some amount of space characters in a String
	 * 
	 * @param amount The amount of space characters
	 * @return The String with the amount of space characters
	 */
	private String produceSpace(int amount){
		String out = "";
		for (int i = 0; i < amount; i++){
			out += " ";
		}
		return out;
	}

	@Override
	public int available() throws IOException {
		return feeder.available();
	}

	@Override
	public void close() throws IOException {
		feeder.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		feeder.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return feeder.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		for (int i = off; i < len - off; i++){
			int r = read();
			if (r != -1)
				b[i] = (byte) r;
			else
				return i - off;
		}
		return len;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public synchronized void reset() throws IOException {
		feeder.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return feeder.skip(n);
	}
	
	
	
}
