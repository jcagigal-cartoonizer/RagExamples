package com.cartoonizer.utils;

import com.cartoonizer.rag.utils.ReadFile;
import java.io.PrintWriter;

public class Utils {
    public static String readFullFile(String path) {
        StringBuilder sb = new StringBuilder();
        ReadFile reader = new ReadFile(path) {
            @Override
            public void processLine(String line) {
                sb.append(line).append("\n");
            }
        };
        reader.load();
        return sb.toString();
    }  

    public static void saveFile(String path, String text) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path);
            writer.println(text);
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
