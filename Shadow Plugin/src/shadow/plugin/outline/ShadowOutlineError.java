package shadow.plugin.outline;

public class ShadowOutlineError
{
  private int line;
  private int column;
  private String message;
  
  public ShadowOutlineError(int errorLine, int errorColumn, String message)
  {
    this.line = errorLine;
    this.column = errorColumn;
    this.message = message;
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
      return "Parse error [" + getLine() + ':' + getColumn() + "]: " + message;
    }
    return "Outline unavailable. Click to check path to shadow.jar.";
  }
}
