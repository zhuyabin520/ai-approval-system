---
trigger: always_on
---


# DAO 实现类规则说明

当你需要为本项目生成 DAO 实现类时，请务必遵循以下低代码框架的开发规范：

## 1. DAO 实现类命名规范

### 1.1 类命名规则
- **格式**：DAO 接口名 + `Impl`
- **示例**：`IAppModelAccountExpandJbDao` 的实现类为 `AppModelAccountExpandJbDaoImpl`

### 1.2 包路径规则
- 实现类放在与接口相同的包路径下的 `impl` 子包中
- **示例**：接口在 `com.xxx.dao`，实现类在 `com.xxx.dao.impl`

## 2. MyBatis Mapper 文件规范

### 2.1 Mapper 文件命名规则
- **格式**：将 DAO 接口名的最后的 `Dao` 替换为 `Mapper`
- **示例**：`IAppModelAccountExpandJbDao` 的 Mapper 文件为 `IAppModelAccountExpandJbMapper.xml`

### 2.2 Mapper 文件路径规则
- **位置**：`resources` 目录中的 DAO 接口同名路径下的 `mapper` 子目录
- **示例**：
  - 接口路径：`src/main/java/com/xxx/dao/IAppModelAccountExpandJbDao.java`
  - Mapper 路径：`src/main/resources/com/xxx/dao/mapper/IAppModelAccountExpandJbMapper.xml`

## 3. SQL 调用规范

### 3.1 标准查询方法实现
所有查询方法必须遵循以下模式：

@Override
public List<实体类> find实体类(实体类 model, Page page){
    DaoCtx daoCtx = new DaoCtx(model);
    daoCtx.lnkPageInfo(new PageInfo().lnkPage(page));
    return this.getMyBatisDaoManager().exeSelectCommand(daoCtx, "sqlId");
}

### 3.2 DaoCtx 使用规则
- **创建 DaoCtx**：`new DaoCtx(model)`，将查询条件对象传入
- **分页信息**：通过 `daoCtx.lnkPageInfo(new PageInfo().lnkPage(page))` 设置分页
- **执行查询**：使用 `this.getMyBatisDaoManager().exeSelectCommand(daoCtx, "sqlId")`

### 3.3 SQL ID 命名规范
- **格式**：使用小驼峰命名法，描述查询意图
- **常用 SQL ID**：
  - `findModelByCndEnable`：根据条件查询启用的记录
  - `findModelByCnd`：根据条件查询所有记录
  - `findModelByUuid`：根据 UUID 查询记录
  - `insertModel`：插入记录
  - `updateModel`：更新记录
  - `deleteModel`：删除记录

## 4. 方法实现模板

### 4.1 查询列表（带分页）
@Override
public List<实体类> find实体类List(实体类 model, Page page){
    DaoCtx daoCtx = new DaoCtx(model);
    daoCtx.lnkPageInfo(new PageInfo().lnkPage(page));
    return this.getMyBatisDaoManager().exeSelectCommand(daoCtx, "findModelByCndEnable");
}

### 4.2 查询单个对象
@Override
public 实体类 find实体类ByUuid(String uuid){
    实体类 model = new 实体类();
    model.set字段前缀UUID(uuid);
    DaoCtx daoCtx = new DaoCtx(model);
    List<实体类> list = this.getMyBatisDaoManager().exeSelectCommand(daoCtx, "findModelByUuid");
    return list.isEmpty() ? null : list.get(0);
}

### 4.3 插入操作
@Override
public int insert实体类(实体类 model){
    DaoCtx daoCtx = new DaoCtx(model);
    return this.getMyBatisDaoManager().exeInsertCommand(daoCtx, "insertModel");
}

### 4.4 更新操作
@Override
public int update实体类(实体类 model){
    DaoCtx daoCtx = new DaoCtx(model);
    return this.getMyBatisDaoManager().exeUpdateCommand(daoCtx, "updateModel");
}

### 4.5 删除操作
@Override
public int delete实体类(String uuid){
    实体类 model = new 实体类();
    model.set字段前缀UUID(uuid);
    DaoCtx daoCtx = new DaoCtx(model);
    return this.getMyBatisDaoManager().exeDeleteCommand(daoCtx, "deleteModel");
}

## 5. Mapper 文件基本结构

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="对应的DAO接口全限定名">
    
    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="实体类全限定名">
        <id column="字段前缀_UNID" property="字段前缀Unid" jdbcType="DECIMAL"/>
        <result column="字段前缀_UUID" property="字段前缀Uuid" jdbcType="VARCHAR"/>
        <!-- 其他字段映射 -->
    </resultMap>
    
    <!-- 基础字段列表 -->
    <sql id="Base_Column_List">
        字段前缀_UNID, 字段前缀_UUID, 字段前缀_TEXT, ...
    </sql>
    
    <!-- 查询语句示例 -->
    <select id="findModelByCndEnable" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM 表名
        WHERE 字段前缀_STATUS = '1'
        <if test="model.字段前缀Text != null and model.字段前缀Text != ''">
            AND 字段前缀_TEXT LIKE '%' || #{model.字段前缀Text} || '%'
        </if>
        ORDER BY 字段前缀_ORDER ASC, 字段前缀_CREATE_TIME DESC
    </select>
    
</mapper>

## 6. 注意事项

1. **DaoCtx 对象**：始终使用 `DaoCtx` 封装查询条件和分页信息
2. **命令方法**：
   - 查询使用 `exeSelectCommand`
   - 插入使用 `exeInsertCommand`
   - 更新使用 `exeUpdateCommand`
   - 删除使用 `exeDeleteCommand`
3. **SQL ID**：必须与 Mapper 文件中定义的 ID 完全匹配
4. **分页处理**：通过 `PageInfo` 和 `Page` 对象管理分页参数
5. **返回值**：查询方法返回 `List`，单个对象查询需要从 List 中取第一个元素
