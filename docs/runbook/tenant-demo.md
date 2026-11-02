# 租户主路径演示

创建套餐并绑定菜单 → 创建两个租户 → 分别登录 → 用户和部门互不可见。

管理页在 `/tenant/package` 和 `/tenant/management`。下面的 curl 与页面走同一套接口。

## 环境

用 `backend/docker`（旧 `backend/scripts/docker/` 已删除）。口令只放在本地 `.env`（从 `.env.example` 复制后自行生成），不要写进仓库。

```bash
cd backend/docker
cp .env.example .env
# 替换 POSTGRES_PASSWORD、REDIS_PWD、MINIO_ROOT_PASSWORD、AES_KEY、WYH_JWT_SECRET
docker compose --env-file .env -f docker-compose.dev.yml up
```

`application-dev.yml` 里 Liquibase 已开启，空库会执行 `db/changelog/postgresql/`（含 `tenant` 表和各业务表的 `tenant_id`）。

浏览器 http://localhost:5888 ，API http://localhost:8000 。只在宿主机起后端时，库和 Redis 的地址、端口、口令、`WYH_JWT_SECRET` 与这份 `.env` 一致，主机名改为 `localhost`。口令不要写进仓库。

## 页面与接口

| 步骤 | 路由 / 方法 | 说明 |
|------|-------------|------|
| 登录 | 页面 `/auth/login`，`POST /auth/login` | 登录页有「租户 ID」输入框，留空表示平台租户 `0`。只在登录请求上带 `X-Tenant-Id` |
| 套餐 | 页面 `/tenant/package`，`POST /tenant/package` | 菜单绑定用 `GET /system/menu/all`。改菜单会回写该套餐下租户的管理员角色 |
| 租户 | 页面 `/tenant/management`，`POST /tenant/management` | 创建时要管理员用户名和 RSA 加密后的密码。默认租户不能删 |
| 用户 | 页面 `/system/user`，`GET /system/user` | 用来核对用户是否串租户 |
| 部门 | `GET /system/department/tree` | 用来核对部门是否串租户 |

平台管理员种子账号：`admin` / `admin123`（租户 `0`）。创建租户、套餐需要已登录，请求头 `Authorization: Bearer <token>`。

登录请求体还要 `clientId`（前端 `.env` 的 `VITE_CLIENT_ID`）：`ef51c9a3e9046c4f2ea45142c8a8344a`，`grantType`：`PASSWORD`。用户名、邮箱或手机号都放在 `username`。

## 1. 平台管理员登录

登录页租户 ID 留空，或：

```bash
curl -s http://127.0.0.1:8000/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"grantType":"PASSWORD","clientId":"ef51c9a3e9046c4f2ea45142c8a8344a","username":"admin","password":"admin123"}'
```

响应 `code` 为 `200` 时，`token` 即后续令牌，`tenantId` 应为 `0`。不要带 `X-Tenant-Id`。若返回需要验证码，说明该账号已连续失败，清掉 Redis 里 `login:retry:` 后再试。

## 2. 创建套餐并绑定菜单

`menuIds` 使用种子菜单：系统管理目录 `1000`、用户管理 `1010` 及其列表权限 `1011`、部门管理 `1070` 及其列表权限 `1071`。租户管理员拿到的菜单就是这一份。

```bash
TOKEN='上一步的 token'
curl -s http://127.0.0.1:8000/tenant/package \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"演示套餐","menuIds":[1000,1010,1011,1070,1071]}'
```

记下返回的 `id`，下面称 `PACKAGE_ID`。

## 3. 创建两个租户

管理员密码仍要 RSA 加密，明文是 `admin123`。公钥是 `application-dev.yml` 里的开发公钥。用户名 4–64 位、字母开头。域名可省略。

```bash
PUB='MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM51dgYtMyF+tTQt80sfFOpSV27a7t9uaUVeFrdGiVxscuizE7H8SMntYqfn9lp8a5GH5P1/GGehVjUD2gF/4kcCAwEAAQ=='
mkdir -p /tmp/tide-demo
printf '%s\n' '-----BEGIN PUBLIC KEY-----' "$PUB" '-----END PUBLIC KEY-----' > /tmp/tide-demo/pub.pem
ENC=$(printf '%s' 'admin123' | openssl pkeyutl -encrypt -pubin -inkey /tmp/tide-demo/pub.pem -pkeyopt rsa_padding_mode:pkcs1 | base64 -w0)

curl -s http://127.0.0.1:8000/tenant/management \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"租户甲\",\"packageId\":$PACKAGE_ID,\"adminUsername\":\"tnta\",\"adminPassword\":\"$ENC\"}"

curl -s http://127.0.0.1:8000/tenant/management \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"租户乙\",\"packageId\":$PACKAGE_ID,\"adminUsername\":\"tntb\",\"adminPassword\":\"$ENC\"}"
```

记下两个 `id`（`TENANT_A`、`TENANT_B`）。创建时会在该租户下写入部门、角色 `admin`（租户管理员）和上述管理员用户，并把套餐菜单赋给这个角色。

## 4. 分租户登录

登录页在「租户 ID」填 `TENANT_A`，用户名 `tnta`，密码 `admin123`。或：

```bash
curl -s http://127.0.0.1:8000/auth/login \
  -H "X-Tenant-Id: $TENANT_A" \
  -H 'Content-Type: application/json' \
  -d '{"grantType":"PASSWORD","clientId":"ef51c9a3e9046c4f2ea45142c8a8344a","username":"tnta","password":"admin123"}'
```

对 `TENANT_B` / `tntb` 再登一次。响应里的 `tenantId` 应等于请求头。

已登录之后，租户以会话为准。再带另一个 `X-Tenant-Id` 不会切到别的租户；要换租户就重新登录。未登录且不带头时落到平台租户 `0`，因此 `admin` 仍按原来的方式登录。

## 5. 核对隔离

用租户甲的 token（不要再改租户头，会话里已经是甲）：

```bash
curl -s 'http://127.0.0.1:8000/system/user?page=1&pageSize=20' \
  -H "Authorization: Bearer $TOKEN_A"

curl -s http://127.0.0.1:8000/system/department/tree \
  -H "Authorization: Bearer $TOKEN_A"
```

期望：

- 租户甲的用户列表里有 `tnta`，没有 `tntb`，也没有平台 `admin`
- 租户乙相反，只有 `tntb`
- 平台 `admin`（租户 `0`）的用户列表里有 `admin`，没有 `tnta` / `tntb`
- 部门树同样只出现本租户的根部门（甲是「租户甲」，乙是「租户乙」，平台是种子「总公司」）

页面上：租户甲登录后打开 `/system/user`，列表里不应出现租户乙的管理员。

## 还没打通

- **数据源级隔离**（`TenantIsolationLevel.DATASOURCE`）只有枚举，没有第二数据源。
- 修改租户管理员密码的接口不存在（菜单权限 `tenant:management:updateAdminUserPwd` 仍在种子里）。
- 更新租户时，空的过期时间、描述不会被写成 null（MyBatis `NOT_NULL`）。
- 下列表有 `tenant_id` 列，实体还没实现 `TenantedEntity`，列表仍可能串数据：成员关系、角色菜单、角色部门、社会化绑定、操作日志、消息、通知、文件、应用。登录日志实体已标记租户列，但写入是 `@Async`，异步线程可能拿不到上下文，失败只记错误日志。
- 手机、邮箱登录会按当前租户查用户，演示路径只用账号登录。
