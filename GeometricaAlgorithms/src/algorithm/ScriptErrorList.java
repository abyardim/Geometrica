package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;
import java.util.Iterator;

// manages a set of errors related to the script
public class ScriptErrorList implements Iterable<ScriptError> {
	private ArrayList<ScriptError> errors;
	
	public ScriptErrorList ()
	{
		errors = new ArrayList<ScriptError>();
	}
	
	// getters/setters:
	
	public ArrayList<ScriptError> getErrors ()
	{
		return errors;
	}
	
	public ArrayList<ScriptError> getErrorsAtLine( int line)
	{
		ArrayList<ScriptError> errorsOnLine;
		
		errorsOnLine = new ArrayList<ScriptError>();
		
		for ( ScriptError e : errors)
		{
			if ( e.getLineNo() == line)
				errorsOnLine.add( e);
		}
		
		return errorsOnLine;
	}
	
	public void addError ( ScriptError e)
	{
		errors.add( e);
	}
	
	public void removeError ( ScriptError e)
	{
		errors.remove( e);
	}
	
	public String toString ()
	{
		String res = "Errors:\n";
		
		for ( ScriptError e : errors)
		{
			res += e + "\n";
		}
		
		return res;
	}
	
	// high level managing of errors:
	
	// used increment/decrement the line numbers of errors
	
	public void removeErrorsAtLine ( int line)
	{
		Iterator<ScriptError> eIter;
		ScriptError e;
		
		eIter = errors.iterator();
		while ( eIter.hasNext())
		{
			e = eIter.next();
			
			if ( e.getLineNo() == line)
				eIter.remove();
		}
	}
	
	public void pushErrorsBeyondLine ( int line, int offset)
	{
		Iterator<ScriptError> eIter;
		ScriptError e;
		
		eIter = errors.iterator();
		while ( eIter.hasNext())
		{
			e = eIter.next();
			
			if ( e.getLineNo() > line)
				e.setLineNo( e.getLineNo() + offset);
		}
	}
	
	public void pullErrorsBeyondLine ( int line, int offset)
	{
		Iterator<ScriptError> eIter;
		ScriptError e;
		
		eIter = errors.iterator();
		while ( eIter.hasNext())
		{
			e = eIter.next();
			
			if ( e.getLineNo() > line)
				e.setLineNo( e.getLineNo() - offset);
		}
	}

	@Override
	public Iterator<ScriptError> iterator() {
		return errors.iterator();
	}
}
