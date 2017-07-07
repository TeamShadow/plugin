package shadow.plugin.outline;

import org.eclipse.core.runtime.Path;

public enum ShadowLabel
{
	COMPILATION_UNIT("class_obj.gif"),
	CLASS("class_obj.gif"),
	PRIVATE_CONSTANT("constant_private_obj.gif"),
	PROTECTED_CONSTANT("constant_protected_obj.gif"),
	PUBLIC_CONSTANT("field_public_obj.gif"),
	ENUM("enum_obj.gif"),
	EXCEPTION("exception_obj.gif"),
	ERROR("error_obj.gif"),
	FIELD("field_private_obj.gif"),
	INTERFACE("int_obj.gif"),
	PACKAGE("package_obj.gif"),
	PRIVATE_METHOD("methpri_obj.gif"),
	PROTECTED_METHOD("methpro_obj.gif"),
	PUBLIC_METHOD("methpub_obj.gif"),
	SINGLETON("singleton_obj.gif");

	public Path path;

	private ShadowLabel(String path)
	{
		this.path = new Path("/icons/" + path);
	}
}