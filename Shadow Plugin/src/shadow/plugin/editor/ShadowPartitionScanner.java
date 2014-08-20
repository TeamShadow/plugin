package shadow.plugin.editor;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class ShadowPartitionScanner
  extends RuleBasedPartitionScanner
{
  public static final String SHADOW_PARTITIONING = "__shadow_partioning";
  public static final String SHADOW_MULTI_LINE_COMMENT = "__shadow_multiline_comment";
  public static final String[] SHADOW_PARTITION_TYPES = { "__shadow_multiline_comment" };
  
  public ShadowPartitionScanner()
  {
    setPredicateRules(new IPredicateRule[] {
    
      new EndOfLineRule("//", Token.UNDEFINED), 
      
      new SingleLineRule("'", "'", Token.UNDEFINED, '\\'), 
      
      new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'), 
      
      new MultiLineRule("/*", "*/", new Token("__shadow_multiline_comment")) });
  }
}
