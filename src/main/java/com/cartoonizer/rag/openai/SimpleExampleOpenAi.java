package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.utils.FragmentQuestionsProcessor;
import com.cartoonizer.rag.utils.GeneralAnswerProcessor;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.shouldSkip;

public class SimpleExampleOpenAi {

//    public static final String PREFIX = "InfoDispatch";
//    public static final String LAYOUT = "fragment_info_dispatch.xml";
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if (shouldSkip(i)) {
                continue;
            }
            new FragmentQuestionsProcessor("./example-files").askQuestions();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
            }
        }
    }

}
