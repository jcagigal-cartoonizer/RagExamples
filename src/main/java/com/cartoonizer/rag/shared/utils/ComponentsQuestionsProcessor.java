package com.cartoonizer.rag.shared.utils;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.SUFFIX;

public class ComponentsQuestionsProcessor extends FragmentQuestionsProcessor {

    public static void main(String[] args) {
        LAYOUT = "custom_dialog.xml";
        PREFIX = "Shared";
        SUFFIX = "CommonDialog";
        new ComponentsQuestionsProcessor("./example-files").askQuestions();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
        }
    }

    public ComponentsQuestionsProcessor(String documentsPath) {
        super(documentsPath);
        fileToGenerate = PREFIX + SUFFIX;
        pathForViewClass = documentsPath  + "/" + "CustomDialog.kt";
        pathViewModel = documentsPath  + "/" + "CustomDialogViewModel.kt";
    }

    @Override
    protected String[] getQuestions(String savedViewModel, String savedViewClass, String xmlLayout) {
        String[] questions = {
            "1. provide a jetpack Compose composable that implements the following jetpack views class \n"
            + savedViewClass
            + "Use the following xml layout"
            + xmlLayout
            + "2. provide a Compose viewModel to be used by the composable based on the following Jetpack Views viewModel, exposing a Compose-friendly `UiState + UiEvent` architecture \n" + savedViewModel
            + "3. Use dialog state, lifecycle collection of state/events for dialog handling \n"
            + "4. Use state holders/data classes to fully replace the fragment button logic. \n"
            + "\nUse only android and jetpack compose references"

        };
        return questions;
    }

}
