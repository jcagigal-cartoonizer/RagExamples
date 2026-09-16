package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;

public class DashboardBlocks implements IGeneralBlocks {
// ## 1) Dashboard UI state and effects
// ## 2) DashboardButtonsState with exact visibility/color behavior
// ### Styling helpers closer to `CustomButton`
// ## 3) Compose-friendly ViewModel with `UiState + UiEvent + SharedFlow<DashboardUiEffect>`
// ## 4) Compose `DashboardScreen`
// ## 5) Composable UI pieces
// ### Header
// ## Buttons row
// ### Zone lists
// ## 6) Compose custom button styling helpers
// ## 7) Compose `CustomDialog` based on your XML dialog pattern
    @Override
    public String getEndTag() {
        return "# 8) Navigation preservation";
    } 
    private static String[] BLOCKS = {
        "# 1) Dashboard UI state and effects",
        "# 2) DashboardButtonsState with exact visibility/color behavior",
        "# Styling helpers closer to `CustomButton`",
        "# 3) Compose-friendly ViewModel with `UiState + UiEvent + SharedFlow<DashboardUiEffect>`",
        "# 4) Compose `DashboardScreen`",
        "## Header",
        "# Buttons row",
        "## Zone lists",
        "# 6) Compose custom button styling helpers",
        "# 7) Compose `CustomDialog` based on your XML dialog pattern",
    };
    private static String[] FILES = {
        "./generated-files/ui/screen/state/" + PREFIX + "UiState.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "ButtonsState.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "ButtonColors.kt",
        "./generated-files/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt",        
        "./generated-files/ui/screen/" + PREFIX + "Screen.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "Header.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "ButtonRow.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "ZonesList.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "CustomButton.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "CustomDialog.kt",
    };
    private static String[] PACKAGES = {
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.compose.viewmodel",
        "ifac.td.taxi.ui.screen",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",
    };

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
