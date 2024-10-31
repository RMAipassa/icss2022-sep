package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
public class Generator {

	public String generate(AST ast) {
		return generateStylesheet(ast.root, 0);
	}

	private String generateStylesheet(Stylesheet root, int indentLevel) {
		StringBuilder css = new StringBuilder();
		for (ASTNode child : root.getChildren()) {
			if (child instanceof Stylerule) {
				css.append(generateStylerule((Stylerule) child, indentLevel)).append("\n");
			}
		}
		return css.toString();
	}

	private String generateStylerule(Stylerule stylerule, int indentLevel) {
		StringBuilder css = new StringBuilder();

		css.append(indent(indentLevel));
		for (Selector selector : stylerule.selectors) {
			css.append(selector).append(" ");
		}
		css.append("{\n");

		for (ASTNode child : stylerule.body) {
			if (child instanceof Declaration) {
				css.append(generateDeclaration(child, indentLevel + 1)).append("\n");
			}
		}

		css.append(indent(indentLevel)).append("}");
		return css.toString();
	}

	private String generateDeclaration(ASTNode astNode, int indentLevel) {
		if (!(astNode instanceof Declaration)) {
			return "";
		}

		Declaration declaration = (Declaration) astNode;
		String propertyName = declaration.property.name;
		String value = generateLiteral(declaration.expression);

		return indent(indentLevel) + propertyName + ": " + value + ";";
	}

	private String generateLiteral(ASTNode expression) {
		if (expression instanceof Literal) {
			return expression.toString();
		}
		return "";
	}

	private String indent(int level) {
		return "  ".repeat(level);
	}
}