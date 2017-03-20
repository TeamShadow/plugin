package shadow.plugin.outline;

public class ShadowOutlineError extends Exception {  	

	private static final long serialVersionUID = 839187742137283860L;
	private int line;
	private int column;	

	public ShadowOutlineError(int errorLine, int errorColumn, String message) {
		super(message);
		this.line = errorLine;
		this.column = errorColumn;    
	}
	
	public ShadowOutlineError() {
		this(0, 0, "Outline unavailable. Click to check path to shadow.jar.");		
	}

	public boolean hasError() {
		return (this.line | this.column) != 0;
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}

	public String toString() {
		if (hasError()) {
			return "Parse error [" + getLine() + ':' + getColumn() + "]: " + getMessage();
		}
		return getMessage();
	}
}
