# 校园生活平台 - Docker 部署指南

## 📋 部署概览

本文档指导你如何将校园生活平台部署到阿里云服务器。

**技术栈：**
- Docker + Docker Compose
- MySQL 8.0
- Redis 7
- Spring Boot 3.3.5
- Nginx（可选）

**预计时间：** 1-2 小时

**预计费用：** 50-100 元/月（学生优惠）

---

## 🛒 步骤 1：购买阿里云服务器

### 1.1 访问阿里云学生优惠页面

访问：https://university.aliyun.com/campus

或者搜索："阿里云学生优惠"

### 1.2 选择服务器配置

**推荐配置：**

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| **CPU** | 2 核 | 足够运行 Spring Boot + MySQL + Redis |
| **内存** | 4GB | Docker 需要一定内存 |
| **系统盘** | 40GB SSD | 存储系统和数据 |
| **带宽** | 1-5Mbps | 个人项目足够 |
| **系统** | Ubuntu 22.04 LTS | 稳定，Docker 支持好 |
| **地域** | 华东 1（杭州）或华东 2（上海） | 国内访问快 |

**价格：** 约 50-100 元/月（学生认证后）

### 1.3 完成购买

1. 登录阿里云账号
2. 完成学生认证（需要学生证或学信网截图）
3. 选择上述配置
4. 支付费用
5. 等待服务器创建（约 1-2 分钟）

### 1.4 获取服务器信息

购买完成后，进入阿里云控制台：

1. 访问：https://ecs.console.aliyun.com/
2. 找到你的服务器实例
3. 记录以下信息：
   - **公网 IP**：如 `47.98.123.456`
   - **登录密码**：购买时设置的密码
   - **安全组**：需要配置端口开放

---

## 🔒 步骤 2：配置安全组（开放端口）

### 2.1 进入安全组配置

1. 在 ECS 控制台，点击你的实例
2. 左侧菜单 → "安全组"
3. 点击安全组 ID 进入配置页面

### 2.2 添加入方向规则

点击"手动添加"，添加以下规则：

| 协议类型 | 端口范围 | 授权对象 | 说明 |
|---------|---------|---------|------|
| TCP | 22 | 0.0.0.0/0 | SSH 远程登录 |
| TCP | 8080 | 0.0.0.0/0 | Spring Boot 后端 |
| TCP | 3306 | 0.0.0.0/0 | MySQL（可选，建议仅开放给特定 IP） |
| TCP | 6379 | 0.0.0.0/0 | Redis（可选，建议仅开放给特定 IP） |

**注意：** 
- MySQL 和 Redis 端口建议只开放给你的 IP，或者不开放（通过 SSH 隧道访问）
- 生产环境建议关闭 3306 和 6379 的公网访问

### 2.3 保存配置

点击"保存"，安全组规则立即生效。

---

## 🖥️ 步骤 3：连接服务器

### 3.1 使用 SSH 连接

**Windows 用户：**

方法 1：使用 PowerShell
```bash
ssh root@你的服务器IP
# 例如：ssh root@47.98.123.456
```

方法 2：使用 PuTTY 或 Xshell
- 下载 PuTTY：https://www.putty.org/
- 填写 IP 和端口 22
- 输入用户名 `root` 和密码

**Mac/Linux 用户：**
```bash
ssh root@你的服务器IP
```

### 3.2 更新系统

连接成功后，执行以下命令：

```bash
# 更新软件包列表
apt update

# 升级已安装的软件包
apt upgrade -y

# 安装常用工具
apt install -y vim wget curl git
```

---

## 🐳 步骤 4：安装 Docker

### 4.1 安装 Docker

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

# 启动 Docker 服务
systemctl start docker

# 设置 Docker 开机自启
systemctl enable docker

# 验证 Docker 安装
docker --version
```

### 4.2 安装 Docker Compose

```bash
# 下载 Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 添加执行权限
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker-compose --version
```

### 4.3 配置 Docker 镜像加速（可选但推荐）

```bash
# 创建 Docker 配置目录
mkdir -p /etc/docker

# 写入镜像加速配置
tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}
EOF

# 重启 Docker
systemctl daemon-reload
systemctl restart docker
```

---

## 📦 步骤 5：上传项目代码

### 5.1 在服务器上创建项目目录

```bash
# 创建项目目录
mkdir -p /opt/campus-life-platform

# 进入目录
cd /opt/campus-life-platform
```

### 5.2 上传代码

**方法 1：使用 Git（推荐）**

如果你的项目在 GitHub/Gitee：

```bash
# 克隆项目
git clone https://github.com/你的用户名/campus-life-platform.git .

# 或者使用 Gitee
git clone https://gitee.com/你的用户名/campus-life-platform.git .
```

**方法 2：使用 SCP 上传（从本地上传）**

在你的本地电脑（Windows）执行：

```bash
# 在 PowerShell 中执行
scp -r D:\javaProjecs\campus-life-platform root@你的服务器IP:/opt/campus-life-platform
```

**方法 3：使用 FTP 工具**

使用 FileZilla、WinSCP 等工具连接服务器，上传项目文件夹。

### 5.3 验证文件上传

```bash
# 查看目录结构
ls -la

