# Prompt Template README

本目录沉淀三种执行策略的可复用 prompt 模板：`step`、`loop`、`react`。

## 目录结构
- `step/system-prompt` 与 `step/user-prompt`
- `loop/system-prompt` 与 `loop/user-prompt`
- `react/system-prompt` 与 `react/user-prompt`

## 占位符约定
所有模板都使用 Java `String#formatted(...)` 风格的 `%s` 占位符，用于后续一键生成 agent 时注入具体任务内容。

常见注入内容包括：
- 任务场景说明 / 交付约束
- 用户原始需求
- 当前可用 MCP 工具清单
- 当前轮次、历史执行记录、当前小步目标
- 已有角色输出摘要

## 使用原则
1. `system-prompt` 负责稳定角色职责、输出约束、不可变规则。
2. `user-prompt` 负责当前任务上下文和执行时动态输入。
3. `%s` 只放在真正需要变化的任务信息位置，不机械替换固定规则。
4. `react` 模板遵循 `Observer -> Reasoner -> Actor -> ... -> Evaluator` 的单步迭代链路。
