package com.cms.permissions.repository;

import com.cms.permissions.entity.MediaResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 媒体资源Repository
 * 提供媒体资源的数据库操作
 */
@Repository
public interface MediaResourceRepository extends JpaRepository<MediaResource, Long> {

    /**
     * 根据对象名称查找媒体资源
     */
    Optional<MediaResource> findByObjectName(String objectName);

    /**
     * 根据文档ID查找所有媒体资源
     */
    List<MediaResource> findByDocumentIdAndIsDeletedFalse(Long documentId);

    /**
     * 根据上传用户查找媒体资源
     */
    List<MediaResource> findByUploadedByAndIsDeletedFalse(Long uploadedBy);

    /**
     * 根据文件分类查找媒体资源
     */
    List<MediaResource> findByFileCategoryAndIsDeletedFalse(String fileCategory);

    /**
     * 根据内容类型查找媒体资源
     */
    List<MediaResource> findByContentTypeContainingAndIsDeletedFalse(String contentType);

    /**
     * 查找指定时间范围内上传的文件
     */
    @Query("SELECT m FROM MediaResource m WHERE m.uploadTime BETWEEN :startTime AND :endTime AND m.isDeleted = false")
    List<MediaResource> findByUploadTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 根据文件名模糊查询
     */
    @Query("SELECT m FROM MediaResource m WHERE m.originalFilename LIKE %:filename% AND m.isDeleted = false")
    List<MediaResource> findByOriginalFilenameContaining(@Param("filename") String filename);

    /**
     * 查找大于指定大小的文件
     */
    List<MediaResource> findByFileSizeGreaterThanAndIsDeletedFalse(Long fileSize);

    /**
     * 查找小于指定大小的文件
     */
    List<MediaResource> findByFileSizeLessThanAndIsDeletedFalse(Long fileSize);

    /**
     * 统计用户上传的文件数量
     */
    @Query("SELECT COUNT(m) FROM MediaResource m WHERE m.uploadedBy = :userId AND m.isDeleted = false")
    Long countByUploadedBy(@Param("userId") Long userId);

    /**
     * 统计用户上传的文件总大小
     */
    @Query("SELECT COALESCE(SUM(m.fileSize), 0) FROM MediaResource m WHERE m.uploadedBy = :userId AND m.isDeleted = false")
    Long sumFileSizeByUploadedBy(@Param("userId") Long userId);

    /**
     * 查找长时间未访问的文件（用于清理）
     */
    @Query("SELECT m FROM MediaResource m WHERE m.lastAccessed < :cutoffTime AND m.isDeleted = false")
    List<MediaResource> findUnusedFiles(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 软删除文件
     */
    @Query("UPDATE MediaResource m SET m.isDeleted = true WHERE m.id = :id")
    void softDeleteById(@Param("id") Long id);

    /**
     * 批量软删除文件
     */
    @Query("UPDATE MediaResource m SET m.isDeleted = true WHERE m.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据标签查找文件
     */
    @Query("SELECT m FROM MediaResource m WHERE m.tags LIKE %:tag% AND m.isDeleted = false")
    List<MediaResource> findByTagsContaining(@Param("tag") String tag);

    /**
     * 查找所有未删除的文件，按上传时间降序排列
     */
    List<MediaResource> findByIsDeletedFalseOrderByUploadTimeDesc();

    /**
     * 分页查找用户的文件
     */
    @Query("SELECT m FROM MediaResource m WHERE m.uploadedBy = :userId AND m.isDeleted = false ORDER BY m.uploadTime DESC")
    List<MediaResource> findUserFiles(@Param("userId") Long userId);
}