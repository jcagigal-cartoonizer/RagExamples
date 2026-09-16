package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;

public class InfoDispatchBlocks implements IGeneralBlocks {

    private static String[] BLOCKS = {
        "## `InfoDispatchRoute.kt`",
        "## `InfoDispatchScreen.kt`",
        "## `InfoDispatchComposeViewModel.kt`",
        "## `InfoDispatchUiState.kt`",
        "## `InfoDispatchUiEvent.kt`",
        "## `InfoDispatchUiEffect.kt`",
        "## `InfoDispatchButtonsState.kt`",
        "## `CustomButtonStyles.kt`",
        "## `InfoDispatchButtons.kt`",
        "## `InfoDispatchDialogType.kt`",
        "## `InfoDispatchDialogHost.kt`",
        "## `InfoDispatchDialogStateHolder.kt`",
        "## Compose `CustomDialog`",
    };
    private static String[] FILES = {
        "./generated-files/compose/routes/" + PREFIX + "Route.kt",
        "./generated-files/ui/screen/" + PREFIX + "Screen.kt",
        "./generated-files/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "UiState.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "UiEvent.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "UiEffect.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "ButtonsState.kt",
        "./generated-files/ui/screen/state/CustomButtonStyles.kt",        
        "./generated-files/ui/screen/state/" + PREFIX + "Buttons.kt",        
        "./generated-files/ui/screen/state/" + PREFIX + "DialogType.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "DialogHost.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "DialogStateHolder.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "CustomDialog.kt",
    };
    private static String[] PACKAGES = {
        "ifac.td.taxi.ui.routes",
        "ifac.td.taxi.ui.screen",
        "ifac.td.taxi.compose.viewmodel",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.components",
    };

    @Override
    public String getEndTag() {
        return "# 9) ";
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
