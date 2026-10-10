/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cartoonizer.rag.utils;

import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ANSWER_FOLDER_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PROCESSED_FOLDER_WITH_ASSISTANT;

/**
 *
 * @author cagi
 */
public class GenerateComposeComponentsFiles {
    public static void main(String[] args) {
            LAYOUT = "custom_dialog.xml";
            PREFIX = "Shared";
            FILE_NAME = "SharedCommonDialog.txt";
            String answerPath = ANSWER_FOLDER_WITH_ASSISTANT + "/" + FILE_NAME;
            String processedAnswerPath = PROCESSED_FOLDER_WITH_ASSISTANT + "/processed-" + FILE_NAME;
            GeneralAnswerProcessor answerProcessor = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
            answerProcessor.load();
            
//            System.out.println("GenerateFiles from " + processedAnswerPath);
            GenerateComposeFiles.GenerateFiles reader = new GenerateComposeFiles.GenerateFiles(answerProcessor, processedAnswerPath, PREFIX);
            reader.load();
    }
    
}
