# 架构师日常工作模板包（AI 协作版）

包含三份模板，外加一个 ArchUnit 示例：

1. ADR（架构决策记录）模板
2. 质量属性场景模板
3. 给 AI 的架构评审提示词（方案评审、质疑者、PR 架构预检）
4. 附：ArchUnit 分层规则示例

使用约定：模板里的 `<尖括号>` 是需要你填写的内容；`【人填】` 表示必须由架构师亲自判断，不要交给 AI 代填。

---

## 一、ADR 模板

放在仓库 `docs/adr/` 下，文件名形如 `ADR007-use-postgres-for-order-store.md`。一个决策一个文件，只追加、不改写历史；决策被推翻时，新建一条 ADR 并把旧的状态改为"已被取代"。

```markdown
# ADR-<编号>：<一句话标题，写决策而不是问题>

- 状态：<提议中 | 已采纳 | 已废弃 | 已被 ADR-xxxx 取代>
- 日期：<YYYY-MM-DD>
- 决策人：<姓名>【人填】
- 相关：<关联的需求、ADR、质量属性场景编号>

## 1. 背景与问题
<要解决什么问题；为什么现在要决定；不决定会怎样>

## 2. 约束与决策驱动因素
- 业务约束：<上线时间、预算、合规>
- 技术约束：<既有技术栈、遗留系统、基础设施>
- 团队约束：<人数、技能、运维能力>
- 关键质量属性：<引用质量属性场景，如 QA-003 订单查询 P99 < 200ms>

## 3. 备选方案
### 方案 A：<名称>
- 描述：
- 优点：
- 缺点 / 风险：
- 成本（开发、运维、迁移）：

### 方案 B：<名称>
（同上）

### 方案 C：不做 / 维持现状
（同上，永远写上这一项）

## 4. 决策
选择 **方案 <X>**。【人填】

理由（必须能不依赖 AI 讲清楚）：
1. <对应哪条约束或质量属性>
2. <为什么放弃其他方案>

## 5. 后果
- 正面：
- 负面 / 接受的代价：
- 需要后续跟进的事项：<迁移计划、监控、培训>
- 这个决策成立的前提假设：<假设失效时需要重新评估>

## 6. 如何验证与落地
- 对应的架构规则 / 自动化检查：<如 ArchUnit 规则、契约测试>
- 写入 AGENTS.md 的约束条目：<哪一条>
- 复审时间或触发条件：<如半年后，或 QPS 超过 x>
```

**让 AI 起草 ADR 的提示词**

```text
你是我的架构助手。请根据下面的材料起草一份 ADR，使用我提供的 ADR 模板。

要求：
1. "决策"一节先留空，只写"待架构师确认"，不要替我选择。
2. 备选方案至少 3 个，必须包含"不做 / 维持现状"。
3. 每个方案的缺点和风险要认真写，不要只写优点。
4. 在"后果"一节单独列出"这个决策成立的前提假设"。
5. 凡是你不确定的技术事实（版本特性、性能数据），标注"需核实"，不要编造。

材料：
- 背景：<...>
- 约束：<...>
- 我倾向的方向（可选）：<...>
```

---

## 二、质量属性场景模板

用"六要素"把"要高性能、要高可用"这类模糊要求变成可度量、可验证的场景。建议放在 `docs/quality-attributes.md`，每条一个编号，ADR 和测试里直接引用编号。

| 要素 | 含义 | 示例 |
|---|---|---|
| 来源 Source | 谁或什么触发 | 终端用户 |
| 刺激 Stimulus | 触发了什么 | 在促销高峰提交订单 |
| 制品 Artifact | 被作用的系统部分 | 订单服务 |
| 环境 Environment | 当时的运行条件 | 正常运行，峰值 2000 QPS |
| 响应 Response | 系统应该怎么做 | 正常创建订单并返回结果 |
| 响应度量 Measure | 怎么判断达标【人填】 | P99 延迟 < 300ms，错误率 < 0.1% |

```markdown
### QA-<编号>：<质量属性类别> - <一句话场景名>

- 优先级：<高 | 中 | 低>【人填】
- 类别：<性能 | 可用性 | 可修改性 | 安全性 | 可测试性 | 可观测性 | 成本>
- 来源：
- 刺激：
- 制品：
- 环境：
- 响应：
- 响应度量：<必须是可测的数字或可检查的条件>【人填】
- 验证方式：<压测 | 混沌演练 | 契约测试 | 架构规则 | 评审清单>
- 相关 ADR：
```

**填好的示例**

```markdown
### QA-003：性能 - 大促期间下单

- 优先级：高
- 类别：性能
- 来源：终端用户
- 刺激：大促高峰期集中提交订单
- 制品：订单服务及其数据库
- 环境：正常运行，峰值 2000 QPS
- 响应：订单创建成功并返回订单号
- 响应度量：P99 < 300ms，错误率 < 0.1%
- 验证方式：上线前压测，纳入发布检查清单
- 相关 ADR：ADR-0007
```

**让 AI 帮你补全场景的提示词**

