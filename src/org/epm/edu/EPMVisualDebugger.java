package org.epm.edu;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.tree.TreeModel;

import org.epm.edu.statechanges.UnknownStateChangeException;

/**
 * A real-time GUI tool to test out user state machine specifications
 */
public class EPMVisualDebugger extends JFrame implements ComponentListener, CaretListener{

	private static final long serialVersionUID = 1L;
	
	/**
	 * The machine that controls the parsing
	 */
	private EasyParseMachine machine;
	
	/**
	 * The starting state of the machine
	 */
	private String startState;
	
	/**
	 * The result pane on the left side
	 */
	private JScrollPane treePane = new JScrollPane();
	
	/**
	 * The 'console' output on the bottom right
	 */
	private JTextArea consoleOutput = new JTextArea();
	
	/**
	 * The user input on the top right
	 */
	private JTextArea languageInput = new JTextArea();
	
	/**
	 * The 'debug' checkbox on the bottom right
	 */
	private Checkbox debug = new Checkbox("Show verbose debug output");
	
	/**
	 * The line number panel
	 */
	private JTextArea lineView = new JTextArea(){

		private static final long serialVersionUID = 1L;

		public void paintComponent(Graphics g){
			Graphics2D g2d = (Graphics2D)g.create();
			g2d.setPaint(new GradientPaint(0,0,new Color(230,230,230),getWidth()+10,0,Color.WHITE));
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.dispose();
			super.paintComponent(g);
		}
	};
	
	/**
	 * The panel used when the parse tree construction failed
	 */
	private JPanel emptyTreePanel = new JPanel(){

		private static final long serialVersionUID = 1L;

		public void paint(Graphics g){
			Graphics2D g2d = (Graphics2D)g.create();
			g2d.setPaint(new GradientPaint(0,0,new Color(131,142,154),getWidth(),0,new Color(230,230,230)));
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.dispose();
		}
	};
	
	/**
	 * The split pane splitting the window into a left and
	 * right view
	 */
	private JSplitPane mainSplitPane;
	
	/**
	 * The split pane splitting the right part of the window
	 * into a top and bottom view
	 */
	private JSplitPane rightSplitPane;
	
