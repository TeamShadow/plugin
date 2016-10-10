package shadow.plugin.compiler;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StyledString;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.outline.ShadowLabel;

public class ShadowCompilerInterface
{	
	private Class<?> contextClass; // replaces nodeClass
	private Class<?> shadowParserClass; // replaces parserClass, deprecated.
	private Class<?> parseCheckerClass; // replaces parserClass. Need to implement	
	private Class<?> loggersClass; // need to implement 
	//private Class<?> loggerClass; // need to implement
	private Class<?> errorReporterClass; // need to implement
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
			
			//JOptionPane.showMessageDialog(null, "The path to Jar: " + pathToJar); // Testing
			
			URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };
			
			//JOptionPane.showMessageDialog(null, "The Url: " + urls.toString()); // Testing
			
			URLClassLoader loader = URLClassLoader.newInstance(urls);
			
			//JOptionPane.showMessageDialog(null, "The loader: " + loader); // Testing
			
			this.shadowParserClass = loader.loadClass("shadow.parse.ShadowParser"); // replaces parserClass 
			this.contextClass = loader.loadClass("shadow.parse.Context"); // replaces nodeClass			
			this.loggersClass = loader.loadClass("shadow.Loggers");
			//this.loggerClass = loader.loadClass("org.apache.logging.log4j.Logger");
			this.errorReporterClass = loader.loadClass("shadow.typecheck.ErrorReporter");
			this.parseCheckerClass = loader.loadClass("shadow.parse.ParseChecker");
			
			
			
			//JOptionPane.showMessageDialog(null, "The Compiler Interface has finished being initialized"); // Testing
			
			//this.declarationClass = loader.loadClass("shadow.parser.javacc.ASTClassOrInterfaceDeclaration");
			//this.dimensionNodeClass = loader.loadClass("shadow.parser.javacc.DimensionNode");
		}
		catch (ClassNotFoundException | MalformedURLException e)
		{
			JOptionPane.showMessageDialog(null, "The Compiler Interface failed to initialize.");
			JOptionPane.showMessageDialog(null, e.toString());
			
			this.shadowParserClass = (this.contextClass = null);
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
	
	// deprecated, and no longer used.
	/*private String getKind(Object declaration)
	{
		try {
			Object kind = declarationClass.getMethod("getKind", new Class[0]).invoke(declaration, new Object[0]);
			return kind.toString().toLowerCase();
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			return "";
		}		
	}*/


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
			
			this.name = getContextChildren(node)[1].toString();
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
	
	private Object getParent(Object context)
	{
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try
			{
				return this.contextClass.getMethod("getParent", new Class[0]).invoke(context, new Object[0]);
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return context;
	}	
	
	private String getType(Object element)
	{
		for(Object context : getContexts(element) )
			if( context.getClass().getSimpleName().equals("Type"))
				return processType(context);
		
		
		return "";
	}
	
	/*@SuppressWarnings("unchecked")
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
	*/

	

	
	
	// where is this used, and is need to use
	private String getModifiers(Object context)
	{
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try
			{
				Object modifiers = this.contextClass.getMethod("getModifiers", new Class[0]).invoke(context, new Object[0]);
				return modifiers.toString();
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return "";
	}
	
	// [work]
	private void bodyDeclaration(Object declaration, Tree tree, ArrayList<Tree> children)
	{
		//element 0 is modifiers, element 1 is declarator
		Object[] modifierAndDeclarator = getContexts(declaration);
		Object declarator = modifierAndDeclarator[1];
		
		if( declarator.getClass().getSimpleName().equals("FieldDeclaration"))
		{
			// may need to get 
			Object[] variables = getContexts(declarator);
			for( int i = 1; i < variables.length; ++i )
				children.add(buildTree(variables[i], tree));							
		}
		else if( declarator.getClass().getSimpleName().equals("MethodDeclaration")  )							
			children.add(buildTree(getContexts(declarator)[0], tree));
		else
			children.add(buildTree(declarator, tree));
	}
	
	// [work]
	private String processType( Object type )
	{		
		Object[] contexts;
		StringBuilder sb;
		boolean first = true;
		
		Class<? extends Object> typeClass = type.getClass();
		//System.out.println("Simple name" + typeClass.getSimpleName()); // for testing
		switch( typeClass.getSimpleName() ) // need to learn what getSimpleName() does
		{
		case "Type": return processType( getContexts(type)[0] );			
		case "PrimitiveType": return type.toString() ; // Look into getImage
		case "FunctionType":
			contexts = getContexts(type);
			return processType(contexts[0]) + " => " + processType(contexts[1]);			
		case "ReferenceType": return  type.toString();//processType( getContexts(type)[0] ) //+ getArrayDimensions( type ); // getArrayDimensions is not longer going to be used, mightjust have to use a toString method.
		case "ClassOrInterfaceType":
			sb = new StringBuilder();
			for( Object context : getContexts(type) )
				if( context.getClass().getSimpleName().equals("ClassOrInterfaceTypeSuffix"))
					if( first )
					{
						sb.append(processType(context));
						first = false;
					}	
					else
						sb.append(":").append(processType(context));
						
			return sb.toString();
		case "ClassOrInterfaceTypeSuffix":
			contexts = getContextChildren(type); //getContexts(type);
			if( contexts.length > 1 )
				return contexts[0].toString() + processType( contexts[1] );
			else
				return contexts[0].toString();
		case "TypeArguments":
		case "TypeParameters":
			sb = new StringBuilder("<");
			for( Object context : getContexts(type) )
				if( first )
				{
					sb.append(processType(context));
					first = false;
				}
				else
					sb.append(",").append(processType(context));
			sb.append(">");
			return sb.toString();
		case "TypeParameter":
			contexts = getContextChildren(type);
			if( contexts.length > 1 )
				return contexts[0].toString() + " " + processType(contexts[1]);
			else
				return contexts[0].toString();
		case "IsList":
			sb = new StringBuilder("is ");
			for( Object context : getContexts(type) )
				if( first )
				{
					sb.append(processType(context));
					first = false;
				}
				else
					sb.append(" and ").append(processType(context));	
			return sb.toString();
		case "ResultTypes":
		case "FormalParameters":
			sb = new StringBuilder("(");
			for( Object context : getContexts(type) )
				if( first )
				{
					sb.append(processType(context));
					first = false;
				}
				else
					sb.append(",").append(processType(context));
			sb.append(")");
			return sb.toString();
		case "ResultType":
		case "FormalParameter":
			contexts = getContexts(type);
			return contexts[0].toString() + processType(contexts[1]);
		}
		
		return "";
	}

	// [work]
	private Tree buildTree(Object element, Tree parent)
	{
		// Change the names that are typed 'node(s)' to 'Context(s)'
		// The parent is null when passed in.
		Tree tree = null; 
		Class<? extends Object> elementClass = element.getClass(); 
		ArrayList<Tree> children = new ArrayList<Tree>();
		Object[] contexts = getContexts(element);	
		
		String modifiers;
		String kind;
		String type;

		String temp = elementClass.getSimpleName();
		
		switch( elementClass.getSimpleName() )
		{
		case "CompilationUnitContext": 
			tree = new Tree(element, parent, ShadowLabel.COMPILATION_UNIT);
			for( Object context : contexts )
			{
				String name = context.getClass().getSimpleName();
				if( name.equals("ClassOrInterfaceDeclaration") || name.equals("EnumDeclaration") )
				{	 
					for( Object detail :  getContexts(context) )
					{
						if( detail.getClass().getSimpleName().equals("UnqualifiedName") )
						{
							children.add(new Tree(detail, tree, ShadowLabel.PACKAGE));
							break;
						}						
					}					
					
					children.add(buildTree(context, tree));
				}
			}
			break;		 
		case "ClassOrInterfaceDeclarationContext": // Professor Wittman said that antler removed the need for this.
			
			String typeParameters = "";
			for( Object node : contexts )
				if( node.getClass().getSimpleName().equals("TypeParametersContext") )
					typeParameters = processType(node);
			
			kind = getContextChildren(element)[0].toString();
			if( kind.contains("singleton"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.SINGLETON);
			else if( kind.contains("exception"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.EXCEPTION);
			else if( kind.contains("interface"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.INTERFACE);
			else
				tree = new Tree(element, parent, typeParameters, ShadowLabel.CLASS);
			
			for( Object node : contexts )
			{
				String name = node.getClass().getSimpleName();
				if( name.equals("ClassOrInterfaceBodyContext") )
				{
					Object[] declarations = getContexts(node);
					for( Object declaration : declarations )
						bodyDeclaration(declaration, tree, children);		
				}
			}
			break;
		case "EnumDeclarationContext": // check what the actual names are
			tree = new Tree(element, parent, ShadowLabel.ENUM);
			for( Object context : contexts )
			{
				String name = context.getClass().getSimpleName();
				if( name.equals("EnumBodyContext") )
				{
					Object[] declarations = getContexts(context);
					for( Object declaration : declarations )
					{
						if( declaration.getClass().getSimpleName().equals("EnumConstantContext"))
							children.add(buildTree(declaration, tree));
						else //body declaration
							bodyDeclaration(declaration, tree, children);
					}					
				}
			}			
			break;
		case "EnumConstantContext":
			tree = new Tree(element, parent, ShadowLabel.CONSTANT);
			break;
		case "VariableDeclaratorContext":
			modifiers = getModifiers(getParent(element));
			type = getType(getParent(element));
			if( modifiers.contains("constant") )
				tree = new Tree(element, parent, type, ShadowLabel.CONSTANT);
			else
				tree = new Tree(element, parent, type, ShadowLabel.FIELD);
			break;
		case "CreateDeclarationContext":
		case "DestroyDeclarationContext":
		case "MethodDeclaratorContext":
			type = "";
			
			if(elementClass.getSimpleName().equals("MethodDeclaratorContext"))
			{
				type = processType(contexts[0]) + " => " + processType(contexts[1]); 
				modifiers = getModifiers(getParent(element));
			}
			else if( elementClass.getSimpleName().equals("CreateDeclarationContext") )
			{
				type = processType(getContexts(contexts[0])[0]);
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

	// [work] [ continued from here
	public Tree compile(Path input)
	{
		this.errorLine = (this.errorColumn = 0);
		this.message = null;
		if (this.shadowParserClass != null) {
			try
			{
				
			   /* Object loggers = this.loggersClass.getConstructor(new Class[0])
			    		.newInstance(new Object[0]);*/						
				
			    
			    java.lang.reflect.Field logger = loggersClass.getField("PARSER");			    			    
			    
			    Object loggerValue = logger.get(null);
			    
			    Class<?> loggerClassType = logger.getType(); 			    
			    
			    Object errorReporter = this.errorReporterClass.getConstructor(new Class[] { loggerClassType })
			    		.newInstance(new Object[] { loggerValue }); 
			    
			    Object parseChecker = this.parseCheckerClass.getConstructor(new Class[] { errorReporter.getClass() })
			    		.newInstance(new Object[] { errorReporter });
				
				
/*				Object parser = this.parserClass.getConstructor(new Class[] { InputStream.class })
						.newInstance(new shadowParserClassObject[] {input });*/
				/*Object parser = this.shadowParserClass.getConstructor(new Class[] { InputStream.class })
						.newInstance(new Object[] { input }); // Need to get the parse checker
*/				
				/*return buildTree(this.parserClass.getMethod("CompilationUnit", new Class[0])
						.invoke(parser, new Object[0]), null);*/
				/*return buildTree(this.shadowParserClass.getMethod("getCompilationUnit", new Class[] { Path.class }) // Get things from parse check and pass in a path.
						.invoke(parser, new Object[] { input }), null);*/
				
				return buildTree(this.parseCheckerClass.getMethod("getCompilationUnit", new Class[] { Path.class })
						.invoke(parseChecker, new Object[] { input }), null);
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

	// [This method seems like it should be deprecated]
	public Object compile(InputStream input, String encoding)
	{
		this.errorLine = (this.errorColumn = 0);
		this.message = null;
		if (this.shadowParserClass != null) {
			try
			{
				/*Object parser = this.parserClass
						.getConstructor(new Class[] {InputStream.class, String.class })
						.newInstance(new Object[] {input, encoding });*/
				
				
				Object parser = this.shadowParserClass
						.getConstructor(new Class[] {InputStream.class, String.class })
						.newInstance(new Object[] {input, encoding });
				
				/*return this.parserClass.getMethod("CompilationUnit", new Class[0])
						.invoke(parser, new Object[0]);*/
				return this.shadowParserClass.getMethod("compilationUnit", new Class[0])
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

	// Is not currently used [checked 1.0]
	/*public boolean hasChildren(Object element)
	{
		return getChildren(element).length != 0;
	}*/

	// Is used multiple times [checked 1.0]
	private Object[] getContexts(Object context)
	{
		// this.contextClass()
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try
			{
				// Need to change the node stuff.
				int numChildren = ((Integer)this.contextClass.getMethod("getChildCount", new Class[0]).invoke(context, new Object[0])).intValue();				 				
				Object[] children = new Object[numChildren];
				
				
				for (int i = 0; i < numChildren; i++) {
					children[i] = this.contextClass.getMethod("getChild", new Class[] { Integer.TYPE }).invoke(context, new Object[] { Integer.valueOf(i) });
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
		
	// Is used multiple times [checked 1.0]
	private Object[] getContextChildren(Object context)
	{
		// this.contextClass()
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try
			{
				// Need to change the node stuff.
				int numChildren = ((Integer)this.contextClass.getMethod("getChildCount", new Class[0]).invoke(context, new Object[0])).intValue();
				ArrayList<Object> contexts = new ArrayList<Object>(); 
				
				//Object[] contexts = new Object[numChildren];
				
				
				for (int i = 0; i < numChildren; i++) {
					Object child = this.contextClass.getMethod("getChild", new Class[] { Integer.TYPE }).invoke(context, new Object[] { Integer.valueOf(i) });
					
					if(this.contextClass.isInstance(context))
					{
						contexts.add(child);
					}
				}

				return contexts.toArray();
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return new Object[0];
	}
	
	// Used in a method that is not used
	/*public Object[] getChildren(Object element)
	{	
		if ((this.contextClass != null) && (this.contextClass.isInstance(element)))
		{
			try
			{   
				int numChildren = ((Integer)this.contextClass.getMethod("getChildCount", new Class[0]).invoke(element, new Object[0])).intValue();
				Object[] children = new Object[numChildren];
				for (int i = 0; i < numChildren; i++) {
					children[i] = this.contextClass.getMethod("getChild", new Class[] { Integer.TYPE }).invoke(element, new Object[] { Integer.valueOf(i) });
				}

				return children;
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return new Object[0];
	}*/
	
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
		
		if ((this.contextClass != null) && (this.contextClass.isInstance(element))) {
			try
			{
				return ((Integer)this.contextClass.getMethod("lineStart", new Class[0]).invoke(element, new Object[0])).intValue();
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
		
		if ((this.contextClass != null) && (this.contextClass.isInstance(element))) {
			try
			{
				return ((Integer)this.contextClass.getMethod("columnStart", new Class[0]).invoke(element, new Object[0])).intValue();
			}
			catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return 0;
	}
}
