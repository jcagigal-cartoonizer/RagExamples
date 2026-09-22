/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FILE_NAME;

/**
 *
 * @author cagi
 */
public class GenerateComposeComponentsFiles {
    public static void main(String[] args) {
            LAYOUT = "custom_dialog.xml";
            PREFIX = "Shared";
            FILE_NAME = "SharedCommonDialog.txt";
            String answerPath = "./output-files/" + FILE_NAME;
            String processedAnswerPath = "./processed-files/processed-" + FILE_NAME;
            GeneralAnswerProcessor answerProcessor = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
            answerProcessor.load();
            
//            System.out.println("GenerateFiles from " + processedAnswerPath);
            GenerateComposeFiles.GenerateFiles reader = new GenerateComposeFiles.GenerateFiles(answerProcessor, processedAnswerPath, PREFIX);
            reader.load();
    }
    
}
