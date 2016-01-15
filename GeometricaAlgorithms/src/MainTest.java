import gui.ScriptTextPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import algorithm.GeoConstruct;
import algorithm.GeoDrawingOptimizer;
import algorithm.GeoInterpreter;
import algorithm.GeoNumericSolver;
import algorithm.GeoProblemSolver;
import algorithm.GeoRenderer;
import algorithm.StringHelpers;
import algorithm.TopologyProvider;

class RenderSurface extends JPanel{

	private static final long serialVersionUID = 1L;
	
	GeoRenderer renderer;
	
	public RenderSurface ( GeoRenderer renderer)
	{
		this.renderer = renderer;
	}
	
    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        renderer.renderTo( g);
    }
}

// temporary class for testing
public class MainTest extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main ( String[] args)
	{
		System.out.println( StringHelpers.countWords( "are#wer#fsd#", "#"));
		System.out.println( StringHelpers.getWord( "are#wer#fsd#", 1, "#"));
		System.out.println( StringHelpers.getWord( "are#wer#fsd#", 2, "#"));
		System.out.println( StringHelpers.getWord( "are#wer#fsd#", 3, "#"));
		// System.out.println( StringHelpers.countWords( "are#wer#fsd#", "#"));
		
		new MainTest();
		
	}
	
	JFrame frame = new JFrame( "Text area test");

	JScrollPane scroll = new JScrollPane();

	ScriptTextPane scriptArea = new ScriptTextPane();
	
	GeoConstruct cons;

	public MainTest ( )
	{
		frame = new JFrame( "Text area test");

		scroll = new JScrollPane();

		scriptArea = new ScriptTextPane();
		//scriptArea.setLineWrap( true);


		scroll.setViewportView( scriptArea);
		
		
		//scroll.add( scriptArea);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new BorderLayout());
		mainPanel.setPreferredSize( new Dimension( 500, 500));
		mainPanel.add( scroll, BorderLayout.CENTER);

		frame.add( mainPanel);
		

		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);

		frame.pack();
		frame.setVisible( true);
		
		scroll.getHorizontalScrollBar().addAdjustmentListener( new AdjustmentListener () {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				//JOptionPane.showMessageDialog( null, "text1");
				( ( JComponent) arg0.getSource()).getParent().repaint();
			}});
		
		scriptArea.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_F5, 0), "recompile");
		scriptArea.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_F6, 0), "showgraph");
		scriptArea.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_F7, 0), "solve");
		scriptArea.getActionMap().put( "recompile", new CompileListener());
		scriptArea.getActionMap().put( "showgraph", new GraphListener());
		scriptArea.getActionMap().put( "solve", new SolveListener());
	}

	private class CompileListener extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed( ActionEvent arg0) {
			GeoInterpreter interpreter = new GeoInterpreter( ( (JTextPane) arg0.getSource()).getText());
			interpreter.interpretString();
			cons = interpreter.getConstruct();
			GeoRenderer renderer = new GeoRenderer ( cons);

			/*GeoNumericSolver solver = new GeoNumericSolver();
			solver.addConstraints(cons.getConstraints());
			solver.addObjects( cons.getRawObjects());

			int trialCounter = 0;
			while ( trialCounter < 100 && !solver.findSolution( true, true))
			{
				trialCounter++;
			}*/
			GeoDrawingOptimizer optimizer = new GeoDrawingOptimizer( cons);
			optimizer.optimize( true);




			JFrame frame = new JFrame( "render result");
			frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE);
			frame.setSize( 500, 500);
			frame.add( new RenderSurface( renderer));
			frame.setVisible( true);

			( (ScriptTextPane) arg0.getSource()).setErrors( interpreter.getErrors());    
			
			System.out.println( interpreter.getErrors());
		}
	}
	
	GeoProblemSolver solver;

	private class GraphListener extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed( ActionEvent arg0) {
			solver = new GeoProblemSolver( cons);

			solver.constructProblem();
			solver.showGraph();
			
			TopologyProvider top = new TopologyProvider( cons);
			//System.out.println( "test1: " + top.isPointInsideAngle( "X", "A", "B", "C"));
			
			//solver.solve();

			// System.out.println( "Solution result: " + solver.getStatus() + "\nSolution:\n\n" + solver.getSolutions());
		} 
	}
	
	private class SolveListener extends AbstractAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed( ActionEvent arg0) {
			solver.updateVisualizationData( );
			
			//solver = new GeoProblemSolver( cons);

			//solver.constructProblem();
			//solver.showGraph();

			solver.solve();
			
			solver.updateVisualizationData( );

			System.out.println( "Solution result: " + solver.getStatus() + "\nSolution:\n\n" + solver.getSolutions());
		} 
	}
}
