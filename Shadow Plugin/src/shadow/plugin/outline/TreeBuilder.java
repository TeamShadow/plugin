package shadow.plugin.outline;

import shadow.parse.Context;
import shadow.parse.ShadowBaseVisitor;
import shadow.parse.ShadowParser;
import shadow.parse.ShadowParser.ResultTypesContext;
import shadow.parse.ShadowParser.VariableDeclaratorContext;

public class TreeBuilder extends ShadowBaseVisitor<Void> {	
	
	private Tree currentTree;
	private ShadowParser.ModifiersContext currentModifiers;
	
	public Tree makeTree(Context ctx) {
		visit( ctx );
		return currentTree;
	}	

	@Override public Void visitCompilationUnit(ShadowParser.CompilationUnitContext ctx)  { 
		currentModifiers = ctx.modifiers();
		
		currentTree = new Tree(ctx, null, null, ShadowLabel.COMPILATION_UNIT);
		return visitChildren(ctx);
	}	
	
	@Override public Void visitClassOrInterfaceDeclaration(ShadowParser.ClassOrInterfaceDeclarationContext ctx) {	
		if( ctx.unqualifiedName() != null )
			new Tree(ctx.unqualifiedName(), currentTree, ctx.unqualifiedName().getText(), ShadowLabel.PACKAGE);
		
		ShadowLabel label;
		
		switch( ctx.children.get(0).getText() ) {		
		default:
		case "class":
			String modifiers = currentModifiers.getText();
			if( modifiers.contains("protected") )
				label = ShadowLabel.PROTECTED_CLASS;
			else if( modifiers.contains("private") )
				label = ShadowLabel.PRIVATE_CLASS;
			else			
				label = ShadowLabel.PUBLIC_CLASS;
			break;
		case "singleton":
			label = ShadowLabel.SINGLETON;
			break;
		case "exception":
			label = ShadowLabel.EXCEPTION;
			break;
		case "interface":
			label = ShadowLabel.INTERFACE;
			break;
		}
		
		Tree oldTree = currentTree;		
		currentTree = new Tree(ctx.Identifier(), currentTree, ctx.Identifier().getText(), label);
		
		visitChildren(ctx);
		
		currentTree = oldTree;
		return null;		
	}
	
	@Override public Void visitEnumDeclaration(ShadowParser.EnumDeclarationContext ctx) { 
		if( ctx.unqualifiedName() != null )
			new Tree(ctx.unqualifiedName(), currentTree, ctx.unqualifiedName().getText(), ShadowLabel.PACKAGE);
		
		Tree oldTree = currentTree;		
		currentTree = new Tree(ctx, currentTree, ctx.Identifier().getText(), ShadowLabel.ENUM);		
	
		visitChildren(ctx);
		
		currentTree = oldTree;
		return null;
	}
	
	@Override public Void visitEnumConstant(ShadowParser.EnumConstantContext ctx) {				
		new Tree(ctx, currentTree, ctx.Identifier().getText(), ShadowLabel.PUBLIC_CONSTANT );
				
		return null;
	}
	
	@Override public Void visitFieldDeclaration(ShadowParser.FieldDeclarationContext ctx) { 
				
		String type = ctx.type().getText();
		ShadowLabel label = null;
		String modifiers = currentModifiers.getText();
		
		if( modifiers.contains("constant") ) {
			if( modifiers.contains("public") )
				label = ShadowLabel.PUBLIC_CONSTANT;
			else if( modifiers.contains("protected") )
				label = ShadowLabel.PROTECTED_CONSTANT;
			else
				label = ShadowLabel.PRIVATE_CONSTANT;
		}
		else
			label = ShadowLabel.FIELD;
		
		
		for( VariableDeclaratorContext variable : ctx.variableDeclarator() )
			new Tree(variable, currentTree, variable.generalIdentifier().getText(), type, label );
				
		return null;
	}
	
	
	@Override public Void visitCreateDeclaration(ShadowParser.CreateDeclarationContext ctx) {		
		new Tree(ctx.createDeclarator(), currentTree, "create", processParameters(ctx.createDeclarator().formalParameters()), getMethodLabel());
		return null;
	}
	
	@Override public Void visitDestroyDeclaration(ShadowParser.DestroyDeclarationContext ctx) {		
		new Tree(ctx, currentTree, "destroy", getMethodLabel());
		return null;
	}
	
	@Override public Void visitMethodDeclarator(ShadowParser.MethodDeclaratorContext ctx) {		
		new Tree(ctx.generalIdentifier(), currentTree, ctx.generalIdentifier().getText(), processParameters(ctx.formalParameters()) + " => " + processResults(ctx.resultTypes()), getMethodLabel());
		return null;
	}	
	
	private ShadowLabel getMethodLabel() {
		String modifiers = currentModifiers.getText();
		
		if( modifiers.contains("public") )
			return ShadowLabel.PUBLIC_METHOD;
		else if( modifiers.contains("protected") )
			return ShadowLabel.PROTECTED_METHOD;
		else
			return ShadowLabel.PRIVATE_METHOD;		
	}
	
	private String processParameters(ShadowParser.FormalParametersContext ctx) {
		StringBuilder builder = new StringBuilder("(");
		boolean first = true;		
		
		for(ShadowParser.FormalParameterContext parameter : ctx.formalParameter()) {
			if( first )
				first = false;
			else
				builder.append(", ");
			
			builder.append(processModifiers(parameter.modifiers()));
			builder.append(parameter.type().getText()).append(" ");
			builder.append(parameter.Identifier().getText());
		}
		
		builder.append(")");
		
		return builder.toString();
	}
	
	private String processResults(ResultTypesContext ctx) {
		StringBuilder builder = new StringBuilder("(");
		boolean first = true;		
		
		for(ShadowParser.ResultTypeContext result : ctx.resultType()) {
			if( first )
				first = false;
			else
				builder.append(", ");			
			
			builder.append(processModifiers(result.modifiers()));
			builder.append(result.type().getText());
			if( result.Identifier() != null )			
				builder.append(" ").append(result.Identifier().getText());
		}
		
		builder.append(")");
		
		return builder.toString();
	}
	
	private String processModifiers(ShadowParser.ModifiersContext ctx) {
		
		if( ctx.modifier() == null )
			return "";
		
	
		StringBuilder builder = new StringBuilder();
		
		for(ShadowParser.ModifierContext modifier : ctx.modifier())
			builder.append(modifier.getText()).append(" ");
		
		
		return builder.toString();
		
	}
	
	
	@Override public Void visitClassOrInterfaceBodyDeclaration(ShadowParser.ClassOrInterfaceBodyDeclarationContext ctx) {
		
		currentModifiers = ctx.modifiers();
		
		visitChildren(ctx);
		
		currentModifiers = null;
		
		return null;
	}	
	
}
