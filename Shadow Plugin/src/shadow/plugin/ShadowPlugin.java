package shadow.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import shadow.plugin.compiler.ShadowCompilerInterface;
import shadow.plugin.editor.ShadowCodeScanner;
import shadow.plugin.editor.ShadowPartitionScanner;
import shadow.plugin.util.ShadowColorProvider;

public class ShadowPlugin
  extends AbstractUIPlugin
{
	public final static String SHADOW_PARTITIONING= "__shadow_partitioning"; //$NON-NLS-1$
  public static final String PLUGIN_ID = "shadow.plugin";
  private static ShadowPlugin plugin;
  private ShadowColorProvider colorProvider;
  private ShadowCodeScanner codeScanner;
  private ShadowPartitionScanner partitionScanner;
  private ShadowCompilerInterface compilerInterface;
  
  public void start(BundleContext context)
    throws Exception
  {
    super.start(context);
    plugin = this;
  }
  
  public void stop(BundleContext context)
    throws Exception
  {
    plugin = null;
    super.stop(context);
  }
  
  public static ShadowPlugin getDefault()
  {
    return plugin;
  }
  
  public ShadowColorProvider getColorProvider()
  {
    if (this.colorProvider == null) {
      this.colorProvider = new ShadowColorProvider();
    }
    return this.colorProvider;
  }
  
  public ShadowCodeScanner getCodeScanner()
  {
    if (this.codeScanner == null) {
      this.codeScanner = new ShadowCodeScanner();
    }
    return this.codeScanner;
  }
  
  public ShadowPartitionScanner getPartitionScanner()
  {
    if (this.partitionScanner == null) {
      this.partitionScanner = new ShadowPartitionScanner();
    }
    return this.partitionScanner;
  }
  
  public ShadowCompilerInterface getCompilerInterface()
  { 	 
    if (this.compilerInterface == null) {
      this.compilerInterface = new ShadowCompilerInterface();
    }
    return this.compilerInterface;
  }
  
  public void resetCompilerInterface()
  {
	  compilerInterface = null;
  }
}
