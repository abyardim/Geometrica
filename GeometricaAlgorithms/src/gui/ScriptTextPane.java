package gui;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import algorithm.ScriptError;
import algorithm.ScriptErrorList;

public class ScriptTextPane extends JTextPane {

	private static final long serialVersionUID = 4387122194014147032L;
	
	// the different style for different parts of the code
	Style styleCommand;
	Style styleLiteral;
	Style styleVar;
	Style styleComment;
	Style stylePreprocessor;
	Style styleEmpty;

	StyledDocument doc;

	// the "info" icon
	ImageIcon infoIcon;

	// the error manager
	ScriptErrorList errors;

	// keeps a history of the text, for keeping the errors in line
	String lastVersion;

	// script specific details:

	String[] commandNames = { "point", "fix-point", "set-dist", "set-angle", "equal-length", 
			"point-on-line", "equal-angle", "parallel", "dist2line", "point-on-line-segment",
			"area", "solve-area", "solve-length", "solve-angle", "reg-polygon"};

	String[] preprocessorCommands = { "#sh", "#end" };

	private static final String COMMENT_TOKEN = "??";
	private static final String NEXT_LINE_TOKEN = ";";

	// regex filters to match parameters
	private static final String REGEX_DOUBLE    = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";
	private static final String REGEX_VAR_NAME  = "[a-zA-Z_][a-zA-Z0-9_]*";
	private static final String REGEX_SEPERATOR = "[ \t]+";

	public ScriptTextPane ( )
	{
		super();

		// initialize the document for this text area
		doc = new DefaultStyledDocument();
		setDocument( doc);

		// setup the styles
		styleCommand = addStyle( "command", null);
		styleLiteral = addStyle( "literal", null);
		styleVar = addStyle( "var", null);
		styleComment = addStyle( "comment", null);
		stylePreprocessor = addStyle( "pre", null);
		styleEmpty = addStyle( "empty", null);

		StyleConstants.setForeground( styleCommand, Color.blue);
		StyleConstants.setItalic( styleCommand, true);
		StyleConstants.setForeground( styleLiteral, new Color( 161, 0, 138));
		StyleConstants.setForeground( styleVar, Color.darkGray);
		StyleConstants.setForeground( styleComment, new Color( 100, 160, 1));
		StyleConstants.setForeground( stylePreprocessor, new Color( 231, 159, 10));
		StyleConstants.setForeground( styleEmpty, Color.black);

		doc.addDocumentListener( new CodeListener());
		this.addMouseListener( new MouseTracker());

		this.setFont( new Font( "Consolas", Font.PLAIN, 20));

		// setup the info icon
		infoIcon = new ImageIcon( getClass().getResource( "/icons/infoIcon2.png"));

		// there are initially no errors
		errors = null;

		/*errors = new ScriptErrorList();
		errors.addError( new ScriptError ("error1", "line 5", 5));
		errors.addError( new ScriptError ("error1.1", "line 6", 6));
		errors.addError( new ScriptError ("error2", "line 2", 2));
		errors.addError( new ScriptError ("error3", "line 7", 7));
		errors.addError( new ScriptError ("error4", "line 9", 9));*/

		// clearErrors();

		//setText( "wewt\nerror2\ngfgsdfg\ngweger\nerror 5\nwertwert\nerror7\ngweger\nerror9");
	}

	// for safety
	boolean setTextCalled = false;

	@Override
	public void setText( String str)
	{
		clearErrors();
		
		//setTextCalled = true;
		super.setText( str);

		// this.clearErrors();
		this.updateSyntaxColor();

		lastVersion = str;

		setTextCalled = false;
	}

	// override paint component to draw script data
	@Override
	public void paintComponent ( Graphics page)
	{
		super.paintComponent( page);

		// draw error highlights
		if ( errors != null)
		{
			int lineHeight = this.getFontMetrics( this.getFont()).getHeight();
			int startOffset = 3;

			for ( ScriptError e : errors.getErrors())
			{				
				page.setColor( new Color( 120, 240, 180, 100));
				// page.setColor( new Color( 150, 20, 20, 128));
				page.fillRect( 0 , startOffset + ( e.getLineNo() - 1) * lineHeight, this.getWidth(), lineHeight);

				// the "details" button
				page.setColor( new Color( 80, 200, 140, 220));

				// the appropriate position of the info button
				Rectangle iconRect = getBoundOfInfoButton ( e.getLineNo());

				page.drawImage( infoIcon.getImage(), iconRect.x, iconRect.y, iconRect.width, iconRect.height, null);
			}
		}		
	}

