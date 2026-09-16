package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;

public class ContactCentralBlocks implements IGeneralBlocks {
// # 1) Compose screen for `DispatchReceivedFragment`
// # 2) Compose-friendly ViewModel with `UiState + UiEvent + UiEffect`
// # 3) `UiState`, `UiEvent`, `UiEffect`, dialog state, button state
// # 4) Button styling helpers matching the custom button closer
// # 5) Compose buttons implementation replacing fragment logic

    private static String[] BLOCKS = {
        "# 1) Compose screen for ",
        "# 2) Compose content with full button logic",
        "# 3) Compose button matching custom button behavior",
        "# 4) Full UiState / UiEvent / UiEffect architecture",
        "## Effects",
        "## Events",
        "# 5) Compose ViewModel",
        "# 6) Exact button coloring/visibility helpers",
        "# 7) Compose CustomDialog equivalent",
    };
    private static String[] FILES = {
        "./generated-files/ui/screen/" + PREFIX + "Screen.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "Content.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "Button.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "ButtonsState.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "UiEffect.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "UiEvent.kt",        
        "./generated-files/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "ButtonColors.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "ComposeCustomDialog.kt",
    };
    private static String[] PACKAGES = {
        "ifac.td.taxi.ui.screen",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.compose.viewmodel",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
    };

    @Override
    public String getEndTag() {
        return "# 8) ";
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
