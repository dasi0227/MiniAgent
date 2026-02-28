package com.dasi.domain.ai.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiSectionType {

    ANALYZER_DEMAND("任务需求分析", "analyzer_demand"),
    ANALYZER_HISTORY("执行历史评估", "analyzer_history"),
    ANALYZER_STRATEGY("执行策略制定", "analyzer_strategy"),
    ANALYZER_PROGRESS("完成度评估", "analyzer_progress"),
    ANALYZER_STATUS("任务状态", "analyzer_status"),
    PERFORMER_TARGET("执行目标", "performer_target"),
    PERFORMER_PROCESS("执行过程", "performer_process"),
    PERFORMER_RESULT("执行结果", "performer_result"),
    SUPERVISOR_ISSUE("问题识别", "supervisor_issue"),
    SUPERVISOR_SUGGESTION("改进建议", "supervisor_suggestion"),
    SUPERVISOR_SCORE("质量评分", "supervisor_score"),
    SUPERVISOR_STATUS("监督状态", "supervisor_status"),
    SUMMARIZER_OVERVIEW("任务总结", "summarizer_overview"),
    INSPECTOR_MCP("MCP 工具", "inspector_mcp"),
    PLANNER_STEP("任务步骤", "planner_step"),
    RUNNER_RESULT("运行结果", "runner_result"),
    RUNNER_STATUS("运行状态", "runner_status"),
    REPLIER_OVERVIEW("任务回复", "replier_overview"),
    OBSERVER_DEMAND("任务目标解析", "observer_demand"),
    OBSERVER_HISTORY("现状观察", "observer_history"),
    OBSERVER_JUDGEMENT("继续判断", "observer_judgement"),
    OBSERVER_STATUS("观察状态", "observer_status"),
    REASONER_TARGET("下一步目标", "reasoner_target"),
    REASONER_ACTION("下一步动作", "reasoner_action"),
    REASONER_ACCEPTANCE("验收条件", "reasoner_acceptance"),
    ACTOR_TARGET("执行目标", "actor_target"),
    ACTOR_PROCESS("执行过程", "actor_process"),
    ACTOR_RESULT("执行结果", "actor_result"),
    EVALUATOR_OVERVIEW("最终结论", "evaluator_overview")
    ;

    private String name;

    private String type;

}
