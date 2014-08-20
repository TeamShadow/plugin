package shadow.plugin.editor;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class ShadowDecimalRule
  implements IRule
{
  private IToken successToken;
  private Set<String> suffixes;
  private StringBuilder builder;
  
  public ShadowDecimalRule(IToken token)
  {
    this.successToken = token;
    this.suffixes = new HashSet<String>();
    this.builder = new StringBuilder();
  }
  
  public void addSuffix(String suffix)
  {
    this.suffixes.add(suffix);
  }
  
  public IToken evaluate(ICharacterScanner scanner)
  {
    boolean foundDecimal = false;
    int length = 0;int eLength = 0;int c = scanner.read();
    if (((c < '0') || (c > '9')) && (c != '.'))
    {
      scanner.unread();
      return Token.UNDEFINED;
    }
    while (((c >= '0') && (c <= '9')) || ((!foundDecimal) && (c == '.')))
    {
      if (c == '.') {
        foundDecimal = true;
      } else {
        length++;
      }
      c = scanner.read();
    }
    if ((!foundDecimal) && (c == '.'))
    {
      c = scanner.read();
      foundDecimal = (c >= '0') && (c <= '9');
      if (!foundDecimal)
      {
        scanner.unread();
        c = '.';
      }
    }
    if ((eLength == 0) && ((c == 'e') || (c == 'E')))
    {
      c = scanner.read();
      if ((c == '+') || (c == '-'))
      {
        eLength = 2;
        c = scanner.read();
      }
      else
      {
        eLength = 1;
      }
      if ((c < '0') || (c > '9'))
      {
        for (int i = 0; i < eLength; i++) {
          scanner.unread();
        }
        eLength = 0;
      }
      else
      {
        do
        {
          eLength++;
          c = scanner.read();
        } while ((c >= '0') && (c <= '9'));
      }
    }
    this.builder.setLength(0);
    while (((c >= '0') && (c <= '9')) || 
      ((c >= 'a') && (c <= 'z')) || (
      (c >= 'A') && (c <= 'Z')))
    {
      this.builder.appendCodePoint(c);
      c = scanner.read();
    }
    scanner.unread();
    if ((length > 0) && (this.suffixes.contains(this.builder.toString()))) {
      return this.successToken;
    }
    for (int i = 0; i < this.builder.length(); i++) {
      scanner.unread();
    }
    for (int i = 0; i < eLength; i++) {
      scanner.unread();
    }
    if (foundDecimal) {
      scanner.unread();
    }
    for (int i = 0; i < length; i++) {
      scanner.unread();
    }
    return Token.UNDEFINED;
  }
}
