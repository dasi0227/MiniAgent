package com.dasi.domain.workspace.model.enumeration;

import com.dasi.types.exception.MiniAgentException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

import static com.dasi.types.constant.ExceptionMessage.ILLEGAL_DATA;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RoleType {

    // -------------------- step --------------------
    STEP_INSPECTOR(StrategyType.STEP, "inspector", "Inspector", 1, "一名专业的 Inspector/任务分析专家，基于提供的信息，深入分析当前任务需求，识别完成任务所需的 MCP 工具，并给出清晰可执行的工具清单。"),
    STEP_PLANNER(StrategyType.STEP, "planner", "Planner", 2, "一名专业的 Planner/任务规划专家，基于提供的信息，将当前任务拆解为按顺序可执行的步骤列表，供后续逐步执行。"),
    STEP_RUNNER(StrategyType.STEP, "runner", "Runner", 3, "一名专业的 Runner/任务执行专家，基于提供的信息，按步骤实际执行当前任务，并输出执行结果与执行状态。"),
    STEP_REPLIER(StrategyType.STEP, "replier", "Replier", 4, "一名专业的 Replier/任务回复专家，基于提供的信息，对当前 step 工作流进行总结回复，说明已实现内容、是否成功、效果如何，并给出面向用户的最终回复。"),

    // -------------------- loop --------------------
    LOOP_ANALYZER(StrategyType.LOOP, "analyzer", "Analyzer", 1, "一名专业的 Analyzer/任务分析专家，基于提供的信息，深入分析用户任务需求，判断任务当前状态并制定明确完整的执行策略。"),
    LOOP_PERFORMER(StrategyType.LOOP, "performer", "Performer", 2, "一名专业的 Performer/任务执行专家，基于提供的信息，根据用户需求和任务分析专家的输出，实际执行当前轮任务，并产出真实结果。"),
    LOOP_SUPERVISOR(StrategyType.LOOP, "supervisor", "Supervisor", 3, "一名专业的 Supervisor 任务监督专家，基于提供的信息，根据用户需求、任务分析专家和任务执行专家的输出，严格评估本轮执行结果是否真正满足原始需求。"),
    LOOP_SUMMARIZER(StrategyType.LOOP, "summarizer", "Summarizer", 4, "一名专业的 Summarizer/任务总结专家，基于提供的信息，根据用户需求、任务分析专家、任务执行专家、任务监督专家的输出，以及全部历史执行过程，直接给出最终可交付回答。"),

    // -------------------- react --------------------
    REACT_OBSERVER(StrategyType.REACT, "observer", "Observer", 1, "一名专业的 Observer/任务观察专家，基于提供的信息，解析当前任务目标、观察现状、识别刚任务开始或上一轮产出带来的变化，并判断是否还需要继续执行下一小步。"),
    REACT_REASONER(StrategyType.REACT, "reasoner", "Reasoner", 2, "一名专业的 Reasoner/任务推理专家，基于 Observer 的判断，只制定“下一小步”的执行策略，明确本轮应该做什么、如何做、做到什么程度算完成。"),
    REACT_ACTOR(StrategyType.REACT, "actor", "Actor", 3, "一名专业的 Actor/任务行动专家，基于 Observer 与 Reasoner 的输出，实际执行当前这一小步，并给出真实结果。"),
    REACT_EVALUATOR(StrategyType.REACT, "evaluator", "Evaluator", 4, "一名专业的 Evaluator 任务评估专家，在流程结束时，对整个 react 执行过程进行收束，给出最终对用户可见的结果。"),
    ;

    private StrategyType strategyType;

    private String clientRole;

    private String templateName;

    private Integer flowSeq;

    private String roleDesc;

    public static List<RoleType> queryByStrategy(StrategyType strategyType) {
        if (strategyType == null) {
            throw new MiniAgentException(ILLEGAL_DATA);
        }
        List<RoleType> roleTypeList = Stream.of(values())
                .filter(item -> strategyType.equals(item.getStrategyType()))
                .toList();
        if (roleTypeList.isEmpty()) {
            throw new MiniAgentException(ILLEGAL_DATA);
        }
        return roleTypeList;
    }

}
