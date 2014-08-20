package shadow.plugin.util;

import org.eclipse.jface.text.rules.IWordDetector;

public class ShadowWordDetector
  implements IWordDetector
{
  public boolean isWordStart(char c)
  {
    return 
      ((c >= '\u0041') && (c <= '\u005a')) || 
      ((c >= '\u0061') && (c <= '\u007a')) || 
      ((c >= '\u00c0') && (c <= '\u00d6')) || 
      ((c >= '\u00d8') && (c <= '\u00f6')) || 
      ((c >= '\u00f8') && (c <= '\u00ff')) || 
      ((c >= '\u0100') && (c <= '\u1fff')) || 
      ((c >= '\u3040') && (c <= '\u318f')) || 
      ((c >= '\u3300') && (c <= '\u337f')) || 
      ((c >= '\u3400') && (c <= '\u3d2d')) || 
      ((c >= '\u4e00') && (c <= '\u9fff')) || (
      (c >= '\uf900') && (c <= '\ufaff'));
  }
  
  public boolean isWordPart(char c)
  {
    return (isWordStart(c)) || 
    	       ((c >= '\u0030') && (c <= '\u0039')) ||
    	       ((c == '\u005f')) || //underscore (not a legal way to start a variable in Shadow)
    	       ((c >= '\u0660') && (c <= '\u0669')) ||
    	       ((c >= '\u06f0') && (c <= '\u06f9')) ||
    	       ((c >= '\u0966') && (c <= '\u096f')) ||
    	       ((c >= '\u09e6') && (c <= '\u09ef')) ||
    	       ((c >= '\u0a66') && (c <= '\u0a6f')) ||
    	       ((c >= '\u0ae6') && (c <= '\u0aef')) ||
    	       ((c >= '\u0b66') && (c <= '\u0b6f')) ||
    	       ((c >= '\u0be7') && (c <= '\u0bef')) ||
    	       ((c >= '\u0c66') && (c <= '\u0c6f')) ||
    	       ((c >= '\u0ce6') && (c <= '\u0cef')) ||
    	       ((c >= '\u0d66') && (c <= '\u0d6f')) ||
    	       ((c >= '\u0e50') && (c <= '\u0e59')) ||
    	       ((c >= '\u0ed0') && (c <= '\u0ed9')) ||
    	       ((c >= '\u1040') && (c <= '\u1049'));
  }
}