# 应该看到：
# Dockerfile
# docker-compose.yml
# campus-backend/
# .dockerignore
```

---

## 🔨 步骤 6：构建和启动项目

### 6.1 构建后端 JAR 包

**方法 1：在服务器上构建（推荐）**

```bash
# 安装 Maven
apt install -y maven

# 进入后端目录
cd campus-backend

# 构建项目（跳过测试）
mvn clean package -DskipTests

# 验证 JAR 包生成
ls -lh target/campus-backend-1.0.0.jar
```

**方法 2：在本地构建后上传**

在本地执行：
```bash
cd D:\javaProjecs\campus-life-platform\campus-backend
mvn clean package -DskipTests
```

然后上传 JAR 包到服务器：
```bash
scp target/campus-backend-1.0.0.jar root@你的服务器IP:/opt/campus-life-platform/campus-backend/target/
```

### 6.2 创建数据目录

```bash
# 回到项目根目录
cd /opt/campus-life-platform

# 创建数据持久化目录
mkdir -p data/mysql
mkdir -p data/redis
```

### 6.3 启动 Docker Compose

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

**预期输出：**
```
NAME                COMMAND                  SERVICE             STATUS              PORTS
campus-mysql        "docker-entrypoint.s…"   mysql               running (healthy)   0.0.0.0:3306->3306/tcp
campus-redis        "docker-entrypoint.s…"   redis               running (healthy)   0.0.0.0:6379->6379/tcp
campus-backend      "java -jar -Xms512m …"   backend             running             0.0.0.0:8080->8080/tcp
```

### 6.4 验证服务启动

```bash
# 等待 30 秒，让所有服务启动完成
sleep 30

# 测试后端 API
curl http://localhost:8080/api/user/shop/list

# 如果返回 JSON 数据，说明后端启动成功
```

---

## 🌐 步骤 7：访问项目

### 7.1 通过公网 IP 访问

在浏览器中访问：

```
http://你的服务器公网IP:8080/api/user/shop/list
```

例如：`http://47.98.123.456:8080/api/user/shop/list`

如果看到 JSON 数据，说明部署成功！

### 7.2 测试秒杀接口

使用 Postman 或 curl 测试秒杀接口：

```bash
# 先获取 Token（需要先注册用户）
curl -X POST http://你的服务器IP:8080/api/user/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 使用 Token 测试秒杀
curl -X POST http://你的服务器IP:8080/api/user/voucher/seckill/1001 \
  -H "Authorization: Bearer 你的Token"
```

---

## 📊 步骤 8：监控和管理

### 8.1 查看容器状态

```bash
# 查看所有容器
docker-compose ps

# 查看容器资源使用
docker stats

# 查看特定容器日志
docker-compose logs backend
docker-compose logs mysql
docker-compose logs redis
```

### 8.2 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
```

### 8.3 停止服务

```bash
# 停止所有服务
docker-compose stop

# 停止并删除容器（保留数据）
docker-compose down

# 停止并删除容器和数据（谨慎使用）
docker-compose down -v
```

### 8.4 更新代码

```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建 JAR 包
cd campus-backend
mvn clean package -DskipTests
cd ..

# 3. 重新构建并启动
docker-compose up -d --build
```

---

## 🔧 故障排查

### 问题 1：容器启动失败

```bash
# 查看容器日志
docker-compose logs backend

# 常见原因：
# - MySQL 连接失败：检查环境变量配置
# - Redis 连接失败：检查密码配置
# - 端口被占用：检查 8080、3306、6379 端口
```

### 问题 2：数据库连接失败

```bash
# 检查 MySQL 容器状态
docker-compose ps mysql

# 查看 MySQL 日志
docker-compose logs mysql

# 进入 MySQL 容器测试连接
docker exec -it campus-mysql mysql -u campus -pcampus123
```

### 问题 3：无法访问后端 API

```bash
# 检查安全组是否开放 8080 端口
# 检查防火墙
ufw status
ufw allow 8080/tcp

# 检查后端容器是否正常运行
docker-compose ps backend
```

---

## 📝 常用命令速查

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose stop

# 重启所有服务
docker-compose restart

# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 进入容器
docker exec -it campus-backend bash
docker exec -it campus-mysql mysql -u root -proot123456

# 重新构建
docker-compose up -d --build

# 清理未使用的镜像
docker image prune -f

# 查看磁盘使用
df -h
```

---

## 🎉 部署完成

恭喜你！项目已成功部署到阿里云服务器。

**访问地址：** `http://你的服务器IP:8080`

**下一步：**
1. 在简历中添加项目访问地址
2. 录制演示视频
3. 准备面试问题

---

## 📚 参考资料

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [阿里云 ECS 文档](https://help.aliyun.com/product/25365.html)
- [MySQL Docker 镜像](https://hub.docker.com/_/mysql)
- [Redis Docker 镜像](https://hub.docker.com/_/redis)
