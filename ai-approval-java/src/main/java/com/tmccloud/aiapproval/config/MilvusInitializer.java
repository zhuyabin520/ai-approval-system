package com.tmccloud.aiapproval.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.index.CreateIndexParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MilvusInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MilvusInitializer.class);

    @Autowired
    private MilvusServiceClient milvusClient;

    @Value("${spring.ai.vectorstore.milvus.collection-name:knowledge_base}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.milvus.embedding-dimension:1536}")
    private int embeddingDimension;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeVectorStore();
    }

    public void initializeVectorStore() {
        logger.info("开始初始化 Milvus 向量库...");
        logger.info("集合名称: {}, 向量维度: {}", collectionName, embeddingDimension);

        try {
            if (!collectionExists()) {
                createCollection();
                createIndex();
                loadCollection();
                logger.info("Milvus 向量库初始化完成");
            } else {
                logger.info("集合 {} 已存在", collectionName);
                // 检查是否需要创建索引
                if (!indexExists()) {
                    createIndex();
                }
                ensureCollectionLoaded();
            }
        } catch (Exception e) {
            logger.error("Milvus 向量库初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("Milvus 向量库初始化失败", e);
        }
    }

    private boolean collectionExists() {
        HasCollectionParam param = HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();

        R<Boolean> response = milvusClient.hasCollection(param);

        if (response.getStatus() != 0) {
            logger.warn("检查集合存在性失败: {}", response.getMessage());
            return false;
        }

        return response.getData();
    }

    private boolean indexExists() {
        try {
            io.milvus.param.index.DescribeIndexParam param =
                    io.milvus.param.index.DescribeIndexParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build();

            R<io.milvus.grpc.DescribeIndexResponse> response = milvusClient.describeIndex(param);

            if (response.getStatus() == 0 && response.getData() != null) {
                int indexCount = response.getData().getIndexDescriptionsCount();
                logger.info("索引检查: 找到 {} 个索引", indexCount);
                return indexCount > 0;
            }
            return false;
        } catch (Exception e) {
            logger.info("索引不存在");
            return false;
        }
    }

    /**
     * 创建集合 - 只保留必要字段
     */
    private void createCollection() {
        logger.info("创建集合: {}", collectionName);

        List<FieldType> fields = new ArrayList<>();

        // 1. 主键字段 id
        fields.add(FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.VarChar)
                .withMaxLength(100)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build());

        // 2. 向量字段 embedding
        fields.add(FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(embeddingDimension)
                .build());

        // 3. 内容字段 content
        fields.add(FieldType.newBuilder()
                .withName("content")
                .withDataType(DataType.VarChar)
                .withMaxLength(65535)
                .build());

        // 4. 元数据字段 metadata (JSON) - 所有业务字段都放这里
        fields.add(FieldType.newBuilder()
                .withName("metadata")
                .withDataType(DataType.JSON)
                .build());

        // 创建集合
        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("知识库向量存储集合")
                .withFieldTypes(fields)
                .withShardsNum(2)
                .build();

        R<RpcStatus> response = milvusClient.createCollection(createParam);

        if (response.getStatus() != 0) {
            throw new RuntimeException("创建集合失败: " + response.getMessage());
        }

        logger.info("集合 {} 创建成功，字段: id, embedding, content, metadata", collectionName);
    }

    /**
     * 创建向量索引
     */
    private void createIndex() {
        logger.info("创建向量索引...");

        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.IP)
                .withExtraParam("{\"nlist\":128}")
                .withSyncMode(true)
                .build();

        R<RpcStatus> response = milvusClient.createIndex(indexParam);

        if (response.getStatus() != 0) {
            throw new RuntimeException("创建索引失败: " + response.getMessage());
        }

        logger.info("向量索引创建完成 (IVF_FLAT, IP)");
    }

    /**
     * 加载集合到内存
     */
    private void loadCollection() {
        logger.info("加载集合到内存: {}", collectionName);

        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withSyncLoad(true)
                .withSyncLoadWaitingInterval(1000L)
                .withSyncLoadWaitingTimeout(60L)
                .build();

        R<RpcStatus> response = milvusClient.loadCollection(loadParam);

        if (response.getStatus() != 0) {
            logger.error("加载集合失败: {}", response.getMessage());
            throw new RuntimeException("加载集合失败: " + response.getMessage());
        }

        logger.info("集合 {} 已加载到内存", collectionName);
    }

    /**
     * 确保集合已加载
     */
    private void ensureCollectionLoaded() {
        try {
            logger.info("检查并加载集合: {}", collectionName);

            LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withSyncLoad(true)
                    .withSyncLoadWaitingInterval(1000L)
                    .withSyncLoadWaitingTimeout(30L)
                    .build();

            R<RpcStatus> response = milvusClient.loadCollection(loadParam);

            if (response.getStatus() == 0) {
                logger.info("集合 {} 已就绪", collectionName);
            } else {
                logger.warn("集合加载状态检查失败: {}", response.getMessage());
            }
        } catch (Exception e) {
            logger.warn("检查集合加载状态时出错: {}", e.getMessage());
        }
    }

    /**
     * 删除集合
     */
    public void dropCollection() {
        try {
            HasCollectionParam hasParam = HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            R<Boolean> hasResponse = milvusClient.hasCollection(hasParam);

            if (hasResponse.getData()) {
                ReleaseCollectionParam releaseParam = ReleaseCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build();
                milvusClient.releaseCollection(releaseParam);

                DropCollectionParam dropParam = DropCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build();
                R<RpcStatus> response = milvusClient.dropCollection(dropParam);

                if (response.getStatus() == 0) {
                    logger.info("集合 {} 已删除", collectionName);
                } else {
                    logger.error("删除集合失败: {}", response.getMessage());
                }
            } else {
                logger.info("集合 {} 不存在", collectionName);
            }
        } catch (Exception e) {
            logger.error("删除集合时出错: {}", e.getMessage());
        }
    }

    /**
     * 重建集合
     */
    public void rebuildCollection() {
        logger.info("开始重建集合: {}", collectionName);
        dropCollection();
        createCollection();
        createIndex();
        loadCollection();
        logger.info("集合 {} 重建完成", collectionName);
    }
}