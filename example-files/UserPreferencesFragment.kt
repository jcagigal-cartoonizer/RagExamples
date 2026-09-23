package ifac.td.taxi.ui.screen

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentUserPreferencesBinding
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.spinnerItem.CustomSpinnerAdapter
import ifac.td.taxi.ui.model.ColorPickerEnum
import ifac.td.taxi.viewmodel.UserPreferencesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar


class UserPreferencesFragment :
    BaseFragment<FragmentUserPreferencesBinding, UserPreferencesViewModel>(R.layout.fragment_user_preferences) {

    private val vModel: UserPreferencesViewModel by viewModel()
    private val TAG = "UserPreferencesFragment"

    private lateinit var userPreferencesModel: UserPreferences
    private var lastSelectedType: Int = -1

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                handleSelectedRingtoneOrNotification(data)
            }
        }

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentUserPreferencesBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
        vModel.checkShifts()
        vModel.checkTaximeter()
        vModel.checkLightOffOnDispatched()
        vModel.checkFiscalLicensing()
        iMainActivity.refreshCabId()
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }
            btnAccept.setAction {
                logCheckBoxStates()

                soundOptions()
                invoiceOptions()
                pinPadOptions()
                sumUpOptions()
                shiftsOptions()
                systemOptions()
                locationOptions()

                checkPhoneNumber()
                checkTimeControl()
                checkPinPad()

                vModel.insertUserPreferences(userPreferencesModel)

                iMainActivity.keepScreenActive()
                iMainActivity.navigateBack()
            }

            btnFiscalPortugal.setOnClickListener {
                val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    if (response.buttonPressed == ButtonType.ACCEPT) {
                        val pin = response.editTextString
                        if (!pin.isNullOrEmpty()) {
                            vModel.checkPinPortugal(pin)
                        }
                    }
                }

                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = getString(R.string.btn_portugal_password),
                        hint = "PIN Portugal",
                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                        editTextTypePin = true,
                    ),
                    callBack,
                    fragmentManager = childFragmentManager
                )
            }

            btnSecurePin.setOnClickListener {
                vModel.navigateToSecurePin()
            }
        }
    }

    private fun logCheckBoxStates() {
        val checkBoxes = arrayListOf<CheckBox?>()
        vBinding.apply {
            checkBoxes.add(cbControlHorario)
            checkBoxes.add(cbSoundUbicationDisabledOnTX)
            checkBoxes.add(cbVibrationAndSound)
            checkBoxes.add(cbTTSForMessages)
            checkBoxes.add(cbTTSForDialogs)
            checkBoxes.add(cbTTSDispatch)
            checkBoxes.add(cbTTSLocation)
            checkBoxes.add(cbUseExternalApp)
            checkBoxes.add(cbUseCustomPaymentTimerWithExternalApp)
            checkBoxes.add(cbShowConfirmationOnAccept)
            checkBoxes.add(cbShowConfirmationOnReject)
            checkBoxes.add(cbFloatingWindowOnBackground)
            checkBoxes.add(cbCheckUbicationMacrazona)
            checkBoxes.add(cbSortFavZonesByProximity)
            checkBoxes.add(cbDisableGlonass)
            checkBoxes.add(cbPrintRedSysTicketAlways)
        }
        checkBoxes.forEach {
            it?.let { cb ->
                val name: String = try {
                    resources.getResourceEntryName(cb.id)
                } catch (e: Resources.NotFoundException) {
                    getString(R.string.resource_for_action_not_found)
                }
                Logs.d(TAG, "${name}. isChecked: ${cb.isChecked}")
            }
        }
    }

    private fun checkTimeControl() {
        if (!userPreferencesModel.useTimeControl) {
            iMainActivity.changeTextTimeControlTopBar(null)
        }
    }

    private fun checkPinPad() {
        if (!vBinding.etNumberPinPad.text.isNotBlank()) {
            iMainActivity.requestPermission(PermissionRequest.PermissionTypeList.PERMISSION_BLUETOOTH)
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.userPreferencesFlow.collect { userPreferences ->
                        if (userPreferences != null) {
                            userPreferencesModel = userPreferences
                            setupView()
                        } else {
                            userPreferencesModel = UserPreferences()
                            setupView()
                        }
                    }
                }

                launch {
                    vModel.shiftsFlow.collect {
                        if (it) {
                            vBinding.btnSelectDateEraseShifts.setOnClickListener {
                                Logs.d(TAG, "btnSelectDateEraseShifts onClick.")
                                iMainActivity.showToast(R.string.toast_sin_turnos_guardados_para_eliminar)
                            }

                            vModel.checkShifts()
                        }
                    }
                }

                launch {
                    vModel.taximeterSkyGlassFlow.collect { isSkyGlassEnabled ->
                        Logs.d(TAG,"taximeterSkyGlassFlow: Received value = $isSkyGlassEnabled")
                        if (isSkyGlassEnabled) {
                            Logs.d(TAG,"taximeterSkyGlassFlow: Showing cbControlHorario")
                            vBinding.cbControlHorario?.visibility = View.VISIBLE
                        } else {
                            Logs.d(TAG,"taximeterSkyGlassFlow: Hiding cbControlHorario")
                            vBinding.cbControlHorario?.visibility = View.GONE
                        }
                    }

                }

                launch {
                    vModel.cbLightOffOnDispatchedFlow.collect {
                        if (it) {
                            vBinding.cbLightOffOnDispatched.visibility = View.VISIBLE
                        } else {
                            vBinding.cbLightOffOnDispatched.visibility = View.GONE
                        }
                    }
                }

                launch {
                    vModel.userInfoFlow.collect { isLoggedIn ->
                        if (isLoggedIn == false) {
                            vBinding.btnChangePin.setOnClickListener {
                                Logs.d(TAG, "btnChangePin onClick.")
                                iMainActivity.showToast(R.string.toast_login)
                            }
                        }
                    }
                }

                launch {
                    vModel.licensingFiscalFlow.collect {
                        if (it) {
                            vBinding.btnFiscalPortugal.visibility = View.VISIBLE
                        } else {
                            vBinding.btnFiscalPortugal.visibility = View.GONE
                        }
                    }
                }

                launch {
                    vModel.ingenicoInstalledFlow.collect {
                        if (it) {
                            vBinding.clPinPadPrinter.visibility = View.VISIBLE
                        } else {
                            vBinding.clPinPadPrinter.visibility = View.GONE
                        }
                    }
                }

                launch {
                    vModel.driverTrunModeFlow.collect {
                        if (it != null) {
                            if (it == 0) {
                                vBinding.btnUploadDriversPhoto.visibility = View.GONE
                                vBinding.btnChangePin.visibility = View.GONE
                            }
                        }
                    }
                }

                launch {
                    vModel.requestCurrentSecurePinFlow.collect {
                        showCurrentSecurePinDialog()
                    }
                }

                launch {
                    vModel.wrongSecurePinFlow.collect {
                        iMainActivity.showToast(R.string.pin_incorrecto)
                    }
                }
            }
        }

    }

    private fun showCurrentSecurePinDialog() {
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                vModel.verifyCurrentSecurePin(response.editTextString ?: "")
            }
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.user_preferences_btn_secure_pin),
                hint = getString(R.string.pin_actual_hint),
                buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                editTextTypePin = true,
            ),
            callBack,
            fragmentManager = childFragmentManager
        )
    }

    private fun setupView() {
        val cards = listOf(
            vBinding.clSoundCard,
            vBinding.clInvoicesCard,
            vBinding.clPinPadCard,
            vBinding.clShiftsCard,
            vBinding.clSystemCard,
            vBinding.clLocationCard
        )
        val contents = listOf(
            vBinding.clContentSound,
            vBinding.clContentInvoices,
            vBinding.clContentPinPad,
            vBinding.clContentShifts,
            vBinding.clContentSystem,
            vBinding.clContentLocation
        )

        val images = listOf(
            vBinding.ivArrow2,
            vBinding.ivArrow3,
            vBinding.ivArrow4,
            vBinding.ivArrow5,
            vBinding.ivArrow6,
            vBinding.ivArrow7,
        )

        val spinners = listOf(
            vBinding.spnCallDelay,
            vBinding.spnCloseMessage,
            vBinding.spnPinPadPrinter,
            vBinding.spnExportFormat,
            vBinding.spnGPSApp,
            vBinding.spnLocutionBlinds,
            vBinding.spnMinutesShift,
            vBinding.spnTypePinPad,
            vBinding.spnOrderStyle,
            vBinding.spnScreenActive,
        )

        cards.zip(contents).zip(images) { (card, content), image ->
            setupToggleOnClick(card, content, image)
        }

        spinners.forEach { spinner ->
            setupSpinnerAdapter(spinner)
        }

        setUpMeetingSignSpinnerAdapter()

        setupDefaultPreferences()

    }

    private fun setUpMeetingSignSpinnerAdapter() {
        val colors = ColorPickerEnum.values()

        val adapter = CustomSpinnerAdapter(requireContext(), R.layout.custom_spinner_item, colors.map { requireContext().getString(it.labelRes) }.toTypedArray())

        vBinding.spnMeetingSignTextColor.adapter = adapter
        vBinding.spnMeetingSignTextColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // Do something when an item is selected
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do something when no item is selected
            }
        }

        val adapterBackground = CustomSpinnerAdapter(requireContext(), R.layout.custom_spinner_item, colors.map { requireContext().getString(it.labelRes) }.toTypedArray())

        vBinding.spnMeetingSignBackgroundColor.adapter = adapterBackground
        vBinding.spnMeetingSignBackgroundColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // Do something when an item is selected
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do something when no item is selected
            }
        }
    }

    private fun soundOptions() {
        userPreferencesModel.beepNoBt = vBinding.cbSoundUbicationDisabledOnTX.isChecked

        userPreferencesModel.vibrateNoBT = vBinding.cbVibrationAndSound.isChecked

        userPreferencesModel.ttsMsg = vBinding.cbTTSForMessages.isChecked

        userPreferencesModel.ttsDialog = vBinding.cbTTSForDialogs?.isChecked ?: false

        userPreferencesModel.ttsDispatch = vBinding.cbTTSDispatch.isChecked

        userPreferencesModel.ttsLocation = vBinding.cbTTSLocation.isChecked

        if (!vBinding.spnLocutionBlinds.selectedItemPosition.equals(null)) {
            userPreferencesModel.blindLocutionPosition =
                vBinding.spnLocutionBlinds.selectedItemPosition
            userPreferencesModel.blindLocutionId =
                vBinding.spnLocutionBlinds.selectedItemId.toInt() + 1
        }
    }


    private fun invoiceOptions() {
        if (vBinding.etIssuerDirection.text.toString().isNotBlank()) {
            userPreferencesModel.invoiceIssuerAddress = vBinding.etIssuerDirection.text.toString()
        }

        if (vBinding.etPostalCode.text.toString().isNotBlank()) {
            userPreferencesModel.invoiceIssuerZipCode = vBinding.etPostalCode.text.toString()
        }

        if (vBinding.etCityIssuer.text.toString().isNotBlank()) {
            userPreferencesModel.invoiceIssuerCity = vBinding.etCityIssuer.text.toString()
        }

        if (vBinding.etInvoiceSeries.text.toString().isNotBlank()) {
            userPreferencesModel.invoiceSerialNumber = vBinding.etInvoiceSeries.text.toString()
        }

        if (vBinding.etInvoiceNumber.text.toString().isNotBlank()) {
            userPreferencesModel.invoiceNumber = vBinding.etInvoiceNumber.text.toString().toInt()
        }

        userPreferencesModel.useExternalApp = vBinding.cbUseExternalApp.isChecked
        userPreferencesModel.useCustomPaymentTimerWithExternalApp = vBinding.cbUseCustomPaymentTimerWithExternalApp.isChecked
        userPreferencesModel.printRedSysCommerceTicketAlways = vBinding.cbPrintRedSysTicketAlways.isChecked
    }

    private fun pinPadOptions() {
        if (vBinding.etNumberPinPad.text.toString() != userPreferencesModel.pinPadSerialNumber) {
            userPreferencesModel.pinPadSerialNumber = vBinding.etNumberPinPad.text.toString()
        }

        if (!vBinding.spnTypePinPad.selectedItemPosition.equals(null)) {
            userPreferencesModel.pinPadTypePosition = vBinding.spnTypePinPad.selectedItemPosition
            userPreferencesModel.pinPadTypeId = vBinding.spnTypePinPad.selectedItemId.toInt() + 1
        }
    }

    private fun sumUpOptions() {
        userPreferencesModel.sumUpMerchantCode = vBinding.etNumberSumUp.text.toString()
    }

    private fun shiftsOptions() {
        if (!vBinding.spnMinutesShift.selectedItemPosition.equals(null)) {
            userPreferencesModel.minutesBeforeWarningPosition =
                vBinding.spnMinutesShift.selectedItemPosition
            userPreferencesModel.minutesBeforeWarningId =
                vBinding.spnMinutesShift.selectedItemId.toInt() + 1
        }

        if (!vBinding.spnPinPadPrinter.selectedItemPosition.equals(null)) {
            userPreferencesModel.pinPadPrinterPosition = vBinding.spnPinPadPrinter.selectedItemPosition
            userPreferencesModel.pinPadPrinterId = vBinding.spnPinPadPrinter.selectedItemId.toInt() + 1
        }

        if (!vBinding.spnExportFormat.selectedItemPosition.equals(null)) {
            userPreferencesModel.fileFormatPosition = vBinding.spnExportFormat.selectedItemPosition
            userPreferencesModel.fileFormatId = vBinding.spnExportFormat.selectedItemId.toInt() + 1
        }

        userPreferencesModel.useTimeControl = vBinding.cbControlHorario?.isChecked ?: false
    }

    private fun checkOverlayDisplayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    private fun requestOverlayDisplayPermission() {

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.dialog_overlay_display_permissions_title),
                description = getString(R.string.dialog_overlay_display_permissions_desc),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT,
                )
            ),
            response = {
                if (it.buttonPressed == ButtonType.ACCEPT) {
                    iMainActivity.launchOverlayDisplayPermissionIntent()
                }
            },
            fragmentManager = childFragmentManager
        )
    }

    private fun systemOptions() {
        userPreferencesModel.showConfAcceptDispatch = vBinding.cbShowConfirmationOnAccept.isChecked
        userPreferencesModel.printDriverSubscriber = vBinding.cbPrintDriverSubscriber.isChecked
        userPreferencesModel.showConfRejectDispatch = vBinding.cbShowConfirmationOnReject.isChecked

        userPreferencesModel.useFloatingWindow = vBinding.cbFloatingWindowOnBackground.isChecked

        userPreferencesModel.lightOffOnDispatched = vBinding.cbLightOffOnDispatched.isChecked

        if (vBinding.cbFloatingWindowOnBackground.isChecked && !checkOverlayDisplayPermission()) {
            requestOverlayDisplayPermission()
        }

        if (!vBinding.spnCloseMessage.selectedItemPosition.equals(null)) {
            userPreferencesModel.secondsToCloseMsgPosition =
                vBinding.spnCloseMessage.selectedItemPosition
            userPreferencesModel.secondsToCloseMsgId =
                vBinding.spnCloseMessage.selectedItemId.toInt() + 1
        }

        if (vBinding.etNoticePhone.text.toString().isNotBlank()) {
            userPreferencesModel.phoneCall = vBinding.etNoticePhone.text.toString()
        } else {
            userPreferencesModel.phoneCall = ""
        }

        if (!vBinding.spnCallDelay.selectedItemPosition.equals(null)) {
            userPreferencesModel.phoneCallTimerPosition =
                vBinding.spnCallDelay.selectedItemPosition
            userPreferencesModel.phoneCallTimerId =
                vBinding.spnCallDelay.selectedItemId.toInt() + 1
        }

        if (!vBinding.spnScreenActive.selectedItemPosition.equals(null)) {
            userPreferencesModel.turnOffScreenPosition =
                vBinding.spnScreenActive.selectedItemPosition
            userPreferencesModel.turnOffScreenId =
                vBinding.spnScreenActive.selectedItemId.toInt() + 1
        }
    }


    private fun locationOptions() {
        userPreferencesModel.closePendingTripsIfEmpty = vBinding.cbClosePendingTripsIfEmpty.isChecked

        userPreferencesModel.macroZoneQuery = vBinding.cbCheckUbicationMacrazona.isChecked

        userPreferencesModel.orderFavoritesByProximity =
            vBinding.cbSortFavZonesByProximity.isChecked

        userPreferencesModel.disableGlonass = vBinding.cbDisableGlonass.isChecked

        userPreferencesModel.numericInputFilter = vBinding.cbNumericInputFilter.isChecked

        if (!vBinding.spnGPSApp.selectedItemPosition.equals(null)) {
            userPreferencesModel.navigatorTypePosition = vBinding.spnGPSApp.selectedItemPosition
            userPreferencesModel.navigatorTypeId = vBinding.spnGPSApp.selectedItemId.toInt() + 1
        }

        if (!vBinding.spnOrderStyle.selectedItemPosition.equals(null)) {
            userPreferencesModel.orderStylePosition = vBinding.spnOrderStyle.selectedItemPosition
            userPreferencesModel.orderStyleId = vBinding.spnOrderStyle.selectedItemId.toInt() + 1
        }
        //MHS
        val selectedTextItem = (vBinding.spnMeetingSignTextColor.selectedItem as String?) ?: ""
        val selectedBackgroundItem = (vBinding.spnMeetingSignBackgroundColor.selectedItem as String?) ?: ""
        Logs.d(TAG, "MHS selectedTextItem $selectedTextItem")
        userPreferencesModel.meetingSignTextColor = ColorPickerEnum.fromValue(selectedTextItem, requireContext())?.argb ?: 0
        userPreferencesModel.meetingSignBackgroundColor = ColorPickerEnum.fromValue(selectedBackgroundItem, requireContext())?.argb ?: 0
    }


    private fun showDatePickerDialog(listener: (year: Int, month: Int, dayOfMonth: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = context?.let {
            DatePickerDialog(
                it, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    listener(selectedYear, selectedMonth, selectedDayOfMonth)
                }, year, month, dayOfMonth
            )
        }

        datePickerDialog?.setOnShowListener {
            val positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.text = context?.getString(R.string.btn_delete)
        }

        datePickerDialog?.show()
    }

    private fun setupDefaultPreferences() {
        setUpDefaultSound()

        setUpDefaultInvoice()

        setUpDefaultPinPad()

        setUpDefaultSumUp()

        setUpDefaultShifts()

        setUpDefaultSystem()

        setUpDefaultLocation()
    }

    private fun setUpDefaultLocation() {
        //Location
        //CheckBoxes
        vBinding.cbClosePendingTripsIfEmpty.isChecked = userPreferencesModel.closePendingTripsIfEmpty
        vBinding.cbCheckUbicationMacrazona.isChecked = userPreferencesModel.macroZoneQuery
        vBinding.cbSortFavZonesByProximity.isChecked =
            userPreferencesModel.orderFavoritesByProximity
        vBinding.cbDisableGlonass.isChecked = userPreferencesModel.disableGlonass
        vBinding.cbNumericInputFilter.isChecked = userPreferencesModel.numericInputFilter
        //Spinners
        vBinding.spnGPSApp.safeSetSelection(userPreferencesModel.navigatorTypePosition)
        vBinding.spnOrderStyle.safeSetSelection(userPreferencesModel.orderStylePosition)

        setUpMeetingSignSpinner()
    }

    private fun setUpMeetingSignSpinner() {
        var textColorIndex = ColorPickerEnum.entries.indexOfFirst {
            it.argb == userPreferencesModel.meetingSignTextColor
        }
        var backgroundColorIndex = ColorPickerEnum.entries.indexOfFirst { it.argb == userPreferencesModel.meetingSignBackgroundColor }
        if (textColorIndex == -1) {
            textColorIndex = 0
        }
        if (backgroundColorIndex == -1) {
            backgroundColorIndex = 0
        }

        vBinding.spnMeetingSignTextColor.safeSetSelection(textColorIndex)
        vBinding.spnMeetingSignBackgroundColor.safeSetSelection(backgroundColorIndex)
    }

    private fun setUpDefaultSystem() {
        //System
        //CheckBoxes
        vBinding.cbShowConfirmationOnAccept.isChecked = userPreferencesModel.showConfAcceptDispatch
        vBinding.cbShowConfirmationOnReject.isChecked = userPreferencesModel.showConfRejectDispatch
        vBinding.cbFloatingWindowOnBackground.isChecked = userPreferencesModel.useFloatingWindow
        vBinding.cbLightOffOnDispatched.isChecked = userPreferencesModel.lightOffOnDispatched
        vBinding.cbPrintDriverSubscriber.isChecked = userPreferencesModel.printDriverSubscriber
        //Spinners
        vBinding.spnScreenActive.safeSetSelection(userPreferencesModel.turnOffScreenPosition)
        vBinding.spnCloseMessage.safeSetSelection(userPreferencesModel.secondsToCloseMsgPosition)
        vBinding.spnCallDelay.safeSetSelection(userPreferencesModel.phoneCallTimerPosition)
        //EditText
        vBinding.etNoticePhone.setText(userPreferencesModel.phoneCall)
        //Buttons
        vBinding.btnSendLogs.setOnClickListener {
            Logs.d(TAG, "btnSendLogs onClick.")
            val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    iMainActivity.showToast(R.string.toast_enviando_logs)
                    vModel.sendLogs(response.editTextString)
                }
            }
            iMainActivity.openDialog(
                CustomDialog.CustomDialogModel(
                    title = getString(R.string.dialog_add_comment),
                    editText = "",
                    buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                ),
                callBack,
                fragmentManager = childFragmentManager
            )
        }

