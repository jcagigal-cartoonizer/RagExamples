package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.SUFFIX;

public class ComponentsAnswerProcessor {

    public static void main(String[] args) {
        LAYOUT = "custom_dialog.xml";
        PREFIX = "Shared";
        FILE_NAME = "SharedCommonDialog.txt";
        String answerPath = "./output-files/" + FILE_NAME;
        String processedAnswerPath = "./processed-files/processed-" + FILE_NAME;
        ReadFile reader = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
        reader.load();
    }
}
