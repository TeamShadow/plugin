package shadow.plugin.outline;

public class ShadowOutlineError extends Exception {  	
	private int line;
	private int column;	

	public ShadowOutlineError(int errorLine, int errorColumn, String message)
	{
		super(message);
		this.line = errorLine;
		this.column = errorColumn;    
	}

	public boolean hasError()
	{
		return (this.line | this.column) != 0;
	}

	public int getLine()
	{
		return this.line;
	}

	public int getColumn()
	{
		return this.column;
	}

	public String toString()
	{
		if (hasError()) {
			return "Parse error [" + getLine() + ':' + getColumn() + "]: " + getMessage();
		}
		return "Outline unavailable. Click to check path to shadow.jar.";
	}
}
