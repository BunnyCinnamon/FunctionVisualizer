package cinnamon;

import com.expression.parser.Parser;
import com.expression.parser.util.ParserResult;
import com.expression.parser.util.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionHelper {

    public static final Map<String, FunctionInfo> EXPRESSION_FUNCTION_CACHE = new HashMap<>();
    public static final Map<String, Map<Integer, Double>> EXPRESSION_CACHE = new HashMap<>();
    public static final Function<String, Map<Integer, Double>> EXPRESSION_CACHE_SUPPLIER = (s) -> new HashMap<>();

    public static final Function<String, FunctionInfo> EXPRESSION_PARSER_SUPPLIER = ExpressionHelper::parse;
    public static final String EXPRESSION_REGEX = "^\\(([\\+\\-\\d]+)\\)\\{(.+)\\}$";

    public static double getExpression(String function, int min, int max, int level) {
        Map<Integer, Double> map = ExpressionHelper.EXPRESSION_CACHE.computeIfAbsent(function, ExpressionHelper.EXPRESSION_CACHE_SUPPLIER);
        if (min > max) min = max;
        if (!map.containsKey(level)) {
            List<Point> points = new ArrayList<>();
            if (function.contains("x"))
                points.add(new Point("x", String.valueOf(min)));
            if (function.contains("y"))
                points.add(new Point("y", String.valueOf(max)));
            if (function.contains("l"))
                points.add(new Point("l", String.valueOf(level)));
            ParserResult result = Parser.eval(function, points.toArray(new Point[]{}));
            map.put(level, result.getValue().doubleValue());
        }
        return map.get(level);
    }

    public static double getExpression(String function, int min, int max) {
        Map<Integer, Double> map = ExpressionHelper.EXPRESSION_CACHE.computeIfAbsent(function, ExpressionHelper.EXPRESSION_CACHE_SUPPLIER);
        if (!map.containsKey(min)) {
            final Point x = new Point("x", String.valueOf(min));
            final Point y = new Point("y", String.valueOf(max));
            ParserResult result = Parser.eval(function, x, y);
            map.put(min, result.getValue());
        }
        return map.get(min);
    }

    public static double getExpression(String[] functionArray, int min, int max) {
        FunctionInfo match = null;
        FunctionInfo temp = null;
        for (String s : functionArray) {
            FunctionInfo info = ExpressionHelper.EXPRESSION_FUNCTION_CACHE.computeIfAbsent(s, ExpressionHelper.EXPRESSION_PARSER_SUPPLIER);
            if (info.matches(min) && isNotOverride(temp, info, min)) {
                match = info;
            }
            temp = info;
        }
        return match != null ? ExpressionHelper.getExpression(match.function, min, max) : 0;
    }

    public static boolean isNotOverride(FunctionInfo prev, FunctionInfo next, int min) {
        return prev == null || next.condition != FunctionInfo.Condition.MinusInfinite
                || (prev.condition == FunctionInfo.Condition.PlusInfinite && min > prev.min)
                || (prev.condition == FunctionInfo.Condition.Between && min > prev.max)
                || (prev.condition == FunctionInfo.Condition.MinusInfinite && min > prev.min);
    }

    public static FunctionInfo parse(String string) {
        Pattern pattern = Pattern.compile(ExpressionHelper.EXPRESSION_REGEX);
        Matcher matcher = pattern.matcher(string.trim());
        if (matcher.matches()) {
            String condition = matcher.group(1).trim().replace(" ", "");
            String function = matcher.group(2).trim();
            if (condition.endsWith("+")) {
                int min = Integer.parseInt(condition.substring(0, condition.length() - 1));
                return new FunctionInfo(FunctionInfo.Condition.PlusInfinite, function, min);
            } else if (condition.startsWith("-")) {
                int min = Integer.parseInt(condition.substring(1));
                return new FunctionInfo(FunctionInfo.Condition.MinusInfinite, function, min);
            } else if (condition.contains("-")){
                int index = condition.indexOf('-');
                int min = Integer.parseInt(condition.substring(0, index));
                int max = Integer.parseInt(condition.substring(index + 1));
                return new FunctionInfo(FunctionInfo.Condition.Between, function, min, max);
            } else {
                int min = Integer.parseInt(condition);
                return new FunctionInfo(FunctionInfo.Condition.Equal, function, min);
            }
        } else {
            throw new IllegalStateException("[ExpressionHelper] - Expression " + string + " is not valid, might be missing a { or }");
        }
    }

    public static class FunctionInfo {

        public Condition condition;
        public String function;
        public int min;
        public int max;

        public FunctionInfo(Condition condition, String function, int min, int max) {
            this.condition = condition;
            this.function = function;
            this.min = min;
            this.max = max;
        }

        public FunctionInfo(Condition condition, String function, int min) {
            this.condition = condition;
            this.function = function;
            this.min = min;
            this.max = -1;
        }

        public boolean matches(int level) {
            return condition.test(level, this.min, this.max);
        }

        public enum Condition {
            PlusInfinite {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level >= levelMin;
                }
            },
            MinusInfinite {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level <= levelMin;
                }
            },
            Between {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level >= levelMin && level <= levelMax;
                }
            },
            Equal {
                @Override
                boolean test(int level, int levelMin, int levelMax) {
                    return level == levelMin;
                }
            };

            abstract boolean test(int level, int levelMin, int levelMax);
        }
    }
}
