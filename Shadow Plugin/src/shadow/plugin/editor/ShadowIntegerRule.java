package shadow.plugin.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class ShadowIntegerRule
  implements IRule
{
  private IToken successToken;
  private int defaultRadix;
  private Map<Character, Integer> prefixes;
  private Set<String> suffixes;
  private StringBuilder builder;
  
  public ShadowIntegerRule(IToken token, int radix)
  {
    this.successToken = token;
    this.defaultRadix = radix;
    this.prefixes = new HashMap<Character, Integer>();
    this.suffixes = new HashSet<String>();
    this.builder = new StringBuilder();
  }
  
  public void addPrefix(char prefix, int radix)
  {
    this.prefixes.put(Character.valueOf(prefix), Integer.valueOf(radix));
  }
  
  public void addSuffix(String suffix)
  {
    this.suffixes.add(suffix);
  }
  
  public IToken evaluate(ICharacterScanner scanner)
  {
    boolean foundPrefix = false;
    int radix = this.defaultRadix;int length = 0;int c = scanner.read();
    if ((c < 48) || (c > 57))
    {
      scanner.unread();
      return Token.UNDEFINED;
    }
    if (c == 48)
    {
      c = scanner.read();
      Integer prefixRadix = null;
      if (c != -1) {
        prefixRadix = (Integer)this.prefixes.get(Character.valueOf((char)c));
      }
      if (prefixRadix != null)
      {
        foundPrefix = true;
        radix = prefixRadix.intValue();
        c = scanner.read();
      }
      else
      {
        scanner.unread();
        c = 48;
      }
    }
    while (Character.digit(c, radix) != -1)
    {
      length++;
      c = scanner.read();
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
    for (int i = 0; i < length; i++) {
      scanner.unread();
    }
    if (foundPrefix)
    {
      scanner.unread();
      scanner.unread();
    }
    return Token.UNDEFINED;
  }
}
