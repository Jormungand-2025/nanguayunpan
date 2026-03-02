# 南瓜云盘 API 文档

## 概述

南瓜云盘是一个基于 Spring Boot 的文件存储和分享平台，提供用户管理、文件上传下载、分享功能等。

**基础信息**
- 基础URL: `http://localhost:7090/api`
- 认证方式: JWT Token 或 Session
- 数据格式: JSON

## 认证说明

### JWT Token 认证
系统支持 JWT 令牌认证，登录后返回 token，需要在请求头中携带：

```http
Authorization: Bearer <your-jwt-token>
```

### Session 认证（兼容）
同时支持传统的 Session 认证，登录后自动创建会话。

## 公共响应格式

### 成功响应
```json
{
  "status": "success",
  "code": 200,
  "info": "操作成功",
  "data": {}
}
```

### 错误响应
```json
{
  "status": "error",
  "code": 500,
  "info": "错误信息",
  "data": null
}
```

## 用户管理接口

### 1. 用户注册

**接口地址**: `POST /user/register`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 用户邮箱 |
| nickName | string | 是 | 用户昵称 |
| password | string | 是 | 密码 |
| checkCode | string | 是 | 邮箱验证码 |

**请求示例**:
```http
POST /api/user/register
Content-Type: application/x-www-form-urlencoded

email=test@example.com&nickName=测试用户&password=123456&checkCode=123456
```

**响应示例**:
```json
{
  "status": "success",
  "code": 200,
  "info": "注册成功",
  "data": null
}
```

### 2. 用户登录

**接口地址**: `POST /user/login`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 用户邮箱 |
| password | string | 是 | 密码 |

**请求示例**:
```http
POST /api/user/login
Content-Type: application/x-www-form-urlencoded

email=test@example.com&password=123456
```

**响应示例**:
```json
{
  "status": "success",
  "code": 200,
  "info": "登录成功",
  "data": {
    "userInfo": {
      "userId": "U123456789",
      "email": "test@example.com",
      "nickName": "测试用户"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

### 3. 获取用户信息

**接口地址**: `GET /user/getUserInfo`

**认证要求**: 需要登录

**请求示例**:
```http
GET /api/user/getUserInfo
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:
```json
{
  "status": "success",
  "code": 200,
  "info": "获取成功",
  "data": {
    "userId": "U123456789",
    "email": "test@example.com",
    "nickName": "测试用户",
    "joinTime": "2026-02-27T14:30:00",
    "lastLoginTime": "2026-02-27T22:02:18",
    "useSpace": 1048576,
    "totalSpace": 1073741824
  }
}
```

### 4. 发送邮箱验证码

**接口地址**: `POST /user/sendEmailCode`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 邮箱地址 |
| type | integer | 是 | 类型：0-注册，1-重置密码 |

**请求示例**:
```http
POST /api/user/sendEmailCode
Content-Type: application/x-www-form-urlencoded

email=test@example.com&type=0
```

## 文件管理接口

### 1. 文件上传

**接口地址**: `POST /file/upload`

**认证要求**: 需要登录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 上传的文件 |
| fileId | string | 否 | 文件ID，不传则自动生成 |
| filePid | string | 否 | 父目录ID，默认0（根目录） |

**请求示例**:
```http
POST /api/file/upload
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

file=[文件内容]&fileId=test123&filePid=0
```

**响应示例**:
```json
{
  "status": "success",
  "code": 200,
  "info": "上传成功",
  "data": {
    "fileId": "F123456789",
    "fileName": "test.pdf",
    "fileSize": 1048576,
    "filePath": "/files/F123456789/test.pdf"
  }
}
```

### 2. 文件下载

**接口地址**: `GET /file/download/{fileId}`

**认证要求**: 需要登录

