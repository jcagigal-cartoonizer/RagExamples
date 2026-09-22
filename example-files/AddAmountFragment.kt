package ifac.td.taxi.ui.screen

import android.view.View
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentAddAmountBinding
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.NO_SUBSCRIBER
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toMoneyValue
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.fields.CustomImportEditText
import ifac.td.taxi.viewmodel.AddAmountViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddAmountFragment :
    BaseFragment<FragmentAddAmountBinding, AddAmountViewModel>(R.layout.fragment_add_amount),
    CustomImportEditText.OnCustomImportEditTextActionListener {

    private val vModel: AddAmountViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val TAG = "AddAmountFragment"

    private val none = 1
    private val manual = 2
    private val onlyInPayment = 3
    private val automatic = 4

    private var serviceAmount: Int = 0
    private var extraAmount: Int = 0
    private var tollAmount: Int = 0
    private var tipAmount: Int = 0
    private var totalAmount: Int = 0

    // Importes originales que llegan del taxímetro, para detectar si el usuario modifica el precio del servicio
    private var originalServiceAmount: Int = 0
    private var originalExtraAmount: Int = 0

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentAddAmountBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        //vModel.getTrip(args.tripId)
        setUpViews()
        setUpEditTexts()

        vModel.useTTSForAmount(
            serviceAmountLocal = serviceAmount,
            extraAmountLocal = extraAmount,
            tollAmountLocal = tollAmount,
            tipAmountLocal = tipAmount,
            totalAmountLocal = totalAmount
        )

    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                collectAmount()
            }
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
        }
    }

    private fun setUpViews() {
        sharedViewModel.tripFlow.value?.let {
            var setUp = false
            if (it.fromDispatch) {
                sharedViewModel.dispatchFlow.value?.let { dispatch ->
                    dispatch.amount?.let { amount ->
                        if (amount > 0) {
                            vModel.allowsModifyCash(it.taximeterAmount == 0, it.taximeterTripId == null, dispatch.clientType != NO_SUBSCRIBER, true).let { canModify ->
                                vBinding.etServiceAmountNumber.isEnabled = canModify
                                Logs.d(TAG, "allowsModifyCash 1: ${vBinding.etServiceAmountNumber.isEnabled}")
                                vBinding.etServiceAmountNumber.setText(amount.toMoneyValue())
                                setUp = true
                            }
                            vBinding.etServiceAmountNumber.setText(amount.toMoneyValue())
                        } else {
                            vModel.allowsModifyCash(it.taximeterAmount == 0, it.taximeterTripId == null, dispatch.clientType != NO_SUBSCRIBER, false).let { canModify ->
                                vBinding.etServiceAmountNumber.isEnabled = canModify
                                Logs.d(TAG, "allowsModifyCash 2: ${vBinding.etServiceAmountNumber.isEnabled}")
                            }
                        }
                    }
                }
            } else {
                vModel.allowsModifyCash(it.taximeterAmount == 0, it.taximeterTripId == null, false, false).let { canModify ->
                    vBinding.etServiceAmountNumber.isEnabled = canModify
                    Logs.d(TAG, "allowsModifyCash 3: ${vBinding.etServiceAmountNumber.isEnabled}")
                }
            }

            checkVisibilityModifiersPrice()

            calculateAmount()

            if (!setUp) {
                setUpAmount(it)
            }
        }
    }

    private fun checkVisibilityModifiersPrice() {
        val allowTolls = vModel.allowsTolls(sharedViewModel.dispatchFlow.value)
        vBinding.clTolls.visibility =
            if (vModel.allowsTolls(sharedViewModel.dispatchFlow.value))
                View.VISIBLE
            else
                View.GONE
        Logs.d(TAG, "allowsTolls: $allowTolls")

        val allowTips = vModel.allowsTips(sharedViewModel.dispatchFlow.value)
        vBinding.clTips.visibility =
            if (vModel.allowsTips(sharedViewModel.dispatchFlow.value))
                View.VISIBLE
            else
                View.GONE
        Logs.d(TAG, "allowsTips: $allowTips")
    }

    private fun setUpAmount(it: Trip) {
        // SERVICE
        serviceAmount = (it.taximeterAmount ?: 0)
        originalServiceAmount = serviceAmount
        val formattedServiceAmount = serviceAmount.toMoneyValue()
        if (it.taximeterAmount != 0) {
            vBinding.etServiceAmountNumber.setText(formattedServiceAmount)
        } else {
            vBinding.etServiceAmountNumber.hint = formattedServiceAmount
        }


        //TODO:: Está Bien independientemente de lo que haya en Licensing?
        vModel.checkIfWorksWithoutTx()

        // EXTRAS
        extraAmount = (it.extra1 + it.extra2 + it.extra3 + it.extra4 + (it.extrasAuto?:0))
        originalExtraAmount = extraAmount
        val formattedExtraAmount = extraAmount.toMoneyValue()
        val formattedServicePlusExtraAmount = (serviceAmount).toMoneyValue()

        //vBinding.clExtras.visibility = View.GONE

        if (extraAmount != 0) {
            vBinding.clExtras.visibility = View.VISIBLE
            vBinding.etExtrasAmountNumber.isEnabled = false
            vBinding.etExtrasAmountNumber.setText(formattedExtraAmount)
            vBinding.etServiceAmountNumber.setText(formattedServicePlusExtraAmount)
        }

        // TOLLS
        tollAmount = (it.tolls ?: 0)
        val formattedTollAmount = tollAmount.toMoneyValue()
        if (it.tolls != 0) {
            vBinding.etTollsAmountNumber.setText(formattedTollAmount)
        } else {
            vBinding.etTollsAmountNumber.hint = formattedTollAmount
        }

        // TIPS
        tipAmount = (it.tips ?: 0)
        val formattedTipAmount = tipAmount.toMoneyValue()
        if (it.tips != 0) {
            vBinding.etTipsAmountNumber.setText(formattedTipAmount)
        } else {
            vBinding.etTipsAmountNumber.hint = formattedTipAmount
        }

        calculateAmount()
    }

    private fun calculateAmount() {
        val serviceAmountLocal = vBinding.etServiceAmountNumber.getValueInCents()

        // Si el usuario tiene permiso para modificar el precio del taxímetro y realmente lo ha
        // modificado respecto al importe original, los extras que llegan del taxímetro se ponen a 0.
        // Si no lo modifica (o vuelve al importe original), se mantienen los extras.
        // (No afecta a tips ni tolls)
        if (originalExtraAmount != 0) {
            val priceModified = vBinding.etServiceAmountNumber.isEnabled &&
                    serviceAmountLocal != originalServiceAmount
            if (priceModified) {
                vBinding.etExtrasAmountNumber.setText(0.toMoneyValue())
                vBinding.clExtras.visibility = View.GONE
            } else {
                vBinding.etExtrasAmountNumber.setText(originalExtraAmount.toMoneyValue())
                vBinding.clExtras.visibility = View.VISIBLE
            }
        }

        val extraAmountLocal = vBinding.etExtrasAmountNumber.getValueInCents()
        val tollAmountLocal = vBinding.etTollsAmountNumber.getValueInCents()
        val tipAmountLocal = vBinding.etTipsAmountNumber.getValueInCents()

        val totalAmountLocal =
            serviceAmountLocal + extraAmountLocal + tollAmountLocal + tipAmountLocal

        vBinding.tvTotalAmountNumber.text = totalAmountLocal.toCurrency()

        serviceAmount = serviceAmountLocal
        extraAmount = extraAmountLocal
        tollAmount = tollAmountLocal
        tipAmount = tipAmountLocal
        totalAmount = totalAmountLocal
    }


    override fun setupObservers() {
        /**viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.withTaximeterFlow.collect {
                        if (!vBinding.etServiceAmountNumber.isEnabled) {
                            vBinding.etServiceAmountNumber.isEnabled = it
                        }
                    }
                }
            }
        }*/
    }

    private fun setUpEditTexts() {
        vBinding.etServiceAmountNumber.actionListener = this
        vBinding.etExtrasAmountNumber.actionListener = this
        vBinding.etTollsAmountNumber.actionListener = this
        vBinding.etTipsAmountNumber.actionListener = this
    }

    private fun collectAmount() {
        calculateAmount()

        val trip = sharedViewModel.tripFlow.value

        trip?.let {
            val maxAmountManual = vModel.maximumAmountManual()
            Logs.d(TAG, "maximumAmountManual: $maxAmountManual")
            val maxAmountTips = vModel.maximumAmountTips()
            Logs.d(TAG, "maximumAmountTips: $maxAmountTips")
            val maxAmountTolls = vModel.maximumAmountTolls()
            Logs.d(TAG, "maximumAmountTolls: $maxAmountTolls")

            if (maxAmountManual > 0 && ((serviceAmount - (trip.taximeterAmount?.div(100f) ?: 0f)) > maxAmountManual)) {
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response -> }
                iMainActivity.openDialog(
                    model = CustomDialog.CustomDialogModel(
                        title = getString(R.string.warning),
                        description = getString(R.string.toastMaxService),
                        buttons = arrayListOf(
                            ButtonType.ACCEPT
                        )
                    ),
                    response = callback
                )
                return
            }

            if (maxAmountTips > 0 && ((tipAmount - (trip.tips?.div(100f) ?: 0f)) > maxAmountTips)) {
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response -> }
                iMainActivity.openDialog(
                    model = CustomDialog.CustomDialogModel(
                        title = getString(R.string.warning),
                        description = getString(R.string.toastMaxTips),

                        buttons = arrayListOf(
                            ButtonType.ACCEPT
                        )
                    ),
                    response = callback
                )
                return
            }

            if (maxAmountTolls > 0 && ((tollAmount - (trip.tolls?.div(100f) ?: 0f)) > maxAmountTolls)) {
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response -> }
                iMainActivity.openDialog(
                    model = CustomDialog.CustomDialogModel(
                        title = getString(R.string.warning),
                        description = getString(R.string.toastMaxTolls),
                        buttons = arrayListOf(
                            ButtonType.ACCEPT
                        )
                    ),
                    response = callback
                )
                return
            }

            Logs.d(TAG, "collectAmount: $totalAmount")

            vModel.updateTrip(
                sharedViewModel.tripFlow.value,
                serviceAmount,
                tollAmount,
                tipAmount,
                totalAmount,
                extraAmount
            )
        }
    }

    override fun updateTopBarIcon() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.BACK, true) {
            iMainActivity.navigateBack()
        }
    }

    override fun onActionDone() {
        calculateAmount()
    }

    override fun onFocusLost() {
        calculateAmount()
    }

}
