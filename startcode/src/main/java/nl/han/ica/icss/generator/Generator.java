package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

    private final StringBuilder sb;
    private int indentationLevel;

    public Generator() {
        this.sb = new StringBuilder();
        this.indentationLevel = 0;
    }

    public String generate(AST ast) {
        this.generateNode(ast.root);
        return sb.toString();
    }

    private void generateNode(ASTNode astNode) {
        for (ASTNode node : astNode.getChildren()) {
            if (node instanceof Stylerule) {
                this.generateSelector(node);
                this.indentationLevel++;
                this.generateDeclaration(node);
                this.indentationLevel--;
                this.sb.append("}\n\n");
            }
        }
        // Remove one \n character.
        if (this.sb.length() > 1) {
            this.sb.delete(this.sb.length() - 1, this.sb.length());
        }
    }

    private void generateSelector(ASTNode astNode) {
        Stylerule stylerule = (Stylerule) astNode;

        for (var selector : stylerule.selectors) {
            sb.append(selector.toString()).append(" ");
        }

        this.sb.append(" {\n");
    }

    private void generateDeclaration(ASTNode astNode) {
        String indent = "  ".repeat(indentationLevel);
        for (ASTNode node : astNode.getChildren()) {
            if (node instanceof Declaration) {
                Declaration declaration = (Declaration) node;
                this.sb.append(indent).append(declaration.property.name).append(": ").append(this.expressionToString(declaration.expression)).append(";\n");
            }
        }
    }

    private String expressionToString(Expression expression) {
        if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value + "%";
        }
        if (expression instanceof ColorLiteral) {
            return (((ColorLiteral) expression).value); // GEEN #, want die zit er al in.
        }
        if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value + "px";
        }

        return ""; // scalar
    }
}