**请求示例**:
```http
GET /api/file/download/F123456789
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应**: 文件二进制流

### 3. 获取文件列表

**接口地址**: `GET /file/list`

**认证要求**: 需要登录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| filePid | string | 是 | 父目录ID |
| pageNo | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页大小，默认20 |

**请求示例**:
```http
GET /api/file/list?filePid=0&pageNo=1&pageSize=20
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:
```json
{
  "status": "success",
  "code": 200,
  "info": "获取成功",
  "data": {
    "list": [
      {
        "fileId": "F123456789",
        "fileName": "test.pdf",
        "fileSize": 1048576,
        "fileType": "pdf",
        "createTime": "2026-02-27T22:02:18",
        "folderType": 0
      }
    ],
    "total": 1,
    "pageNo": 1,
    "pageSize": 20
  }
}
```

### 4. 创建文件夹

**接口地址**: `POST /file/createFolder`

**认证要求**: 需要登录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileName | string | 是 | 文件夹名称 |
| filePid | string | 是 | 父目录ID |

### 5. 重命名文件

**接口地址**: `POST /file/rename`

**认证要求**: 需要登录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | string | 是 | 文件ID |
| fileName | string | 是 | 新文件名 |

### 6. 删除文件

**接口地址**: `POST /file/delete`

**认证要求**: 需要登录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileIds | string | 是 | 文件ID，多个用逗号分隔 |

## 分享管理接口

### 1. 创建分享

**接口地址**: `POST /share/create`

**认证要求**: 需要登录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | string | 是 | 文件ID |
| shareType | integer | 否 | 分享类型：0-公开，1-加密，默认0 |
| expireDays | integer | 否 | 过期天数，默认7 |
| password | string | 否 | 分享密码（加密分享时需要） |

**请求示例**:
```http
POST /api/share/create
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/x-www-form-urlencoded

fileId=F123456789&shareType=0&expireDays=7
```

**响应示例**:
```json
{
  "status": "success",
  "code": 200,
  "info": "分享创建成功",
  "data": {
    "shareCode": "ABCD1234",
    "shareUrl": "http://localhost:7090/api/share/info/ABCD1234",
    "expireTime": "2026-03-06T22:02:18"
  }
}
```

### 2. 获取分享信息

**接口地址**: `GET /share/info/{shareCode}`

**认证要求**: 公开接口

**请求示例**:
```http
GET /api/share/info/ABCD1234
```

**响应示例**:
```json
{
  "status": "success",
  "code": 200,
  "info": "获取成功",
  "data": {
    "shareCode": "ABCD1234",
    "fileId": "F123456789",
    "fileName": "test.pdf",
    "shareType": 0,
    "expireTime": "2026-03-06T22:02:18",
    "hasPassword": false
  }
}
```

### 3. 验证分享密码

**接口地址**: `POST /share/validate`

**认证要求**: 公开接口

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| shareCode | string | 是 | 分享码 |
| password | string | 是 | 分享密码 |

### 4. 下载分享文件

**接口地址**: `GET /share/download/{shareCode}/{fileId}`

**认证要求**: 公开接口（加密分享需要先验证密码）

**请求示例**:
```http
GET /api/share/download/ABCD1234/F123456789
```

### 5. 获取我的分享列表

**接口地址**: `GET /share/my`

**认证要求**: 需要登录

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/未登录 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 使用示例

### 完整文件上传流程

1. **登录获取令牌**
```http
POST /api/user/login
Content-Type: application/x-www-form-urlencoded

email=test@example.com&password=123456
```

2. **上传文件**
```http
POST /api/file/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file=[文件]&fileId=test123&filePid=0
```

3. **创建分享**
```http
POST /api/share/create
Authorization: Bearer <token>
Content-Type: application/x-www-form-urlencoded

fileId=<fileId>&shareType=0&expireDays=7
```

## 注意事项

1. **文件大小限制**: 默认支持最大10MB文件上传
2. **令牌过期**: JWT令牌默认24小时过期
3. **分享有效期**: 默认7天
4. **空间限制**: 新用户默认1GB存储空间
5. **支持的文件类型**: 文档、图片、视频、音频等常见格式

---

**文档版本**: v1.0  
**最后更新**: 2026-02-27  
**维护者**: 南瓜云盘开发团队