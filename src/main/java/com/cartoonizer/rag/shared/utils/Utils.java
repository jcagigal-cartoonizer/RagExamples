package com.cartoonizer.rag.shared.utils;

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
}
