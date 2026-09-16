package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;

public class GeneralBlocks implements IGeneralBlocks {
// # 1) Compose screen for `DispatchReceivedFragment`
// # 2) Compose-friendly ViewModel with `UiState + UiEvent + UiEffect`
// # 3) `UiState`, `UiEvent`, `UiEffect`, dialog state, button state
// # 4) Button styling helpers matching the custom button closer
// # 5) Compose buttons implementation replacing fragment logic

    private static String[] BLOCKS = {
        "# 1) Compose screen for ",
        "# 2) Compose-friendly ViewModel",
        "# 3) `UiState`, `UiEvent`, `UiEffect`, dialog state, button state",
        "# 4) Button styling helpers matching the custom button closer",
        "# 5) Compose buttons implementation replacing fragment logic",};
    private static String[] FILES = {
        "./generated-files/ui/screen/" + PREFIX + "Screen.kt",
        "./generated-files/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "DialogState.kt",
        "./generated-files/compose/routes/" + PREFIX + "UiState.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "Dialogs.kt",};
    private static String[] PACKAGES = {
        "ifac.td.taxi.ui.screen",
        "ifac.td.taxi.compose.viewmodel",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.components",};

    @Override
    public String getEndTag() {
        return "# 6) ";
    } 
    @Override
    public String[] getBLOCKS() {
        return BLOCKS;
    }

    @Override
    public String[] getFILES() {
        return FILES;
    }

    @Override
    public String[] getPACKAGES() {
        return PACKAGES;
    }

}
