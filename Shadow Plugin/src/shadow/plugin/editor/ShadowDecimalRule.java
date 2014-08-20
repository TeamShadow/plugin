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
    this.suffixes = new HashSet();
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
    if (((c < 48) || (c > 57)) && (c != 46))
    {
      scanner.unread();
      return Token.UNDEFINED;
    }
    while (((c >= 48) && (c <= 57)) || ((!foundDecimal) && (c == 46)))
    {
      if (c == 46) {
        foundDecimal = true;
      } else {
        length++;
      }
      c = scanner.read();
    }
    if ((!foundDecimal) && (c == 46))
    {
      c = scanner.read();
      foundDecimal = (c >= 48) && (c <= 57);
      if (!foundDecimal)
      {
        scanner.unread();
        c = 46;
      }
    }
    if ((eLength == 0) && ((c == 101) || (c == 69)))
    {
      c = scanner.read();
      if ((c == 43) || (c == 45))
      {
        eLength = 2;
        c = scanner.read();
      }
      else
      {
        eLength = 1;
      }
      if ((c < 48) || (c > 57))
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
        } while ((c >= 48) && (c <= 57));
      }
    }
    this.builder.setLength(0);
    while (((c >= 48) && (c <= 57)) || 
      ((c >= 97) && (c <= 122)) || (
      (c >= 65) && (c <= 90)))
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
