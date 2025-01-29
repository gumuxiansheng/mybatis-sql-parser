package cn.mikezhu.mysqlparser.utils;

import java.util.*;
import java.lang.reflect.Field;

public class ParamConstraint {
    private final String paramPath;
    private final String operator;
    private final Object comparedValue;
    private final Class<?> valueType;
    private final ConstraintType constraintType;

    public enum ConstraintType {
        NOT_NULL,       // 空值检查：param != null
        NULL,           // 空值检查：param == null
        COMPARISON,     // 比较运算：>, <, ==, >=, <= 等
        ENUM,           // 枚举匹配：status == 'ACTIVE'
        TYPE,           // 类型匹配：status instanceof Status
        IN,             // 集合包含：status in {'01', '02'}
        COLLECTION,     // 集合操作：list.size() > 0
        BOOLEAN,        // 布尔判断：isActive == true
        UNKNOWN         // 未知类型
    }

    // 构造函数（需自动推断类型和约束类型）
    public ParamConstraint(String paramPath, String operator, Object comparedValue) {
        this.paramPath = paramPath;
        this.operator = operator;
        this.comparedValue = comparedValue;
        this.valueType = inferValueType();
        this.constraintType = determineConstraintType();
    }

    //--------------------- 核心方法 ---------------------
    /**
     * 生成覆盖约束条件的测试值列表
     */
    public List<Object> generateTestValues() {
        switch (constraintType) {
            case NOT_NULL:
            case NULL:
                return Arrays.asList(null, generateNonNullValue());
            case COMPARISON:
                return generateComparisonValues();
            case ENUM:
                return Arrays.asList(comparedValue, generateOtherEnumValue());
            case COLLECTION:
                return Arrays.asList(Collections.emptyList(), generateNonEmptyCollection());
            case BOOLEAN:
                return Arrays.asList(!((Boolean) comparedValue), comparedValue);
            default:
                return Collections.singletonList(comparedValue);
        }
    }

    //--------------------- 类型推断 ---------------------
    /**
     * 类型推断逻辑
     */
    private Class<?> inferValueType() {
        // 1. 根据比较值的类型推断
        if (comparedValue != null) {
            return comparedValue.getClass();
        }

        // 2. 根据参数路径猜测类型
        String[] pathSegments = paramPath.split("\\.");
        String fieldName = pathSegments[pathSegments.length - 1];

        // 简单类型推断（可根据项目需求扩展）
        Map<String, Class<?>> typeMapping = new HashMap<>();

        return typeMapping.getOrDefault(fieldName, Object.class);
    }

    //--------------------- 约束类型判断 ---------------------
    private ConstraintType determineConstraintType() {
        // 空值检查
        if ("!=".equals(operator) && comparedValue == null) {
            return ConstraintType.NOT_NULL;
        }

        if ("==".equals(operator) && comparedValue == null) {
            return ConstraintType.NULL;
        }

        // 枚举类型判断
        if (valueType.isEnum() || (comparedValue != null && comparedValue.getClass().isEnum())) {
            return ConstraintType.ENUM;
        }

        // 集合类型判断
        if (paramPath.endsWith(".size()") || paramPath.endsWith(".length") || Collection.class.isAssignableFrom(valueType)) {
            return ConstraintType.COLLECTION;
        }

        // 布尔类型判断
        if (valueType == Boolean.class || valueType == boolean.class) {
            return ConstraintType.BOOLEAN;
        }

        // 默认视为比较操作
        return ConstraintType.COMPARISON;
    }

    //--------------------- 各类型值生成策略 ---------------------
    /**
     * 生成非空值（根据类型）
     */
    private Object generateNonNullValue() {
        if (valueType == Integer.class) return 0;
        if (valueType == String.class) return "c_test";
        if (valueType == Boolean.class) return true;
        return "c_test_em"; // 默认返回新对象
    }

    /**
     * 生成比较操作的边界值
     */
    private List<Object> generateComparisonValues() {
        List<Object> values = new ArrayList<>();

        if (comparedValue instanceof Number) {
            Number num = (Number) comparedValue;
            switch (operator) {
                case ">":
                case ">=":
                    values.add(num.intValue() - 1); // 不满足
                    values.add(num.intValue() + 1); // 满足
                    break;
                case "<":
                case "<=":
                    values.add(num.intValue() + 1);
                    values.add(num.intValue() - 1);
                    break;
                case "==":
                case "!=":
                    values.add(num);
                    values.add(num.intValue() + 1); // 不等于的值
                    break;
            }
        } else if (comparedValue instanceof String) {
            String cmpVal = "";
            if (((String) comparedValue).contains(".")){
                String[] parts = ((String) comparedValue).split("\\.");
                cmpVal = parts[0];
            }
            values.add(cmpVal);
            values.add(cmpVal + "c_other");
        } else {
            values.add(comparedValue);
            values.add(comparedValue + "c_other");
        }

        return values;
    }

    /**
     * 生成其他枚举值
     */
    private Object generateOtherEnumValue() {
        if (valueType.isEnum()) {
            Object[] enumConstants = valueType.getEnumConstants();
            for (Object constant : enumConstants) {
                if (!constant.equals(comparedValue)) {
                    return constant;
                }
            }
        }
        return null;
    }

    /**
     * 生成非空集合
     */
    private Object generateNonEmptyCollection() {
        if (valueType == List.class) {
            return Arrays.asList("c_item1", "c_item2");
        }
        return Collections.singletonList("c_dummy");
    }

    //--------------------- Getter 方法 ---------------------
    public String getParamPath() { return paramPath; }
    public String getOperator() { return operator; }
    public Object getComparedValue() { return comparedValue; }
    public Class<?> getValueType() { return valueType; }
    public ConstraintType getConstraintType() { return constraintType; }

    //--------------------- 辅助方法 ---------------------
    @Override
    public String toString() {
        return String.format("%s %s %s (%s)",
                paramPath, operator, comparedValue, constraintType);
    }

    /**
     * 生成反向约束（用于逻辑NOT操作）
     */
    public ParamConstraint reverse() {
        String reversedOp = reverseOperator(operator);
        return new ParamConstraint(paramPath, reversedOp, comparedValue);
    }

    private String reverseOperator(String op) {
        switch (op) {
            case ">": return "<=";
            case "<": return ">=";
            case "==": return "!=";
            case "!=": return "==";
            default: return op;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ParamConstraint that = (ParamConstraint) obj;
        return Objects.equals(paramPath, that.paramPath) && Objects.equals(operator, that.operator) && Objects.equals(comparedValue, that.comparedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramPath, operator, comparedValue);
    }
}