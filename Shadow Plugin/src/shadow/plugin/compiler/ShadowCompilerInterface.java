package shadow.plugin.compiler;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Scanner;

import org.eclipse.jface.preference.IPreferenceStore;

import shadow.plugin.ShadowPlugin;

public class ShadowCompilerInterface
{
	private Class<?> parserClass;
	private Class<?> nodeClass;
	private int errorLine;
	private int errorColumn;

	public ShadowCompilerInterface()
	{
		try
		{
			//ShadowClassLoader loader = new ShadowClassLoader(getClass().getClassLoader());
			//String pathToJar = "E:\\Documents\\Shadow Stuff\\shadow 0.6\\shadow.jar";
			
			IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
			        .getPreferenceStore();
			//String pathToJar = "E:\\Documents\\Shadow Stuff\\shadow 0.6\\shadow.jar";
			String pathToJar = preferenceStore.getString("PATH");
			
			URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };
			URLClassLoader loader = URLClassLoader.newInstance(urls);
			//String className = className.replace('/', '.');
			//Class c = loader.loadClass(className);

			//this.parserClass = Class.forName("shadow.parser.javacc.ShadowParser", false, loader);
			this.parserClass = loader.loadClass("shadow.parser.javacc.ShadowParser");
			//this.nodeClass = Class.forName("shadow.parser.javacc.Node", false, loader);
			this.nodeClass = loader.loadClass("shadow.parser.javacc.Node");
		}
		catch (ClassNotFoundException | MalformedURLException e)
		{
			this.parserClass = (this.nodeClass = null);
		}
	}

	public int getErrorLine()
	{
		return this.errorLine;
	}

	public int getErrorColumn()
	{
		return this.errorColumn;
	}


	public static class Tree
	{  

		enum Kind
		{
			COMPILATION_UNIT,
			CONSTANT,
			CLASS,			
			INTERFACE,
			ENUM,
			EXCEPTION,
			SINGLETON,
			FIELD,
			METHOD		  
		}	  

		private Object node;
		private Tree[] children;
		private Tree parent;
		private Kind kind;

		public Tree(Object node, Tree parent, Kind kind)
		{
			this.node = node;
			this.parent = parent;
			this.kind = kind;
		}

		public void setChildren(Tree[] children)
		{
			this.children = children;		  
		}

		public Tree[] getChildren() 
		{
			return children;
		}
		
		public boolean hasChildren()
		{
			return children != null && children.length > 0;			
		}
		
		public Tree getParent() 
		{
			return parent;
		}
		
		public String toString()
		{
			return node.toString();			
		}
		
		public Object getNode()
		{
			return node;
		}
		
		public Kind getKind()
		{
			return kind;			
		}		
	}

	private Object[] getNodes(Object node)
	{
		if ((this.nodeClass != null) && (this.nodeClass.isInstance(node))) {
			try
			{
				int numChildren = ((Integer)this.nodeClass.getMethod("jjtGetNumChildren", new Class[0]).invoke(node, new Object[0])).intValue();
				Object[] nodes = new Object[numChildren];
				for (int i = 0; i < numChildren; i++) {
					nodes[i] = this.nodeClass.getMethod("jjtGetChild", new Class[] { Integer.TYPE }).invoke(node, new Object[] { Integer.valueOf(i) });
				}

				return nodes;
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return new Object[0];
	}

	private Tree buildTree(Object element, Tree parent)
	{
		Tree tree = null;
		Class<? extends Object> elementClass = element.getClass();
		ArrayList<Tree> children = new ArrayList<Tree>();
		Object[] nodes = getNodes(element);		

		switch( elementClass.getSimpleName() )
		{
		case "ASTCompilationUnit": 
			tree = new Tree(element, parent, Tree.Kind.COMPILATION_UNIT);
			for( Object node : nodes )
			{
				String name = node.getClass().getSimpleName();
				if( name.equals("ASTClassOrInterfaceDeclaration") || name.equals("ASTEnumDeclaration") )
					children.add(buildTree(node, tree));
			}
			break;
		case "ASTClassOrInterfaceDeclaration":
			//TODO: Find a reasonable way to distinguish between class, interface, singleton, and exception
			tree = new Tree(element, parent, Tree.Kind.CLASS);
			for( Object node : nodes )
			{
				String name = node.getClass().getSimpleName();
				if( name.equals("ASTClassOrInterfaceBody") )
				{
					Object[] declarations = getNodes(node);
					for( Object declaration : declarations )
					{
						//element 0 is modifiers, element 1 is declarator
						Object[] modifierAndDeclarator = getNodes(declaration);
						children.add(buildTree(modifierAndDeclarator[1], tree));
					}					
				}
			}
			break;
		case "ASTEnumDeclaration":
			tree = new Tree(element, parent, Tree.Kind.ENUM);
			for( Object node : nodes )
			{
				String name = node.getClass().getSimpleName();
				if( name.equals("ASTEnumBody") )
				{
					Object[] declarations = getNodes(node);
					for( Object declaration : declarations )
					{
						if( declaration.getClass().getSimpleName().equals("ASTEnumConstant"))
							children.add(buildTree(declaration, tree));
						else //body declaration
						{						
							//element 0 is modifiers, element 1 is declarator
							Object[] modifierAndDeclarator = getNodes(declaration);
							children.add(buildTree(modifierAndDeclarator[1], tree));
						}
					}					
				}
			}			
			break;
		case "ASTEnumConstant":
			tree = new Tree(element, parent, Tree.Kind.CONSTANT);
			break;
		case "ASTFieldDeclaration":
			tree = new Tree(element, parent, Tree.Kind.FIELD);
			break;
		case "ASTCreateDeclaration":
		case "ASTDestroyDeclaration": 		
		case "ASTMethodDeclaration":
			tree = new Tree(element, parent, Tree.Kind.METHOD);
			break;
		}

		tree.setChildren(children.toArray(new Tree[children.size()]));
		return tree;
	}

	public Tree compile(InputStream input)
	{
		this.errorLine = (this.errorColumn = 0);
		if (this.parserClass != null) {
			try
			{
				Object parser = this.parserClass.getConstructor(new Class[] { InputStream.class })
						.newInstance(new Object[] {input });
				return buildTree(this.parserClass.getMethod("CompilationUnit", new Class[0])
						.invoke(parser, new Object[0]), null);
			}
			catch (InvocationTargetException ex)
			{
				Scanner scanner = new Scanner(ex.getTargetException().getMessage());
				scanner.useDelimiter("(Encountered \".*\" at line |, column |\\.)");
				this.errorLine = scanner.nextInt();
				this.errorColumn = scanner.nextInt();
				scanner.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}

	public Object compile(InputStream input, String encoding)
	{
		this.errorLine = (this.errorColumn = 0);
		if (this.parserClass != null) {
			try
			{
				Object parser = this.parserClass
						.getConstructor(new Class[] {InputStream.class, String.class })
						.newInstance(new Object[] {input, encoding });
				return this.parserClass.getMethod("CompilationUnit", new Class[0])
						.invoke(parser, new Object[0]);
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}

	public boolean hasChildren(Object element)
	{
		return getChildren(element).length != 0;
	}

	public Object[] getChildren(Object element)
	{	  
		ArrayList<Object> displayedChildren = new ArrayList<Object>();  

		if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
			try
			{    	  
				Class<? extends Object> elementClass = element.getClass();
				int numChildren = ((Integer)this.nodeClass.getMethod("jjtGetNumChildren", new Class[0]).invoke(element, new Object[0])).intValue();
				Object[] children = new Object[numChildren];
				for (int i = 0; i < numChildren; i++) {
					children[i] = this.nodeClass.getMethod("jjtGetChild", new Class[] { Integer.TYPE }).invoke(element, new Object[] { Integer.valueOf(i) });
				}

				switch( elementClass.getSimpleName() )
				{
				case "ASTCompilationUnit": 
					for( Object child : children )
					{
						String name = child.getClass().getSimpleName();
						if( name.equals("ASTClassOrInterfaceDeclaration") || name.equals("ASTEnumDeclaration") )
							displayedChildren.add(child);
					}
					break;
				case "ASTClassOrInterfaceDeclaration": break;
				case "ASTEnumDeclaration": break;
				case "ASTClassOrInterfaceBodyDeclaration": break;
				}

				return children;
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return displayedChildren.toArray();
	}

	public int getLine(Object element)
	{
		if( element instanceof Tree )
		{
			Tree tree = (Tree) element;
			element = tree.getNode();			
		}
		
		if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
			try
			{
				return ((Integer)this.nodeClass.getMethod("getLineStart", new Class[0]).invoke(element, new Object[0])).intValue();
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return 0;
	}

	public int getColumn(Object element)
	{		
		if( element instanceof Tree )
		{
			Tree tree = (Tree) element;
			element = tree.getNode();			
		}
		
		if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
			try
			{
				return ((Integer)this.nodeClass.getMethod("getColumnStart", new Class[0]).invoke(element, new Object[0])).intValue();
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return 0;
	}
}
