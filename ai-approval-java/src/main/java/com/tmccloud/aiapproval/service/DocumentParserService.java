package com.tmccloud.aiapproval.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 文档解析服务
 * 支持 PDF、Word、Excel、PPT、TXT 等多种格式
 */
@Service
public class DocumentParserService {

    /**
     * 解析文档文件
     */
    public List<Document> parseDocument(MultipartFile file) throws IOException {
        // 保存临时文件
        Path tempFile = Files.createTempFile("doc_", getFileExtension(file.getOriginalFilename()));
        file.transferTo(tempFile.toFile());

        try {
            return parseDocument(tempFile.toFile(), file.getContentType());
        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 解析文档文件
     */
    public List<Document> parseDocument(File file, String contentType) {
        String fileName = file.getName();

        // PDF 文档
        if (contentType != null && contentType.contains("pdf")) {
            return parsePdfDocument(file);
        }

        // Word、Excel、PPT 等使用 Tika 解析
        if (contentType != null && (
                contentType.contains("word") ||
                        contentType.contains("document") ||
                        contentType.contains("spreadsheet") ||
                        contentType.contains("presentation") ||
                        contentType.contains("text/plain"))) {
            return parseWithTika(file);
        }

        // 默认使用 Tika 解析
        return parseWithTika(file);
    }

    /**
     * 解析 PDF 文档（带配置）
     */
    private List<Document> parsePdfDocument(File file) {
        // PDF 解析配置
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageBottomMargin(0)
                .withPageExtractedTextFormatter(
                        ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .withNumberOfBottomTextLinesToDelete(0)
                                .withNumberOfTopPagesToSkipBeforeDelete(0)
                                .build()
                )
                .withPagesPerDocument(10)  // 每10页作为一个 Document
                .build();
        Resource resource = new FileSystemResource(file);
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource, config);
        return pdfReader.get();
    }

    /**
     * 使用 Apache Tika 解析多种文档格式
     * 支持: Word, Excel, PowerPoint, HTML, TXT 等
     */
    private List<Document> parseWithTika(File file) {
        Resource resource = new FileSystemResource(file);
        TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
        return tikaReader.get();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) return ".tmp";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return ".tmp";
        return fileName.substring(lastDot);
    }
}