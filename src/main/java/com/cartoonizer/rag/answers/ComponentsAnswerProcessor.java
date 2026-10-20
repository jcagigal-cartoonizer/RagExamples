package com.cartoonizer.rag.answers;

import com.cartoonizer.rag.utils.ReadFile;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.ANSWER_FOLDER;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.GENERATED_FILES;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.PROCESSED_FOLDER;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.SUFFIX;

public class ComponentsAnswerProcessor {

    public static void main(String[] args) {
        LAYOUT = "custom_dialog.xml";
        PREFIX = "Shared";
        FILE_NAME = "SharedCommonDialog.txt";
        String answerPath = ANSWER_FOLDER + "/" + FILE_NAME;
        String processedAnswerPath = PROCESSED_FOLDER + "/processed-" + FILE_NAME;
        ReadFile reader = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
        reader.load();
    }
}
