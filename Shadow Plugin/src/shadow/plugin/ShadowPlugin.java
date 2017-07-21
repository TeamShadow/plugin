package shadow.plugin;

import java.io.IOException;
import java.net.URL;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import shadow.Loggers;
import shadow.plugin.editor.ShadowCodeScanner;
import shadow.plugin.editor.ShadowPartitionScanner;
import shadow.plugin.util.ShadowColorProvider;

public class ShadowPlugin
extends AbstractUIPlugin
{
	public final static String SHADOW_PARTITIONING= "__shadow_partitioning"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "shadow.plugin";
	public static final String SHADOW_ICON = "shadow.plugin.icon";

	private static ShadowPlugin plugin;
	private ShadowColorProvider colorProvider;
	private ShadowCodeScanner codeScanner;
	private ShadowPartitionScanner partitionScanner;  

	public ShadowPlugin() {
		try {
			URL fileURL = FileLocator.find(getBundle(), new Path("/icons/shadow.png"), null);
			URL url = FileLocator.resolve(fileURL);
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
			getImageRegistry().put(SHADOW_ICON, imageDescriptor);
		}
		catch (IOException e)
		{}

		//turn off parse error logging for plugin
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(Loggers.PARSER.getName()); 
		loggerConfig.setLevel(Level.OFF);
		ctx.updateLoggers(); 
	}

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
}
