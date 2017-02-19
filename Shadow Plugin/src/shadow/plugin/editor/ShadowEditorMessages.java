package shadow.plugin.editor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ShadowEditorMessages {

	private static final String RESOURCE_BUNDLE= "shadow.plugin.editor.ShadowEditorMessages";//$NON-NLS-1$

	private static ResourceBundle resourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private ShadowEditorMessages() {
	}

	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	public static ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
