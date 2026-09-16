package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.LAYOUT;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.LAYOUTS;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIXES;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProcessAnswerFile {

    public static String FILE_NAME = PREFIX + "Fragment.txt";
    public static IGeneralBlocks iface;

    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            FragmentAnswerProcessor.processAll();
        }
    }

}
