package algorithm;
// Created: 26.94.2015, Ali Batuhan Yardým

import java.util.Scanner;

// a class to read and convert script strings into appropriate constraints
public class GeoInterpreter {
	
	// script string to be processed
	private String script;

	// the final construct as the product of the interpretation
	private GeoConstruct construct;

	// temporary "debug mode"
	boolean debugActivated;
	
	// output of the interpreter, for errors
	private ScriptErrorList errors;
	
	////// related to the language specifications:
	
	// language tokens
	private static final String COMMENT_TOKEN = "?";
	private static final String NEXT_LINE_TOKEN = ";";

	// regex filters to match parameters
	private static final String REGEX_DOUBLE    = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";
	private static final String REGEX_VAR_NAME  = "[a-zA-Z_][a-zA-Z0-9_]*";
	private static final String REGEX_COMM_NAME = "[a-zA-Z_][a-zA-Z0-9_\\-]*";
	private static final String REGEX_INT = "[1-9][0-9]*";
	private static final String REGEX_SEPERATOR = "[ \t]+";
	
	// used internally to keep track of the line number
	private int curLine;
	
	// construct from script string
	public GeoInterpreter ( String script)
	{
		this.script = script;
		
		errors = new ScriptErrorList();
		construct = new GeoConstruct();
		construct.setScript( script);
	}

	// returns the final geometric construct, as interpreted
	// from the given string
	public GeoConstruct getConstruct ( )
	{
		return construct;
	}
	
	// get debug option
	public boolean isDebugActivated ()
	{
		return debugActivated;
	}
	
	// get the error output
	public ScriptErrorList getErrors ()
	{
		return errors;
	}
	
	// run the interpreter
	public void interpretString ()
	{
		clearErrors();
		clearComments();
		processNewlines();
		
		curLine = 0;
		
		// for line by line reading
		Scanner scan = new Scanner( script);
		
		debugActivated = false;
		
		// read until we reach end of the string
		while ( scan.hasNext())
		{
			curLine++;
			processLine( scan.nextLine());
		}
		
		scan.close();
		
		return;
	} // end of method interpretSting

