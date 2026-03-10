package com.dasi.domain.user.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ClientRoleType {

    STEP_INSPECTOR("step", "inspector", "任务分析专家，基于提供的信息，深入分析当前任务需求，识别完成任务所需的 MCP 工具，并给出清晰可执行的工具清单。"),
    STEP_PLANNER("step", "planner", "任务规划专家，基于提供的信息，将当前任务拆解为按顺序可执行的步骤列表，供后续逐步执行。"),
    STEP_RUNNER("step", "runner", "任务执行专家，基于提供的信息，按步骤实际执行当前任务，并输出执行结果与执行状态。"),
    STEP_REPLIER("step", "replier", "任务回复专家，基于提供的信息，对当前 step 工作流进行总结回复，说明已实现内容、是否成功、效果如何，并给出面向用户的最终回复。"),

    LOOP_ANALYZER("loop", "analyzer", "任务分析专家，基于提供的信息，深入分析用户任务需求，判断任务当前状态并制定明确完整的执行策略。"),
    LOOP_PERFORMER("loop", "performer", "任务执行专家，基于提供的信息，根据用户需求和任务分析专家的输出，实际执行当前轮任务，并产出真实结果。"),
    LOOP_SUPERVISOR("loop", "supervisor", "任务监督专家，基于提供的信息，根据用户需求、任务分析专家和任务执行专家的输出，严格评估本轮执行结果是否真正满足原始需求。"),
    LOOP_SUMMARIZER("loop", "summarizer", "任务总结专家，基于提供的信息，根据用户需求、任务分析专家、任务执行专家、任务监督专家的输出，以及全部历史执行过程，直接给出最终可交付回答。"),

    REACT_OBSERVER("react", "observer", "任务观察专家，基于提供的信息，解析当前任务目标、观察现状、识别任务开始或上一轮产出的变化，并判断是否需要继续执行下一小步。"),
    REACT_REASONER("react", "reasoner", "任务推理专家，基于 Observer 的判断，只制定下一小步的执行策略，明确本轮应该做什么、如何做、做到什么程度算完成。"),
    REACT_ACTOR("react", "actor", "任务行动专家，基于 Observer 与 Reasoner 的输出，实际执行当前这一小步，并给出真实结果。"),
    REACT_EVALUATOR("react", "evaluator", "任务评估专家，在流程结束时对整个 react 执行过程进行收束，给出最终对用户可见的结果。")
    ;

    private String strategy;

    private String roleName;

    private String roleDesc;
}
