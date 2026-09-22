package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.shared.utils.FragmentQuestionsProcessor;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIXES;

public class SimpleExampleOpenAi {

//    public static final String PREFIX = "InfoDispatch";
//    public static final String LAYOUT = "fragment_info_dispatch.xml";
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if (!PREFIX.equals(ONLY_THIS)) {
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
