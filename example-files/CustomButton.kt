package ifac.td.taxi.ui.custom.button

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView.ScaleType.CENTER_INSIDE
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ifac.td.taxi.R
import ifac.td.taxi.databinding.TestButtonBinding
import ifac.td.taxi.framework.util.Logs
import kotlin.math.abs

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    private var TAG = "CustomButton"
    private var savedOnClickListener: (() -> Unit)? = null
    var customFunctionValue: Int? = null

    private var binding: TestButtonBinding
    private var progressAnimator: ObjectAnimator? = null

    private var backgroundColor: BackgroundButtonColor? = null
    private var styleButton: StyleButton? = null

    private var lastClickTime = 0L
    private val debounceTime = 350L

    init {
        binding = TestButtonBinding.inflate(LayoutInflater.from(context), this, true)
        styleButton = StyleButton.ENABLE
        attrs?.let {
            val typedArray: TypedArray = context.obtainStyledAttributes(it, R.styleable.TestButton)

            // Get the value of customFunction attribute
            customFunctionValue = typedArray.getInt(R.styleable.TestButton_customFunction, 0)
            checkTextOnly(customFunctionValue)

            typedArray.recycle()
            setButtonType()
            setUpIconSize()

            setOnClickListener {
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime < debounceTime) {
                    // Ignorar click
                    Logs.d("CustomButton", "Click ignorado por debounce")
                    return@setOnClickListener
                }
                lastClickTime = currentTime

                val type = ButtonType.fromValue(customFunctionValue) ?: ButtonType.OTHERS

                if (styleButton == StyleButton.ENABLE) {
                    Logs.d("CustomButton", "${type.name}: onClick")
                    savedOnClickListener?.invoke()
                } else {
                    Logs.d("CustomButton", "${type.name}: not enabled")
                    Toast.makeText(
                            context,
                        context.getString(R.string.toast_action_cancelled), Toast.LENGTH_SHORT
                        ).show()
                }
            }
        }
    }

    private fun setUpIconSize() {
        binding.ivIcon.scaleType = CENTER_INSIDE
        binding.ivIcon.scaleX = 0.9F
        binding.ivIcon.scaleY = 0.9F
    }

    enum class StyleButton {
        ENABLE,
        DISABLE,
        LOADING,
    }

    enum class BackgroundButtonColor {
        GREEN,
        RED,
        BLUE,
        YELLOW,
        ORANGE,
        DEFAULT,
        BLACK,
        TRANSPARENT
    }

    fun changeText(text: String) {
        binding.txButtonText.text = text
    }

    fun changeTvOnlyText(text: String) {
        binding.tvOnlyText.text = text
    }

    private fun updateBorderPadding() {
        val marginPx = if (binding.layoutTimerButton.isVisible) {
            Logs.d(TAG, "updateBorderPadding: layoutTimerButton is visible")
            // convertir 8dp a px
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.toFloat(), resources.displayMetrics).toInt()
        } else {
            Logs.d(TAG, "updateBorderPadding: layoutTimerButton is not visible")
            0
        }

        val params = binding.centerButton.layoutParams as LayoutParams
        params.setMargins(marginPx, marginPx, marginPx, marginPx)
        binding.centerButton.layoutParams = params

        Logs.d(TAG, "updateBorderPadding: centerButton margins updated")
    }


    private var continuousAnimator: ValueAnimator? = null
    private var countdownEndTime = 0L

    fun showTimerButton(maxSeconds: Long, leftSeconds: Long, countdownStartTime: Long = 0L) {
        binding.layoutTimerButton.let { layout ->
            layout.visibility = View.VISIBLE
            updateBorderPadding()

            val drawable = layout.drawable ?: return

            // When the countdown should end
            val newEndTime = countdownStartTime + (maxSeconds * 1000L)

            // If this is a new countdown or the end time changed significantly
            if (abs(newEndTime - countdownEndTime) > 2000 || continuousAnimator == null) {
                countdownEndTime = newEndTime
                startContinuousAnimation(drawable, maxSeconds * 1000L, countdownStartTime)
            }
        }
    }

    private fun startContinuousAnimation(drawable: Drawable, totalDurationMs: Long, startTime: Long) {
        continuousAnimator?.cancel()

        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        val remainingTime = (totalDurationMs - elapsedTime).coerceAtLeast(0L)

        if (remainingTime <= 0) {
            drawable.level = 10000
            return
        }


        val startProgress = (elapsedTime.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

        continuousAnimator = ValueAnimator.ofFloat(startProgress, 1f).apply {
            duration = remainingTime
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                val level = (progress * 10000).toInt().coerceIn(0, 10000)
                drawable.level = level
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    continuousAnimator = null
                    drawable.level = 10000
                }

                override fun onAnimationCancel(animation: Animator) {
                    continuousAnimator = null
                }
            })

            start()
        }
    }

    fun stopTimer() {
        continuousAnimator?.cancel()
        continuousAnimator = null
    }

    fun hideTimerButton() {
        stopTimer()
        binding.layoutTimerButton.visibility = View.GONE
    }

    fun changeBackground(bg: BackgroundButtonColor) {
        backgroundColor = bg
        if (styleButton == StyleButton.ENABLE) {
            when (bg) {
                BackgroundButtonColor.GREEN -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_button_green)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_button_green)
                }

                BackgroundButtonColor.RED -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_button_red)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_button_red)
                }

                BackgroundButtonColor.BLUE -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_button_blue)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_button_blue)
                }

                BackgroundButtonColor.YELLOW -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_button_yellow)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_button_yellow)
                }

                BackgroundButtonColor.ORANGE -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_button_orange)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_button_orange)
                }

                BackgroundButtonColor.DEFAULT -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_button_blue)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_button_blue)
                }

                BackgroundButtonColor.BLACK -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_dialog_black)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_dialog_black)
                }

                BackgroundButtonColor.TRANSPARENT -> {
                    binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_dialog_transparent)
                    binding.centerButton.background = getDrawable(context, R.drawable.background_dialog_transparent)
                }
            }
        } else {
            if (bg == BackgroundButtonColor.BLACK) {
                binding.rootLayoutButton.background = getDrawable(context, R.drawable.background_dialog_black)
                binding.centerButton.background = getDrawable(context, R.drawable.background_dialog_black)

            }
        }
    }

    fun setButtonStyle(type: StyleButton) {
        styleButton = type
        binding.apply {
            when (type) {
                StyleButton.ENABLE -> {
                    ivIcon.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    txButtonText.setTextColor(ContextCompat.getColor(context, R.color.white))
                    ivIcon.setColorFilter(
                        ContextCompat.getColor(context, R.color.white),
                        android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                    backgroundColor?.let { bg ->
                        changeBackground(bg)
                    } ?: run {
                        changeBackground(BackgroundButtonColor.BLUE)
                    }
                }

                StyleButton.DISABLE -> {
                    ivIcon.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    // assign grey bg directly so it doesn't override previous one if ENABLED later
                    binding.rootLayoutButton?.background = getDrawable(context, R.drawable.background_button_grey)
                    binding.centerButton?.background = getDrawable(context, R.drawable.background_button_grey)
                    txButtonText.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.disabled_grey
                        )
                    )
                    ivIcon.setColorFilter(
                        ContextCompat.getColor(context, R.color.disabled_white),
                        android.graphics.PorterDuff.Mode.MULTIPLY
                    )
                }

                StyleButton.LOADING -> {
                    ivIcon.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }


    fun setButtonType(value: Int? = customFunctionValue) {
        binding.apply {
            ivIcon.visibility = View.VISIBLE
            when (ButtonType.fromValue(value)) {
                ButtonType.ACCEPT -> {
                    ivIcon.setImageResource(R.drawable.baseline_check_192)
                    txButtonText.text = context.getString(R.string.btn_accept).uppercase()
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.CANCEL -> {
                    txButtonText.text = context.getString(R.string.btn_cancel).uppercase()
                    ivIcon.setImageResource(R.drawable.cerrar)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.INICIO -> {
                    txButtonText.text = context.getString(R.string.btn_start).uppercase()
                    ivIcon.setImageResource(R.drawable.iniciocentral)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.AJUSTES -> {
                    txButtonText.text = context.getString(R.string.btn_settings).uppercase()
                    ivIcon.setImageResource(R.drawable.configuracion)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.AJUSTES_WELCOME -> {
                    txButtonText.text = context.getString(R.string.welcome_settings).uppercase()
                    ivIcon.setImageResource(R.drawable.configuracion)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.CONFIGURACION -> {
                    txButtonText.text = context.getString(R.string.btn_configuration).uppercase()
                    ivIcon.setImageResource(R.drawable.server)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.PREFERENCIAS -> {
                    txButtonText.text = context.getString(R.string.btn_preferences).uppercase()
                    ivIcon.setImageResource(R.drawable.configuracion)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.ESTADISTICAS -> {
                    txButtonText.text = context.getString(R.string.btn_statistics).uppercase()
                    ivIcon.setImageResource(R.drawable.estadisticas)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.TURNOS -> {
                    txButtonText.text = context.getString(R.string.btn_shifts).uppercase()
                    ivIcon.setImageResource(R.drawable.cierreautoact_white)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.PARCIALES -> {
                    txButtonText.text = context.getString(R.string.btn_partials).uppercase()
                    ivIcon.setImageResource(R.drawable.totalizadores)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.PARCIALES_CIERRE -> {
                    txButtonText.text = context.getString(R.string.btn_partials_closing).uppercase()
                    ivIcon.setImageResource(R.drawable.contado)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.LOGIN -> {
                    txButtonText.text = context.getString(R.string.btn_login).uppercase()
                    ivIcon.setImageResource(R.drawable.iniciocentral)
                    changeBackground(BackgroundButtonColor.GREEN)

                }

                ButtonType.SALIR -> {
                    txButtonText.text = context.getString(R.string.btn_exit).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_exit_to_app_192)
                    changeBackground(BackgroundButtonColor.RED)

                }

                ButtonType.IMPRIMIR -> {
                    txButtonText.text = context.getString(R.string.btn_print).uppercase()
                    ivIcon.setImageResource(R.drawable.imprimir)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.EXPORTAR -> {
                    txButtonText.text = context.getString(R.string.btn_export).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_arrow_circle_down_192)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.BLUETOOTH -> {
                    txButtonText.text = context.getString(R.string.btn_bluetooth).uppercase()
                    ivIcon.setImageResource(R.drawable.taximetro)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.BUSCAR -> {
                    txButtonText.text = context.getString(R.string.btn_search).uppercase()
                    ivIcon.setImageResource(R.drawable.detalleactivado)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.GPS -> {
                    txButtonText.text = context.getString(R.string.btn_gps).uppercase()
                    ivIcon.setImageResource(R.drawable.navegacion)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.ACERCADE -> {
                    txButtonText.text = context.getString(R.string.btn_about).uppercase()
                    ivIcon.setImageResource(R.drawable.informacion)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.UBICACION -> {
                    txButtonText.text = context.getString(R.string.btn_location).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)

                }

                ButtonType.SOON_TO_CLEAR -> {
                    txButtonText.text = context.getString(R.string.btn_soon_to_clear).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)

                }

                ButtonType.RECIBO -> {
                    txButtonText.text = context.getString(R.string.btn_receipt).uppercase()
                    ivIcon.setImageResource(R.drawable.imprimir)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.PENDIENTES -> {
                    txButtonText.text = context.getString(R.string.btn_pending).uppercase()
                    ivIcon.setImageResource(R.drawable.pendientes)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.UBICAR_PARADA -> {
                    txButtonText.text = context.getString(R.string.btn_locate_stop).uppercase()
                    ivIcon.setImageResource(R.drawable.ubparada)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.MENSAJES -> {
                    txButtonText.text = context.getString(R.string.btn_messages).uppercase()
                    ivIcon.setImageResource(R.drawable.recientes)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.MENSAJES_CENTRAL -> {
                    txButtonText.text = context.getString(R.string.btn_messages).uppercase()
                    ivIcon.setImageResource(R.drawable.chat)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.LUMINOSO -> {
                    txButtonText.text = context.getString(R.string.btn_lights).uppercase()
                    ivIcon.setImageResource(R.drawable.lumact)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.LUCES -> {
                    txButtonText.text = context.getString(R.string.btn_lights).uppercase()
                    ivIcon.setImageResource(R.drawable.lumact)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.CENTRAL -> {
                    txButtonText.text = context.getString(R.string.btn_central).uppercase()
                    ivIcon.setImageResource(R.drawable.central)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.PETICION_VOZ -> {
                    txButtonText.text = context.getString(R.string.btn_voice_request).uppercase()
                    ivIcon.setImageResource(R.drawable.central)
                    changeBackground(BackgroundButtonColor.BLUE)
                }


                ButtonType.INFORMACION -> {
                    txButtonText.text = context.getString(R.string.btn_information).uppercase()
                    ivIcon.setImageResource(R.drawable.informacion)
                    changeBackground(BackgroundButtonColor.BLUE)
                }


                ButtonType.CONSULTA_UB -> {
                    txButtonText.text = context.getString(R.string.btn_check_location).uppercase()
                    ivIcon.setImageResource(R.drawable.ubconsulta)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.PREVIOS -> {
                    txButtonText.text = context.getString(R.string.btn_previous).uppercase()
                    ivIcon.setImageResource(R.drawable.imprimir_2)
                    changeBackground(BackgroundButtonColor.GREEN)

                }

                ButtonType.FACTURA -> {
                    txButtonText.text = context.getString(R.string.btn_invoice).uppercase()
                    ivIcon.setImageResource(R.drawable.generate_invoice)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.FOTO -> {
                    txButtonText.text = context.getString(R.string.btn_photo).uppercase()
                    ivIcon.setImageResource(R.drawable.camera)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.TARJETA -> {
                    txButtonText.text = context.getString(R.string.tarjeta_payment).uppercase()
                    ivIcon.setImageResource(R.drawable.tarjeta)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.ABONADO -> {
                    txButtonText.text = context.getString(R.string.srtAbonado).uppercase()
                    ivIcon.setImageResource(R.drawable.abonado)
                    changeBackground(BackgroundButtonColor.RED)

                }

                ButtonType.CONTADO -> {
                    txButtonText.text = context.getString(R.string.btn_cash).uppercase()
                    ivIcon.setImageResource(R.drawable.contado)
                    changeBackground(BackgroundButtonColor.GREEN)

                }

                ButtonType.VACIO -> {
                    changeBackground(BackgroundButtonColor.BLACK)
                }

                ButtonType.EMAIL -> {
                    txButtonText.text = context.getString(R.string.btn_email).uppercase()
                    ivIcon.setImageResource(R.drawable.mail)
                    changeBackground(BackgroundButtonColor.ORANGE)

                }

                ButtonType.EDIT -> {
                    txButtonText.text = context.getString(R.string.btn_edit).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_edit_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.ANSWER -> {
                    txButtonText.text = context.getString(R.string.btn_answer).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_question_answer_192)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.NEW_MESSAGE -> {
                    txButtonText.text = context.getString(R.string.btn_new_message).uppercase()
                    ivIcon.setImageResource(R.drawable.mail)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.PREDEFINED -> {
                    txButtonText.text =
                        context.getString(R.string.btn_predefined_message).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_article_192)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.DELETE -> {
                    txButtonText.text = context.getString(R.string.btn_delete).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_delete_outline_192)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.SELECT -> {
                    txButtonText.text = context.getString(R.string.btn_select).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_check_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.SEND -> {
                    txButtonText.text = context.getString(R.string.btn_send).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_send_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.VOLVER -> {
                    txButtonText.text = context.getString(R.string.btn_back).uppercase()
                    ivIcon.setImageResource(R.drawable.back)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.RECHAZAR -> {
                    txButtonText.text = context.getString(R.string.btn_rechazar).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_close_192)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.AVISOS -> {
                    txButtonText.text = context.getString(R.string.btn_avisos).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_notifications_active_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.NAVEGADOR -> {
                    txButtonText.text = context.getString(R.string.btn_navegador).uppercase()
                    ivIcon.setImageResource(R.drawable.navegacion)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.CLIENTE -> {
                    txButtonText.text = context.getString(R.string.btn_cliente).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_emoji_people_192)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.DEVOLVER -> {
                    txButtonText.text = context.getString(R.string.btn_devolver).uppercase()
                    ivIcon.setImageResource(R.drawable.despdevolver)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.HAY_CLIENTE -> {
                    txButtonText.text = context.getString(R.string.btn_hay_cliente).uppercase()
                    ivIcon.setImageResource(R.drawable.cliente)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.AT_DOOR -> {
                    txButtonText.text = context.getString(R.string.at_door).uppercase()
                    ivIcon.setImageResource(R.drawable.outline_door_front_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.RIDER_IN_CAB -> {
                    txButtonText.text = context.getString(R.string.rider_in_cab).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_face_192)
                    changeBackground(BackgroundButtonColor.ORANGE)
                }

                ButtonType.DISPATCH_INFO -> {
                    txButtonText.text = context.getString(R.string.dispatch_info).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_info_outline_192)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.UPDATE -> {
                    txButtonText.text = context.getString(R.string.btn_update).uppercase()
                    ivIcon.setImageResource(R.drawable.ic_system_update)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.FRONT_CAMERA -> {
                    txButtonText.text = context.getString(R.string.btn_front_camera).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_camera_front_192)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.BACK_CAMERA -> {
                    txButtonText.text = context.getString(R.string.btn_back_camera).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_photo_camera_back_192)
                    changeBackground(BackgroundButtonColor.ORANGE)
                }

                ButtonType.SELECT_IMAGE -> {
                    txButtonText.text = context.getString(R.string.btn_select_image).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_image_search_192)
                    changeBackground(BackgroundButtonColor.ORANGE)
                }

                ButtonType.HIDDEN -> {
                    txButtonText.text = ""
                    changeBackground(BackgroundButtonColor.TRANSPARENT)
                }

                ButtonType.CUSTOM -> {
                    txButtonText.text = context.getString(R.string.btn_custom)
                }

                ButtonType.CLEAR -> {
                    txButtonText.text = context.getString(R.string.clear).uppercase()
                    ivIcon.setImageResource(R.drawable.trash)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.TODOS -> {
                    txButtonText.text = context.getString(R.string.btn_all).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_check_192)
                    changeBackground(BackgroundButtonColor.GREEN)

                }

                ButtonType.CERRAR -> {
                    txButtonText.text = context.getString(R.string.btn_close).uppercase()
                    ivIcon.setImageResource(R.drawable.cierreautoact_white)
                    changeBackground(BackgroundButtonColor.GREEN)

                }

                ButtonType.CERRAR_CROSS -> {
                    txButtonText.text = context.getString(R.string.btn_close).uppercase()
                    ivIcon.setImageResource(R.drawable.cerrar)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.SCANNER -> {
                    txButtonText.text = context.getString(R.string.bnt_scanner).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_qr_code_scanner_192)
                    changeBackground(BackgroundButtonColor.YELLOW)

                }

                ButtonType.TRIPS -> {
                    txButtonText.text = context.getString(R.string.btn_servicios).uppercase()
                    ivIcon.setImageResource(R.drawable.lista_clientes)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.CARS -> {
                    txButtonText.text = context.getString(R.string.btn_vehiculos).uppercase()
                    ivIcon.setImageResource(R.drawable.lista_coches)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.ADD_FAVOURITES -> {
                    txButtonText.text = context.getString(R.string.add_to_favourite).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_add_circle_outline_192)
                    changeBackground(BackgroundButtonColor.GREEN)

                }

                ButtonType.REMOVE_FAVOURITES -> {
                    txButtonText.text =
                        context.getString(R.string.remove_from_favourite).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_remove_192)
                    changeBackground(BackgroundButtonColor.RED)

                }

                ButtonType.POI -> {
                    txButtonText.text = context.getString(R.string.poi).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.MINUTE -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "1"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_0 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "0"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_1 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "1"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_2 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "2"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_3 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "3"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_4 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "4"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_5 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "5"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_10 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "10"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_15 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "15"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_20 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "20"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REINFORCEMENT_25 -> {
                    tvOnlyText.visibility = View.VISIBLE
                    tvOnlyText.text = "25"

                    ivIcon.visibility = View.GONE
                    txButtonText.visibility = View.GONE

                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.RETRY -> {
                    txButtonText.text = context.getString(R.string.btn_reintentar).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_autorenew_192)
                    changeBackground(BackgroundButtonColor.YELLOW)
                }

                ButtonType.SHOW_ALL -> {
                    txButtonText.text = context.getString(R.string.show_all).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_add_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.SHOW_RECENT -> {
                    txButtonText.text = context.getString(R.string.show_recent).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_remove_192)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.LOGIN_WITH_CENTRAL -> {
                    txButtonText.text = context.getString(R.string.btn_with_central).uppercase()
                    ivIcon.setImageResource(R.drawable.iniciocentral)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.LOGIN_WITHOUT_CENTRAL -> {
                    txButtonText.text = context.getString(R.string.btn_without_central).uppercase()
                    ivIcon.setImageResource(R.drawable.iniciosincentral)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.LOGIN_REINFORCEMENT -> {
                    txButtonText.text = context.getString(R.string.btn_reinforcement).uppercase()
                    ivIcon.setImageResource(R.drawable.iniciocentral)
                    changeBackground(BackgroundButtonColor.ORANGE)
                }

                ButtonType.EMPTY -> {
                    txButtonText.text = ""
                    ivIcon.visibility = View.GONE
                    changeBackground(BackgroundButtonColor.BLACK)
                }

                ButtonType.GENERATE_INVOICE -> {
                    txButtonText.text = context.getString(R.string.btn_invoice).uppercase()
                    ivIcon.setImageResource(R.drawable.invoice)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.BIZUM -> {
                    txButtonText.text = context.getString(R.string.btn_monei).uppercase()
                    ivIcon.setImageResource(R.drawable.abonado)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.FINISH -> {
                    txButtonText.text = context.getString(R.string.btn_finish).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_check_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.REFUND -> {
                    txButtonText.text = context.getString(R.string.btn_refund).uppercase()
                    ivIcon.setImageResource(R.drawable.tarjeta)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.PRERESERVATION -> {
                    txButtonText.text = context.getString(R.string.btn_prereservations).uppercase()
                    ivIcon.setImageResource(R.drawable.pendientes)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.UBICAR_DESTINO -> {
                    txButtonText.text = context.getString(R.string.btn_ubicar_destino).uppercase()
                    ivIcon.visibility = View.GONE
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.NAVEGAR -> {
                    txButtonText.text = context.getString(R.string.btn_navegar).uppercase()
                    ivIcon.visibility = View.GONE
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.UBICAR_DESTINO_NAVEGAR -> {
                    txButtonText.text = context.getString(R.string.btn_ubicar_destino_navegar).uppercase()
                    ivIcon.visibility = View.GONE
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.PERMISSIONS -> {
                    txButtonText.text = context.getString(R.string.btn_permissions).uppercase()
                    ivIcon.setImageResource(R.drawable.outline_back_hand_48)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.APP_PAYMENT -> {
                    txButtonText.text = context.getString(R.string.payment_by_app).uppercase()
                    ivIcon.setImageResource(R.drawable.tarjeta)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.OTHERS -> {
                    txButtonText.text = context.getString(R.string.btn_otros).uppercase()
                    ivIcon.setImageResource(R.drawable.contado)
                    changeBackground(BackgroundButtonColor.RED)
                }

                ButtonType.CALL -> {
                    txButtonText.text = context.getString(R.string.btn_call).uppercase()
                    ivIcon.setImageResource(R.drawable.peticionvoz)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.SHARE -> {
                    txButtonText.text = context.getString(R.string.btn_share).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_share_192)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.SHORT_BREAK -> {
                    txButtonText.text = context.getString(R.string.btn_short_break).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_pause_192)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.SOON_IN_ZONE -> {
                    txButtonText.text = context.getString(R.string.soon_in_zone).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.WITH_ZONE  -> {
                    txButtonText.text = context.getString(R.string.with_zone).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)
                    changeBackground(BackgroundButtonColor.GREEN)
                }

                ButtonType.WITHOUT_ZONE  -> {
                    txButtonText.text = context.getString(R.string.without_zone).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.HOME -> {
                    txButtonText.text = context.getString(R.string.btn_home).uppercase()
                    ivIcon.setImageResource(R.drawable.outline_home_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.WEB -> {
                    txButtonText.text = context.getString(R.string.btn_web).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_language_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.FIXED_PRICE -> {
                    txButtonText.text = context.getString(R.string.estimated_fixed_price).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_add_location_alt_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.DASHBOARD -> {
                    //txButtonText.text = context.getString(R.string.dashboard).uppercase()
                    txButtonText.text = context.getString(R.string.btn_dashboard)
                    ivIcon.setImageResource(R.drawable.round_dashboard_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.REQUIREMENTS -> {
                    //txButtonText.text = context.getString(R.string.dashboard).uppercase()
                    txButtonText.text = context.getString(R.string.requirements_button).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_rule_192)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.ORIGIN_ZONE -> {
                    txButtonText.text = context.getString(R.string.origin_zone).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.CURRENT_ZONE -> {
                    txButtonText.text = context.getString(R.string.current_zone).uppercase()
                    ivIcon.setImageResource(R.drawable.ubactivar)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.DEVICE_SETTINGS -> {
                    txButtonText.text = context.getString(R.string.device_settings).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_app_settings_alt_192)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.CROP -> {
                    txButtonText.text = context.getString(com.canhub.cropper.R.string.crop_image_menu_crop).uppercase()
                    ivIcon.setImageResource(R.drawable.crop)
                    changeBackground(BackgroundButtonColor.ORANGE)

                }

                ButtonType.CHANGE_CREDENTIALS -> {
                    txButtonText.text = context.getString(R.string.change_user).uppercase()
                    ivIcon.setImageResource(R.drawable.configuracion)
                    changeBackground(BackgroundButtonColor.BLUE)

                }

                ButtonType.GOING_HOME -> {
                    txButtonText.text = context.getString(R.string.driver_going_home).uppercase()
                    ivIcon.setImageResource(R.drawable.outline_home_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.OPEN_REINFORCEMENT -> {
                    txButtonText.text = context.getString(R.string.btn_reinforcement).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_emergency_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.TOTALIZERS -> {
                    txButtonText.text = context.getString(R.string.btn_totalizers).uppercase()
                    ivIcon.setImageResource(R.drawable.taximetro)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.TOOLS -> {
                    txButtonText.text = context.getString(R.string.btn_tools).uppercase()
                    ivIcon.setImageResource(R.drawable.baseline_construction_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                ButtonType.MEETING_SIGN -> {
                    txButtonText.text = context.getString(R.string.btn_meeting_sign).uppercase()
                    ivIcon.setImageResource(R.drawable.outline_connecting_airports_24)
                    changeBackground(BackgroundButtonColor.BLUE)
                }

                else -> {
                    ivIcon.setImageResource(R.drawable.baseline_check_192)
                    txButtonText.text = context.getString(R.string.btn_default).uppercase()
                    changeBackground(BackgroundButtonColor.BLUE)
                }
            }

            if (!txButtonText.text.contains(" ")) {
                txButtonText.maxLines = 1
            } else {
                txButtonText.maxLines = 2
            }

            customFunctionValue = value
        }
    }

    fun changeTextColor(isBlack: Boolean) {
        if (isBlack) {
            binding.tvOnlyText.setTextColor(context.getColor(R.color.black))
        } else {
            binding.tvOnlyText.setTextColor(context.getColor(R.color.white))
        }
    }


    fun getButtonText(): String {
        return binding.txButtonText.text.toString()
    }

    /**
     * Saves a callback on the button's onClickListener, invoked only when the button's Type is enabled.
     *
     * @param listener The unit to be invoked. It does not take any parameters.
     *
     * @see setOnClickListener()
     * @see setButtonType()
     * @see StyleButton.ENABLE
     */
    fun setAction(listener: () -> Unit) {
        savedOnClickListener = listener
    }

    fun changeButtonIcon(drawable: Int) {
        binding.ivIcon.setImageResource(drawable)
    }

    fun checkTextOnly(btnType: Int? = customFunctionValue) {
        when (ButtonType.fromValue(btnType)) {
            ButtonType.MINUTE,
            ButtonType.REINFORCEMENT_0,
            ButtonType.REINFORCEMENT_1,
            ButtonType.REINFORCEMENT_2,
            ButtonType.REINFORCEMENT_3,
            ButtonType.REINFORCEMENT_4,
            ButtonType.REINFORCEMENT_5,
            ButtonType.REINFORCEMENT_10,
            ButtonType.REINFORCEMENT_15,
            ButtonType.REINFORCEMENT_20,
            ButtonType.REINFORCEMENT_25 -> {
                val root = binding.root
                val btnText = binding.tvOnlyText

                val constraintSet = ConstraintSet()
                constraintSet.apply {
                    clone(root)

                    constraintSet.clear(btnText.id, ConstraintSet.TOP)
                    constraintSet.clear(btnText.id, ConstraintSet.BOTTOM)

                    connect(btnText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                    connect(btnText.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                    setVerticalBias(btnText.id, 0.5F)
                    btnText.textSize = resources.getDimension(R.dimen.text_size_small_medium)
                    applyTo(root)
                }
            }
            else -> {
                return
            }
        }
    }
}