```text
下面是一个系统的背景和我已确定的约束。请按"来源、刺激、制品、环境、响应、响应度量"六要素，
为性能、可用性、可修改性、安全性、可观测性各起草 2～3 条质量属性场景。

要求：
1. "响应度量"一栏只给出建议区间，并说明依据，最终数值由我决定。
2. 指出哪些场景之间可能互相冲突（例如强一致性与高可用）。
3. 先列出你认为需要我澄清的信息，再起草。

背景与约束：<...>
```

---

## 三、给 AI 的架构评审提示词

### 3.1 评审"AI 或自己提出的方案"

```text
你是一名资深架构评审专家。请评审下面这份方案。

输入：
- 背景与目标：<...>
- 约束（团队、预算、遗留系统、合规）：<...>
- 质量属性要求：<引用 QA 编号或粘贴>
- 已有相关决策：<引用 ADR>
- 待评审方案：<...>

请按以下结构输出：
1. 方案摘要：用 3 句话复述你理解的方案，如有理解不确定处先指出。
2. 隐含假设：列出方案成立所依赖、但文中没有明说的前提。
3. 与约束的匹配度：逐条对照我给出的约束和质量属性，标注 满足 / 部分满足 / 不满足 / 无法判断，并说明理由。
4. 主要风险：技术风险、运维风险、迁移风险、团队能力风险，按严重度排序。
5. 过度设计检查：哪些组件或抽象在当前规模下可以不要？
6. 遗漏的权衡：方案没有提到的代价。
7. 与已有 ADR 的冲突：如有，指出并说明。
8. 需要核实的事实：你不确定的技术特性、版本、性能数据，单独列出，不要当成结论。
9. 结论：<可以采纳 | 修改后采纳 | 不建议采纳>，并给出最关键的三条理由。

不要为了显得友好而淡化问题。不确定的地方明确说"不确定"。
```

### 3.2 质疑者模式（建议在另一个新会话里使用）

```text
你的角色是质疑者，唯一任务是找出下面这份方案最可能失败的地方。
你不需要给出平衡的评价，也不需要提供替代方案。

请回答：
1. 假设一年后这个方案失败了，最可能的三个原因是什么？
2. 方案里哪个假设最脆弱？什么情况出现时它会失效？
3. 哪个决策最难回退？回退成本大概是多少？
4. 方案推荐者最可能没有认真考虑的是什么？
5. 如果团队只有 <人数> 人、没有专职运维，哪些部分会成为负担？

方案：<粘贴方案>
约束：<粘贴约束>
```

用法：先让会话 A 出方案，再把方案贴给全新的会话 B 做质疑，最后由你判断哪些质疑成立。

### 3.3 PR 的架构预检（给评审 Agent 或 CI 里的 AI 步骤）

```text
你负责对一次代码变更做"架构层面"的预检，不评论代码风格和局部实现细节。

依据：
- 架构约束文档：<AGENTS.md 中的架构章节>
- 相关 ADR：<列表>
- 本次变更 diff：<...>

请检查并输出：
1. 是否违反分层与依赖方向（如 controller 直接访问 repository、领域层依赖基础设施层）。
2. 是否新增了外部依赖或中间件，是否有对应的 ADR。
3. 是否修改了对外接口、事件 schema 或数据库结构，是否保持向后兼容。
4. 是否引入跨模块的新耦合（新的跨模块调用、共享表、共享类）。
5. 是否绕过了既有的统一机制（鉴权、日志、事务、异常处理、配置）。
6. 是否属于"需要架构师人工评审"的高风险变更。

输出格式：
- 风险等级：低 / 中 / 高
- 发现的问题：（每条写明文件、依据的规则、建议）
- 是否需要架构师评审：是 / 否，并说明原因

没有发现问题时，直接写"未发现架构层面问题"，不要为了凑内容而挑刺。
```

---

## 四、附：ArchUnit 分层规则示例（Java）

把 ADR 里的决策变成"违反就失败"的测试，是让架构约束真正约束到 AI 和团队的关键一步。以下为示意，包名需按你的项目调整。

```java
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.example.app", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    // 分层依赖方向：只允许上层依赖下层
    @ArchTest
    static final ArchRule layered = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Domain").definedBy("..domain..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

    // 领域层不依赖框架和基础设施
    @ArchTest
    static final ArchRule domainIsPure = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "..repository..", "..controller..");

    // 业务模块之间不允许循环依赖
    @ArchTest
    static final ArchRule noCycles = slices().matching("com.example.app.(*)..")
            .should().beFreeOfCycles();
}
```

给 AI 的提示词可以这样写：

```text
请根据下面这份 ADR 中的"决策"与"后果"，为 Java 项目起草 ArchUnit 规则。
每条规则要标注它对应 ADR 的哪一句话；无法用 ArchUnit 检查的约束，单独列出并建议其他验证手段。
ADR：<粘贴>
```

---

## 建议的起步顺序

1. 先从**最近一个已经做出的架构决策**补写一份 ADR，熟悉模板。
2. 挑 3～5 个最重要的质量属性，写成 QA 场景。
3. 把 ADR 里最关键的一两条约束，写成 ArchUnit 规则，接入 CI。
4. 在下一次技术选型时，试用"方案评审 + 质疑者"两段式流程。