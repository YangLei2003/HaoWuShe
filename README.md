# 好物社 (HaoWuShe) - 生活笔记分享与电商平台

一个结合**内容社区**与**电商转化**的全栈项目。用户可以在平台分享生活笔记，浏览商品，并实现社交互动。项目核心功能对标小红书，实现了笔记与商品的融合展示。

## ✨ 核心功能

-   **内容社区**：用户发布图文/视频笔记，拥有完整的点赞、评论、关注/取关、私聊功能。
-   **商品电商**：商品浏览、购物车、下单流程。
-   **笔记挂载商品**：笔记详情页支持关联对应商品，实现“内容种草 -> 商品转化”的闭环。
-   **实时私聊**：基于 WebSocket 的点对点实时消息推送。
-   **全文搜索**：笔记内容基于 Elasticsearch 实现高性能全文检索。

## 🛠️ 技术栈

| 模块 | 技术选型 |
| :--- | :--- |
| **后端框架** | Spring Boot, MyBatis-Plus |
| **缓存/秒杀** | Redis (实现商品缓存、点赞、关注 Feed 流、GEO 附近功能) |
| **搜索引擎** | Elasticsearch (笔记全文检索) |
| **实时通信** | WebSocket (私聊、系统通知) |
| **前端** | Vue 3, Element Plus |
| **数据库** | MySQL |
| **部署** | Maven, Git |

## 🚀 快速启动

### 1. 环境准备
-   JDK 1.8+
-   MySQL 8.0+
-   Redis 6.x+
-   Elasticsearch 7.x+
-   Maven 3.6+

### 2. 数据库配置
-   创建 MySQL 数据库 `haowushe`，导入项目根目录下的 SQL 文件。
-   修改 `application.yml` 中的数据库、Redis 和 ES 连接配置。

mvn clean package
# 运行 jar 包
java -jar target/hao-wu-she.jar
