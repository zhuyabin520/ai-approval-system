---
trigger: always_on
---

# 数据库建表规则说明 (DM 数据库)

当你需要为本项目生成建表脚本（DDL）时，请务必遵循以下低代码框架的命名和结构规范：

## 1. 命名规范

### 1.1 表命名规则
- **格式**：`APP_MODEL_{业务首字母缩写}_{四位随机字母}`
- **要求**：全大写。
- **示例**：用户信息表 -> `APP_MODEL_YHXX_AFSD`

### 1.2 字段前缀规则
- **格式**：`{业务首字母缩写的第一位}{四位随机字母的第一位}_`
- **要求**：全大写。
- **示例**：若业务缩写为 `YHXX`，随机字母为 `AFSD`，则字段前缀为 `YA_`。

## 2. 必备默认字段
所有新创建的业务表必须包含以下标准字段（`XX_` 替换为上述定义的字段前缀）：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `XX_UNID` | `NUMBER(10,0)` | 唯一标识UNID (主键) |
| `XX_UUID` | `VARCHAR2(48)` | 唯一标识UUID |
| `XX_CMM_USAGE_SCENARIOS` | `VARCHAR2(128)` | 业务使用场景 |
| `XX_CREATOR_DEPT_UUID` | `VARCHAR2(48)` | 创建者部门标识 |
| `XX_CREATOR_DEPT_NAME` | `VARCHAR2(128)` | 创建者部门名称 |
| `XX_CREATOR` | `VARCHAR2(48)` | 创建者标识 |
| `XX_CREATOR_NAME` | `VARCHAR2(128)` | 创建者名称 |
| `XX_CREATE_TIME` | `TIMESTAMP(6)` | 创建时间 |
| `XX_LAST_UPDATER` | `VARCHAR2(48)` | 更新者标识 |
| `XX_LAST_UPDATER_NAME` | `VARCHAR2(128)` | 更新者名称 |
| `XX_LAST_UPDATE_TIME` | `TIMESTAMP(6)` | 更新时间 |
| `XX_STATUS` | `VARCHAR2(128)` | 记录状态 |
| `XX_ORDER` | `NUMBER(10,0)` | 记录排序号 |
| `XX_MODELID` | `VARCHAR2(128)` | 业务模型唯一标识 |
| `XX_TEXT` | `VARCHAR2(128)` | 标题名称 |
| `XX_CMM_FORM_UUID` | `VARCHAR2(128)` | 表单唯一标识 |
| `XX_CMM_ENTITY_UUID` | `VARCHAR2(128)` | 业务唯一标识 |
| `XX_CMM_GROUP_UUID` | `VARCHAR2(128)` | 表单分组唯一标识 |

## 3. 生成指南
在接收到新建表需求时，请按以下步骤操作：
1. 确定业务名称并提取首字母缩写。
2. 生成四位随机大写字母。
3. 按照规则拼装表名。
4. 按照规则提取字段前缀。
5. 生成包含 18 个必备字段以及业务自定义字段的 `CREATE TABLE` 语句。
6. 确保数据类型符合 DM (达梦) 数据库规范。
