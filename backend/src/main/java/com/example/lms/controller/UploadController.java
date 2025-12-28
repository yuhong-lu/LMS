package com.example.lms.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
  private final Path uploadPath;
  private final Set<String> allowedExtensions;
  private final Set<String> allowedContentTypes;

  public UploadController(
      @Value("${app.upload.dir}") String uploadDir,
      @Value("${app.upload.allowed-extensions:}") String allowedExtensions,
      @Value("${app.upload.allowed-content-types:}") String allowedContentTypes) {
    this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    this.allowedExtensions = parseList(allowedExtensions);
    this.allowedContentTypes = parseList(allowedContentTypes);
  }

  @PostMapping
  public ResponseEntity<Map<String, String>> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "courseId", required = false) Long courseId)
      throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    Path targetDir = courseId == null ? uploadPath : uploadPath.resolve("courses").resolve(courseId.toString());
    Files.createDirectories(targetDir);

    String original = StringUtils.cleanPath(file.getOriginalFilename());
    String ext = "";
    int dot = original.lastIndexOf('.');
    if (dot > -1) {
      ext = original.substring(dot);
    }
    String cleanExt = ext.replace(".", "").toLowerCase();
    String contentType = file.getContentType();
    if (!isAllowed(cleanExt, contentType)) {
      throw new IllegalArgumentException("File type not allowed");
    }

    String filename = UUID.randomUUID() + ext;
    Path target = targetDir.resolve(filename);

    Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

    Map<String, String> response = new HashMap<>();
    String urlPath =
        courseId == null ? "/uploads/" + filename : "/uploads/courses/" + courseId + "/" + filename;
    response.put("url", urlPath);
    response.put("name", original);
    return ResponseEntity.ok(response);
  }

  private boolean isAllowed(String ext, String contentType) {
    boolean extAllowed = allowedExtensions.isEmpty() || allowedExtensions.contains(ext);
    if (contentType == null || contentType.isBlank()) {
      return extAllowed;
    }
    boolean typeAllowed = allowedContentTypes.isEmpty() || allowedContentTypes.contains(contentType);
    return extAllowed || typeAllowed;
  }

  private Set<String> parseList(String raw) {
    if (raw == null || raw.isBlank()) {
      return Set.of();
    }
    Set<String> result = new HashSet<>();
    Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(String::toLowerCase)
        .forEach(result::add);
    return result;
  }
}
