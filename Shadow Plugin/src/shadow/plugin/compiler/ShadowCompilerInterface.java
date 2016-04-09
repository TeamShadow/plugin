package shadow.plugin.compiler;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StyledString;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.outline.ShadowLabel;

public class ShadowCompilerInterface
{
	private Class<?> parserClass;
	private Class<?> declarationClass;
	private Class<?> nodeClass;
	private Class<?> dimensionNodeClass;
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
			this.dimensionNodeClass = loader.loadClass("shadow.parser.javacc.DimensionNode");
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
		private String extra;
		
		public Tree(Object node, Tree parent, ShadowLabel label)
		{
			this(node, parent, "", label);
		}

		public Tree(Object node, Tree parent, String extra, ShadowLabel label)
		{
			this.node = node;
			this.parent = parent;
			this.label = label;	
			this.extra = extra;
			
			this.name = getImage(node);
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
			if( extra != null && !extra.isEmpty() )
			{
				if( label == ShadowLabel.FIELD || label == ShadowLabel.CONSTANT ) 
					return name + ": " + extra;
			
				else 
					return name + extra;
			}			
			
			return name;
		}
		
		public StyledString toStyledString()
		{
			if( extra != null && !extra.isEmpty() )
			{
				if( label == ShadowLabel.FIELD || label == ShadowLabel.CONSTANT ) 
					return new StyledString(name).append( ": " + extra, StyledString.DECORATIONS_STYLER);
			
				else 
					return new StyledString(name + extra);
			}			
			
			return new StyledString(name);
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
		
		public int getLength()
		{
			return name.length();
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
	
	private String getImage(Object node)
	{		
		try {
			return (String)nodeClass.getMethod("getImage", new Class[0]).invoke(node, new Object[0]);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			
			return "";
		}
	}
	
	private String getType(Object element)
	{
		for(Object node : getNodes(element) )
			if( node.getClass().getSimpleName().equals("ASTType"))
				return processType(node);
		
		
		return "";
	}
	
	@SuppressWarnings("unchecked")
	private String getArrayDimensions(Object node)
	{		
		try {
			
			List<Integer> dimensions = (List<Integer>)dimensionNodeClass.getMethod("getArrayDimensions", new Class[0]).invoke(node, new Object[0]);
			StringBuilder sb = new StringBuilder();
			for( int size : dimensions )
			{
					sb.append("[");
					for( int i = 1; i < size; ++i  )
						sb.append(",");
					sb.append("]");
			}
			
			return sb.toString(); //can be empty String if dimensions are empty 	
			
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			
			return "";
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
	
	private String processType( Object type )
	{		
		Object[] nodes;
		StringBuilder sb;
		boolean first = true;
		
		Class<? extends Object> typeClass = type.getClass();
		switch( typeClass.getSimpleName() )
		{
		case "ASTType": return processType( getNodes(type)[0] );			
		case "ASTPrimitiveType": return getImage( type );
		case "ASTFunctionType":
			nodes = getNodes(type);
			return processType(nodes[0]) + " => " + processType(nodes[1]);			
		case "ASTReferenceType": return processType( getNodes(type)[0] ) + getArrayDimensions( type );
		case "ASTClassOrInterfaceType":
			sb = new StringBuilder();
			for( Object node : getNodes(type) )
				if( node.getClass().getSimpleName().equals("ASTClassOrInterfaceTypeSuffix"))
					if( first )
					{
						sb.append(processType(node));
						first = false;
					}	
					else
						sb.append(":").append(processType(node));
						
			return sb.toString();
		case "ASTClassOrInterfaceTypeSuffix":
			nodes = getNodes(type);
			if( nodes.length > 0 )
				return getImage(type) + processType( nodes[0] );
			else
				return getImage(type);
		case "ASTTypeArguments":
		case "ASTTypeParameters":
			sb = new StringBuilder("<");
			for( Object node : getNodes(type) )
				if( first )
				{
					sb.append(processType(node));
					first = false;
				}
				else
					sb.append(",").append(processType(node));
			sb.append(">");
			return sb.toString();
		case "ASTTypeParameter":
			nodes = getNodes(type);
			if( nodes.length > 0 )
				return getImage(type) + " " + processType(nodes[0]);
			else
				return getImage(type);
		case "ASTIsList":
			sb = new StringBuilder("is ");
			for( Object node : getNodes(type) )
				if( first )
				{
					sb.append(processType(node));
					first = false;
				}
				else
					sb.append(" and ").append(processType(node));	
			return sb.toString();
		case "ASTResultTypes":
		case "ASTFormalParameters":
			sb = new StringBuilder("(");
			for( Object node : getNodes(type) )
				if( first )
				{
					sb.append(processType(node));
					first = false;
				}
				else
					sb.append(",").append(processType(node));
			sb.append(")");
			return sb.toString();
		case "ASTResultType":
		case "ASTFormalParameter":
			nodes = getNodes(type);
			return nodes[0].toString() + processType(nodes[1]);
		}
		
		return "";
	}

	private Tree buildTree(Object element, Tree parent)
	{
		Tree tree = null;
		Class<? extends Object> elementClass = element.getClass();
		ArrayList<Tree> children = new ArrayList<Tree>();
		Object[] nodes = getNodes(element);	
		
		String modifiers;
		String kind;
		String type;

		switch( elementClass.getSimpleName() )
		{
		case "ASTCompilationUnit": 
			tree = new Tree(element, parent, ShadowLabel.COMPILATION_UNIT);
			for( Object node : nodes )
			{
				String name = node.getClass().getSimpleName();
				if( name.equals("ASTClassOrInterfaceDeclaration") || name.equals("ASTEnumDeclaration") )
				{	 
					for( Object detail :  getNodes(node) )
					{
						if( detail.getClass().getSimpleName().equals("ASTUnqualifiedName") )
						{
							children.add(new Tree(detail, tree, ShadowLabel.PACKAGE));
							break;
						}						
					}					
					
					children.add(buildTree(node, tree));
				}
			}
			break;		 
		case "ASTClassOrInterfaceDeclaration":
			
			String typeParameters = "";
			for( Object node : nodes )
				if( node.getClass().getSimpleName().equals("ASTTypeParameters") )
					typeParameters = processType(node);
			
			kind = getKind(element);
			if( kind.contains("singleton"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.SINGLETON);
			else if( kind.contains("exception"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.EXCEPTION);
			else if( kind.contains("interface"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.INTERFACE);
			else
				tree = new Tree(element, parent, typeParameters, ShadowLabel.CLASS);
			
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
			type = getType(getParent(element));
			if( modifiers.contains("constant") )
				tree = new Tree(element, parent, type, ShadowLabel.CONSTANT);
			else
				tree = new Tree(element, parent, type, ShadowLabel.FIELD);
			break;
		case "ASTCreateDeclaration":
		case "ASTDestroyDeclaration":
		case "ASTMethodDeclarator":
			type = "";
			if(elementClass.getSimpleName().equals("ASTMethodDeclarator"))
			{
				type = processType(nodes[0]) + " => " + processType(nodes[1]); 
				modifiers = getModifiers(getParent(element));
			}
			else if( elementClass.getSimpleName().equals("ASTCreateDeclaration") )
			{
				type = processType(getNodes(nodes[0])[0]);
				modifiers = getModifiers(element);				
			}
			else
				modifiers = getModifiers(element);			
			if( modifiers.contains("public"))
				tree = new Tree(element, parent, type, ShadowLabel.PUBLIC_METHOD);
			else if( modifiers.contains("protected"))
				tree = new Tree(element, parent, type, ShadowLabel.PROTECTED_METHOD);
			else
				tree = new Tree(element, parent, type, ShadowLabel.PRIVATE_METHOD);
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
		if ((this.nodeClass != null) && (this.nodeClass.isInstance(element)))
		{
			try
			{   
				int numChildren = ((Integer)this.nodeClass.getMethod("jjtGetNumChildren", new Class[0]).invoke(element, new Object[0])).intValue();
				Object[] children = new Object[numChildren];
				for (int i = 0; i < numChildren; i++) {
					children[i] = this.nodeClass.getMethod("jjtGetChild", new Class[] { Integer.TYPE }).invoke(element, new Object[] { Integer.valueOf(i) });
				}

				return children;
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new Object[0];
	}
	
	public int getLength(Object element)
	{
		if( element instanceof Tree )
		{
			Tree tree = (Tree) element;
			return tree.getLength();			
		}
		
		return 1;
		
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
