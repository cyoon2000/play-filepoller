package utils;

import java.io.File;
import java.lang.ClassFormatError;
import java.util.Comparator;

public class FileComparable implements Comparator<File>{

    @Override
    public int compare(File f1, File f2) {
        return (f1.lastModified() < f2.lastModified() ? -1 : (f1.lastModified() == f2.lastModified() ? 0 : 1));
    }
}