	// setup the error source
	public void setErrors ( ScriptErrorList errors)
	{
		this.errors = errors;
		
		repaint();
	}

	// clear all errors
	public void clearErrors ()
	{
		this.errors = null;
	}

	// returns the bounds of a info button
	public Rectangle getBoundOfInfoButton ( int line )
	{
		int startOffset = 3;
		int lineHeight = this.getFontMetrics( this.getFont()).getHeight();

		return new Rectangle ( this.getWidth() - 50, startOffset + ( line - 1) * lineHeight + 3, 19, 19);
	}

	/////////////////////////////////// methods for managing syntax highlighting

	// scans the document for appropriate syntax highlighting
	private void updateSyntaxColor ()
	{
		// to keep track of our position in the document
		int charNo;
		// the actual text:
		String script;
		String line;

		// scanner for ease of operation
		Scanner scriptScanner;

		// get the text and separate it into lines
		script = this.getText();

		// repace the semicolons with newlines
		script = script.replace( NEXT_LINE_TOKEN, "" + '\n');

		// scan every line one by one
		scriptScanner = new Scanner( script);
		charNo = 0;
		while ( scriptScanner.hasNext())
		{
			line = scriptScanner.nextLine();
			// color this line appropriately
			processLineColor( line, charNo);

			charNo += line.length() + 1;
		}

		scriptScanner.close();

		// set the style semicolons as black
		int i;
		i = script.indexOf( ';');
		while ( i >= 0)
		{
			doc.setCharacterAttributes( i, 1, getStyle( "empty"), true);

			i = script.indexOf( ';', i + 1);
		}
	}

	// colors a single line appropriately:
	private void processLineColor( String line, int startPos)
	{
		int charPos = 0;
		int tokenStart = 0;
		int tokenNo = 0;

		if ( line.isEmpty())
			return;

		// first filter comments
		if ( (tokenStart = line.indexOf( COMMENT_TOKEN)) >= 0)
		{
			doc.setCharacterAttributes( tokenStart + startPos, line.length() - tokenStart, getStyle( "comment"), true);
			line = line.substring( 0, tokenStart);
		}

		// scan each token one by one
		while ( charPos < line.length())
		{
			// advance till the next token start
			while ( charPos < line.length() && ( "" + line.charAt( charPos)).matches( REGEX_SEPERATOR))
			{
				charPos++;
			}

			if ( charPos > line.length())
				return;

			// this is the start of our current token
			tokenStart = charPos;

			charPos++;
			// advance till the token end
			while ( charPos < line.length() && !( "" + line.charAt( charPos)).matches( REGEX_SEPERATOR) )
			{
				charPos++;
			}

			if ( charPos > line.length())
				return;

			// we now have the start and end points of this token
			// so process the token's style:
			if ( tokenNo == 0) // if we are on the first token, possibly a command
			{
				// if is a valid command
				if ( Arrays.asList( commandNames).contains( line.substring(tokenStart, charPos)))
				{
					doc.setCharacterAttributes( tokenStart + startPos, charPos - tokenStart, getStyle( "command"), true);
				}
				else if ( line.charAt( tokenStart) == '#') // preprocessor commands
				{
					doc.setCharacterAttributes( tokenStart + startPos, line.length() + tokenStart, getStyle( "pre"), true);
					return;
				}
				else
				{
					doc.setCharacterAttributes( tokenStart + startPos, charPos - tokenStart, getStyle( "empty"), true);
				}
			}
			else 
			{
				String token = line.substring( tokenStart, charPos);

				if ( token.matches( REGEX_DOUBLE)) // a double literal
				{
					doc.setCharacterAttributes( tokenStart + startPos, charPos - tokenStart, getStyle( "literal"), true);
				}
				else if ( token.matches( REGEX_VAR_NAME)) // a variable name
				{
					doc.setCharacterAttributes( tokenStart + startPos, charPos - tokenStart, getStyle( "var"), true);
				}
				else
				{
					doc.setCharacterAttributes( tokenStart + startPos, charPos - tokenStart, getStyle( "empty"), true);
				}
			}

			tokenNo++;
		}
		
		
	}

