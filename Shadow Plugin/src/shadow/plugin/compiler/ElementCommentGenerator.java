package shadow.plugin.compiler;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import shadow.parse.Context;
import shadow.parse.ShadowBaseVisitor;
import shadow.parse.ShadowParser;
import shadow.parse.ShadowParser.FormalParameterContext;
import shadow.parse.ShadowParser.ResultTypeContext;

/**
 * @author Barry Wittman
 *
 */
public class ElementCommentGenerator extends ShadowBaseVisitor<Void> {
		
	private int charOffset;	
	private IDocument document;
	private final static String NEWLINE = System.lineSeparator();
	private ShadowParser.ModifiersContext modifiers = null;
	private boolean stop = false;

	public void generateComment(int charOffset, Context ctx, IDocument document) {
		this.charOffset = charOffset;		
		this.document = document;
		stop = false;
		visit( ctx );		
	}
	
	private String getIndentation(int start) {		
		try {
			int line = document.getLineOfOffset(start);
			int offset = document.getLineOffset(line);
			StringBuffer buffer = new StringBuffer();
			char c;
			while( Character.isWhitespace(c = document.getChar(offset)) ) {
				buffer.append(c);
				offset++;
			}
			
			return buffer.toString().replaceAll("    ", "\t");
		}
		catch (BadLocationException e) {
			return "";
		}
	}
	
	@Override public Void visitModifiers(ShadowParser.ModifiersContext ctx)  {
		if( !ctx.modifier().isEmpty() )
			modifiers = ctx;
		else
			modifiers = null;
		return null;
	}
	

	@Override public Void visitCompilationUnit(ShadowParser.CompilationUnitContext ctx)  {		
		return visitChildren(ctx);
	}
	
	private void setComment(int offset, String comment) {
		try {
			document.replace(offset, 0, comment);
		} catch (BadLocationException e) {}	
		stop = true;
	}
	
	@Override public Void visitClassOrInterfaceDeclaration(ShadowParser.ClassOrInterfaceDeclarationContext ctx) {	
		if( stop )
			return null;		
		else if( charOffset < ctx.classOrInterfaceBody().start.getStartIndex() ) {
			int beginning = ctx.start.getStartIndex();
			if( modifiers != null )
				beginning = modifiers.getStart().getStartIndex();
			
			String kind = ctx.getChild(0).getText();
			kind = Character.toUpperCase(kind.charAt(0)) + kind.substring(1);
			String indentation = getIndentation(beginning);
			String comment;
			if( modifiers != null && modifiers.getText().contains("private"))			
				comment = String.format("/*%s%s * %s %s%s%s */%s%s", NEWLINE, indentation, kind, ctx.Identifier().getText(), NEWLINE, indentation, NEWLINE, indentation);
			else
				comment = String.format("/**%s%s * %s {@code %s}%s%s */%s%s", NEWLINE, indentation, kind, ctx.Identifier().getText(), NEWLINE, indentation, NEWLINE, indentation);
			setComment(beginning, comment);
		}
		else
			visitChildren(ctx);
		
		return null;		
	}
	
	@Override public Void visitEnumDeclaration(ShadowParser.EnumDeclarationContext ctx) { 
		if( stop )
			return null;
		else if( charOffset < ctx.enumBody().start.getStartIndex() ) {
			int beginning = ctx.start.getStartIndex();
			if( modifiers != null )
				beginning = modifiers.getStart().getStartIndex();
			
			String indentation = getIndentation(beginning);
			String comment = String.format("/**%s%s * Enum {@code %s}%s%s */%s%s", NEWLINE, indentation, ctx.Identifier().getText(), NEWLINE, indentation, NEWLINE, indentation);
			setComment(beginning, comment);		
		}
		else
			visitChildren(ctx);
		
		return null;
	}
	
	@Override public Void visitEnumConstant(ShadowParser.EnumConstantContext ctx) {	
		if( stop )
			return null;		
		else if( charOffset <= ctx.Identifier().getSymbol().getStopIndex() ) {
			int beginning = ctx.start.getStartIndex();			
			String indentation = getIndentation(beginning);
			String comment = String.format("/// Constant {@code %s}%s%s", ctx.Identifier().getText(), NEWLINE, indentation);
			setComment(beginning, comment);
		}		
				
		return null;
	}
	
