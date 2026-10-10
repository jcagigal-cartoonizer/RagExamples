package com.cartoonizer.rag.utils;

import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ANSWER_FOLDER_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.GENERATED_FILES_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PROCESSED_FOLDER_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.SUFFIX;

public class ComponentsAnswerProcessor {

    public static void main(String[] args) {
        LAYOUT = "custom_dialog.xml";
        PREFIX = "Shared";
        FILE_NAME = "SharedCommonDialog.txt";
        String answerPath = ANSWER_FOLDER_WITH_ASSISTANT + "/" + FILE_NAME;
        String processedAnswerPath = PROCESSED_FOLDER_WITH_ASSISTANT + "/processed-" + FILE_NAME;
        ReadFile reader = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
        reader.load();
    }
}
