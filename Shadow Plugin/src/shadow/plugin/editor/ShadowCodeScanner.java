package shadow.plugin.editor;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import shadow.plugin.ShadowPlugin;
import shadow.plugin.util.ShadowColorProvider;
import shadow.plugin.util.ShadowData;
import shadow.plugin.util.ShadowWhitespaceDetector;
import shadow.plugin.util.ShadowWordDetector;

public class ShadowCodeScanner
  extends RuleBasedScanner
  implements ShadowData
{
  public ShadowCodeScanner()
  {
    ShadowColorProvider colorProvider = ShadowPlugin.getDefault().getColorProvider();
    IToken defaultToken = colorProvider.getToken(ShadowColorProvider.DEFAULT);
    IToken literalToken = colorProvider.getToken(ShadowColorProvider.LITERAL);
    IToken typeToken = colorProvider.getToken(ShadowColorProvider.TYPE);
    IToken keywordToken = colorProvider.getToken(ShadowColorProvider.KEYWORD);
    IToken commentToken = colorProvider.getToken(ShadowColorProvider.COMMENT);
    
    ShadowDecimalRule decimalRule = new ShadowDecimalRule(literalToken);
    for (String suffix : DECIMAL_SUFFIXES) {
      decimalRule.addSuffix(suffix);
    }
    ShadowIntegerRule integerRule = new ShadowIntegerRule(literalToken, PREFIXES[0]);
    for (int i = 1; i < PREFIXES.length; i += 2) {
      integerRule.addPrefix((char)PREFIXES[i], 
        PREFIXES[(i + 1)]);
    }
    for (String suffix : INTEGRAL_SUFFIXES) {
      integerRule.addSuffix(suffix);
    }
    WordRule wordRule = new WordRule(new ShadowWordDetector(), defaultToken);
    for (String word : LITERALS) {
      wordRule.addWord(word, literalToken);
    }
    for (String word : TYPES) {
      wordRule.addWord(word, typeToken);
    }
    for (String word : KEYWORDS) {
      wordRule.addWord(word, keywordToken);
    }
    setRules(new IRule[] {
    
      new WhitespaceRule(new ShadowWhitespaceDetector()), 
      
      new EndOfLineRule("//", commentToken), 
      
      new SingleLineRule("'", "'", literalToken, '\\'), 
      
      new SingleLineRule("\"", "\"", literalToken, '\\'), 
      
      decimalRule, integerRule, 
      
      wordRule });
  }
}
