package shadow.plugin.outline;

public class ShadowOutlineError
{
  private int line;
  private int column;
  
  public ShadowOutlineError(int errorLine, int errorColumn)
  {
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
      return "Error [" + getLine() + ':' + getColumn() + ']';
    }
    return "Outline unavailable. Click to check path to shadow.jar.";
  }
}
