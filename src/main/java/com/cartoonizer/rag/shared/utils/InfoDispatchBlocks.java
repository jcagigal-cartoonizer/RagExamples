package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;

public class InfoDispatchBlocks implements IGeneralBlocks {

    private static String[] BLOCKS = {
        "# 1) Compose UI state, events, and effects",
        "# 2) Full `InfoDispatchButtonsState` with button coloring/visibility matching fragment behavior",
        "# 3) Styling helpers to mirror `CustomButton`",
        "# 4) Compose ViewModel with `UiState + UiEvent + SharedFlow<UiEffect>`",
        "# 5) Compose screen preserving navigation",
        "# 6) Main content composable",
        "# 7) Buttons row composable",
        "# 8) Compose custom button",
        "# 9) Compose `CustomDialog` implementation",
        "# 10) Tabs UI",
    };
    private static String[] FILES = {
        "./generated-files/ui/screen/state/" + PREFIX + "UiState.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "ButtonsState.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "ComposeCustomButtonDefaults.kt",
        "./generated-files/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt",
        "./generated-files/ui/screen/" + PREFIX + "Screen.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "Content.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "ButtonsRow.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "ActionButton.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "CustomDialog.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "Tabs.kt",
    };
    private static String[] PACKAGES = {
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.compose.viewmodel",
        "ifac.td.taxi.ui.screen",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",
    };

    @Override
    public String getEndTag() {
        return "# Notes on preserving behavior";
    } 
    @Override
    public String[] getBLOCKS() {
        return BLOCKS;
    }

    @Override
    public String[] getFILES() {
        return InfoDispatchBlocks.FILES;
    }

    @Override
    public String[] getPACKAGES() {
        return InfoDispatchBlocks.PACKAGES;
    }

}
