package shadow.plugin.util;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ShadowColorProvider
{
  public static RGB DEFAULT = new RGB(0, 0, 0);
  public static RGB LITERAL = new RGB(0, 128, 0);
  public static RGB TYPE = new RGB(0, 0, 128);
  public static RGB KEYWORD = new RGB(128, 0, 128);
  public static RGB COMMENT = new RGB(128, 128, 128);
  private Map<RGB, Color> table;
  
  public ShadowColorProvider()
  {
    this.table = new HashMap();
  }
  
  public Color getColor(RGB rgb)
  {
    Color color = (Color)this.table.get(rgb);
    if (color == null) {
      this.table.put(rgb, color = new Color(Display.getCurrent(), rgb));
    }
    return color;
  }
  
  public IToken getToken(RGB rgb)
  {
    return new Token(new TextAttribute(getColor(rgb)));
  }
  
  public void dispose()
  {
    for (Color color : this.table.values()) {
      color.dispose();
    }
  }
}
