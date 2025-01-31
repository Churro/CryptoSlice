package at.tugraz.iaik.cryptoslice.analysis.preprocessing;

import at.tugraz.iaik.cryptoslice.analysis.Analysis;
import at.tugraz.iaik.cryptoslice.analysis.AnalysisException;
import at.tugraz.iaik.cryptoslice.analysis.Step;
import at.tugraz.iaik.cryptoslice.application.Application;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Unpacks and decodes the .apk file
public class ExtractApkStep extends Step {
  public ExtractApkStep(boolean enabled) {
    this.name = "Extract .apk";
    this.enabled = enabled;
  }

  public boolean doProcessing(Analysis analysis) throws AnalysisException {
    Application app = analysis.getApp();

    File apk = app.getApkFile();
    File apkContentDir = app.getBytecodeApkDirectory();

    try {
      LOGGER.info("Extracting content to " + apkContentDir.getAbsolutePath());
      extractApk(apk, apkContentDir);
      LOGGER.info("Decoding extracted content to " + apkContentDir.getAbsolutePath());
    } catch (Exception e1) {
      throw new AnalysisException(e1);
    }

    return true;
  }

  private static void extractApk(File archive, File dest) throws IOException {
    Path destPath = dest.toPath();

    try (ZipFile zipFile = new ZipFile(archive, ZipFile.OPEN_READ)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        Path entryPath = destPath.resolve(entry.getName());
        if (!entryPath.normalize().startsWith(dest.toPath())) {
          throw new IOException("Zip entry attempted path traversal");
        }

        if (entry.isDirectory()) {
          Files.createDirectories(entryPath);
        } else {
          Files.createDirectories(entryPath.getParent());

          try (InputStream in = zipFile.getInputStream(entry)) {
            try (OutputStream out = new FileOutputStream(entryPath.toFile())) {
              IOUtils.copy(in, out);
            }
          }
        }
      }
    }
  }
}