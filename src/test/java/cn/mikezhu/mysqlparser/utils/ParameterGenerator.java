package cn.mikezhu.mysqlparser.utils;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParameterGenerator {
    public static List<Map<String, Object>> generate(List<ParamConstraint> constraints) {
        Map<String, List<ParamValue>> valueGroups = new HashMap<>();

        for (ParamConstraint constraint : constraints) {
            List<ParamValue> values = constraint.generateTestValues().stream().distinct()
                    .map(v -> new ParamValue(
                            ParamConstraint.ConstraintType.COLLECTION.equals(constraint.getConstraintType()) ? constraint.getParamPath().replace(".size()", "").replace(".length", "") : constraint.getParamPath()
                            , v))
                    .collect(Collectors.toList());
            if (valueGroups.containsKey(constraint.getParamPath())) {
                valueGroups.get(constraint.getParamPath()).addAll(values);
            } else {
                valueGroups.put(constraint.getParamPath(), values);
            }
        }

        return generateCombinations(valueGroups);
    }

    private static List<Map<String, Object>> generateCombinations(Map<String, List<ParamValue>> valueGroups) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<List<ParamValue>> cartesianProduct = Lists.cartesianProduct(new ArrayList<>(valueGroups.values()));
        cartesianProduct.forEach(cp -> {
            Map<String, Object> param = new HashMap<>();
            cp.forEach(pv -> {
                Map<String, Object> nestedParam = NestedParamBuilder.buildNestedParam(pv.name, pv.value);
                mergeMaps(param, nestedParam);
            });
            result.add(param);
        });
        return result.stream().distinct().collect(Collectors.toList());
    }

    public static void mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
        for (Map.Entry<String, Object> entry : map2.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (map1.containsKey(key)) {
                Object existingValue = map1.get(key);
                if (existingValue instanceof Map && value instanceof Map) {
                    mergeMaps((Map<String, Object>) existingValue, (Map<String, Object>) value);
                } else {
                    map1.put(key, value);
                }
            } else {
                map1.put(key, value);
            }
        }
    }

    private static class ParamValue {
        String name;
        Object value;

        public ParamValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class NestedParamBuilder {
        public static Map<String, Object> buildNestedParam(String path, Object value) {
            Map<String, Object> param = new HashMap<>();
            String[] keys = path.split("\\.");

            Map<String, Object> current = param;
            for (int i = 0; i < keys.length - 1; i++) {
                current.put(keys[i], new HashMap<>());
                current = (Map<String, Object>) current.get(keys[i]);
            }
            current.put(keys[keys.length - 1], value);

            return param;
        }
    }
}