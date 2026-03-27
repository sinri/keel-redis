# 5.0.1 版本说明

本文档目录针对 **5.0.1** 版本线编写。构建元数据（以仓库 `gradle.properties` 为参考）中与本版本线相关的约束如下：

| 项                         | 值                            |
|---------------------------|------------------------------|
| 本库 artifact               | `io.github.sinri:keel-redis` |
| keel-core（`api`）          | 5.0.1                        |
| vertx-redis-client（`api`） | 5.0.8                        |
| Java                      | 17                           |

## 与 5.0.0 的关系

5.0.1 为 **5.0.0 的补丁/小版本线**：公共 API 以源码为准，升级时请关注：

- `gradle.properties` / 发布说明中 **传递依赖版本** 是否变化。
- [deprecations.md](deprecations.md) 中自 5.0.0 起新增的弃用项（如池化场景下的 `clientId` / 事务方法等）。

若需精确的 5.0.0 → 5.0.1 变更列表，请在 Git 仓库中对标签 **`v5.0.0`** 与 **`v5.0.1`**（或对应提交）做 `git log` /
`git diff` 比对。