	@Override public Void visitVariableDeclarator(ShadowParser.VariableDeclaratorContext ctx) { 
		if( stop )
			return null;		
		else if( charOffset <= ctx.stop.getStopIndex() ) {
			int beginning = ctx.getParent().start.getStartIndex();
			if( modifiers != null )
				beginning = modifiers.getStart().getStartIndex();
			String indentation = getIndentation(beginning);
			String comment;
			if( modifiers != null && modifiers.getText().contains("constant") )
				comment = String.format("/// Constant {@code %s}%s%s", ctx.generalIdentifier().getText(), NEWLINE, indentation);
			else
				comment = String.format("// Variable %s%s%s", ctx.generalIdentifier().getText(), NEWLINE, indentation);
			setComment(beginning, comment);	
		}
		
		return null;
	}
	
	
	@Override public Void visitCreateDeclarator(ShadowParser.CreateDeclaratorContext ctx) {		
		if( stop )
			return null;		
		else if( charOffset < ctx.stop.getStopIndex() ) {
			int beginning = ctx.start.getStartIndex();
			if( modifiers != null )
				beginning = modifiers.getStart().getStartIndex();
						
			String indentation = getIndentation(beginning);
			StringBuffer buffer;
			if( modifiers != null && modifiers.getText().contains("private") )				
				buffer = new StringBuffer("/*");
			else 
				buffer = new StringBuffer("/**");			
			buffer.append(String.format("%s%s * Create%s", NEWLINE, indentation, NEWLINE));

			for( FormalParameterContext parameter : ctx.formalParameters().formalParameter())
				buffer.append(String.format("%s * @param %s%s", indentation, parameter.Identifier().getText(), NEWLINE));
			
			buffer.append(String.format("%s */%s%s", indentation, NEWLINE, indentation));			
			setComment(beginning, buffer.toString());		
		}
		else
			visitChildren(ctx);
		
		return null;	
	}
	
	@Override public Void visitDestroyDeclaration(ShadowParser.DestroyDeclarationContext ctx) {		
		if( stop )
			return null;		
		else if( charOffset < ctx.start.getStopIndex() ) {
			int beginning = ctx.start.getStartIndex();
			if( modifiers != null )
				beginning = modifiers.getStart().getStartIndex();
						
			String indentation = getIndentation(beginning);
			String comment = String.format("/**%s%s * Destroy%s%s */%s%s", NEWLINE, indentation, NEWLINE, indentation, NEWLINE, indentation);
			setComment(beginning, comment);		
		}
		else
			visitChildren(ctx);
		
		return null;	
	}
	
	@Override public Void visitMethodDeclarator(ShadowParser.MethodDeclaratorContext ctx) {		
		if( stop )
			return null;		
		else if( charOffset < ctx.stop.getStopIndex() ) {
			int beginning = ctx.start.getStartIndex();
			if( modifiers != null )
				beginning = modifiers.getStart().getStartIndex();
						
			String indentation = getIndentation(beginning);
			StringBuffer buffer;
			if( modifiers != null && modifiers.getText().contains("private") )				
				buffer = new StringBuffer(String.format("/*%s%s * Method %s%s", NEWLINE, indentation, ctx.generalIdentifier().getText(), NEWLINE));
			else 
				buffer = new StringBuffer(String.format("/**%s%s * Method {@code %s}%s", NEWLINE, indentation, ctx.generalIdentifier().getText(), NEWLINE));
			
			for( FormalParameterContext parameter : ctx.formalParameters().formalParameter())
				buffer.append(String.format("%s * @param %s%s", indentation, parameter.Identifier().getText(), NEWLINE));
			
			for( ResultTypeContext result : ctx.resultTypes().resultType()) {
				if( result.Identifier() != null )
					buffer.append(String.format("%s * @return %s%s", indentation, result.Identifier().getText(), NEWLINE));
				else
					buffer.append(String.format("%s * @return%s", indentation, NEWLINE));
			}
			
			buffer.append(String.format("%s */%s%s", indentation, NEWLINE, indentation));			
			setComment(beginning, buffer.toString());		
		}
		else
			visitChildren(ctx);
		
		return null;	
	}
}
