package com.springforge.generator.application.steps;

import com.springforge.generator.domain.pipeline.GenerationContext;
import com.springforge.generator.domain.pipeline.PipelineStep;
import com.springforge.generator.domain.pipeline.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Order(4)
public class PostProcessStep implements PipelineStep {

    private static final Logger log = LoggerFactory.getLogger(PostProcessStep.class);

    @Override
    public StepResult execute(GenerationContext context) {
        Path outputDir = context.getOutputDirectory();
        if (outputDir == null || !Files.exists(outputDir)) {
            return StepResult.failed("Output directory does not exist");
        }

        try {
            Path zipFile = outputDir.getParent().resolve(outputDir.getFileName().toString() + ".zip");
            createZip(outputDir, zipFile);
            deleteDirectory(outputDir);
            context.setOutputDirectory(zipFile);
            log.info("Project packaged as ZIP: {}", zipFile);
            return StepResult.ok();
        } catch (IOException e) {
            log.error("Post-processing failed", e);
            return StepResult.failed("ZIP packaging failed: " + e.getMessage());
        }
    }

    private void createZip(Path sourceDir, Path zipFile) throws IOException {
        try (OutputStream fos = Files.newOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = sourceDir.getParent().relativize(file);
                    zos.putNextEntry(new ZipEntry(relativePath.toString().replace("\\", "/")));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(sourceDir)) {
                        Path relativePath = sourceDir.getParent().relativize(dir);
                        zos.putNextEntry(new ZipEntry(relativePath.toString().replace("\\", "/") + "/"));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public String name() { return "post-process"; }
}
