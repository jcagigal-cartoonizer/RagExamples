// ## 1) Compose theme object -> SmartTDTheme.kt

This example models the styling values from your XML as a Compose `ColorScheme`-like object plus typography helpers. Since the XML references resources (`@color/...`, `@dimen/...`, `@font/...`), I’ll represent them with Compose constants and placeholders where needed.

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object SmartTDTheme {
    object Colors {
        val White = Color(0xFFFFFFFF)
        val Black = Color(0xFF000000)
        val Orange = Color(0xFFFF9800)
        val DisabledWhite = Color(0x80FFFFFF)
        val Transparent = Color.Transparent
        val DarkBackground = Color(0xFF121212)
    }

    object Typography {
        val MontserratMedium = FontFamily.Default // replace with your montserrat_medium font if available

        val TextSizeSmall: TextUnit = 12.sp
        val TextSizeMedium: TextUnit = 14.sp
        val TextSizeLarge: TextUnit = 16.sp
        val TextSizeLargeConf: TextUnit = 18.sp
        val TextSizeTopBar: TextUnit = 20.sp
    }
}


// ## 2) Modifier extensions for the XML styles -> CustomModifiers

These extensions map the style properties to Compose modifiers.  
Some XML attributes do not have a direct `Modifier` equivalent (for example `textAllCaps`, `fontFamily`, `textSize`, `textColor`), so those are usually applied on the `Text` composable itself. I’ll include them in helper modifiers where possible and mention where composable parameters should be used.

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.smartTdAboutTextViewStyle(): Modifier = this
    .background(SmartTDTheme.Colors.Transparent)

fun Modifier.smartTdCustomFloatingButtonStyle(): Modifier = this
    .background(SmartTDTheme.Colors.Black) // replace with drawable-like background if needed

fun Modifier.smartTdPreferencesButtonStyle(): Modifier = this
    .padding(all = 2.dp)
    .background(SmartTDTheme.Colors.DisabledWhite)

fun Modifier.smartTdPreferencesButtonLogsStyle(): Modifier = this
    .padding(all = 2.dp)
    .background(SmartTDTheme.Colors.Orange)

fun Modifier.smartTdTextViewBlackOrangeStyle(): Modifier = this
    .background(SmartTDTheme.Colors.Orange)

fun Modifier.smartTdTopBarTextViewStyle(): Modifier = this
    .background(SmartTDTheme.Colors.White)


// ## 3) Compose equivalents for common TextView style behavior

These are not `Modifier` extensions, but they are essential to reproduce the XML styles correctly:

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun SmartTDText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SmartTDTheme.Colors.White,
    fontSize: TextUnit = SmartTDTheme.Typography.TextSizeMedium,
    fontFamily: FontFamily = SmartTDTheme.Typography.MontserratMedium,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    allCaps: Boolean = false
) {
    Text(
        text = if (allCaps) text.uppercase() else text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}


// ## 4) Compose implementation of the provided layout XML

Your XML layout is essentially:

- A full-screen `ConstraintLayout`
- A top centered `TextView`
- A bottom section with a black background and 2x2 grid-like arrangement
- Five custom buttons; in XML one cell seems duplicated in the snippet, but the intended layout is clearly a tiled button area

In Compose, a `LazyVerticalGrid` or `Column` + `Row` would be natural, but to stay close to the XML layout, I’ll implement it using `ConstraintLayout` + `Box` + `Column`/`Row`.

### Button model

enum class CustomFunction {
    SHORT_BREAK,
    CANCEL,
    PETICION_VOZ,
    MENSAJES_CENTRAL,
    INFORMACION
}

### Reusable button composable -> CustomButton.kt

This is a placeholder for your `CustomButton` view:

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

@Composable
fun CustomButton(
    function: CustomFunction,
    modifier: Modifier = Modifier,
    onClick: (CustomFunction) -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(SmartTDTheme.Colors.DarkBackground)
            .clickable { onClick(function) }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = function.name,
            color = SmartTDTheme.Colors.White,
            fontSize = SmartTDTheme.Typography.TextSizeSmall,
            fontFamily = SmartTDTheme.Typography.MontserratMedium
        )
    }
}

### Main screen composable -> FragmentContactCentral.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun SmartTDMainScreen(
    modifier: Modifier = Modifier,
    title: String = "BTN CENTRAL",
    onButtonClick: (CustomFunction) -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val (topLabel, menuContainer) = createRefs()

        // Top centered label
        SmartTDText(
            text = title,
            modifier = Modifier.constrainAs(topLabel) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(menuContainer.top)
                width = Dimension.wrapContent
            },
            color = SmartTDTheme.Colors.White,
            fontSize = SmartTDTheme.Typography.TextSizeMedium,
            allCaps = true,
            maxLines = 1
        )

        // Bottom grid container
        Box(
            modifier = Modifier
                .constrainAs(menuContainer) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    // 3:2 ratio roughly matches the XML
                    height = Dimension.ratio("3:2")
                }
                .background(SmartTDTheme.Colors.Black)
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CustomButton(
                        function = CustomFunction.SHORT_BREAK,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = onButtonClick
                    )
                    CustomButton(
                        function = CustomFunction.PETICION_VOZ,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = onButtonClick
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CustomButton(
                        function = CustomFunction.MENSAJES_CENTRAL,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = onButtonClick
                    )
                    CustomButton(
                        function = CustomFunction.INFORMACION,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = onButtonClick
                    )
                }
            }
        }
    }
}


