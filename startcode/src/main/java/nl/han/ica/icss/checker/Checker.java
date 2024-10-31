package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
         variableTypes = new HANLinkedList<>();
         checkstylesheet(ast.root);
    }

    private void checkstylesheet(ASTNode node) {
        Stylesheet stylesheet = (Stylesheet) node;
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            }
        }
    }

    private void checkVariableAssignment(ASTNode node) {
        VariableAssignment variableAssignment = (VariableAssignment) node;
        VariableReference variableReference = variableAssignment.name;
        ExpressionType expressionType = checkExpression(variableAssignment.expression);

        if(expressionType == null || expressionType == ExpressionType.UNDEFINED){
            node.setError("Variable assignment is invalid because of faulty expression.");
        }
        HashMap<String, ExpressionType> currentType = new HashMap<>();
        currentType.put(variableReference.name, expressionType);
        variableTypes.addFirst(currentType);
    }

    private void checkStylerule(ASTNode node) {

        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                checkIfClause(child);
            }
        }
    }

    private void checkDeclaration(ASTNode node) {
        Declaration declaration = (Declaration) node;
        switch (declaration.property.name) {
            case "width":
                if (declaration.expression instanceof VariableReference) {
                    checkVariableReference(node, (VariableReference) declaration.expression, ExpressionType.PIXEL, "width");
                } else if (declaration.expression instanceof Operation) {
                    if(checkExpression(declaration.expression) != ExpressionType.PIXEL){
                        node.setError("Property: \"width\" has been assigned an invalid type");
                    }
                } else if (!(declaration.expression instanceof PixelLiteral)) {
                    node.setError("Property: \"width\" has been assigned an invalid type");
                }
                break;
            case "height":
                if (declaration.expression instanceof VariableReference) {
                    checkVariableReference(node, (VariableReference) declaration.expression, ExpressionType.PIXEL, "height");
                } else if (declaration.expression instanceof Operation) {
                    if(checkExpression(declaration.expression) != ExpressionType.PIXEL){
                        node.setError("Property: \"height\" has been assigned an invalid type");
                    }
                } else if (!(declaration.expression instanceof PixelLiteral)) {
                    node.setError("Property: \"height\" has been assigned an invalid type");
                }
                break;
            case "color":
                if (declaration.expression instanceof VariableReference) {
                    checkVariableReference(node, (VariableReference) declaration.expression, ExpressionType.COLOR, "color");
                } else if (!(declaration.expression instanceof ColorLiteral)) {
                    node.setError("Property: \"color\" has been assigned an invalid type");
                }
                break;
            case "background-color":
                if (declaration.expression instanceof VariableReference) {
                    checkVariableReference(node, (VariableReference) declaration.expression, ExpressionType.COLOR, "background-color");
                } else if (!(declaration.expression instanceof ColorLiteral)) {
                    node.setError("Property: \"background-color\" has been assigned an invalid type");
                }
                break;
            default:
                node.setError("Property: \"" + declaration.property.name + "\" is not a valid property. It has to fall under: width, height, color, background-color");
        }
    }

    private void checkVariableReference(ASTNode node, VariableReference variableRef, ExpressionType expectedType, String propertyName) {
        String variableName = variableRef.name;
        ExpressionType actualType = getType(variableName);

        if (actualType == null) {
            node.setError("Property: \"" + propertyName + "\" has been assigned a variable that has not been declared");
        } else if (actualType != expectedType) {
            node.setError("Property: \"" + propertyName + "\" has been assigned an invalid type");
        }
    }

    private ExpressionType getType(String variableName) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(variableName)) {
                return variableTypes.get(i).get(variableName);
            }
        }
        return null;
    }



    private ExpressionType checkExpression(ASTNode child) {
        Expression expression = (Expression) child;
        if (expression instanceof Operation) {
            return checkOperation((Operation) expression);
        }
        return checkExpressionType(expression);
    }

    private ExpressionType checkExpressionType(Expression expression) {
            if (expression instanceof PercentageLiteral) {
                return ExpressionType.PERCENTAGE;
            } else if (expression instanceof PixelLiteral) {
                return ExpressionType.PIXEL;
            } else if (expression instanceof ColorLiteral) {
                return ExpressionType.COLOR;
            } else if (expression instanceof ScalarLiteral) {
                return ExpressionType.SCALAR;
            } else if (expression instanceof BoolLiteral) {
                return ExpressionType.BOOL;
            }
        return ExpressionType.UNDEFINED;
    }



    private ExpressionType checkOperation(Operation operation) {
        ExpressionType left;
        ExpressionType right;

        if(operation.lhs instanceof Operation){
            left = checkOperation((Operation) operation.lhs);
        } else if(operation.lhs instanceof VariableReference){
            left = getType(((VariableReference) operation.lhs).name);
        } else {
            left = checkExpressionType(operation.lhs);

        }
        if(operation.rhs instanceof Operation){
            right = checkOperation((Operation) operation.rhs);
        }else if (operation.rhs instanceof VariableReference){
            right = getType(((VariableReference) operation.rhs).name);
        }
        else {
            right = checkExpressionType(operation.rhs);
        }
        if(left == ExpressionType.COLOR || right == ExpressionType.COLOR || left == ExpressionType.BOOL || right == ExpressionType.BOOL){
            operation.setError("Neither booleans nor colors are allowed in operations.");
            return ExpressionType.UNDEFINED;
        }

        if(operation instanceof MultiplyOperation){
            if(left != ExpressionType.SCALAR && right != ExpressionType.SCALAR){
                operation.setError("Multiply is only allowed with at least one scalar literal.");
                return ExpressionType.UNDEFINED;
            }
            return right != ExpressionType.SCALAR ? right : left;
        } else if ((operation instanceof SubtractOperation || operation instanceof AddOperation) && left != right){
            operation.setError("You can only do add and subtract operations with the same literal.");
            return ExpressionType.UNDEFINED;
        }

        return left;
    }

    private void checkIfClause(ASTNode node) {
        IfClause ifClause = (IfClause) node;
        if(ifClause.conditionalExpression instanceof VariableReference){
            if(getType(((VariableReference) ifClause.conditionalExpression).name) != ExpressionType.BOOL){
                ifClause.setError("ConditionalExpression should be a boolean literal.");
            }
        }
        else {
            ExpressionType expressionType = checkExpression(ifClause.conditionalExpression);
            if (expressionType != ExpressionType.BOOL) {
                ifClause.setError("ConditionalExpression should be a boolean literal.");
            }
        }
        for (ASTNode child : ifClause.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration(child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment(child);
            } else if (child instanceof IfClause) {
                checkIfClause(child);
            }
        }
        if(ifClause.elseClause != null){
            checkElseClause(ifClause.elseClause);
        }

    }

    private void checkElseClause(ASTNode node) {
        ElseClause elseClause = (ElseClause) node;
        for (ASTNode child : elseClause.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration(child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment(child);
            } else if (child instanceof IfClause) {
                checkIfClause(child);
            }
        }
    }
    
}
