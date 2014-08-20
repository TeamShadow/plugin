package shadow.plugin.outline;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public class ShadowLabelProvider
  extends BaseLabelProvider
  implements ILabelProvider
{
  public String getText(Object element)
  {
    if ((element == null) || ((element instanceof String)) || 
      ((element instanceof ShadowOutlineError))) {
      return String.valueOf(element);
    }
    return element.getClass().getSimpleName();
  }
  
  public Image getImage(Object element)
  {
    return null;
  }
  
  public void dispose()
  {
    super.dispose();
  }
}
