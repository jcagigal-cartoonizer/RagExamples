package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;

public class DashboardBlocks implements IGeneralBlocks {
// ## `DashboardScreen.kt`
// ## `DashboardViewModel.kt`
// ## `DashboardButtonsState.kt`
// ## `DashboardButtonStyles.kt`
// # 5) Dashboard header composable
// ## `CustomDialog.kt` (Compose version)
// ## `DashboardEffectCollector.kt`
    @Override
    public String getEndTag() {
        return "# 8) ";
    } 
    private static String[] BLOCKS = {
        "# `DashboardScreen.kt`",
        "# `DashboardViewModel.kt`",
        "# `DashboardButtonsState.kt`",
        "# `DashboardButtonStyles.kt`",
        "# 5) Dashboard header composable",
        "# `CustomDialog.kt`",
        "# `DashboardEffectCollector.kt`",
    };
    private static String[] FILES = {
        "./generated-files/ui/screen/" + PREFIX + "Screen.kt",
        "./generated-files/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "ButtonsState.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "ButtonStyles.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "Header.kt",
        "./generated-files/ui/screen/components/" + PREFIX + "CustomDialog.kt",
        "./generated-files/ui/screen/state/" + PREFIX + "DashboardEffectCollector.kt",
    };
    private static String[] PACKAGES = {
        "ifac.td.taxi.ui.screen",
        "ifac.td.taxi.compose.viewmodel",
        "ifac.td.taxi.ui.screen.state",
        "ifac.td.taxi.ui.screen.components",
        "ifac.td.taxi.ui.screen.components",        
        "ifac.td.taxi.ui.screen.components",        
        "ifac.td.taxi.ui.screen.state",
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
