# Keel Redis 集成 — 文档索引（5.0.1）

本目录为 **Keel Integration for Redis**（Maven 坐标：`io.github.sinri:keel-redis`）对应 **5.0.1** 版本线的使用说明。仓库中
`gradle.properties` 可能为 `5.0.1-SNAPSHOT`；发布到 Maven Central 的正式版以 **5.0.1** 为准阅读本文档即可。

## 文档结构

| 文档                            | 说明                                                       |
|-------------------------------|----------------------------------------------------------|
| [概述与范围](overview.md)          | 定位、模块边界、版本与依赖矩阵                                          |
| [版本说明](version-notes.md)      | 5.0.1 依赖矩阵及与 5.0.0 的关系                                   |
| [架构视图](architecture.md)       | 组件关系与调用路径（含示意图）                                          |
| [环境与依赖](installation.md)      | JDK、Gradle/Maven、JPMS 模块声明                               |
| [快速开始](quick-start.md)        | 最小可运行示例与生命周期                                             |
| [连接与配置](configuration.md)     | `RedisConfig`、连接串、连接池参数                                  |
| [RedisKit 使用指南](redis-kit.md) | `api` / `withConnection` / `withTransaction`、阻塞命令注意事项    |
| [异步缓存](async-cache.md)        | `KeelAsyncCacheWithRedis` 与 `KeelAsyncCacheInterface` 契约 |
| [API 速查](api-reference.md)    | 按数据结构与主题归类的方法索引                                          |
| [弃用与迁移](deprecations.md)      | `@Deprecated` API 及推荐替代方式                                |
| [故障排查](troubleshooting.md)    | 常见问题与约束说明                                                |

## 官方链接

- 项目主页：<https://github.com/sinri/keel-redis>
- 许可证：GPL-3.0（见仓库根目录说明）

## 与 JavaDoc 的关系

本库发布 **`-javadoc.jar`**。本文档侧重**使用场景、配置与约束**；方法级细节以源码/JavaDoc 为准，二者互补。
