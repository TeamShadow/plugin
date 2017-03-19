package shadow.plugin.compiler;

import java.lang.reflect.Field;
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
import shadow.plugin.outline.ShadowOutlineError;
import shadow.plugin.preferences.PreferencePage;


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
			String pathToJar = preferenceStore.getString(PreferencePage.COMPILER_PATH);			

			URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };			

			URLClassLoader loader = URLClassLoader.newInstance(urls);

			shadowParserClass = loader.loadClass("shadow.parse.ShadowParser");  
			contextClass = loader.loadClass("shadow.parse.Context"); 	
			terminalInterface = loader.loadClass("org.antlr.v4.runtime.tree.TerminalNode");
			tokenInterface = loader.loadClass("org.antlr.v4.runtime.Token");
			loggersClass = loader.loadClass("shadow.Loggers");
			errorReporterClass = loader.loadClass("shadow.typecheck.ErrorReporter");
			parseCheckerClass = loader.loadClass("shadow.parse.ParseChecker");
			shadowExceptionClass = loader.loadClass("shadow.ShadowException");
			typeCheckerClass = loader.loadClass("shadow.typecheck.TypeChecker");	
			configurationClass = loader.loadClass("shadow.Configuration");	

			lineStartMethod = shadowExceptionClass.getMethod("lineStart");
			lineEndMethod = shadowExceptionClass.getMethod("lineEnd");
			columnStartMethod = shadowExceptionClass.getMethod("columnStart");
			columnEndMethod = shadowExceptionClass.getMethod("columnEnd");
			startCharacterMethod = shadowExceptionClass.getMethod("startCharacter");
			stopCharacterMethod = shadowExceptionClass.getMethod("stopCharacter");
		}
		catch (ClassNotFoundException | MalformedURLException | NoSuchMethodException | SecurityException e) {			
			shadowParserClass = contextClass = null;
		}
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
	private Object parse(Path inputPath) throws ShadowOutlineError {							
		if (shadowParserClass != null) {			
			Object parseErrorReporter = null;			
			try {			
				Field logger = loggersClass.getField("PARSER");
				Object loggerValue = logger.get(null);
	
				Class<?> loggerClassType = logger.getType(); 			    
	
				parseErrorReporter = errorReporterClass.getConstructor(new Class[] { loggerClassType })
						.newInstance(new Object[] { loggerValue }); 
	
				Object parseChecker = parseCheckerClass.getConstructor(new Class[] { parseErrorReporter.getClass() })
						.newInstance(new Object[] { parseErrorReporter });							  
	
				Object context = parseCheckerClass.getMethod("getCompilationUnit", new Class[] { Path.class })
							.invoke(parseChecker, new Object[] { inputPath });
				return context;
			}		
			catch( InvocationTargetException ex ) {								
				try {
					List<Object> errorList = (List<Object>) errorReporterClass.getMethod("getErrorList").invoke(parseErrorReporter, new Object[] {});
		
					String targetException = ex.getTargetException().getMessage();
					if(targetException != null) {
						int errorLine = 0;
						int errorColumn = 0;
						
						if(errorList.size() > 0) {
							errorLine = (int) lineStartMethod.invoke(errorList.get(0), new Object[] {}); //scanner.nextInt();
							errorColumn = (int) columnStartMethod.invoke(errorList.get(0), new Object[] {}); //scanner.nextInt();
						}						
						
						throw new ShadowOutlineError(errorLine, errorColumn, cleanError(targetException));
					}
				} 
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
				{}
			}
			catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | InstantiationException | NoSuchMethodException e)
			{}				
		}
		return null;
	}
	
	private static String cleanError(String error) {		
		//exception: (file.shadow) [5:10] or
		//exception: [5:10]
		if( error.matches(".*\\(.+\\..+\\)\\s*\\[\\d+:\\d+\\][\\s\\S]*") ||
			error.matches(".*\\[\\d+:\\d+\\][\\s\\S]*"))
			error = error.substring(error.indexOf(']') + 1);
		//exception: (file.shadow)
		else if(error.matches(".*\\(.+\\..+\\)[\\s\\S]*") )
			error = error.substring(error.indexOf(')') + 1);
		
		//then remove trailing lines showing the error in context
		if( error.contains("\n") )
			error = error.substring(0, error.indexOf('\n'));
		
		//get \r just in case
		if( error.contains("\r") )
			error = error.substring(0, error.indexOf('\r'));
		
		return error.trim();		
	}
	
	public Object buildOutline(FileEditorInput newInput) { 
		Path inputPath = newInput.getPath().toFile().toPath();
		
		try {	
			Object parseCheckerContext = parse(inputPath);	
			if( parseCheckerContext == null )
				return null;
			
			return buildTree(parseCheckerContext, null);
		}
		catch( ShadowOutlineError error ) {
			return error;
		}
	}	
	

	@SuppressWarnings("unchecked")
	public void compile(FileEditorInput newInput) {	
		Path inputPath = newInput.getPath().toFile().toPath();
		IFile inputIFile = newInput.getFile();

		// delete all of the existing IMarkers.
		try {
			inputIFile.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
		}
		catch (CoreException e)
		{}		
		
		
		if( configurationClass != null ) {		
			try {
				
				configurationClass.getMethod( "buildConfiguration", new Class[] {String.class, String.class, Boolean.TYPE}).invoke(null, new Object[] { inputPath.toString(), null, new Boolean(true)});
				
				Field logger = loggersClass.getField("TYPE_CHECKER");
				Object loggerValue = logger.get(null);
				Class<?> loggerClassType = logger.getType();			
				
				Object typeErrorReporter = errorReporterClass.getConstructor(new Class[] { loggerClassType })
						.newInstance(new Object[] { loggerValue });			
				Method typeCheckerContexts = typeCheckerClass.getMethod("typeCheck", new Class[] { Path.class, Boolean.TYPE, typeErrorReporter.getClass() });
		
				try{
					//stores errors and warnings into typeErrorReporter
					typeCheckerContexts.invoke(null, new Object[] { inputPath, new Boolean(false), typeErrorReporter });				
				}
				catch( InvocationTargetException e ) {
					//jumps here whenever anything fails				
				}
				
				List<Object> errorList = (List<Object>) this.errorReporterClass.getMethod("getErrorList").invoke(typeErrorReporter, new Object[] {});
				List<Object> warningList = (List<Object>) this.errorReporterClass.getMethod("getWarningList").invoke(typeErrorReporter, new Object[] {});				
				
				listErrors( errorList, inputIFile, IMarker.SEVERITY_ERROR);
				listErrors( warningList, inputIFile, IMarker.SEVERITY_WARNING);			
			}
			catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException error )
			{}
		}
	}
		
	private void listErrors( List<Object> errors, IFile file, int severity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for(Object error : errors) {			
			String message = cleanError(error.toString());
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
		try {
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
		} 
		catch(CoreException ex)
		{}
	}

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
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex) 
			{}
		}

		return new Object[0];
	}	

	public int getLength(Object element) {
		if( element instanceof Tree ) {
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

	public int getLine(Object element) {
		if( element instanceof Tree ) {
			Tree tree = (Tree) element;
			element = tree.getNode();			
		}

		if( contextClass != null && contextClass.isInstance(element) ) {			
			try {
				Object token = getToken(element);					
				return ((Integer)this.tokenInterface.getMethod("getLine", new Class[0]).invoke(token, new Object[0])).intValue();
			}	
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex) 
			{}
		}
		return 0;
	}

	public int getColumn(Object element) {		
		if( element instanceof Tree ) {
			Tree tree = (Tree) element;
			element = tree.getNode();			
		}

		if( contextClass != null && contextClass.isInstance(element) ) {
			try {
				Object token = getToken(element);				
				return ((Integer)this.tokenInterface.getMethod("getCharPositionInLine", new Class[0]).invoke(token, new Object[0])).intValue();
			}			
			catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException ex) 
			{}
		}
		return 0;
	}
}
