package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        Stylesheet stylesheet = ast.root;
        applyStylesheet(stylesheet);
    }


    private void applyStylesheet(ASTNode node) {
        List<ASTNode> Remove = new ArrayList<>();
        for (ASTNode child : node.getChildren()) {
            if(child instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) child);
                Remove.add(child);
            }
            if (child instanceof Stylerule) {
                applyStylerule((Stylerule) child);
            }
        }

        Remove.forEach(node::removeChild);
    }



    private void applyStylerule(Stylerule node) {
        ArrayList<ASTNode> addToBody = new ArrayList<>();
        variableValues.addFirst(new HashMap<>());

        for (ASTNode child : node.body) {
            applyBody(child, addToBody);
        }
        variableValues.removeFirst();

        node.body = addToBody;
    }

    private void applyBody(ASTNode child, ArrayList<ASTNode> addtoBody) {
        if(child instanceof VariableAssignment) {
            applyVariableAssignment((VariableAssignment) child);
            return;
        }
        if (child instanceof Declaration) {
            applyDeclaration((Declaration) child);
            addtoBody.add(child);
            return;
        }
        if (child instanceof IfClause) {
            IfClause ifClause = (IfClause) child;
            ifClause.conditionalExpression = transformExpression(ifClause.conditionalExpression);

            if (((BoolLiteral) ifClause.conditionalExpression).value) {
                if(ifClause.elseClause != null) {
                    ifClause.removeChild(ifClause.elseClause);
                }
            } else {
                if (ifClause.elseClause == null) {
                    System.out.println("elsecluase is null");
                    ifClause.body = new ArrayList<>();
                    return;
                } else {
                    System.out.println("elsecluase is not null");
                    ifClause.body = ifClause.elseClause.body;
                    ifClause.removeChild(ifClause.elseClause);
                }
            }
            applyIfClause((IfClause) child, addtoBody);
        }
    }

    private Literal transformExpression(Expression expression) {
        if (expression instanceof Operation) {
            return transformOperation((Operation) expression);
        }

        if (expression instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) expression;
            Literal value = findVariable(variableReference.name);
            if (value == null) {
                throw new RuntimeException("Variable not found: " + variableReference.name);
            }
            return value;
        }

        return (Literal) expression;
    }

    private Literal transformOperation(Operation operation) {
        Literal left;
        Literal right;

        if (operation.lhs instanceof Operation) {
            left = transformOperation((Operation) operation.lhs);
        } else if (operation.lhs instanceof VariableReference) {
            left = findVariable(((VariableReference) operation.lhs).name);
        } else {
            left = (Literal) operation.lhs;
        }

        if (operation.rhs instanceof Operation) {
            right = transformOperation((Operation) operation.rhs);
        } else if (operation.rhs instanceof VariableReference) {
            right = findVariable(((VariableReference) operation.rhs).name);
        } else {
            right = (Literal) operation.rhs;
        }

        int leftValue = getLitValue(left);
        int rightValue = getLitValue(right);

        if (operation instanceof AddOperation) {
            return applyLiteralOperation(leftValue + rightValue, left);
        } else if (operation instanceof SubtractOperation) {
            return applyLiteralOperation(leftValue - rightValue, left);
        } else if (operation instanceof MultiplyOperation) {
            if (right instanceof ScalarLiteral) {
                return applyLiteralOperation(leftValue * rightValue, left);
            } else {
                return applyLiteralOperation(leftValue * rightValue, right);
            }
        } else {
            return applyLiteralOperation(leftValue / rightValue, left);
        }
    }


    private Literal applyLiteralOperation(int value, Literal literal) {
        if (literal instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (literal instanceof ScalarLiteral) {
            return new ScalarLiteral(value);
        } else {
            return new PercentageLiteral(value);
        }
    }

    private int getLitValue(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        } else {
            return ((PercentageLiteral) literal).value;
        }
    }

    private void applyVariableAssignment(VariableAssignment node) {
        Expression expression = node.expression;
        node.expression = transformExpression(expression);
        HashMap<String, Literal> variableAssignment = new HashMap<>();
        variableAssignment.put(node.name.name, (Literal) node.expression);
        variableValues.addFirst(variableAssignment);
    }

    private void applyDeclaration(Declaration node) {
        Literal evaluatedValue =  transformExpression(node.expression);
        node.expression = evaluatedValue;
    }

    private void applyIfClause (IfClause node, ArrayList<ASTNode> addToBody) {
        for (ASTNode child : node.body) {
            applyBody(child, addToBody);
        }
    }

    private Literal findVariable(String name) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            HashMap<String, Literal> scope = variableValues.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }


}



