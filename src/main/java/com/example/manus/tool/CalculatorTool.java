package com.example.manus.tool;


import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 一个计算器工具，为Agent提供进行数学计算的能力。
 * 使用 @Component 注解，使其成为一个由Spring管理的Bean。
 */
@Component
public class CalculatorTool {

    private static final ExpressionConfiguration CONFIG = ExpressionConfiguration.defaultConfiguration();

    @Tool("当需要进行数学四则运算或更复杂的数学计算时使用此工具。输入应该是一个标准的数学表达式字符串，例如 '25 * 4' 或 '(18 + 56) / 2'。")
    public BigDecimal calculate(@P("要计算的数学表达式，例如 '12.5 * (3 + 4)'") String expression) {

        System.out.println("🧮 CalculatorTool is called with expression: " + expression);

        try {
            Expression expr = new Expression(expression, CONFIG);
            return expr.evaluate().getNumberValue();
        } catch (Exception e) {
            System.err.println("Error evaluating expression: " + e.getMessage());
            throw new RuntimeException("无效的数学表达式: " + expression, e);
        }
    }
}