//        vBinding.btnRestoreDefault.setOnClickListener {
//            val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
//                if (response.buttonPressed == ButtonType.ACCEPT) {
//                    userPreferencesEntity = UserPreferencesEntity()
//                    vModel.insertUserPreferences(userPreferencesEntity)
//                    iMainActivity.navigateBack()
//                }
//            }
//            iMainActivity.openDialog(
//                CustomDialog.CustomDialogModel(
//                    title = resources.getString(R.string.dialog_restore_settings),
//                    description = resources.getString(R.string.dialog_restore_description),
//                    buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
//                ),
//                callBack
//            )
//        }
    }

    private fun setUpDefaultShifts() {
        //Shifts
        //Spinners
        vBinding.spnMinutesShift.safeSetSelection(userPreferencesModel.minutesBeforeWarningPosition)

        vModel.hasIngenicoInstalled()
        vModel.checkDriverTrunMode()

        if (userPreferencesModel.pinPadPrinterPosition != -1) {
            vBinding.spnPinPadPrinter.safeSetSelection(userPreferencesModel.pinPadPrinterPosition)
        }

        if (userPreferencesModel.fileFormatPosition != -1) {
            vBinding.spnExportFormat.safeSetSelection(userPreferencesModel.fileFormatPosition)
        }

        //Buttons
        vBinding.btnSelectDateEraseShifts.setOnClickListener {
            Logs.d(TAG, "btnSelectDateEraseShifts onClick.")
            showDatePickerDialog { year, month, dayOfMonth ->
                vModel.deleteShiftByDate(year, month, dayOfMonth)
                iMainActivity.showToast(R.string.shifts_deleted_toast)
            }

            vModel.checkShifts()
        }

        vBinding.btnChangePin.setOnClickListener {
            Logs.d(TAG, "btnChangePin onClick.")
            navigateDriverPin()
        }

        vBinding.btnUploadDriversPhoto.isEnabled = false
        vBinding.btnUploadDriversPhoto.setOnClickListener {
            Logs.d(TAG, "btnUploadDriversPhoto onClick.")
            //TODO photoPicker?
        }

        vBinding.cbControlHorario?.isChecked = userPreferencesModel.useTimeControl

    }

    private fun navigateDriverPin() {
        val navDeepLink =
            NavDeepLinkRequest.Builder.fromUri("android-app://ifac.td.taxi/changeDriverPinFragment/".toUri())
                .build()
        iMainActivity.navigateTo(navDeepLink)
    }

    private fun setUpDefaultInvoice() {
        //Invoice
        vBinding.cbUseExternalApp.isChecked = userPreferencesModel.useExternalApp
        vBinding.cbUseExternalApp.setOnCheckedChangeListener { _, isChecked ->
            Logs.d(TAG, "cbUseExternalApp isChecked: $isChecked")
            if (isChecked) {
                vBinding.cbUseCustomPaymentTimerWithExternalApp.visibility = View.VISIBLE
            } else {
                vBinding.cbUseCustomPaymentTimerWithExternalApp.visibility = View.GONE
                vBinding.cbUseCustomPaymentTimerWithExternalApp.isChecked = false
            }
        }
        if (userPreferencesModel.useExternalApp) {
            vBinding.cbUseCustomPaymentTimerWithExternalApp.visibility = View.VISIBLE
        }
        vBinding.cbUseCustomPaymentTimerWithExternalApp.isChecked = userPreferencesModel.useCustomPaymentTimerWithExternalApp
        vBinding.cbPrintRedSysTicketAlways.isChecked = userPreferencesModel.printRedSysCommerceTicketAlways

        val isRedSysConfigured = userPreferencesModel.pinPadSerialNumber.isNotBlank() || userPreferencesModel.pinPadTypeId == 2
        vBinding.cbPrintRedSysTicketAlways.visibility = if (isRedSysConfigured) View.VISIBLE else View.GONE

        //EditTexts
        vBinding.etIssuerDirection.setText(userPreferencesModel.invoiceIssuerAddress)
        vBinding.etPostalCode.setText(userPreferencesModel.invoiceIssuerZipCode)
        vBinding.etCityIssuer.setText(userPreferencesModel.invoiceIssuerCity)
        vBinding.etInvoiceSeries.setText(userPreferencesModel.invoiceSerialNumber)
        vBinding.etInvoiceNumber.setText(userPreferencesModel.invoiceNumber.toString())
        vBinding.ibInfoInvoices.setOnClickListener {
            Logs.d(TAG, "ibInfoInvoices onClick.")
            val callBack: (CustomDialog.CustomDialogResponse) -> Unit = { response ->

            }
            iMainActivity.openDialog(
                CustomDialog.CustomDialogModel(
                    title = getString(R.string.dialog_information),
                    description = getString(R.string.dialog_invoice_direction),
                    buttons = arrayListOf(ButtonType.ACCEPT),
                ),
                callBack
            )
        }

    }

    private fun setUpDefaultPinPad() {
        //PinPad
        //Spinner
        vBinding.spnTypePinPad.safeSetSelection(userPreferencesModel.pinPadTypePosition)
        //EditText
        vBinding.etNumberPinPad.setText(userPreferencesModel.pinPadSerialNumber)
    }

    private fun setUpDefaultSumUp() {
        //EditText
        vBinding.etNumberSumUp.setText(userPreferencesModel.sumUpMerchantCode)
    }

    private fun setUpDefaultSound() {
        //Sounds
        //CheckBoxes
        vBinding.cbSoundUbicationDisabledOnTX.isChecked = userPreferencesModel.beepNoBt
        vBinding.cbVibrationAndSound.isChecked = userPreferencesModel.vibrateNoBT
        vBinding.cbTTSDispatch.isChecked = userPreferencesModel.ttsDispatch
        vBinding.cbTTSLocation.isChecked = userPreferencesModel.ttsLocation
        vBinding.cbTTSForMessages.isChecked = userPreferencesModel.ttsMsg
        vBinding.cbTTSForDialogs?.isChecked = userPreferencesModel.ttsDialog
        //Buttons
        vBinding.btnNotificationSound.text =
            if (userPreferencesModel.notificationUri == null) {
                resources.getString(R.string.dialog_no_custom_sound)
            } else {
                userPreferencesModel.notificationTitle
            }

        vBinding.btnNotificationSound.setOnClickListener {
            Logs.d(TAG, "btnNotificationSound onClick.")
            selectNotification()
        }

        vBinding.btnIncomingSound.text =
            if (userPreferencesModel.ringtoneUri == null) {
                resources.getString(R.string.dialog_no_custom_sound)
            } else {
                userPreferencesModel.ringtoneTitle
            }

        vBinding.btnIncomingSound.setOnClickListener {
            Logs.d(TAG, "btnIncomingSound onClick.")
            selectRingtone()
        }
        //Spinner
        vBinding.spnLocutionBlinds.safeSetSelection(userPreferencesModel.blindLocutionPosition)
    }

    private fun setupToggleOnClick(
        card: ConstraintLayout,
        content: ConstraintLayout,
        image: ImageView,
    ) {
        card.setOnClickListener {
            Logs.d(TAG, "card onClick.")
            vModel.userInfoFlow.value?.let { isLoggedIn ->
                if (card == vBinding.clInvoicesCard) {
                    if (isLoggedIn) {
                        if (content.visibility == View.GONE) {
                            content.visibility = View.VISIBLE
                            image.setImageResource(R.drawable.arrow_up_white)
                        } else {
                            content.visibility = View.GONE
                            image.setImageResource(R.drawable.arrow_down_white)
                        }
                    } else {
                        iMainActivity.showToast(R.string.toast_login)
                    }
                } else {
                    if (content.visibility == View.GONE) {
                        content.visibility = View.VISIBLE
                        image.setImageResource(R.drawable.arrow_up_white)
                    } else {
                        content.visibility = View.GONE
                        image.setImageResource(R.drawable.arrow_down_white)
                    }
                }
            }
        }
    }

    private fun selectNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            lastSelectedType = RingtoneManager.TYPE_NOTIFICATION
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, userPreferencesModel.notificationUri ?: Settings.System.DEFAULT_NOTIFICATION_URI)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, lastSelectedType)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.strNotificationSelection))
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)

            try {
                resultLauncher.launch(intent)
            } catch (e: Exception) {
                Logs.e(TAG, "ERROR REQUEST_CODE_NOTIFICATION_PICKER $e")
            }
        }
    }

    private fun selectRingtone() {
        CoroutineScope(Dispatchers.IO).launch {
            lastSelectedType = RingtoneManager.TYPE_RINGTONE
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, userPreferencesModel.ringtoneUri ?: Settings.System.DEFAULT_RINGTONE_URI)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, lastSelectedType)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.strRingtoneSelection))
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)

            try {
                resultLauncher.launch(intent)
            } catch (e: Exception) {
                Logs.e(TAG, "ERROR REQUEST_CODE_RINGTONE_PICKER $e")
            }
        }
    }

    private fun handleSelectedRingtoneOrNotification(data: Intent?) {
        CoroutineScope(Dispatchers.IO).launch {
            Logs.d(TAG, "handleSelectedRingtoneOrNotification() called with data: $data")

            val selectedRingtoneUri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }

            Logs.d(TAG, "handleSelectedRingtoneOrNotification: Selected URI: $selectedRingtoneUri")

            if (selectedRingtoneUri != null) {
                    val track = RingtoneManager.getRingtone(context, selectedRingtoneUri)
                    val trackTitle = track.getTitle(context)

                Logs.d(TAG, "handleSelectedRingtoneOrNotification: Track title: $trackTitle")
                Logs.d(TAG, "handleSelectedRingtoneOrNotification: Ringtone type (from lastSelectedType): $lastSelectedType")

                when (lastSelectedType) {
                    RingtoneManager.TYPE_RINGTONE -> {
                        Logs.d(TAG, "handleSelectedRingtoneOrNotification: Setting ringtone")
                        userPreferencesModel.ringtoneUri = selectedRingtoneUri
                        userPreferencesModel.ringtoneTitle = trackTitle
                        withContext(Dispatchers.Main) {
                            vBinding.btnIncomingSound.text = trackTitle
                        }
                    }
                    RingtoneManager.TYPE_NOTIFICATION -> {
                        Logs.d(TAG, "handleSelectedRingtoneOrNotification: Setting notification sound")
                        userPreferencesModel.notificationUri = selectedRingtoneUri
                        userPreferencesModel.notificationTitle = trackTitle
                        withContext(Dispatchers.Main) {
                            vBinding.btnNotificationSound.text = trackTitle
                        }
                    }
                    else -> {
                        Logs.e(TAG, "handleSelectedRingtoneOrNotification: Unknown ringtone type selected")
                    }
                }
            } else {
                Logs.e(TAG, "handleSelectedRingtoneOrNotification: No ringtone selected or URI is null")
            }
        }
    }



    private fun checkPhoneNumber() {
        if (vBinding.etNoticePhone.text.isNotBlank()) {
            iMainActivity.requestPermission(PermissionRequest.PermissionTypeList.PERMISSION_PHONE_CALL)
        }
    }

    private fun setupSpinnerAdapter(spinner: Spinner) {
        val arr = when (spinner) {
            vBinding.spnCallDelay -> resources.getStringArray(R.array.arr_one_to_sixty)
            vBinding.spnCloseMessage -> resources.getStringArray(R.array.arr_seconds_to_close_msg_list)
            vBinding.spnPinPadPrinter -> resources.getStringArray(R.array.arr_pin_pad_printers)
            vBinding.spnExportFormat -> resources.getStringArray(R.array.arr_file_format)
            vBinding.spnGPSApp -> resources.getStringArray(R.array.arr_navigators)
            vBinding.spnLocutionBlinds -> resources.getStringArray(R.array.arr_blind_locution_options)
            vBinding.spnMinutesShift -> resources.getStringArray(R.array.arr_one_to_thirty)
            vBinding.spnTypePinPad -> resources.getStringArray(R.array.arr_pin_pad_types)
            vBinding.spnOrderStyle -> resources.getStringArray(R.array.arr_order_style_options)
            vBinding.spnScreenActive -> resources.getStringArray(R.array.arr_screen_active)
            else -> arrayOf("1", "2", "3")
        }
        val adapter = CustomSpinnerAdapter(requireContext(), R.layout.custom_spinner_item, arr)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // Do something when an item is selected
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do something when no item is selected
            }
        }
    }

    private fun Spinner.safeSetSelection(position: Int) {
        val adapterCount = adapter?.count ?: 0
        if (adapterCount > 0) {
            setSelection(position.coerceIn(0, adapterCount - 1))
        }
    }
}

