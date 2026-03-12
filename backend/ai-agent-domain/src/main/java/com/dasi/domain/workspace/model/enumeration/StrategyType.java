package com.dasi.domain.workspace.model.enumeration;

import com.dasi.types.exception.WorkException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.stream.Stream;

import static com.dasi.types.constant.ExceptionMessage.ILLEGAL_DATA;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum StrategyType {

    STEP("分步执行", "step", stepDesc()),
    LOOP("循环推演", "loop", loopDesc()),
    REACT("单步推理", "react", reactDesc()),
    ;

    private String name;

    private String type;

    private String strategyDesc;

    public static StrategyType from(String strategy) {
        if (!StringUtils.hasText(strategy)) {
            throw new WorkException(ILLEGAL_DATA);
        }
        String value = strategy.toLowerCase();
        return Stream.of(values())
                .filter(item -> value.equals(item.getType()))
                .findFirst()
                .orElseThrow(() -> new WorkException(ILLEGAL_DATA));
    }

    private static String stepDesc() {
        return """
                Step 策略是“先规划、再执行、再收束”的线性闭环：
                1) 由 Inspector 先分析任务目标与上下文约束，识别可用工具和关键前置条件；
                2) 由 Planner 基于分析结果列出完整步骤清单，明确每一步目标、输入和预期产出；
                3) 由 Runner 严格按步骤顺序逐步执行，不跳步、不并步，输出真实执行结果；
                4) 由 Replier 汇总全链路结果，生成最终可交付回答。
                链路走向：Inspector -> Planner -> Runner -> Replier（单次顺序执行）。
                """;
    }

    private static String loopDesc() {
        return """
                Loop 策略是“分析-执行-监督”的迭代闭环：
                1) Analyzer 基于当前状态与历史产物分析缺口，给出本轮最短执行策略；
                2) Performer 按本轮策略实施动作并返回真实结果；
                3) Supervisor 对本轮结果做合规与完成度判定，决定继续迭代还是结束。
                当 Supervisor 判断满足完成条件后，再由 Summarizer 对全流程进行最终总结输出。
                链路走向：Analyzer -> Performer -> Supervisor -> (继续循环 或 进入 Summarizer)。
                """;
    }

    private static String reactDesc() {
        return """
                React 策略是“观察-推理-行动”的单步滚动闭环：
                1) Observer 观察当前状态与上一轮结果，判断是否继续推进；
                2) Reasoner 只规划下一小步行动，不做长链规划；
                3) Actor 执行该小步并返回可验证结果。
                若 Observer 判断任务已完成，则停止循环并进入 Evaluator 进行最终收束。
                链路走向：Observer -> Reasoner -> Actor -> Observer ... -> Evaluator。
                """;
    }

}
