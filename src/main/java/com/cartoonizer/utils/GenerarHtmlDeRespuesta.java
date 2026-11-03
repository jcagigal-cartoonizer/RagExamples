package com.cartoonizer.utils;

import com.cartoonizer.rag.utils.Utils;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author cagi
 */
public class GenerarHtmlDeRespuesta {
    public static void main(String[] args) {
        String path = "/Cagi/Portfolio/RagUtils/RagExamples/answer-files/answer-murakami.txt";
        String plantilla = 
                """
                           
                """;
        GenerarHtmlDeRespuesta gen = new GenerarHtmlDeRespuesta(path, plantilla);
        gen.print();
    }
    private ArrayList<String> lines;
    public GenerarHtmlDeRespuesta(String path, String plantilla) {
        String contents = Utils.readFullFile(path);
        String[] parts = contents.split("\n");
        lines = new ArrayList<>();
        for (int i = 0; parts != null && i < parts.length; i++) {
            lines.add(parts[i]);
        }
    }
    public void print() {
        for (String line : lines) {
// - **1Q84 (Libros 1 y 2) (Bolsillo)** — 14,95 €
            String[] tmp = line.replaceAll("- ", "").replaceAll(" — ", "").trim().split(Pattern.quote("**"));
            System.out.println("[" + tmp[1] + "] [" + tmp[2] + "]");
        }
    }
}
