package shadow.plugin.editor;

import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.swt.graphics.RGB;
import shadow.plugin.ShadowPlugin;
import shadow.plugin.util.ShadowColorProvider;

public class ShadowColorScanner
  extends RuleBasedScanner
{
  public ShadowColorScanner(RGB color)
  {
    setDefaultReturnToken(ShadowPlugin.getDefault().getColorProvider().getToken(color));
  }
}
