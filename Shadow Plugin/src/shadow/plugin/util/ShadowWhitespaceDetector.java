package shadow.plugin.util;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class ShadowWhitespaceDetector
  implements IWhitespaceDetector
{
  public boolean isWhitespace(char c)
  {
    return (c == ' ') || (c == '\t') || (c == '\n') || (c == '\r') || (c == '\f');
  }
}
