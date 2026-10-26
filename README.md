# 校园生活服务平台

这是一个基于 Java 17 + Spring Boot 3 + Vue 3 的校园生活服务平台项目，包含商铺浏览、优惠券活动、订单管理等功能。

## 项目结构

```
campus-life-platform/
├── campus-backend/          # Spring Boot 后端
├── campus-frontend/         # Vue 3 前端
└── README.md
```

## 技术栈

### 后端
- Java 17
- Spring Boot 3.3.5
- MyBatis-Plus 3.5.9
- MySQL 8.0
- Redis (Memurai)
- Redisson 3.37.0
- JWT (jjwt 0.12.6)
- Knife4j 4.5.0 (API 文档)

### 前端
- Vue 3.5
- Vite 6.0
- TypeScript 5.6
- Element Plus 2.9
- Pinia (状态管理)
- Vue Router 4
- Axios

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis (Memurai 或 Redis 6+)
- Node.js 18+
- IntelliJ IDEA (推荐)

### 数据库初始化

1. 创建数据库（已自动创建）:
```sql
CREATE DATABASE campus_life CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本:
```bash
mysql -u root -p campus_life < campus-backend/src/main/resources/db/schema.sql
```

数据库已初始化，包含以下表：
- user (用户表)
- shop_category (商铺分类)
- shop (商铺)
- voucher (优惠券/活动)
- seckill_voucher (秒杀库存)
- orders (订单)
- order_detail (订单明细)

管理员账号：
- 手机号: 13800000000
- 密码: admin123

### 后端启动 (IntelliJ IDEA)

1. 用 IDEA 打开 `campus-backend` 目录
2. IDEA 会自动下载 Maven 依赖（首次可能需要几分钟）
3. 等待依赖下载完成
4. 找到 `com.campus.CampusApplication` 类
5. 右键 → Run 'CampusApplication'
6. 后端启动在 http://localhost:8080

验证：
- 访问 API 文档: http://localhost:8080/doc.html
- 测试登录接口: POST http://localhost:8080/api/common/login

### 前端启动

1. 进入前端目录:
```bash
cd campus-frontend
```

2. 安装依赖:
```bash
npm install
```

3. 启动开发服务器:
```bash
npm run dev
```

4. 前端启动在 http://localhost:5173
5. 自动代理 `/api` 请求到后端 http://localhost:8080

## 项目特性

### 已实现功能
- ✅ 用户注册/登录（JWT 认证）
- ✅ 商铺分类管理
- ✅ 商铺列表浏览和详情查看
- ✅ 管理后台基础框架
- ✅ 统一响应格式和错误处理
- ✅ Redis 缓存配置
- ✅ API 文档自动生成

### 计划实现功能
- 优惠券/活动管理
- 秒杀活动（Lua 脚本 + Redis Stream）
- 订单管理
- 数据统计

## API 文档

启动后端后访问: http://localhost:8080/doc.html

主要接口：
- POST /api/common/login - 用户登录
- POST /api/common/register - 用户注册
- GET /api/user/shop/list - 商铺列表
- GET /api/user/shop/{id} - 商铺详情
- GET /api/user/shop/category/list - 分类列表

## 开发说明

### 后端配置

开发环境配置文件: `campus-backend/src/main/resources/application-dev.yml`

敏感信息（数据库密码等）通过环境变量管理：
- `MYSQL_PASSWORD` - 数据库密码
- `JWT_SECRET` - JWT 密钥

### 前端开发

- 路由配置: `campus-frontend/src/router/index.ts`
- API 封装: `campus-frontend/src/api/`
- 状态管理: `campus-frontend/src/stores/`
- 页面组件: `campus-frontend/src/views/`

## 注意事项

1. **密码安全**: `application-dev.yml` 已加入 `.gitignore`，不会提交到 Git
2. **数据库**: 使用独立的 `campus_life` 数据库，不影响其他项目
3. **端口**: 后端 8080，前端 5173，Redis 6379
4. **管理员账号**: 初始密码为 admin123，首次登录后建议修改

## 项目状态

当前处于第一阶段：项目骨架搭建完成，基础功能开发中。

## License

MIT
