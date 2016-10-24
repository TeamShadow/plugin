package shadow.plugin.compiler;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StyledString;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.outline.ShadowLabel;

public class ShadowCompilerInterface
{	
	private Class<?> contextClass; 
	private Class<?> shadowParserClass;
	private Class<?> parseCheckerClass;	
	private Class<?> loggersClass;  
	private Class<?> errorReporterClass; 
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
			
			this.shadowParserClass = loader.loadClass("shadow.parse.ShadowParser");  
			this.contextClass = loader.loadClass("shadow.parse.Context"); 			
			this.loggersClass = loader.loadClass("shadow.Loggers");
			this.errorReporterClass = loader.loadClass("shadow.typecheck.ErrorReporter");
			this.parseCheckerClass = loader.loadClass("shadow.parse.ParseChecker");
						
		}
		catch (ClassNotFoundException | MalformedURLException e)
		{			
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
				
			Object[] tokens = getTokens(node);
			
			if(label == ShadowLabel.CLASS || label == ShadowLabel.SINGLETON
					|| label == ShadowLabel.EXCEPTION || label == ShadowLabel.INTERFACE)
			{				
				if(checkForPackage(tokens))
					this.name = tokens[3].toString();
				else
					this.name = tokens[1].toString();
			}
			else if(label == ShadowLabel.PACKAGE)
			{
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < tokens.length; i++)
				{
					sb.append(tokens[i].toString());
				}
				this.name = sb.toString();
			}
			else
				this.name = tokens[0].toString();
		}
		
		private boolean checkForPackage(Object[] tokens)
		{
			if(tokens.length >= 4 && tokens[2].toString().equals("@"))
				return true;
			
			return false;
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
		for(Object tokens : getTokens(element) ) 
			if( tokens.getClass().getSimpleName().equals("TypeContext"))
				return processType(tokens);
				
		return "";
	}
	
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
	
	private void bodyDeclaration(Object declaration, Tree tree, ArrayList<Tree> children)
	{
		//element 0 is modifiers, element 1 is declarator
		Object[] modifierAndDeclarator = getTokens(declaration);
		Object declarator = modifierAndDeclarator[1];		
		
		if( declarator.getClass().getSimpleName().equals("FieldDeclarationContext"))
		{			
			Object[] variables = getTokens(declarator); 
			for( int i = 1; i < variables.length; ++i ) // variables[0] should be the data type
				if(this.contextClass.isInstance(variables[i])) 
					children.add(buildTree(variables[i], tree));							
		}
		else if( declarator.getClass().getSimpleName().equals("MethodDeclarationContext")  )							
			children.add(buildTree(getTokens(declarator)[0], tree)); 
		else
			children.add(buildTree(declarator, tree));
	}
		
	private String processType( Object type )
	{		
		Object[] tokens;
		StringBuilder sb;
		boolean first = true;
		
		Class<? extends Object> typeClass = type.getClass();
		
		switch( typeClass.getSimpleName() ) 
		{
		case "TypeContext":
			return processType( getTokens(type)[0] );			
		case "PrimitiveTypeContext": 
			return getTokens(type)[0].toString();
		case "FunctionTypeContext":
			tokens = getTokens(type);
			return processType(tokens[1]) + " => " + processType(tokens[3]);			
		case "ReferenceTypeContext": 
			return  processType(getTokens(type)[0]);
		case "ClassOrInterfaceTypeContext":
			sb = new StringBuilder();
			for( Object token : getTokens(type) )
				if( token.getClass().getSimpleName().equals("ClassOrInterfaceTypeSuffixContext"))
					if( first )
					{
						sb.append(processType(token));
						first = false;
					}	
					else
						sb.append(":").append(processType(token));
						
			return sb.toString();
		case "ClassOrInterfaceTypeSuffixContext":
			tokens = getTokens(type); 
			if( tokens.length > 1 )
				return tokens[0].toString() + processType( tokens[1] );
			else
				return tokens[0].toString();
		case "TypeArgumentsContext":
		case "TypeParametersContext":
			sb = new StringBuilder("<");
			for( Object token : getTokens(type) ) 
			{
				if(this.contextClass.isInstance(token))
				{
					if( first )
					{
						sb.append(processType(token));
						first = false;
					}
					else
						sb.append(",").append(processType(token));
				}
			}
			sb.append(">");
			return sb.toString();
		case "TypeParameterContext":
			tokens = getTokens(type); 
			if( tokens.length > 1 )
				return tokens[0].toString() + " " + processType(tokens[1]);
			else
				return tokens[0].toString();
		case "IsListContext":
			sb = new StringBuilder("is ");
			for( Object token : getTokens(type) )
				if(this.contextClass.isInstance(token))
				{
					if( first )
					{
						sb.append(processType(token));
						first = false;
					}
					else
						sb.append(" and ").append(processType(token));
				}
			return sb.toString();
		case "ResultTypesContext":
		case "FormalParametersContext":
			sb = new StringBuilder("(");
			for( Object token : getTokens(type) )
			{								
				if ( contextClass.isInstance(token)){
					if( first )
					{
						sb.append(processType(token));
						first = false;
					}
					else
						sb.append(",").append(processType(token));
				}
			}
			sb.append(")");
			return sb.toString();
		case "ResultTypeContext":
		case "FormalParameterContext":
			tokens = getTokens(type);			
			return getModifiers(tokens[0]) + processType(tokens[1]);
		}
		
		return "";
	}

	private Tree buildTree(Object element, Tree parent)
	{
		Tree tree = null; 
		Class<? extends Object> elementClass = element.getClass(); 
		ArrayList<Tree> children = new ArrayList<Tree>();
		Object[] tokens = getTokens(element);	
		
		String modifiers;
		String kind;
		String type;
				
		switch( elementClass.getSimpleName() )
		{
		case "CompilationUnitContext": 
			tree = new Tree(element, parent, ShadowLabel.COMPILATION_UNIT);
			for( Object token : tokens )
			{
				String name = token.getClass().getSimpleName();
				if( name.equals("ClassOrInterfaceDeclarationContext") || name.equals("EnumDeclarationContext") )
				{
					for( Object detail :  getTokens(token) )
					{
						if( detail.getClass().getSimpleName().equals("UnqualifiedNameContext") )
						{
							children.add(new Tree(detail, tree, ShadowLabel.PACKAGE));
							break;
						}						
					}					
					
					children.add(buildTree(token, tree));
				}
			}
			break;		 
		case "ClassOrInterfaceDeclarationContext": 
			
			String typeParameters = "";
			for( Object token : tokens )
			{
				if( token.getClass().getSimpleName().equals("TypeParametersContext") )
					typeParameters = processType(token);
			}
						
			
			kind = getTokens(element)[0].toString();
			if( kind.contains("singleton"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.SINGLETON);
			else if( kind.contains("exception"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.EXCEPTION);
			else if( kind.contains("interface"))
				tree = new Tree(element, parent, typeParameters, ShadowLabel.INTERFACE);
			else // [Working]
				tree = new Tree(element, parent, typeParameters, ShadowLabel.CLASS);
			
			for( Object token : tokens )
			{
				String name = token.getClass().getSimpleName();
				if( name.equals("ClassOrInterfaceBodyContext") )
				{					
					for( Object declaration : getTokens(token) )
						if(this.contextClass.isInstance(declaration))
							bodyDeclaration(declaration, tree, children);		
				}
			}
			break;
		case "EnumDeclarationContext": 
			tree = new Tree(element, parent, ShadowLabel.ENUM);
			for( Object token : tokens )
			{
				String name = token.getClass().getSimpleName();
				if( name.equals("EnumBodyContext") )
				{
					for( Object declaration : getTokens(token) )
					{
						if( declaration.getClass().getSimpleName().equals("EnumConstantContext"))
							children.add(buildTree(declaration, tree));
						else 
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
				type = processType(tokens[1]) + " => " + processType(tokens[3]); 
				modifiers = getModifiers(getParent(element));
			}
			else if( elementClass.getSimpleName().equals("CreateDeclarationContext") )
			{
				type = processType(getTokens(tokens[0])[1]); 
				modifiers = getModifiers(element);
				element = tokens[0];
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

	public Tree compile(Path input)
	{
		this.errorLine = (this.errorColumn = 0);
		this.message = null;
		if (this.shadowParserClass != null) {
			try
			{
			    
			    java.lang.reflect.Field logger = loggersClass.getField("PARSER");			    			    
			    
			    Object loggerValue = logger.get(null);
			    
			    Class<?> loggerClassType = logger.getType(); 			    
			    
			    Object errorReporter = this.errorReporterClass.getConstructor(new Class[] { loggerClassType })
			    		.newInstance(new Object[] { loggerValue }); 
			    
			    Object parseChecker = this.parseCheckerClass.getConstructor(new Class[] { errorReporter.getClass() })
			    		.newInstance(new Object[] { errorReporter });							  
				
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
	// Further testing needs to be done. 
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

	private Object[] getTokens(Object context)
	{
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try
			{
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
