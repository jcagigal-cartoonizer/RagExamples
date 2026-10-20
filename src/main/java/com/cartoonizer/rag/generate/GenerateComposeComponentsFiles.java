/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cartoonizer.rag.generate;

import com.cartoonizer.rag.answers.GeneralAnswerProcessor;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.ANSWER_FOLDER;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.PROCESSED_FOLDER;

/**
 *
 * @author cagi
 */
public class GenerateComposeComponentsFiles {
    public static void main(String[] args) {
            LAYOUT = "custom_dialog.xml";
            PREFIX = "Shared";
            FILE_NAME = "SharedCommonDialog.txt";
            String answerPath = ANSWER_FOLDER + "/" + FILE_NAME;
            String processedAnswerPath = PROCESSED_FOLDER + "/processed-" + FILE_NAME;
            GeneralAnswerProcessor answerProcessor = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
            answerProcessor.load();
            
//            System.out.println("GenerateFiles from " + processedAnswerPath);
            GenerateComposeFiles.GenerateFiles reader = new GenerateComposeFiles.GenerateFiles(answerProcessor, processedAnswerPath, PREFIX);
            reader.load();
    }
    
}
