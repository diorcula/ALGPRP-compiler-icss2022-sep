// Checker.java
package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HashmapTableLinked;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

public class Checker {
    private HashmapTableLinked<String, ExpressionType> variableTypes;

    public void check(AST ast) {
        variableTypes = new HashmapTableLinked<>();
        addNewScope();
        checkStyleSheet(ast.root);
        removeCurrentScope();
    }

    private void checkStyleSheet(Stylesheet stylesheet) {
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment(child);
            }
            if (child instanceof Stylerule) {
                checkStyleRule(child);
            }
        }
    }

    private void checkStyleRule(ASTNode astNode) {
        Stylerule stylerule = (Stylerule) astNode;
        addNewScope();
        checkRuleBody(stylerule);
        removeCurrentScope();
    }

    private void checkRuleBody(ASTNode astNode) {
        for (ASTNode child : astNode.getChildren()) {
            if (child instanceof Stylerule) {
                checkRuleBody(child);
            }

            if (child instanceof Declaration) {
                checkDeclaration(child);
            }
            if (child instanceof VariableAssignment) {
                checkVariableAssignment(child);
            }
        }
    }

    private void checkDeclaration(ASTNode astNode) {
        Declaration declaration = (Declaration) astNode;
        ExpressionType expressionType = checkExpression(declaration.expression);

        switch (declaration.property.name) {
            case "color":
            case "background-color":
                if (expressionType != ExpressionType.COLOR) {
                    astNode.setError(declaration.property.name + " waarde moet een #HEX kleur zijn.");
                }
                break;
            case "width":
            case "height":
                if (expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
                    astNode.setError(declaration.property.name + " waarde moet een pixel of percentage zijn.");
                }
                break;
        }
    }

    private void checkVariableAssignment(ASTNode astNode) {
        VariableAssignment variableAssignment = (VariableAssignment) astNode;
        VariableReference variableReference = variableAssignment.name;
        ExpressionType expressionType = checkExpression(variableAssignment.expression);

        if (expressionType == null || expressionType == ExpressionType.UNDEFINED) {
            astNode.setError("Variable assignment is undefined/null.");
            return;
        }
        addVariableToCurrentScope(variableReference.name, expressionType);
        System.out.println("Assigned variable " + variableReference.name + " with type " + expressionType);
    }

    private ExpressionType checkVariableReference(VariableReference expression) {
        System.out.println("Checking variable reference -------------- " + expression.name);
        ExpressionType type = variableTypes.getVariable(expression.name);
        if (type == null) {
            expression.setError("Variable " + expression.name + " is out of scope.");
        } else {
            System.out.println("Referenced variable found---------" + expression.name + " with type " + type);
        }
        return type;
    }

    private void addNewScope() {
        variableTypes.pushScope();
    }

    private void removeCurrentScope() {
        variableTypes.popScope();
    }

    private void addVariableToCurrentScope(String variableName, ExpressionType type) {
        variableTypes.putVariable(variableName, type);
    }

    private ExpressionType checkExpression(Expression expression) {
        if (expression instanceof VariableReference) {
            return checkVariableReference((VariableReference) expression);
        } else if (expression instanceof Literal) {
            if (expression instanceof BoolLiteral) {
                return ExpressionType.BOOL;
            } else if (expression instanceof ColorLiteral) {
                return ExpressionType.COLOR;
            } else if (expression instanceof PercentageLiteral) {
                return ExpressionType.PERCENTAGE;
            } else if (expression instanceof PixelLiteral) {
                return ExpressionType.PIXEL;
            } else if (expression instanceof ScalarLiteral) {
                return ExpressionType.SCALAR;
            }
        } else if (expression instanceof Operation) {
            return checkOperation((Operation) expression);
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkOperation(Operation operation) {
        ExpressionType left;
        ExpressionType right;

        left = checkExpression(operation.lhs);
        right = checkExpression(operation.rhs);

        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Colors are not allowed in operations.");
            return ExpressionType.UNDEFINED;
        } else if (operation instanceof MultiplyOperation) {
            if (left != ExpressionType.SCALAR && right != ExpressionType.SCALAR) {
                operation.setError("Multiply is only allowed with at least one scalar literal.");
                return ExpressionType.UNDEFINED;
            }
            return right != ExpressionType.SCALAR ? right : left;
        } else {
            if ((operation instanceof SubtractOperation || operation instanceof AddOperation) && left != right) {
                operation.setError("You can only do add and subtract operations with the same literal.");
                return ExpressionType.UNDEFINED;
            }
            return left;
        }
    }
}