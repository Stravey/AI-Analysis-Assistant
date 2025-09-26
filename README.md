# 🤖 AI 简历评分系统（AI Resume Scoring System）

## 📌 项目简介

这是一个基于 Spring Boot 开发的 **AI 简历评分与对话系统**，支持用户上传简历文件（PDF、DOC、DOCX），通过 AI 模型（DeepSeek、豆包）进行智能分析，并提供多轮对话功能，帮助用户优化简历内容。

系统还具备完整的 **用户权限管理体系**，支持角色与权限分配、JWT 登录认证、管理员后台管理等功能，适用于企业内部人才筛选、招聘平台集成等场景。

------

## 🧠 核心功能

| 模块        | 功能描述                                              |
| :---------- | :---------------------------------------------------- |
| 🔐 用户认证  | 注册、登录、JWT Token、Token 刷新、登出、Token 黑名单 |
| 👤 用户管理  | 用户信息管理、角色分配、权限分配                      |
| 🧾 简历分析  | 上传简历文件，AI 自动提取关键信息并给出评分与建议     |
| 💬 智能对话  | 基于简历内容与 AI 进行多轮对话，优化简历              |
| 🔒 权限控制  | 基于角色与权限的访问控制（RBAC）                      |
| 🧑‍💼 管理后台 | 用户、角色、权限的可视化管理界面                      |

------

## 🧱 技术架构

### 后端技术栈

- **框架**：Spring Boot 3.x
- **安全**：JWT + 拦截器 + 过滤器
- **数据库**：JPA + Hibernate
- **文件处理**：Apache PDFBox、POI（Word 支持）
- **AI 接口**：DeepSeek API、豆包 API（火山引擎）
- **配置管理**：Spring Boot ConfigurationProperties
- **工具**：Lombok、Jackson、Maven

### 前端（默认模板）

- 提供基础 HTML 模板（如 `login.html`, `register.html`, `interview.html` 等）
- 可对接 Vue/React 等前端框架

------

## 📁 项目结构

```
org.example.airesumescoring
├── component/           # 通用组件（如 TokenBlacklist）
├── config/              # 配置类（AI、JWT、Web）
├── controller/          # 控制器（REST 接口与页面跳转）
├── dto/                 # 数据传输对象
├── exception/           # 自定义异常
├── filter/              # JWT 过滤器
├── interceptor/         # 权限拦截器
├── model/               # 实体类（用户、角色、权限、对话历史等）
├── repository/          # JPA 数据访问层
├── service/             # 业务逻辑层（AI、用户、权限等）
└── util/                # 工具类（JWT、加密等）
```

------

## 🧪 支持的 AI 模型

| 模型名称       | 说明                            | 配置方式                                       |
| :------------- | :------------------------------ | :--------------------------------------------- |
| DeepSeek       | 默认模型，支持简历分析与对话    | `deepseek.api.key`                             |
| 豆包（DouBao） | 火山引擎平台模型，支持 IAM 认证 | `doubao.api.key` / `access-key` + `secret-key` |

------

## 🔐 权限系统设计

### 实体关系

- 用户（Users）↔ 角色（Role）↔ 权限（Permission）
- 用户可直接分配权限
- 角色可批量分配权限
- 权限拦截器基于请求路径与方法动态解析所需权限

### 示例权限

| 权限名称         | 描述             |
| :--------------- | :--------------- |
| `admin:access`   | 管理后台访问权限 |
| `resume:analyze` | 简历分析权限     |
| `user:manage`    | 用户管理权限     |

------

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-username/ai-resume-scoring.git
cd ai-resume-scoring
```

### 2. 配置环境

在 `application.yml` 中添加以下配置：

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400

deepseek:
  api:
    key: your-deepseek-key
    url: https://api.deepseek.com/v1/chat/completions

doubao:
  api:
    key: your-doubao-key
    url: https://ark.cn-beijing.volces.com/api/v3/chat/completions
    model-id: Doubao-lite-128k
    access-key: optional
    secret-key: optional

ai:
  model-default: deepseek
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

访问：

- 登录页面：http://localhost:8080/login
- 注册页面：http://localhost:8080/register
- 简历上传与对话：http://localhost:8080/interview
- 管理后台：http://localhost:8080/admin/users

------

## 📬 API 接口示例

### 上传简历并分析

```http
POST /api/resume/upload
Content-Type: multipart/form-data

resume: [文件]
model: deepseek (可选)
```

### 简历对话

```http
POST /api/resume/dialogue
Content-Type: application/json
X-Session-ID: abc123

{
  "question": "如何优化我的简历？",
  "resumeText": "我的简历内容是..."
}
```

------

## 🛡️ 安全说明

- 所有密码目前为明文存储（开发阶段），建议生产环境使用 BCrypt 加密
- JWT 支持 Token 刷新与黑名单机制
- 权限控制基于路径与方法，支持动态扩展

------

## 📌 开源计划

- ✅ 基础功能完成（用户、权限、AI 分析、对话）
- 🔄 后续计划：
  - 前端 Vue3 + Ant Design 管理后台
  - 简历模板推荐系统
  - AI 模型评分可视化
  - 多语言支持（中英文）

------

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

请遵循以下规范：

- 提交前请格式化代码
- 新增功能请添加单元测试
- 提交信息请清晰描述变更内容

------

## 📄 开源协议

MIT License

------

## 📞 联系方式

如有问题或建议，欢迎联系：

- 邮箱：2205487298@qq.com
- GitHub：[Stravey](https://github.com/Stravey)

------

> ⭐ 如果这个项目对你有帮助，欢迎点个 Star！