	// process a single line
	private void processLine ( String line)
	{
		// whitespace line
		if ( line.trim().equals( ""))
			return;
		
		String command_name = StringHelpers.getWord( line, 1);
		String param;
		
		// according to the command name, generate a constraint:
		if ( command_name.equals( "point")) // point definition
		{		
			// get number of words to add
			int paramNo = StringHelpers.countWords( line) - 1;
			
			// add each point one by one
			for ( int i = 2; i < paramNo + 2; i++)
			{
				param = StringHelpers.getWord( line, i);
				
				// check validity of the point name
				if ( !param.matches( REGEX_VAR_NAME))
				{
					addError( "Invalid point name.", "Point \"" + param + "\"", curLine);
				}
				else
				{	
					// check for points with the same name
					if ( construct.findPoint( param) == null)
					{
						construct.addPoint( new GeoPoint( param));
					}
					else // duplicate point exists
					{
						addError( "Duplicate point detected.", "Point \"" + param + "\"", curLine);
					}	
				}
			}
		}
		
		else if ( command_name.equals( "fix-point")) // point fixing
		{
			// check for the parameter list
			if ( testParameterList( line, "C P D D"))
			{
				param = StringHelpers.getWord( line, 2);

				// find the referenced point
				if ( validatePoints( param))
				{
					GeoPoint p = construct.findPoint( param);

					p.setX( Double.parseDouble( StringHelpers.getWord( line, 3)));
					p.setY( Double.parseDouble( StringHelpers.getWord( line, 4)));
					p.setFixed( true);
				}
			}
			else
				addErrorInvalidParams( "fix-point");
		}
		
		else if ( command_name.equalsIgnoreCase( "set-dist")) // set the distance between two points
		{
			if ( testParameterList( line, "C P P D"))
			{
				// create a distance constraint
				double dist = Double.parseDouble( StringHelpers.getWord( line, 4));

				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3)) && dist >= 0)
				{
					GeoPoint p1 = construct.findPoint( StringHelpers.getWord( line, 2));
					GeoPoint p2 = construct.findPoint( StringHelpers.getWord( line, 3));
					
					GeoConstraint c = new GeoConstraint.CPointToPointDistance( p1, p2, dist);
					construct.addConstraint( c);
				}
				else if ( dist < 0) // negative distance
				{
					addError( "Value not within acceptable range.", "Parameter \"" + dist + "\"", curLine);
				}
			}
			else
			{
				addErrorInvalidParams( "set-dist");
			}
		}
		else if ( command_name.equals( "set-angle")) // set the angle between 3 points
		{
			if ( testParameterList( line, "C P P P D"))
			{
				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4)))
				{
					double angle = Double.parseDouble( StringHelpers.getWord( line, 5)) * Math.PI / 180; // to radians
					GeoPoint p1 = construct.findPoint( StringHelpers.getWord( line, 2));
					GeoPoint p2 = construct.findPoint( StringHelpers.getWord( line, 3));
					GeoPoint p3 = construct.findPoint( StringHelpers.getWord( line, 4));
					
					GeoConstraint c = new GeoConstraint.CInteriorAngle( p1, p2, p3, angle);
					construct.addConstraint( c);
				}
			}
			else // invalid parameters
			{
				addErrorInvalidParams( "set-angle");
			}
		}
		else if ( command_name.equals( "equal-length")) // two pairs of points have the same length
		{
			if ( testParameterList( line, "C P P P P"))
			{
				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4), StringHelpers.getWord( line, 5)))
				{
					GeoPoint p1 = construct.findPoint( StringHelpers.getWord( line, 2));
					GeoPoint p2 = construct.findPoint( StringHelpers.getWord( line, 3));
					GeoPoint p3 = construct.findPoint( StringHelpers.getWord( line, 4));
					GeoPoint p4 = construct.findPoint( StringHelpers.getWord( line, 5));
					
					GeoConstraint c = new GeoConstraint.CEqualLength( p1, p2, p3, p4);
					construct.addConstraint( c);
				}
			}
			else
			{
				addErrorInvalidParams( "equal-length");
			}
		}
		else if ( command_name.equals( "point-on-line")) // point on an infinite line
		{
			if ( testParameterList( line, "C P P P"))
			{
				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4)))
				{
					GeoPoint p1 = construct.findPoint( StringHelpers.getWord( line, 2));
					GeoPoint p2 = construct.findPoint( StringHelpers.getWord( line, 3));
					GeoPoint p3 = construct.findPoint( StringHelpers.getWord( line, 4));
					
					GeoConstraint c = new GeoConstraint.CPointOnLine( p1, p2, p3);
					construct.addConstraint( c);
				}
			}
			else
			{
				addErrorInvalidParams( "point-on-line");
			}
		}
		else if ( command_name.equals( "equal-angle")) // two equal angles
		{
			if ( testParameterList( line, "C P P P P P P"))
			{
				GeoPoint a1 = construct.findPoint( StringHelpers.getWord( line, 2));
				GeoPoint a2 = construct.findPoint( StringHelpers.getWord( line, 3));
				GeoPoint a3 = construct.findPoint( StringHelpers.getWord( line, 4));

				GeoPoint b1 = construct.findPoint( StringHelpers.getWord( line, 5));
				GeoPoint b2 = construct.findPoint( StringHelpers.getWord( line, 6));
				GeoPoint b3 = construct.findPoint( StringHelpers.getWord( line, 7));

				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4), 
									 StringHelpers.getWord( line, 5), StringHelpers.getWord( line, 5), StringHelpers.getWord( line, 6)))
				{
					GeoConstraint c = new GeoConstraint.CEqualAngle( a1, a2, a3, b1, b2, b3);
					construct.addConstraint( c);
				}
			}
			else
			{
				addErrorInvalidParams( "equal-angle");
			}
		}
		else if ( command_name.equals( "parallel")) // parallel lines
		{
			if ( testParameterList( line, "C P P P P"))
			{
				GeoPoint a1 = construct.findPoint( StringHelpers.getWord( line, 2));
				GeoPoint a2 = construct.findPoint( StringHelpers.getWord( line, 3));
				GeoPoint a3 = construct.findPoint( StringHelpers.getWord( line, 4));			
				GeoPoint a4 = construct.findPoint( StringHelpers.getWord( line, 5));

				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4),
									 StringHelpers.getWord( line, 5)))
				{
					GeoConstraint c = new GeoConstraint.CParallelLine( a1, a2, a3, a4);
					construct.addConstraint( c);
				}
			}
			else
			{
				addErrorInvalidParams( "parallel");
			}
		}
		else if ( command_name.equals( "dist2line"))
		{
			if ( testParameterList( line, "C P P P D"))
			{
				// create a distance constraint
				GeoPoint p1 = construct.findPoint( StringHelpers.getWord( line, 2));
				GeoPoint p2 = construct.findPoint( StringHelpers.getWord( line, 3));
				GeoPoint p3 = construct.findPoint( StringHelpers.getWord( line, 4));
				double dist = Double.parseDouble( StringHelpers.getWord( line, 5));

				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4)) && dist >= 0)
				{
					GeoConstraint c = new GeoConstraint.CPointToLineDistance( p1, p2, p3, dist);
					construct.addConstraint( c);
				}
				else if ( dist < 0)
				{
					addError( "Value not within acceptable range.", "Parameter \"" + dist + "\"", curLine);
				}
			}
			else
			{
				addErrorInvalidParams( "dist2line");
			}
		}
		else if ( command_name.equals( "point-on-line-segment"))
		{
			if ( testParameterList( line, "C P P P"))
			{
				GeoPoint p1 = construct.findPoint( StringHelpers.getWord( line, 2));
				GeoPoint p2 = construct.findPoint( StringHelpers.getWord( line, 3));
				GeoPoint p3 = construct.findPoint( StringHelpers.getWord( line, 4));

				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4)))
				{
					GeoConstraint c = new GeoConstraint.CPointOnLineSegment( p1, p2, p3);
					construct.addConstraint( c);
				}
			}
			else
			{
				addErrorInvalidParams( "point-on-line-segment");
			}
		}
		else if ( command_name.equals( "area"))
		{
			if ( testParameterList( line, "C P P P D"))
			{
				// read the vertices of the triangle
				GeoPoint p1 = construct.findPoint( StringHelpers.getWord( line, 2));
				GeoPoint p2 = construct.findPoint( StringHelpers.getWord( line, 3));
				GeoPoint p3 = construct.findPoint( StringHelpers.getWord( line, 4));

				// read the wanted area
				double area = Double.parseDouble( StringHelpers.getWord( line, 5));

				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4)) && area >= 0)
				{
					construct.addConstraint( new GeoConstraint.CTriangularArea( p1, p2, p3, area));
				}
				else if ( area < 0)
				{
					addError( "Value not within acceptable range.", "Parameter \"" + area + "\"", curLine);
				}
			}
			else
			{
				addErrorInvalidParams( "area");
			}
		}
		else if ( command_name.equals( "reg-polygon"))
		{
			// get number of vertex names to add
			int paramNo = StringHelpers.countWords( line) - 1;
			GeoPoint[] vertices;
			
			vertices = new GeoPoint[ paramNo];
			
			if ( paramNo < 3)
			{
				addErrorInvalidParams( "reg-polygon");
				return;
			}

			// add each point one by one
			for ( int i = 2; i < paramNo + 2; i++)
			{
				param = StringHelpers.getWord( line, i);

				// check validity of the point name
				if ( !param.matches( REGEX_VAR_NAME))
				{
					addErrorInvalidParams( "reg-polygon");
					return;
				}
				else
				{	
					// check for the point's name
					if ( construct.findPoint( param) == null)
					{
						addErrorPointNotFound( param);
						return;
					}
					else // duplicate point exists
					{
						vertices[i - 2] = construct.findPoint( param);
					}	
				}
			}
			
			construct.addConstraint( new GeoConstraint.CRegularPolygon( vertices));
			
		}
		else if ( command_name.equals( "solve-area")) // an area query
		{
			// check if the points exist and the function is in the correct format
			// get number of words to add
			int paramNo = StringHelpers.countWords( line) - 1;
			
			String[] points = new String[paramNo];
			boolean fail = false;
			
			if ( paramNo < 3)
				this.addError( "Not a valid area.", "solve-area", curLine);

			// add each point one by one
			for ( int i = 2; i < paramNo + 2; i++)
			{
				param = StringHelpers.getWord( line, i);

				// check validity of the point name
				if ( construct.findPoint( param) == null)
				{
					this.addErrorPointNotFound( param);
					fail = true;
					break;
				}
				else
				{	
					points[i - 2] = param;
				}
			}
			
			// if all the points exist, add the area query 
			if ( !fail)
			{
				construct.addQuery( "are#" + StringHelpers.generateSentence( "#", points));
			}

		}
		else if ( command_name.equals( "solve-angle")) // an angle query
		{
			// check if the points exist and the function is in the correct format
			if ( testParameterList( line, "C P P P"))
			{
				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3), StringHelpers.getWord( line, 4)))
				{
					construct.addQuery( StringHelpers.generateSentence( "#", "ang", StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3),
																		StringHelpers.getWord( line, 4)));
				}
			}
			else
			{
				addErrorInvalidParams( "solve-angle");
			}
		}
		else if ( command_name.equals( "solve-length")) // a length query
		{
			// check if the points exist and the function is in the correct format
			if ( testParameterList( line, "C P P"))
			{
				if ( validatePoints( StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3)))
				{
					construct.addQuery( StringHelpers.generateSentence( "#", "len", StringHelpers.getWord( line, 2), StringHelpers.getWord( line, 3)));
				}
			}
			else
			{
				addErrorInvalidParams( "solve-length");
			}
		}
		else if ( command_name.equals( "_debug")) // temporary debug mode
		{
			debugActivated = true;
		}
		else
		{
			addError( "Command not understood.", "Command: " + command_name, curLine);
		}
	}
	
	// pre-processes ';' tokens into newlines
	private void processNewlines ()
	{
		script = script.replaceAll( NEXT_LINE_TOKEN, System.getProperty("line.separator"));
	}
	
	// clears parts of the script starting with "COMMENT_TOKEN"
	// which are comments
	private void clearComments ()
	{
		// the indices of the beginning and ending of a comment block 
		int index1;
		int index2;
		
		index1 = script.indexOf( COMMENT_TOKEN);
		index2 = 0;
		if ( index1 >= 0)
			index2 = script.indexOf( '\n', index1);
		while ( index1 >= 0)
		{
			// remove comments
			
			if ( index2 > 0)
				script = script.replace( script.substring( index1, index2), "");
			else
				script = script.replace( script.substring( index1), "");
			
			index1 = script.indexOf( COMMENT_TOKEN);
			
			if ( index1 >= 0)
				index2 = script.indexOf( '\n', index1);
		}
	}
	
	// validates if a point exists and returns true only if all
	// point names in "names" maps to an existing point
	// add an error to output if not
	private boolean validatePoints ( String... names)
	{
		for ( String str : names)
		{
			if ( construct.findPoint( str) == null)
			{
				addErrorPointNotFound( str);
				return false;
			}
		}
		
		return true;
	}
	
	// test if a line fits a parameter list style
	public static boolean testParameterList ( String line, String parameterList)
	{
		// RegEx to test for
		String regex;
		
		regex = "";
		for( int i = 0; i < parameterList.length(); i++)
		{
			char c = parameterList.charAt( i);
			
			if ( c == ' ')
			{
				regex += REGEX_SEPERATOR;
			}
			else if ( c == 'D')
			{
				regex += REGEX_DOUBLE;
			}
			else if ( c == 'P')
			{
				regex += REGEX_VAR_NAME;
			}
			else if ( c == 'C')
			{
				regex += REGEX_COMM_NAME;
			}
			else if ( c == 'I')
			{
				regex += REGEX_INT;
			}
		}
		
		return line.matches( regex);
	}
	
	
	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// error logging functions:                              ////////
	
	private void addError ( String text, String target, int lineNo)
	{
		errors.addError( new ScriptError( text, target, lineNo));
	}
	
	private void addErrorInvalidParams ( String functionName)
	{
		addError( "Invalid parameter list.", "Function \"" + functionName + "\"", curLine);
	}
	
	private void addErrorPointNotFound ( String name)
	{
		addError( "Point non-existent.", "Point \"" + name + "\"", curLine);
	}
	
	private void clearErrors ()
	{
		errors = new ScriptErrorList();
	}
}
