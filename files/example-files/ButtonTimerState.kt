package ifac.td.taxi.ui.custom.button

data class ButtonTimerState(
    val maxSeconds: Long,
    val remainingSeconds: Long,
    val isActive: Boolean,
    val startTime: Long,
    val buttonType: ButtonType? = null
)
