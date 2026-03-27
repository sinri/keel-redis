# 环境与依赖

## 运行环境

- **JDK 17+**
- 可运行的 **Redis** 服务端（版本需支持你实际调用的命令，例如 `LCS`、`GETEX` 等）。

## Maven

```xml
<dependency>
  <groupId>io.github.sinri</groupId>
  <artifactId>keel-redis</artifactId>
  <version>5.0.1</version>
</dependency>
```

`keel-redis` 的 `api` 依赖会传递引入 `keel-core` 与 `vertx-redis-client`，一般无需重复声明（除非你需要固定其中某个传递版本）。

## Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.sinri:keel-redis:5.0.1")
}
```

## JPMS（`module-info.java`）

若你的应用使用 Java 模块系统，在模块描述符中加入：

```java
requires io.github.sinri.keel.integration.redis;
```

本模块还 **传递** 依赖 `io.github.sinri.keel.core`、`io.vertx.core`、`io.vertx.redis.client` 等；具体以编译器/模块路径解析为准。

## SNAPSHOT 与正式版

开发中可能使用 `5.0.1-SNAPSHOT`。使用 SNAPSHOT 需在构建中配置对应 Maven 仓库；生产环境建议使用已发布的 **5.0.1** 正式坐标。
