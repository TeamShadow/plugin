package shadow.plugin.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.MarkerUtilities;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.outline.ShadowLabel;


public class ShadowCompilerInterface {
	
	private Class<?> contextClass;
	private Class<?> tokenInterface;
	private Class<?> terminalInterface;
	private Class<?> shadowParserClass;
	private Class<?> parseCheckerClass;	
	private Class<?> loggersClass;  
	private Class<?> errorReporterClass;
	private Class<?> shadowExceptionClass;	
	private Class<?> typeCheckerClass;
	private Class<?> configurationClass;
	private int errorLine;
	private int errorColumn;
	private String message = null;

	private Method lineStartMethod;
	private Method lineEndMethod;
	private Method columnStartMethod;
	private Method columnEndMethod;
	private Method startCharacterMethod;
	private Method stopCharacterMethod;

	public ShadowCompilerInterface() {		
		try {						
			IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
					.getPreferenceStore();
			String pathToJar = preferenceStore.getString("PATH");			

			URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };			

			URLClassLoader loader = URLClassLoader.newInstance(urls);

			this.shadowParserClass = loader.loadClass("shadow.parse.ShadowParser");  
			this.contextClass = loader.loadClass("shadow.parse.Context"); 	
			this.terminalInterface = loader.loadClass("org.antlr.v4.runtime.tree.TerminalNode");
			this.tokenInterface = loader.loadClass("org.antlr.v4.runtime.Token");
			this.loggersClass = loader.loadClass("shadow.Loggers");
			this.errorReporterClass = loader.loadClass("shadow.typecheck.ErrorReporter");
			this.parseCheckerClass = loader.loadClass("shadow.parse.ParseChecker");
			this.shadowExceptionClass = loader.loadClass("shadow.ShadowException");
			this.typeCheckerClass = loader.loadClass("shadow.typecheck.TypeChecker");
			this.configurationClass = loader.loadClass("shadow.Configuration");	

			lineStartMethod = this.shadowExceptionClass.getMethod("lineStart");
			lineEndMethod = this.shadowExceptionClass.getMethod("lineEnd");
			columnStartMethod = this.shadowExceptionClass.getMethod("columnStart");
			columnEndMethod = this.shadowExceptionClass.getMethod("columnEnd");
			startCharacterMethod = this.shadowExceptionClass.getMethod("startCharacter");
			stopCharacterMethod = this.shadowExceptionClass.getMethod("stopCharacter");
		}
		catch (ClassNotFoundException | MalformedURLException | NoSuchMethodException | SecurityException e) {			
			this.shadowParserClass = (this.contextClass = null);
		}
	}

	public int getErrorLine() {
		return this.errorLine;
	}

	public int getErrorColumn() {
		return this.errorColumn;
	}

	public String getMessage() {
		return message;
	}	

	public class Tree {  
		private Object node;
		private Tree[] children;
		private Tree parent;
		private ShadowLabel label;
		private String name;
		private String extra;

		public Tree(Object node, Tree parent, ShadowLabel label) {
			this(node, parent, "", label);
		}

		public Tree(Object node, Tree parent, String extra, ShadowLabel label) {
			this.node = node;
			this.parent = parent;
			this.label = label;	
			this.extra = extra;

			Object[] tokens = getTokens(node);

			if(label == ShadowLabel.CLASS || label == ShadowLabel.SINGLETON
					|| label == ShadowLabel.EXCEPTION || label == ShadowLabel.INTERFACE) {				
				if(checkForPackage(tokens))
					this.name = tokens[3].toString();
				else
					this.name = tokens[1].toString();
			}
			else if(label == ShadowLabel.PACKAGE) {
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < tokens.length; i++)				
					sb.append(tokens[i].toString());				
				this.name = sb.toString();
			}
			else
				this.name = tokens[0].toString();
		}

		private boolean checkForPackage(Object[] tokens) {
			if(tokens.length >= 4 && tokens[2].toString().equals("@"))
				return true;

			return false;
		}

		public void setChildren(Tree[] children) {
			this.children = children;		  
		}

		public Tree[] getChildren() {
			return children;
		}

		public boolean hasChildren() {
			return children != null && children.length > 0;			
		}

		public Tree getParent() {
			return parent;
		}

		public String toString() {
			if( extra != null && !extra.isEmpty() ) {
				if( label == ShadowLabel.FIELD || label == ShadowLabel.CONSTANT ) 
					return name + ": " + extra;

				else 
					return name + extra;
			}			

			return name;
		}

		public StyledString toStyledString() {
			if( extra != null && !extra.isEmpty() ) {
				if( label == ShadowLabel.FIELD || label == ShadowLabel.CONSTANT ) 
					return new StyledString(name).append( ": " + extra, StyledString.DECORATIONS_STYLER);

				else 
					return new StyledString(name + extra);
			}			

			return new StyledString(name);
		}

		public Object getNode() {
			return node;
		}

		public ShadowLabel getLabel() {
			return label;			
		}

		public void setLabel(ShadowLabel label) {
			this.label = label;
		}

		public int getLength() {
			return name.length();
		}
	}	

	private Object getParent(Object context) {
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try {
				return this.contextClass.getMethod("getParent", new Class[0]).invoke(context, new Object[0]);
			}
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex) {
				ex.printStackTrace();
			}
		}

		return context;
	}	

	private String getType(Object element) {
		for(Object tokens : getTokens(element) ) 
			if( tokens.getClass().getSimpleName().equals("TypeContext"))
				return processType(tokens);

		return "";
	}

	private String getModifiers(Object context) {
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try {
				Object modifiers = this.contextClass.getMethod("getModifiers", new Class[0]).invoke(context, new Object[0]);
				return modifiers.toString();
			}
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex ) {
				ex.printStackTrace();
			}
		}

		return "";
	}

	private void bodyDeclaration(Object declaration, Tree tree, ArrayList<Tree> children) {
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

	private String processType( Object type ) {		
		Object[] tokens;
		StringBuilder sb;
		boolean first = true;

		Class<? extends Object> typeClass = type.getClass();

		switch( typeClass.getSimpleName() ) {
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
					if( first ) {
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
			for( Object token : getTokens(type) ) {
				if(contextClass.isInstance(token)) {
					if( first ) {
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
				if(this.contextClass.isInstance(token)) {
					if( first ) {
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
			for( Object token : getTokens(type) ) {								
				if ( contextClass.isInstance(token)){
					if( first ) {
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

	private Tree buildTree(Object element, Tree parent) {
		Tree tree = null; 
		Class<? extends Object> elementClass = element.getClass(); 
		ArrayList<Tree> children = new ArrayList<Tree>();
		Object[] tokens = getTokens(element);	

		String modifiers;
		String kind;
		String type;

		switch( elementClass.getSimpleName() ) {
		case "CompilationUnitContext": 
			tree = new Tree(element, parent, ShadowLabel.COMPILATION_UNIT);
			for( Object token : tokens ) {
				String name = token.getClass().getSimpleName();
				if( name.equals("ClassOrInterfaceDeclarationContext") || name.equals("EnumDeclarationContext") ) {
					for( Object detail :  getTokens(token) ) {
						if( detail.getClass().getSimpleName().equals("UnqualifiedNameContext") ) {
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
			for( Object token : tokens ) {
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

			for( Object token : tokens ) {
				String name = token.getClass().getSimpleName();
				if( name.equals("ClassOrInterfaceBodyContext") ) {					
					for( Object declaration : getTokens(token) )
						if(this.contextClass.isInstance(declaration))
							bodyDeclaration(declaration, tree, children);		
				}
			}
			break;
		case "EnumDeclarationContext": 
			tree = new Tree(element, parent, ShadowLabel.ENUM);
			for( Object token : tokens ) {
				String name = token.getClass().getSimpleName();
				if( name.equals("EnumBodyContext") ) {
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
			if(elementClass.getSimpleName().equals("MethodDeclaratorContext")) {
				type = processType(tokens[1]) + " => " + processType(tokens[3]); 
				modifiers = getModifiers(getParent(element));
			}
			else if( elementClass.getSimpleName().equals("CreateDeclarationContext") ) {
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

	@SuppressWarnings("unchecked")
	public Tree compile(FileEditorInput newInput)
	{		
		//IPath path = input.getPath();
		Tree tree = null;
		Path inputPath = newInput.getPath().toFile().toPath(); //path.toFile().toPath();
		IFile inputIFile = newInput.getFile();
		
		// delete all of the existing IMarkers.
		int depth = IResource.DEPTH_INFINITE;


		java.lang.reflect.Field logger;
		Object loggerValue;
		Class<?> loggerClassType;
		Object parseErrorReporter;
		Object typeErrorReporter = null;
		Object parseChecker;		
		Object parseCheckerContext;
		Method typeCheckerContexts;		

		this.errorLine = (this.errorColumn = 0);
		this.message = null;
		try {
			inputIFile.deleteMarkers(null, true, depth);
			if (this.shadowParserClass != null) {
				try {
					logger = loggersClass.getField("PARSER");
					loggerValue = logger.get(null);

					loggerClassType = logger.getType(); 			    

					parseErrorReporter = this.errorReporterClass.getConstructor(new Class[] { loggerClassType })
							.newInstance(new Object[] { loggerValue }); 

					parseChecker = this.parseCheckerClass.getConstructor(new Class[] { parseErrorReporter.getClass() })
							.newInstance(new Object[] { parseErrorReporter });							  

					parseCheckerContext = null;				

					try {
						parseCheckerContext = this.parseCheckerClass.getMethod("getCompilationUnit", new Class[] { Path.class })
								.invoke(parseChecker, new Object[] { inputPath });

						tree = buildTree(parseCheckerContext, null);

						this.configurationClass.getMethod( "buildConfiguration",new Class[] {String.class, String.class, Boolean.TYPE}).invoke(null, new Object[] { inputPath.toString(), null, new Boolean(true)});

						logger = loggersClass.getField("TYPE_CHECKER");
						loggerValue = logger.get(null);
						loggerClassType = logger.getType();

						typeErrorReporter = this.errorReporterClass.getConstructor(new Class[] { loggerClassType })
								.newInstance(new Object[] { loggerValue });

						typeCheckerContexts = this.typeCheckerClass.getMethod("typeCheck", new Class[] { Path.class, Boolean.TYPE, typeErrorReporter.getClass() });

						//stores errors and warnings into typeErrorReporter
						typeCheckerContexts.invoke(null, new Object[] { inputPath, new Boolean(true), typeErrorReporter });
					}
					catch (InvocationTargetException ex) {	
						List<Object> errorList;
						List<Object> warningList;

						//if it is null, type checking was never reached
						if(typeErrorReporter != null) {
							errorList = (List<Object>) this.errorReporterClass.getMethod("getErrorList").invoke(typeErrorReporter, new Object[] {});
							warningList = (List<Object>) this.errorReporterClass.getMethod("getWarningList").invoke(typeErrorReporter, new Object[] {});
						}
						// parse errors
						else {
							errorList = (List<Object>) this.errorReporterClass.getMethod("getErrorList").invoke(parseErrorReporter, new Object[] {});
							warningList = (List<Object>) this.errorReporterClass.getMethod("getWarningList").invoke(parseErrorReporter, new Object[] {});

							String targetException = ex.getTargetException().getMessage();
							if(targetException != null) {
								Scanner scanner = new Scanner(ex.getTargetException().getMessage());
								//Format of error:
								//[15:9] Unexpected uint
								scanner.useDelimiter("(\\[|:|\\])");
								if(errorList.size() > 0)
								{
									this.errorLine = (int) lineStartMethod.invoke(errorList.get(0), new Object[] {}); //scanner.nextInt();
									this.errorColumn = (int) columnStartMethod.invoke(errorList.get(0), new Object[] {}); //scanner.nextInt();
								}
								this.message = scanner.next().trim();
								scanner.close();
							}
						}

						listErrors( errorList, inputIFile, IMarker.SEVERITY_ERROR);
						listErrors( warningList, inputIFile, IMarker.SEVERITY_WARNING);
					}


					return tree;
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} 
		catch(CoreException ex) {}

		return null;
	}

	private void listErrors( List<Object> errors, IFile file, int severity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for(Object error : errors) {
			
			String message = error.toString();
			//first remove file name, in parentheses
			if( message.contains(")") )
				message = message.substring(message.indexOf(')') + 1);
			
			//then remove line and column
			if( message.contains("]") )
				message = message.substring(message.indexOf(']') + 1);
			
			//then remove trailing lines showing the error in context
			if( message.contains("\n") )
				message = message.substring(0, message.indexOf('\n'));
			
			//get \r just in case
			if( message.contains("\r") )
				message = message.substring(0, message.indexOf('\r'));

			
			if( !message.isEmpty() )			
				addMarker(file, IMarker.PROBLEM, message, 
						(int) lineStartMethod.invoke(error, new Object[] {}),
						(int) lineEndMethod.invoke(error, new Object[] {}),
						(int) columnStartMethod.invoke(error, new Object[] {}),
						(int) columnEndMethod.invoke(error, new Object[] {}),
						severity, IMarker.PRIORITY_NORMAL,
						(int) startCharacterMethod.invoke(error, new Object[] {}),
						(int) stopCharacterMethod.invoke(error, new Object[] {}));
		}		
	}

	private void addMarker(IFile file, String markerType, String message, int lineStart, int lineEnd, int columnStart, 
			int columnEnd, int severity, int priority, int startCharacter, int stopCharacter) {
		try{
			Map<String, Object> attrs = new HashMap<String, Object>();			
			MarkerUtilities.setMessage(attrs, message);			

			MarkerUtilities.setCharStart(attrs, startCharacter);			

			if(startCharacter == stopCharacter)
				stopCharacter = stopCharacter + 2;
			else
				stopCharacter++;

			MarkerUtilities.setCharEnd(attrs, stopCharacter);

			attrs.put(IMarker.SEVERITY, severity);
			attrs.put(IMarker.PRIORITY, priority);		


			MarkerUtilities.createMarker(file, attrs, markerType);				

		} catch(CoreException ex)
		{

			System.out.println(ex.toString());
			// Need to handle the case where the marker no longer exists
		}

	}


	/*

	public Object compile(InputStream input, String encoding) {
		this.errorLine = (this.errorColumn = 0);
		this.message = null;
		if( shadowParserClass != null ) {
			try {
				Object parser = this.shadowParserClass
						.getConstructor(new Class[] {InputStream.class, String.class })
						.newInstance(new Object[] {input, encoding });

				return this.shadowParserClass.getMethod("compilationUnit", new Class[0])
						.invoke(parser, new Object[0]);
			}
			catch (InvocationTargetException ex) {	
				Scanner scanner = new Scanner(ex.getTargetException().getMessage());
				//Format of error:
				//[15:9] Unexpected uint
				scanner.useDelimiter("(\\[|:|\\])");
				this.errorLine = scanner.nextInt();
				this.errorColumn = scanner.nextInt();				
				this.message = scanner.next().trim();
				scanner.close();
			} 
			catch (IllegalAccessException |  IllegalArgumentException | NoSuchMethodException | SecurityException | InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return null;
	}
	 */

	private Object[] getTokens(Object context) {
		if ((this.contextClass != null) && (this.contextClass.isInstance(context))) {
			try {
				int numChildren = ((Integer)this.contextClass.getMethod("getChildCount", new Class[0]).invoke(context, new Object[0])).intValue();				 				
				Object[] children = new Object[numChildren];


				for (int i = 0; i < numChildren; i++) {
					children[i] = this.contextClass.getMethod("getChild", new Class[] { Integer.TYPE }).invoke(context, new Object[] { Integer.valueOf(i) });
				}

				return children;
			}
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex) {
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

	//Takes in a context and retrieves its first meaningful token  
	private Object getToken(Object element) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String name = element.getClass().getSimpleName();
		Object[] tokens = getTokens(element);

		//classes, interfaces, exceptions, and enums need special handling because of kind: "class", "interface", etc.
		if( name.equals("ClassOrInterfaceDeclarationContext") || name.equals("EnumDeclarationContext") ) {
			element = tokens[1];

			//also, they can have an optional package in front
			name = element.getClass().getSimpleName();					
			if( name.equals("UnqualifiedNameContext") )
				element = tokens[3]; //skip the package name and '@' sign
		}
		else
			element = tokens[0];

		return terminalInterface.getMethod("getSymbol", new Class[0]).invoke(element, new Object[0]);		
	}

	public int getLine(Object element)
	{
		if( element instanceof Tree ) {
			Tree tree = (Tree) element;
			element = tree.getNode();			
		}

		if ((this.contextClass != null) && (this.contextClass.isInstance(element))) {			
			try {
				Object token = getToken(element);					
				return ((Integer)this.tokenInterface.getMethod("getLine", new Class[0]).invoke(token, new Object[0])).intValue();
			}	
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex) {
				ex.printStackTrace();
			}
		}
		return 0;
	}

	public int getColumn(Object element) {		
		if( element instanceof Tree ) {
			Tree tree = (Tree) element;
			element = tree.getNode();			
		}

		if ((this.contextClass != null) && (this.contextClass.isInstance(element))) {
			try {
				Object token = getToken(element);				
				return ((Integer)this.tokenInterface.getMethod("getCharPositionInLine", new Class[0]).invoke(token, new Object[0])).intValue();
			}			
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex) {
				ex.printStackTrace();
			}
		}
		return 0;
	}
}
