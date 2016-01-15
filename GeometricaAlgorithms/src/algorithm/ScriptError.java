package algorithm;
// Created: 23.04.2015, Ali Batuhan Yardým

public class ScriptError {
	private String errorText;
	private String errorTarget;
	private int lineNo;

	public ScriptError ( String errorText, String errorTarget, int lineNo)
	{
		this.errorText = errorText;
		this.errorTarget = errorTarget;
		this.lineNo = lineNo;
	}

	// getters
	public int getLineNo ()
	{
		return lineNo;
	}

	public void setLineNo ( int lineNo)
	{
		this.lineNo = lineNo;
	}

	public String getText ()
	{
		return errorText;
	}

	public String getTarget ()
	{
		return errorTarget;
	}

	public String toString ()
	{
		return "Line: " + lineNo + " " + errorTarget + " Error: " + errorText;
	}
}
