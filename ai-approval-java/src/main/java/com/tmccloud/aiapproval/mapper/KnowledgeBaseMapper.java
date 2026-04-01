package com.tmccloud.aiapproval.mapper;

import com.tmccloud.aiapproval.entity.KnowledgeBase;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {
    @Select("SELECT * FROM knowledge_base")
    List<KnowledgeBase> findAll();

    @Select("SELECT * FROM knowledge_base WHERE id = #{id}")
    KnowledgeBase findById(Long id);

    @Insert("INSERT INTO knowledge_base (name, description, document_path, document_type, file_size, content, upload_time, status, user_id, tags) VALUES (#{name}, #{description}, #{documentPath}, #{documentType}, #{fileSize}, #{content}, #{uploadTime}, #{status}, #{userId}, #{tags})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(KnowledgeBase knowledgeBase);

    @Update("UPDATE knowledge_base SET status = #{status}, vectorization_time = #{vectorizationTime}, error_message = #{errorMessage} WHERE id = #{id}")
    void updateStatus(KnowledgeBase knowledgeBase);

    @Delete("DELETE FROM knowledge_base WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM knowledge_base WHERE tags LIKE CONCAT('%', #{tag}, '%')")
    List<KnowledgeBase> findByTag(String tag);

    @Select("SELECT COUNT(*) FROM knowledge_base")
    int count();

    @Select("SELECT COUNT(*) FROM knowledge_base WHERE status = #{status}")
    int countByStatus(String status);
}
