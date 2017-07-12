package shadow.plugin.outline;

import org.eclipse.core.runtime.Path;

public enum ShadowLabel
{
	ERROR("error_obj.gif", -1),
	
	COMPILATION_UNIT("class_obj.gif", 0),
	
	PACKAGE("package_obj.gif", 1),
	
	PUBLIC_CLASS("class_obj.gif", 2),
	PRIVATE_CLASS("innerclass_private_obj.gif", 2),
	PROTECTED_CLASS("innerclass_protected_obj.gif", 2),
	EXCEPTION("exception_obj.gif", 2),	
	ENUM("enum_obj.gif", 2),
	INTERFACE("int_obj.gif", 2),
	SINGLETON("singleton_obj.gif", 2),
	
	PRIVATE_CONSTANT("constant_private_obj.gif", 3),
	PROTECTED_CONSTANT("constant_protected_obj.gif", 3),
	PUBLIC_CONSTANT("field_public_obj.gif", 3),
	
	FIELD("field_private_obj.gif", 4),
	
	PRIVATE_METHOD("methpri_obj.gif", 5),
	PROTECTED_METHOD("methpro_obj.gif", 5),
	PUBLIC_METHOD("methpub_obj.gif", 5);
	

	public Path path;
	public int level;

	private ShadowLabel(String path, int level)
	{
		this.path = new Path("/icons/" + path);
		this.level = level;
	}
}