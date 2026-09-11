package ifac.td.taxi.ui.custom.dialog


import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.interfacom.sdk.taximeter.utils.UserInteractionManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.CustomDialogBinding
import ifac.td.taxi.domain.model.PrimeType
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.adapter.DialogMessageAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.spinnerItem.CustomSpinnerPrimeAdapter
import ifac.td.taxi.ui.util.ConfigurationUtils


class CustomDialog : DialogFragment() {

    private val viewModel: CustomDialogViewModel by viewModels()
    private val TAG = "CustomDialog"

    var model: CustomDialogModel? = null
    var onDismissFunction: ((CustomDialogResponse) -> Unit)? = null

    private lateinit var binding: CustomDialogBinding
    private val response = CustomDialogResponse()

    private val DESCRIPTION_LIMIT_COUNT = 200

    override fun onResume() {
        super.onResume()

        if (!ConfigurationUtils.isLandscape(resources)) {
            dialog?.let { dialogIt ->
                dialogIt.window?.let { windowsIt ->
                    val params: ViewGroup.LayoutParams = windowsIt.attributes
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    windowsIt.attributes = params as WindowManager.LayoutParams
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (binding.edDialog.text?.isNotEmpty() == true) {
            response.editTextString = binding.edDialog.text.toString()
        }

        if (binding.edImportDialog.text?.isNotEmpty() == true) {
            response.importEditTextValue = binding.edImportDialog.getValueInCents()
        }
        if (viewModel.model?.checkBoxText != null) {
            response.checkBoxStatus = binding.cbAccept.isChecked
        }
        viewModel.model?.listOptions?.let {
            try {
                val selectedItem = binding.spnPrimeOptions.selectedItem as Pair<*, *>
                response.selectedOption = PrimeType(selectedItem.first as Int, selectedItem.second as String)
                Logs.d(TAG, "Selected item: ${response.selectedOption}")
            } catch (e: Exception) {
                Logs.e(TAG, "Error on list selected")
            }
        }

        viewModel.stopTTS()

        viewModel.onDismissFunction?.invoke(response)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = CustomDialogBinding.inflate(inflater, container, false)
        model?.let { currentModel ->
            viewModel.model = currentModel
            viewModel.onDismissFunction = onDismissFunction
        }

        if (viewModel.model?.editText == null && viewModel.model?.hint == null && viewModel.model?.checkBoxText == null && viewModel.model?.description == null && viewModel.model?.hrefText == null) {
            binding.ivTitleDivider.visibility = View.GONE
            val layoutParams =
                binding.lytContainerFlowMenu.layoutParams as ViewGroup.MarginLayoutParams
            val marginBottomPx =
                resources.getDimensionPixelSize(R.dimen.only_title_buttons_margin_top)
            layoutParams.topMargin = marginBottomPx
            binding.lytContainerFlowMenu.layoutParams = layoutParams
        }

        if (viewModel.model?.buttons != null) {
            loadButtons()
        }
        if (viewModel.model?.title != null) {
            binding.tvTitle.text = viewModel.model?.title
        } else {
            binding.tvTitle.visibility = View.GONE
            binding.ivTitleDivider.visibility = View.GONE
        }
        val icon = viewModel.model?.icon
        if (icon != null) {
            val drawable = ContextCompat.getDrawable(requireContext(), icon)
            binding.icTitle.setImageDrawable(drawable)
        } else {
            binding.icTitle.visibility = View.GONE
        }

        val description = viewModel.model?.description
        if (description != null) {
            binding.tvDescription.text = description
            Logs.d(TAG, "description: $description  description.count(): ${description.count()}")
            if (description.count() > DESCRIPTION_LIMIT_COUNT) {
                adjustScrollViewHeight()
            }
        } else {
            binding.tvDescription.visibility = View.GONE
        }

        if (viewModel.model?.messageOptions != null && viewModel.model?.messageOptions != emptyList<String>()) {
            val adapter =
                DialogMessageAdapter(requireContext(), R.layout.row_message) { selectedString ->
                    binding.edDialog.setText(selectedString)
                }

            binding.rvOptionMessage.layoutManager = LinearLayoutManager(requireContext())
            binding.rvOptionMessage.adapter = adapter

            viewModel.model?.messageOptions?.let {
                adapter.setItems(it)
            }
        } else {
            binding.rvOptionMessage.visibility = View.GONE
        }

        val primeTripsType = viewModel.model?.listOptions
        if (primeTripsType != null) {
            val adapter =
                CustomSpinnerPrimeAdapter(requireContext(), R.layout.row_message, primeTripsType) { selectedMap ->
                    response.selectedOption = PrimeType(id = selectedMap.first, name = selectedMap.second)
                }

            binding.spnPrimeOptions.adapter = adapter
        } else {
            binding.spnPrimeOptions.visibility = View.GONE
        }

        if (viewModel.model?.hrefText != null) {
            binding.tvHref.text = viewModel.model?.hrefText
        } else {
            binding.tvHref.visibility = View.GONE
        }

        binding.tvHref.setOnClickListener {
            Logs.d("CustomDialog", "tvHref: onClick")
            viewModel.model?.href?.invoke()
        }


        if (viewModel.model?.checkBoxText != null) {
            binding.cbAccept.text = viewModel.model?.checkBoxText
        } else {
            binding.cbAccept.visibility = View.GONE
        }
        if (viewModel.model?.checkDisableButton != null) {
            val acceptButton = if (binding.btn1.customFunctionValue == ButtonType.ACCEPT.value) {
                binding.btn1
            } else if (binding.btn2.customFunctionValue == ButtonType.ACCEPT.value) {
                binding.btn2
            } else if (binding.btn3.customFunctionValue == ButtonType.ACCEPT.value) {
                binding.btn3
            } else {
                null
            }
            acceptButton?.setAction { }
            this.isCancelable = false
            binding.cbAccept.setOnCheckedChangeListener { _, checked ->
                Logs.d("CustomDialog", "cbAccept. isChecked: $checked")
                if (checked) {
                    acceptButton?.setAction {
                        setUserInteraction()
                        response.buttonPressed =
                            ButtonType.fromValue(acceptButton?.customFunctionValue)
                        this.dismiss()
                    }
                } else {
                    acceptButton?.setAction { }
                }
            }
        }

        if (viewModel.model?.isCancellable != null) {
            val acceptButton = if (binding.btn1.customFunctionValue == ButtonType.ACCEPT.value) {
                binding.btn1
            } else if (binding.btn2.customFunctionValue == ButtonType.ACCEPT.value) {
                binding.btn2
            } else if (binding.btn3.customFunctionValue == ButtonType.ACCEPT.value) {
                binding.btn3
            } else {
                null
            }
            this.isCancelable = false
            acceptButton?.setAction {
                setUserInteraction()
                response.buttonPressed = ButtonType.fromValue(acceptButton?.customFunctionValue)
                this.dismiss()
            }
        }

        if (viewModel.model?.centerText != null) {
            if (viewModel.model?.centerText == true) {
                binding.tvDescription.textAlignment = TEXT_ALIGNMENT_CENTER
            }
        }

        if (viewModel.model?.hint != null) {
            binding.edDialog.hint = viewModel.model?.hint
        }
        if (viewModel.model?.editText != null) {
            binding.edDialog.setText(viewModel.model?.editText)
        }
        if (viewModel.model?.hint == null && viewModel.model?.editText == null) {
            binding.edDialog.visibility = View.GONE
        }

        if (viewModel.model?.importEditText == null) {
            binding.edImportDialog.visibility = View.GONE
        }

        if (viewModel.model?.editTextTypePin == true) {
            binding.edDialog.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }


        viewModel.model?.editTextMaxLength?.let { maxLength ->
            if (maxLength > 0) {
                binding.edDialog.filters = arrayOf(InputFilter.LengthFilter(maxLength))
            }
        }

        viewModel.speakTTS(buildTTSText())

        return binding.root
    }

    private fun buildTTSText(): String {
        val ttsText = StringBuilder().apply {
            append(
                binding.tvDescription.text.toString().ifEmpty { binding.tvTitle.text.toString() })

            val buttons = mutableListOf<String>()
            if (binding.btn1.visibility != View.GONE) buttons.add(binding.btn1.getButtonText())
            if (binding.btn2.visibility != View.GONE) buttons.add(binding.btn2.getButtonText())
            if (binding.btn3.visibility != View.GONE) buttons.add(binding.btn3.getButtonText())

            if (buttons.isNotEmpty()) {
                append(". ")
                append(buttons.dropLast(1).joinToString(", "))
                if (buttons.size > 1) {
                    append(" o ")
                    append(buttons.last())
                } else {
                    append(buttons.first())
                }
            }
        }.toString()
        return ttsText
    }

    private fun moveBtnGridRight() {
        binding.apply {
            val clDialogContentParams =
                ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            clDialogContentParams.apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToStart = lytContainerFlowMenu.id
                horizontalWeight = 0.7f
            }
            clDialogContent.layoutParams = clDialogContentParams

            val btnGridParams =
                ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
            btnGridParams.apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToEnd = clDialogContent.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                horizontalWeight = 0.3f
            }

            lytContainerFlowMenu.apply {
                for (i in 0 until childCount) {
                    val button = getChildAt(i)
                    button.layoutParams = (button.layoutParams as GridLayout.LayoutParams).apply {
                        columnSpec = GridLayout.spec(0)
                    }
                }
                rowCount = 3
                columnCount = 1
                // recolocar botones en nuevas filas y columnas
                for (i in 0 until childCount) {
                    val button = getChildAt(i)
                    button.layoutParams = (button.layoutParams as GridLayout.LayoutParams).apply {
                        columnSpec = GridLayout.spec(0, 1, 1f)
                        rowSpec = GridLayout.spec(i, 1, 1f)
                    }
                }
                layoutParams = btnGridParams
            }
        }
    }

    private fun adjustScrollViewHeight() {
        val orientation = resources.configuration.orientation
        val heightDimen = resources.getDimensionPixelSize(R.dimen.dialog_scroll_height)

        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            Logs.d(TAG, "adjustScrollViewHeightForHorizontal: Adjusting height for horizontal orientation")

            val screenHeight = resources.displayMetrics.heightPixels
            val maxHeight = (screenHeight * 0.5).toInt()
            val minHeight = heightDimen

            // me aseguro de que la altura del scrollview cabe en pantalla
            val finalHeight = minHeight.coerceIn(minHeight, maxHeight)

            binding.scrollView.layoutParams = binding.scrollView.layoutParams.apply {
                height = finalHeight
            }

            binding.scrollView.isScrollbarFadingEnabled = false
            binding.scrollView.isVerticalScrollBarEnabled = true
            binding.scrollView.scrollBarStyle = View.SCROLLBARS_INSIDE_INSET

        } else {
            binding.scrollView.layoutParams = binding.scrollView.layoutParams.apply {
                height = heightDimen
            }
        }
    }

