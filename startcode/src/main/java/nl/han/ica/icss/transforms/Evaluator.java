package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HashmapTableLinked;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.ArrayList;
import java.util.List;

public class Evaluator implements Transform {

    private final HashmapTableLinked<String, Literal> variableValues;

    public Evaluator() {
        variableValues = new HashmapTableLinked<>();
    }

    @Override
    public void apply(AST ast) {
        Stylesheet stylesheet = ast.root;
        this.transformStylesheet(stylesheet, ast);
    }

    private void transformStylesheet(ASTNode astNode, AST ast) {
        List<ASTNode> toRemove = new ArrayList<>();
        this.variableValues.pushScope(); // Add scope voor stylesheet

        for (ASTNode child : astNode.getChildren()) {
            if (child instanceof VariableAssignment) {
                this.transformVariableAssignment((VariableAssignment) child);
                toRemove.add(child);
                continue;
            }

            if (child instanceof Stylerule) {
                this.transformStylerule((Stylerule) child, ast);
            }
        }

        this.variableValues.popScope(); // Remove scope voor  stylesheet

        for (ASTNode rem : toRemove) {
            astNode.removeChild(rem);
        }
    }

    private void transformStylerule(Stylerule stylerule, AST ast) {
        ArrayList<ASTNode> toAdd = new ArrayList<>();
        this.variableValues.pushScope(); // Add scope for stylerule

        for (ASTNode child : stylerule.body) {
            this.transformRuleBody(child, toAdd, ast, stylerule.selectors);
        }

        this.variableValues.popScope(); // Remove scope for stylerule
        stylerule.body = toAdd;
    }

    private void transformRuleBody(ASTNode astNode, ArrayList<ASTNode> parentBody, AST ast, ArrayList<Selector> parentSelectors) {
        if (astNode instanceof VariableAssignment) {
            this.transformVariableAssignment((VariableAssignment) astNode);
            return;
        }

        if (astNode instanceof Declaration) {
            this.transformDeclaration((Declaration) astNode);
            Declaration newDeclaration = (Declaration) astNode;
            boolean isDeclarationUpdated = false;

            for (int i = 0; i < parentBody.size(); i++) {
                ASTNode node = parentBody.get(i);
                if (node instanceof Declaration) {
                    Declaration existingDeclaration = (Declaration) node;
                    if (existingDeclaration.property.name.equals(newDeclaration.property.name)) {
                        parentBody.set(i, newDeclaration);
                        isDeclarationUpdated = true;
                        break;
                    }
                }
            }

            if (!isDeclarationUpdated) {
                parentBody.add(newDeclaration);
            }
            return;
        }

        if (astNode instanceof IfClause) {
            IfClause ifClause = (IfClause) astNode;
            ifClause.conditionalExpression = this.transformExpression(ifClause.conditionalExpression);

            if (((BoolLiteral) ifClause.conditionalExpression).value) {
                this.transformIfClause(ifClause, parentBody, ast, parentSelectors);
            } else {
                if (ifClause.elseClause == null) {
                    ifClause.body.clear();
                    return;
                } else {
                    ifClause.body = ifClause.elseClause.body;
                }
            }

            this.transformIfClause((IfClause) astNode, parentBody, ast, parentSelectors);
        }
        if (astNode instanceof Stylerule) {
            Stylerule nestedStylerule = (Stylerule) astNode;
            ArrayList<Selector> newSelectors = new ArrayList<>(parentSelectors);
            newSelectors.addAll(nestedStylerule.selectors);
            nestedStylerule.selectors = newSelectors;
            this.transformStylerule(nestedStylerule, ast);
            ast.root.addChild(nestedStylerule);
        }
    }

    private void transformIfClause(IfClause ifClause, ArrayList<ASTNode> parentBody, AST ast, ArrayList<Selector> parentSelectors) {
        for (ASTNode child : ifClause.getChildren()) {
            this.transformRuleBody(child, parentBody, ast, parentSelectors);
        }
    }

    private void transformDeclaration(Declaration declaration) {
        declaration.expression = this.transformExpression(declaration.expression);
    }

    private void transformVariableAssignment(VariableAssignment variableAssignment) {
        Expression expression = variableAssignment.expression;
        variableAssignment.expression = this.transformExpression(expression);
        this.variableValues.putVariable(variableAssignment.name.name, (Literal) variableAssignment.expression);
    }

    private Literal transformExpression(Expression expression) {
        if (expression instanceof Operation) {
            return this.transformOperation((Operation) expression);
        }

        if (expression instanceof VariableReference) {
            return this.variableValues.getVariable(((VariableReference) expression).name);
        }

        return (Literal) expression;
    }

    private Literal transformOperation(Operation operation) {
        Literal left;
        Literal right;

        int leftValue;
        int rightValue;

        if (operation.lhs instanceof VariableReference) {
            left = this.variableValues.getVariable(((VariableReference) operation.lhs).name);
        } else {
            left = (Literal) operation.lhs;
        }

        if (operation.rhs instanceof VariableReference) {
            right = this.variableValues.getVariable(((VariableReference) operation.rhs).name);
        } else {
            right = (Literal) operation.rhs;
        }

        leftValue = this.getLiteralValue(left);
        rightValue = this.getLiteralValue(right);

        if (operation instanceof AddOperation) {
            return this.literalFromOperation(left, leftValue + rightValue);
        } else if (operation instanceof SubtractOperation) {
            return this.literalFromOperation(left, leftValue - rightValue);
        } else {
            if (right instanceof ScalarLiteral) {
                return this.literalFromOperation(left, leftValue * rightValue);
            } else {
                return this.literalFromOperation(right, leftValue * rightValue);
            }
        }
    }

    private int getLiteralValue(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        } else if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        } else {
            return 0;
        }
    }

    private Literal literalFromOperation(Literal literal, int value) {
        if (literal instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (literal instanceof ScalarLiteral) {
            return new ScalarLiteral(value);
        } else {
            return new PercentageLiteral(value);
        }
    }
}