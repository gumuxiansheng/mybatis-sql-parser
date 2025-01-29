package cn.mikezhu.mysqlparser.utils;

import org.apache.ibatis.ognl.ASTChain;
import org.apache.ibatis.ognl.ASTConst;
import org.apache.ibatis.ognl.SimpleNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AstVisitor {

    public List<ParamConstraint> visit(SimpleNode node) {
        List<ParamConstraint> constraints = new ArrayList<>();
        processNode(node, constraints);
        return constraints;
    }

    private void processNode(SimpleNode node, List<ParamConstraint> constraints) {
        switch (node.getClass().getSimpleName()) {
            case "ASTEq":
                handleComparison(node, "==", constraints);
                break;
            case "ASTGreater":
                handleComparison(node, ">", constraints);
                break;
            case "ASTGreaterEq":
                handleComparison(node, ">=", constraints);
                break;
            case "ASTLess":
                handleComparison(node, "<", constraints);
                break;
            case "ASTLessEq":
                handleComparison(node, "<=", constraints);
                break;
            case "ASTNotEq":
                handleComparison(node, "!=", constraints);
                break;
            case "ASTAnd":
                handleLogical(node, "&&", constraints);
                break;
            case "ASTOr":
                handleLogical(node, "||", constraints);
                break;
            case "ASTNot":
                handleNot(node, constraints);
                // 其他节点类型处理...
        }
    }

    /**
     * 处理逻辑运算符（AND/OR）的递归方法
     *
     * @param node     当前AST节点
     * @param operator 逻辑运算符类型（"&&" 或 "||"）
     * @param context  参数约束收集上下文
     */
    public void handleLogical(SimpleNode node, String operator,
                              List<ParamConstraint> context) {
        int childrenSize = node.jjtGetNumChildren();

        List<List<ParamConstraint>> constraintGroups = new ArrayList<>();
        for (int i = 0; i < childrenSize; i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            List<ParamConstraint> childConstraints = new AstVisitor().visit(child);
            constraintGroups.add(childConstraints);
            new AstVisitor().processNode(child, childConstraints);
        }

        // 合并逻辑关系
        combineConstraints(operator, constraintGroups, context);
    }

    private void handleNot(SimpleNode node, List<ParamConstraint> context) {
        SimpleNode child = (SimpleNode) node.jjtGetChild(0);
        new AstVisitor().processNode(child, context);
    }

    /**
     * 合并子条件的参数约束
     */
    private void combineConstraints(String operator,
                                    List<List<ParamConstraint>> constraints,
                                    List<ParamConstraint> result) {
        switch (operator) {
            case "&&":
                handleAnd(constraints, result);
                break;
            case "||":
                handleOr(constraints, result);
                break;
        }
    }

    private void handleAnd(List<List<ParamConstraint>> constraints,
                           List<ParamConstraint> result) {
        // 生成满足所有条件的组合
        List<ParamConstraint> mustMeet = new ArrayList<>();
        constraints.forEach(mustMeet::addAll);
        result.addAll(mustMeet);

        // 生成至少一个条件失败的组合
        generateFailureCases(constraints, result);
    }

    private void generateFailureCases(List<List<ParamConstraint>> constraints,
                                      List<ParamConstraint> result) {
        // 左条件失败，右条件成功
        constraints.forEach(constraint -> {
            List<ParamConstraint> onlyFail = createNegativeConstraints(constraint);
            onlyFail.addAll(constraints.stream().filter(c -> c != constraint).flatMap(Collection::stream).collect(Collectors.toList()));
            result.addAll(onlyFail);
        });
    }

    /**
     * 创建反向约束（将条件取反）
     */
    private List<ParamConstraint> createNegativeConstraints(List<ParamConstraint> constraints) {
        return constraints.stream()
                .map(c -> new ParamConstraint(
                        c.getParamPath(),
                        reverseOperator(c.getOperator()),
                        c.getComparedValue()))
                .collect(Collectors.toList());
    }

    private String reverseOperator(String op) {
        switch (op) {
            case ">":
                return "<=";
            case "==":
                return "!=";
            case "<":
                return ">=";
            case ">=":
                return "<";
            case "<=":
                return ">";
            case "!=":
                return "==";
            default:
                throw new IllegalArgumentException("不支持的运算符：" + op);
        }
    }

    private void handleOr(List<List<ParamConstraint>> constraints,
                          List<ParamConstraint> result) {
        // 生成满足任意条件的组合
        constraints.forEach(result::addAll);

        // 生成所有条件均失败的组合
        List<ParamConstraint> allFail = new ArrayList<>();
        constraints.forEach(c -> allFail.addAll(createNegativeConstraints(c)));
        result.addAll(allFail);
    }

    private void handleComparison(SimpleNode node, String operator,
                                  List<ParamConstraint> constraints) {
        // 获取左右子树
        SimpleNode left = (SimpleNode) node.jjtGetChild(0);
        SimpleNode right = (SimpleNode) node.jjtGetChild(1);

        // 提取参数路径和比较值
        String paramPath = extractPropertyPath(left);
        Object value = extractConstantValue(right);

        constraints.add(new ParamConstraint(paramPath, operator, value));
    }

    private String extractPropertyPath(SimpleNode node) {
        // 处理链式调用，如 "a.b.c"
        StringBuilder path = new StringBuilder();
        if (node instanceof ASTChain) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode child = (SimpleNode) node.jjtGetChild(i);
                path.append(child).append(i < node.jjtGetNumChildren() - 1 ? "." : "");
            }
        } else {
            path.append(node.toString());
        }
        return path.toString();
    }

    private Object extractConstantValue(SimpleNode node) {
        if (node instanceof ASTConst) {
            return ((ASTConst) node).getValue();
        } else if (node instanceof ASTChain) {
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode child = (SimpleNode) node.jjtGetChild(i);
                if (child instanceof ASTConst) {
                    path.append(child);
                }
            }
            return path.toString();
        }

        return "";
    }

}