    private fun loadButtons() {
        val buttonArray = viewModel.model?.buttons ?: return
        val checkDisableButton = viewModel.model?.checkDisableButton == true

        val buttons = listOf(binding.btn1, binding.btn2, binding.btn3)

        buttonArray.forEachIndexed { index, buttonType ->
            if (index < buttons.size) {
                val button = buttons[index]
                button.setButtonType(buttonType.value)
                button.visibility = View.VISIBLE
                button.setAction {
                    setUserInteraction()

                    response.buttonPressed = buttonType
                    viewModel.stopTTS()

                    if (checkDisableButton && response.buttonPressed == ButtonType.ACCEPT && !binding.cbAccept.isChecked) {
                        context?.applicationContext?.let { appContext ->
                            Toast.makeText(
                                appContext,
                                appContext.getString(R.string.check_box_not_checked),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Logs.d(TAG, "btn${index + 1}.setAction: Checkbox not checked")
                    } else {
                        when (response.buttonPressed) {
                            else -> this.dismiss()
                        }
                    }
                }
            } else {
                Logs.d(TAG, "More than 3 buttons detected, ignoring extra buttons.")
            }
        }

        for (i in buttonArray.size until buttons.size) {
            buttons[i].visibility = View.GONE
        }
    }

    private fun setUserInteraction() {
        UserInteractionManager.getInstance().isRecentUserInteraction = true
    }

    data class TripLite(
        val tripId: Long? = null,
        val ticketBuffer: String? = null,
        val invoiceDate: String? = null,
        val initDate: String? = null,
    )

    data class CustomDialogModel(
        val title: String? = null,
        val description: String? = null,
        val messageOptions: List<String>? = null,
        @DrawableRes
        val icon: Int? = null,
        val hrefText: String? = null,
        val href: (() -> Unit)? = null,
        val checkBoxText: String? = null,
        val checkDisableButton: Boolean? = null,
        val editText: String? = null,
        val hint: String? = null,
        val buttons: ArrayList<ButtonType>? = null,
        val isCancellable: Boolean? = null,
        val centerText: Boolean? = null,
        val importEditText: Boolean? = null,
        val editTextTypePin: Boolean? = null,
        val editTextMaxLength: Int? = null,
        val dialogTAG: CustomDialogTAG? = null,
        val trip: TripLite? = null,
        val listOptions: Map<Int, String>? = null,
    )

    enum class CustomDialogTAG(val TAG: String) {
        SUBSCRIBER_RESPONSE_SUCCESS_DIALOG("subscriberResponseSuccess"),
        SUBSCRIBER_RESPONSE_ERROR_DIALOG("subscriberResponseError"),
        SUBSCRIBER_RESPONSE_TEMPORAL_DIALOG("subscriberResponseTemporal"),
        INVOICE_CHOOSE_OPTION_DIALOG("invoiceChooseOptionDialog"),
        LOCATE_ON_STAND_DIALOG("RankDialog"),
        URGENT_MESSAGE_DIALOG("urgentMessageDialog"),
        PRIME_PAYMENT_DIALOG("primePaymentDialog"),
        NO_CLIENT_IN_TAXI("noClientInTaxiDialog"),
        PORTUGAL_EXTERNAL_CUSTOMER_DIALOG("portugalExternalCustomerDialog"),
        NO_BT_DELOCATES("noBtDelocates"),
        EMERGENCY_BUTTON_DIALOG("emergencyButtonDialog")
    }

    data class CustomDialogResponse(
        var editTextString: String? = null,
        var checkBoxStatus: Boolean? = null,
        var buttonPressed: ButtonType? = null,
        var importEditTextValue: Int? = null,
        var ticketUri: Uri? = null,
        var selectedOption: PrimeType? = null,
    )
}