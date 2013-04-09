package utils;

import models.FileEntry;
import utils.FileComparable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.Long;
import java.lang.String;
import java.lang.System;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Checksum;
import java.util.zip.CheckedInputStream;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


public class JavaUtilities {

  public static List<File> getFilesToPoll(String rootDir) {

    List<File> files = new ArrayList<File>();

    // file extention filter = none, recursive = true
    for (File f : FileUtils.listFiles(new File(rootDir), null, true)) {
      if ( f.isFile() && FileEntry.findByName(f.getAbsolutePath()) )
        files.add(f);
    }
    Collections.sort(files, new FileComparable());

    return files;
  }

  public static String getSignature(File f) {
    return String.valueOf(getCRC32(f));
  }

  public static long getCRC32(File f) {
    try {
        Checksum checksum = new CRC32();
        byte [] bytes = IOUtils.toByteArray(new BufferedInputStream(new FileInputStream(f)));
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();

    } catch(Exception e) {
        e.printStackTrace();
        return 0;
    }
  }

}