	/**
	 * Create a new window for visual debugging of a given
	 * EPM with a certain start state. 
	 * 
	 * @param machine The machine to debug/view
	 * @param start The start state of the machine
	 */
	public EPMVisualDebugger(EasyParseMachine machine, String start){
		super("Easy Parsing Machine Visual Debugging Tool");
		
		this.machine = machine;
		this.startState = start;
		
		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		treePane.setViewportView(emptyTreePanel);
		mainSplitPane.setLeftComponent(treePane);
		mainSplitPane.setRightComponent(rightSplitPane);
		
		JScrollPane inputScrollPane = new JScrollPane(languageInput);
		rightSplitPane.setTopComponent(inputScrollPane);
		
		JPanel consolePanel = new JPanel();
		consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.PAGE_AXIS));
		rightSplitPane.setBottomComponent(new JScrollPane(consolePanel));
		
		consoleOutput.setEditable(false);
		consoleOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		consoleOutput.setBackground(Color.BLACK);
		consoleOutput.setForeground(Color.WHITE);
		
		consolePanel.add(debug);
		consolePanel.add(consoleOutput);
		
		languageInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		languageInput.addCaretListener(this);
		
		lineView.setEditable(false);
		lineView.setOpaque(false);
		lineView.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		inputScrollPane.setRowHeaderView(lineView);
		
		setContentPane(mainSplitPane);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setExtendedState(MAXIMIZED_BOTH);  
		
		addComponentListener(this);
		
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		}
		
		setVisible(true);
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		mainSplitPane.setDividerLocation(0.25);
		rightSplitPane.setDividerLocation(0.75);
	}

	private String lastText = null;
	
	/**
	 * When the input text has been updated, try to parse it
	 * and construct a parse tree view.
	 * 
	 * @param arg0 The caret update event
	 */
	@Override
	public void caretUpdate(CaretEvent arg0) {
		String text = languageInput.getText();
		if (text.equals(lastText))
			return;
		
		lineView.setText("");
		for (int i = 0; i < languageInput.getLineCount(); i++)
			lineView.append(i + ": \n");
		consoleOutput.setText("");
		EPMDebugStream stream = new EPMDebugStream(text);
		machine.resetMachine();
		machine.setInput(stream);
		
		if (debug.getState())
			machine.setVerbose(osForConsole());
		else
			machine.setVerbose(null);
		
		TreeModel tm = null;
		try {
			tm = machine.parse(startState);
			if (tm == null)
				throw new RuntimeException();
		} catch (IOException e) {
			consoleOutput.append("UNEXPECTED ERROR: " + e.getMessage());
		} catch (UnknownStateChangeException e){
			consoleOutput.append("FAILED WITH UnknownStateChangeException:\n" + e.getMessage());
		} catch (RuntimeException e){
			consoleOutput.append("FAILED WHILE PARSING LINE: " + stream.getCurrentLine() + "\n");
			consoleOutput.append("  " + stream.getCurrentPartialLine() + "\n");
			consoleOutput.append("  " + stream.getLineLastPointer() + "\n");
			consoleOutput.append("EXPECTED TO FINISH/ADD TO STATE: " + snapshotToString(machine.getDeletionSnapshot()) + "\n\n");
		}
		if (tm != null){
			JTree tree = new JTree(tm){

				private static final long serialVersionUID = 1L;

				public void paintComponent(Graphics g){
					Graphics2D g2d = (Graphics2D)g.create();
					g2d.setPaint(new GradientPaint(0,0,new Color(131,142,154),getWidth(),0,getBackground()));
					g2d.fillRect(0, 0, getWidth(), getHeight());
					g2d.dispose();
					super.paintComponent(g);
				}
			};
			if (machine.isAmbiguous()){
				tree.setBackground(new Color(255,230,230));
				consoleOutput.append("Your parse tree is ambiguous :(\n\n");
			} else {
				tree.setBackground(new Color(230,230,230));
				consoleOutput.append("Parsing went down without any problems :)\n\n");
			}
			tree.setOpaque(false);
			for (int i = 0; i < tree.getRowCount(); i++) {
		         tree.expandRow(i);
			}
			
			tree.addMouseListener(new MouseAdapter(){

				private boolean expanded = true;
				
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if (arg0.getButton() == MouseEvent.BUTTON3){
						JTree tree = (JTree) arg0.getComponent();
						if (expanded){
							expanded = false;
							for (int i = tree.getRowCount()-1; i >= 0; i--) {
								tree.collapseRow(i);
							}
						} else{
							expanded = true;
							for (int i = 0; i < tree.getRowCount(); i++) {
								tree.expandRow(i);
							}
						}
					}
				}
				
			});
			
			treePane.setViewportView(tree);
		} else {
			treePane.setViewportView(emptyTreePanel);
		}
		lastText = text;
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	consoleOutput.setCaretPosition(0);
            }
         });
	}
	
	/**
	 * Get a PrintStream for the 'console' JTextArea
	 * 
	 * @return A PrintStream that prints to the 'console'
	 */
	private PrintStream osForConsole(){
		return new PrintStream(System.out){

			@Override
			public void print(String s) {
				push(s);
			}

			@Override
			public void println(String x) {
				push(x + "\n");
			}
			
			private void push(final String s){
				SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		            	consoleOutput.append(s);
		            }
		         });
			}
			
		};
	}
	
	/**
	 * Given a deletion snapshot, create a human readable string
	 * 
	 * @param snapshot The deletion snapshot
	 * @return The fancy string
	 */
	private String snapshotToString(Collection<String> snapshot){
		String out = "";
		Iterator<String> it = snapshot.iterator();
		while (it.hasNext()){
			String next = it.next();
			if ("".equals(out)){
				//First
				out += next;
			} else if (!it.hasNext()){
				//Last
				out += " or " + next;
			} else {
				//Middle
				out += ", " + next;
			}
		}
		return out;
	}
}
