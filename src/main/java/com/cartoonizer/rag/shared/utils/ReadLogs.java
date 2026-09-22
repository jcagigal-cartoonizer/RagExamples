package com.cartoonizer.rag.shared.utils;

public class ReadLogs extends ReadFile {
    public static void main(String[] args) {
        String path = "/Cagi/Portfolio/RagUtils/RagExamples/log.txt";
        ReadLogs reader = new ReadLogs(path);
        reader.load();
    }

    public ReadLogs(String pathOrigen) {
        super(pathOrigen);
    }

    @Override
    public void processLine(String line) {
//        if (line.contains("OPEN ") || line.contains("OPEN:") || line.contains("CLOSE ") || line.contains("CLOSED ") || line.contains("CLOSED:") ||
//                line.contains("isEndTag") || line.contains("DEBUG!!!") || line.contains("*** processBlocks call openFile")) {
        if (line.contains("MessageDetail")) {
            System.out.println(line);
        }
    }
    
}
