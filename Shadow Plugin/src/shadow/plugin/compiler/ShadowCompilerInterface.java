package shadow.plugin.compiler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.MarkerUtilities;

import shadow.Configuration;
import shadow.ConfigurationException;
import shadow.Loggers;
import shadow.Main;
import shadow.ShadowException;
import shadow.parse.Context;
import shadow.parse.ParseChecker;
import shadow.parse.ShadowParser.CompilationUnitContext;
import shadow.plugin.ShadowPlugin;
import shadow.plugin.outline.TreeBuilder;
import shadow.plugin.preferences.PreferencePage;
import shadow.tac.TACBuilder;
import shadow.typecheck.ErrorReporter;
import shadow.typecheck.TypeChecker;
import shadow.typecheck.type.Type;


public class ShadowCompilerInterface {
	
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
	
	public static Object buildOutline(FileEditorInput newInput, IDocument document) { 
		Path inputPath = newInput.getPath().toFile().toPath();
		ErrorReporter reporter = new ErrorReporter(Loggers.PARSER);
	 	ParseChecker checker = new ParseChecker(reporter);
	 	try{
	 		Context compilationUnit = checker.getCompilationUnit(document.get(), inputPath);
	 		TreeBuilder maker = new TreeBuilder();
	 		return maker.makeTree(compilationUnit);
	 	}
	 	catch (IOException e) {
			return null;
		}		
	}
	
	public static void generateElementComment(FileEditorInput input, IDocument document, int charOffset) { 
		Path inputPath = input.getPath().toFile().toPath();
		ErrorReporter reporter = new ErrorReporter(Loggers.PARSER);
	 	ParseChecker checker = new ParseChecker(reporter);
	 	try{	 		
	 		Context compilationUnit = checker.getCompilationUnit(document.get(), inputPath);
	 		ElementCommentGenerator adder = new ElementCommentGenerator();	 		
	 		adder.generateComment(charOffset, compilationUnit, document);	 		
	 	}
	 	catch(IOException e) {
		}		
	}
	
	public static CompilationUnitContext getCompilationUnit(FileEditorInput input, IDocument document) {
		Path inputPath = input.getPath().toFile().toPath();
		ErrorReporter reporter = new ErrorReporter(Loggers.PARSER);
	 	ParseChecker checker = new ParseChecker(reporter);
	 	try{	 		
	 		return checker.getCompilationUnit(document.get(), inputPath);	 			 		
	 	}
	 	catch(IOException e) {
		}		
	 	
	 	return null;
	}
	
	public static synchronized void reportTypeCheckErrors(FileEditorInput newInput, IDocument document) {
		
		Path inputPath = newInput.getPath().toFile().toPath();
		IFile inputIFile = newInput.getFile();
		ErrorReporter reporter = new ErrorReporter(Loggers.PARSER);
		List<ShadowException> errorList;
		List<ShadowException> warningList;
		
		try {
			
			// delete all of the existing IMarkers.
			try {
				inputIFile.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
			}
			catch (CoreException exception)
			{}			
			
			
		 	ParseChecker checker = new ParseChecker(reporter);		 	
		 	checker.getCompilationUnit(document.get(), inputPath);
		 	errorList = reporter.getErrorList();
			warningList = reporter.getWarningList();		
			listErrors( warningList, inputIFile, IMarker.SEVERITY_WARNING, document);
			listErrors( errorList, inputIFile, IMarker.SEVERITY_ERROR, document );	
		 	
		 	if( errorList.size() == 0 ) {
				IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
						.getPreferenceStore();
				String configurationPath = preferenceStore.getString(PreferencePage.CONFIGURATION_PATH);
				if( configurationPath == null || configurationPath.trim().isEmpty() )
					configurationPath = System.getenv("SHADOW_HOME");
				Configuration.buildConfiguration(inputPath.toString(), configurationPath, true);
				
				reporter = new ErrorReporter(Loggers.TYPE_CHECKER);								
				Context node = TypeChecker.typeCheck(document.get(), inputPath, reporter);
				if( reporter.getErrorList().size() == 0 )
					Main.optimizeTAC(new TACBuilder().build(node), reporter, true);
							
				errorList = reporter.getErrorList();
				warningList = reporter.getWarningList();		
				listErrors( warningList, inputIFile, IMarker.SEVERITY_WARNING, document);
				listErrors( errorList, inputIFile, IMarker.SEVERITY_ERROR, document );
		 	}
		} 
		catch (ShadowException e ) {
			errorList = reporter.getErrorList();
			warningList = reporter.getWarningList();		
			listErrors( warningList, inputIFile, IMarker.SEVERITY_WARNING, document);
			listErrors( errorList, inputIFile, IMarker.SEVERITY_ERROR, document );
		}
		catch( ConfigurationException | IOException e) { 
			
		}		
	}
	
	public static synchronized Type typeCheck(FileEditorInput newInput, IDocument document) {
		
		Path inputPath = newInput.getPath().toFile().toPath();
		
		try {			
			ErrorReporter reporter = new ErrorReporter(Loggers.PARSER);
		 	ParseChecker checker = new ParseChecker(reporter);		 	
		 	checker.getCompilationUnit(document.get(), inputPath);
		 	List<ShadowException> errorList = reporter.getErrorList();
		 	
		 	if( errorList.size() == 0 ) {
				IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
						.getPreferenceStore();
				String configurationPath = preferenceStore.getString(PreferencePage.CONFIGURATION_PATH);
				if( configurationPath == null || configurationPath.trim().isEmpty() )
					configurationPath = System.getenv("SHADOW_HOME");
				Configuration.buildConfiguration(inputPath.toString(), configurationPath, true);
				
				reporter = new ErrorReporter(Loggers.TYPE_CHECKER);								
				Context node = TypeChecker.typeCheck(document.get(), inputPath, reporter);
							
				errorList = reporter.getErrorList();
				if( errorList.size() == 0 )
					return node.getType();
		 	}
		} 
		catch (ShadowException | ConfigurationException | IOException e) {
		}
		
		return null;
	}
		
	private static void listErrors( List<ShadowException> errors, IFile file, int severity, IDocument document )  {
		for( ShadowException error : errors ) {			
			String message = cleanError(error.toString());
			if( !message.isEmpty() ) {
				try {					
					Map<String, Object> attrs = new HashMap<String, Object>();	
					
					int startCharacter = error.startCharacter();
					if( startCharacter == -1 )
						startCharacter = document.getLineOffset(error.lineStart() - 1) + error.columnStart();					
					
					int stopCharacter = error.stopCharacter();
					if( stopCharacter == -1 )
						stopCharacter = document.getLineOffset(error.lineEnd() - 1) + error.columnEnd();
					
					// for an exclusive ending
					stopCharacter++;					
					
					MarkerUtilities.setMessage(attrs, message);
					MarkerUtilities.setCharStart(attrs, startCharacter);
					MarkerUtilities.setCharEnd(attrs, stopCharacter);
	
					attrs.put(IMarker.SEVERITY, severity);
					attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
	
					try {
						MarkerUtilities.createMarker(file, attrs, IMarker.PROBLEM);
					}
					catch (CoreException e) {}
				}
				catch(BadLocationException e) {}
			}
		}		
	}
}