	/////////////////////////////////// methods for managing error indicators

	// update the positions of the errors depending on the event
	public void manageErrors ( DocumentEvent event)
	{
		// System.out.println( "c = " + clip.y + " " + clip.x + " " + clip.width + " " + clip.height);

		if ( errors == null)
			return;

		if ( event.getType() == DocumentEvent.EventType.INSERT) // text was inserted
		{
			String addedString = "";
			String text;
			int linesTillOffset;
			int newLines = 0;

			text = this.getText();

			try {
				addedString = doc.getText( event.getOffset(), event.getLength());
			} catch (BadLocationException e) {
			}

			linesTillOffset = 0;
			// count the line of the change
			for ( int i = 0; i < event.getOffset(); i++)
			{
				if ( text.charAt( i) == '\n')
					linesTillOffset++;
			}

			// count the number of newlines added
			for ( int i = 0; i < addedString.length(); i++)
			{
				if ( addedString.charAt( i) == '\n')
					newLines++;
			}

			errors.pushErrorsBeyondLine( linesTillOffset + 1, newLines);

		}
		else if ( event.getType() == DocumentEvent.EventType.REMOVE) // text was removed
		{
			// the deleted part of the string
			String deletedString;
			// line number at the start of the string
			int lineAtStart;
			// removed newline count
			int newlinesRemoved;

			deletedString = "";

			// calculate the line where the deleting took place
			lineAtStart = 1;
			for ( int i = 0; i < event.getOffset(); i++)
			{
				if ( lastVersion.charAt( i) == '\n' )
				{
					lineAtStart++;
				}
			}

			// get the deleted part
			deletedString = lastVersion.substring( event.getOffset(), event.getOffset() + event.getLength());

			// first case: the deleted string consists of one character which a newline character
			if ( deletedString.equals( "\n") || deletedString.equals( "\r\n"))
			{
				// delete the errors in the merged lines
				errors.removeErrorsAtLine( lineAtStart); // TODO can improve this
				errors.removeErrorsAtLine( lineAtStart + 1);

				// pull the errors after the deleted spot
				errors.pullErrorsBeyondLine( lineAtStart, 1);
			}
			else // multiple characters were deleted
			{
				// count the number of newlines removed
				newlinesRemoved = 0;
				for ( int i = 0; i < deletedString.length(); i++)
				{
					if ( deletedString.charAt( i) == '\n' )
					{
						newlinesRemoved++;
					}
				}

				// if we are on a line end
				if (  lastVersion.length() == event.getOffset() + event.getLength() || lastVersion.charAt( event.getOffset() + event.getLength()) == '\n')
				{
					deletedString += "\n";
				}

				// if we are on a line beginning
				if ( 0 == event.getOffset() || lastVersion.charAt( event.getOffset() - 1) == '\n')
				{
					deletedString =  "\n" + deletedString;
				}

				// delete the errors between the text removed
				int oldPos = -1, curPos;
				int lineNo = lineAtStart - 1;
				for ( int i = deletedString.indexOf( '\n'); i >= 0; i = deletedString.indexOf( '\n', i + 1))
				{
					curPos = i;

					if ( oldPos >= 0)
						errors.removeErrorsAtLine( lineNo);

					lineNo++;
					oldPos = curPos;
				}

				// pull back the errors after the deleted spot
				errors.pullErrorsBeyondLine( lineAtStart, newlinesRemoved);
			}
		}

		lastVersion = this.getText();
	}

	private class CodeListener implements DocumentListener
	{

		@Override
		public void changedUpdate (DocumentEvent e) {

		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if ( setTextCalled)
				return;
			
			ScriptTextPane.this.clearErrors();

			// update the error signs:
			// manageErrors( e);

			// update the syntax coloring
			SwingUtilities.invokeLater( new Runnable() {
				public void run()
				{
					updateSyntaxColor();
				}
			});
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if ( setTextCalled)
				return;

			// update the error signs:
			// manageErrors( e);
			
			ScriptTextPane.this.clearErrors();

			// update the syntax coloring
			SwingUtilities.invokeLater( new Runnable() {
				public void run()
				{
					updateSyntaxColor();
				}
			});
		}
	}

	private class MouseTracker extends MouseAdapter
	{
		@Override
		public void mouseMoved ( MouseEvent e)
		{
			System.out.println( "moved" );
		}
	}
}


