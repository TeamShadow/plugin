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
import shadow.plugin.outline.ShadowLabel;

public class ShadowCompilerInterface
{
	private Class<?> parserClass;
	private Class<?> declarationClass;
	private Class<?> nodeClass;
	private int errorLine;
	private int errorColumn;
	private String message = null;	

	public ShadowCompilerInterface()
	{
		try
		{
			IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
			        .getPreferenceStore();
			String pathToJar = preferenceStore.getString("PATH");
			
			URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };
			URLClassLoader loader = URLClassLoader.newInstance(urls);
			this.parserClass = loader.loadClass("shadow.parser.javacc.ShadowParser");
			this.nodeClass = loader.loadClass("shadow.parser.javacc.Node");
			this.declarationClass = loader.loadClass("shadow.parser.javacc.ASTClassOrInterfaceDeclaration");
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
	
	public String getMessage()
	{
		return message;
	}
	
	
	private String getKind(Object declaration)
	{
		try {
			Object kind = declarationClass.getMethod("getKind", new Class[0]).invoke(declaration, new Object[0]);
			return kind.toString().toLowerCase();
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			return "";
		}		
	}


	public class Tree
	{  
		private Object node;
		private Tree[] children;
		private Tree parent;
		private ShadowLabel label;
		private String name;

		public Tree(Object node, Tree parent, ShadowLabel label)
		{
			this.node = node;
			this.parent = parent;
			this.label = label;	
			
			try {
				this.name = (String)nodeClass.getMethod("getImage", new Class[0]).invoke(node, new Object[0]);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				this.name = "";
			}			
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
			return name;
		}
		
		public Object getNode()
		{
			return node;
		}
		
		public ShadowLabel getLabel()
		{
			return label;			
		}
		
		public void setLabel(ShadowLabel label)
		{
			this.label = label;
		}
	}	
	
	private Object getParent(Object node)
	{
		if ((this.nodeClass != null) && (this.nodeClass.isInstance(node))) {
			try
			{
				return this.nodeClass.getMethod("jjtGetParent", new Class[0]).invoke(node, new Object[0]);
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return node;
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
	
	private String getModifiers(Object node)
	{
		if ((this.nodeClass != null) && (this.nodeClass.isInstance(node))) {
			try
			{
				Object modifiers = this.nodeClass.getMethod("getModifiers", new Class[0]).invoke(node, new Object[0]);
				return modifiers.toString();
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return "";
	}
	
	private void bodyDeclaration(Object declaration, Tree tree, ArrayList<Tree> children)
	{
		//element 0 is modifiers, element 1 is declarator
		Object[] modifierAndDeclarator = getNodes(declaration);
		Object declarator = modifierAndDeclarator[1];
		
		if( declarator.getClass().getSimpleName().equals("ASTFieldDeclaration"))
		{
			Object[] variables = getNodes(declarator);
			for( int i = 1; i < variables.length; ++i )
				children.add(buildTree(variables[i], tree));							
		}
		else if( declarator.getClass().getSimpleName().equals("ASTMethodDeclaration")  )							
			children.add(buildTree(getNodes(declarator)[0], tree));
		else
			children.add(buildTree(declarator, tree));
	}

	private Tree buildTree(Object element, Tree parent)
	{
		Tree tree = null;
		Class<? extends Object> elementClass = element.getClass();
		ArrayList<Tree> children = new ArrayList<Tree>();
		Object[] nodes = getNodes(element);	
		
		String modifiers;
		String kind;

		switch( elementClass.getSimpleName() )
		{
		case "ASTCompilationUnit": 
			tree = new Tree(element, parent, ShadowLabel.COMPILATION_UNIT);
			for( Object node : nodes )
			{
				String name = node.getClass().getSimpleName();
				if( name.equals("ASTClassOrInterfaceDeclaration") || name.equals("ASTEnumDeclaration") )
					children.add(buildTree(node, tree));
			}
			break;
		case "ASTClassOrInterfaceDeclaration":
			kind = getKind(element);
			if( kind.contains("singleton"))
				tree = new Tree(element, parent, ShadowLabel.SINGLETON);
			else if( kind.contains("exception"))
				tree = new Tree(element, parent, ShadowLabel.EXCEPTION);
			else if( kind.contains("interface"))
				tree = new Tree(element, parent, ShadowLabel.INTERFACE);
			else
				tree = new Tree(element, parent, ShadowLabel.CLASS);
			
			for( Object node : nodes )
			{
				String name = node.getClass().getSimpleName();
				if( name.equals("ASTClassOrInterfaceBody") )
				{
					Object[] declarations = getNodes(node);
					for( Object declaration : declarations )
						bodyDeclaration(declaration, tree, children);		
				}
			}
			break;
		case "ASTEnumDeclaration":
			tree = new Tree(element, parent, ShadowLabel.ENUM);
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
							bodyDeclaration(declaration, tree, children);
					}					
				}
			}			
			break;
		case "ASTEnumConstant":
			tree = new Tree(element, parent, ShadowLabel.CONSTANT);
			break;
		case "ASTVariableDeclarator":
			modifiers = getModifiers(getParent(element));
			if( modifiers.contains("constant") )
				tree = new Tree(element, parent, ShadowLabel.CONSTANT);
			else
				tree = new Tree(element, parent, ShadowLabel.FIELD);
			break;
		case "ASTCreateDeclaration":
		case "ASTDestroyDeclaration":
		case "ASTMethodDeclarator":			
			if(elementClass.getSimpleName().equals("ASTMethodDeclarator"))			
				modifiers = getModifiers(getParent(element));
			else
				modifiers = getModifiers(element);			
			if( modifiers.contains("public"))
				tree = new Tree(element, parent, ShadowLabel.PUBLIC_METHOD);
			else if( modifiers.contains("protected"))
				tree = new Tree(element, parent, ShadowLabel.PROTECTED_METHOD);
			else
				tree = new Tree(element, parent, ShadowLabel.PRIVATE_METHOD);
			break;
		}

		tree.setChildren(children.toArray(new Tree[children.size()]));
		return tree;
	}

	public Tree compile(InputStream input)
	{
		this.errorLine = (this.errorColumn = 0);
		this.message = null;
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
				//Format of error:
				//[15:9] Unexpected uint
				scanner.useDelimiter("(\\[|:|\\])");				
				this.errorLine = scanner.nextInt();
				this.errorColumn = scanner.nextInt();				
				this.message = scanner.next().trim();
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
		this.message = null;
		if (this.parserClass != null) {
			try
			{
				Object parser = this.parserClass
						.getConstructor(new Class[] {InputStream.class, String.class })
						.newInstance(new Object[] {input, encoding });
				return this.parserClass.getMethod("CompilationUnit", new Class[0])
						.invoke(parser, new Object[0]);
			}
			catch (InvocationTargetException ex) 
			{	
				Scanner scanner = new Scanner(ex.getTargetException().getMessage());
				//Format of error:
				//[15:9] Unexpected uint
				scanner.useDelimiter("(\\[|:|\\])");
				this.errorLine = scanner.nextInt();
				this.errorColumn = scanner.nextInt();				
				this.message = scanner.next().trim();
				scanner.close();
			}
			catch (Exception ex)
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
