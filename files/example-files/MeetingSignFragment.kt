package ifac.td.taxi.ui.screen

import android.content.pm.ActivityInfo
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentMeetingSignBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.MeetingSignViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MeetingSignFragment : BaseFragment<FragmentMeetingSignBinding, MeetingSignViewModel>(
    R.layout.fragment_meeting_sign
) {
    private val TAG = "MeetingSignFragment"
    private val vModel: MeetingSignViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val safeArgs: MeetingSignFragmentArgs by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentMeetingSignBinding.inflate(layoutInflater)

    override fun setupComponents() {
        setComponentsColors(safeArgs.textColor, safeArgs.backgroundColor)
        iMainActivity.showHeader(false)
        iMainActivity.showBottomBar(false)
        iMainActivity.requestedOrientationCustom(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        setUpFabButtons()
    }

    private fun setComponentsColors(textColor: Int, backgroundColor: Int) {
        if (textColor != 0) {
            vBinding.tvMeetingSign.setTextColor(textColor)
        }
        if (backgroundColor != 0) {
            vBinding.root.setBackgroundColor(backgroundColor)
        }
    }

    private fun setUpFabButtons() {
        vBinding.apply {
            fabEdit.alpha = 0f
            fabDispatch.alpha = 0f

            fabEdit.translationY = 100f
            fabDispatch.translationY = 100f

            fabOptions.setOnClickListener {
                Logs.d(TAG, "fabMain onClick.")
                if (fabMenuOpen) {
                    closeMenu()
                } else {
                    openMenu()
                }

            }
            fabEdit.setOnClickListener {
                Logs.d(TAG, "fabEdit onClick.")
                openEditDialog()
            }
            var isInSettings = false
            activity?.let { activityNotNull ->
                val navHostFragment =
                    activityNotNull.supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val navController: NavController = navHostFragment.navController

                isInSettings = navController.currentDestination?.parent?.id == R.id.settings
            }

            Logs.d(TAG, "isInSettings $isInSettings")

            if (sharedViewModel.tripFlow.value?.fromDispatch == true && !isInSettings) {
                fabDispatch.setOnClickListener {
                    Logs.d(TAG, "fabDispatch onClick.")
                    //navigate to infoDispatch and clear this from navigation
                    iMainActivity.navigateTo(R.id.action_meetingSignFragment_to_infoDispatchFragment)
                }
            } else {
                context?.let {
                    fabDispatch.setImageDrawable(it.getDrawable(R.drawable.back))
                }
                fabDispatch.setOnClickListener {
                    Logs.d(TAG, "fabDispatch onClick.")
                    iMainActivity.navigateBack()
                }
            }

            closeMenu()
        }
    }

    private var fabMenuOpen = false

    private fun closeMenu() {
        fabMenuOpen = false
        vBinding.apply {
            fabOptions.animate().rotation(0f).setDuration(300).start()
            fabDispatch.animate().translationY(100f).alpha(0f).setDuration(300).start()
            fabEdit.animate().translationY(100f).alpha(0f).setDuration(300).start()
            fabDispatch.visibility = View.GONE
            fabEdit.visibility = View.GONE
        }

    }

    private fun openMenu() {
        fabMenuOpen = true
        vBinding.apply {
            fabDispatch.visibility = View.VISIBLE
            fabEdit.visibility = View.VISIBLE
            fabOptions.animate().rotation(180f).setDuration(300).start()
            fabDispatch.animate().translationY(0f).alpha(1f).setDuration(300).start()
            fabEdit.animate().translationY(0f).alpha(1f).setDuration(300).start()
        }

    }

    private fun openEditDialog(isCancellable: Boolean = true) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                if (response.editTextString != null && response.editTextString?.isNotBlank() == true) {
                    response.editTextString?.let { text ->
                        vModel.changeMessageSignText(text)
                    }
                } else {
                    iMainActivity.showToast(R.string.editText_error_no_text)
                }
            }
        }

        val currentName: String? = if (vModel.meetingSignNameFlow.value?.isNotBlank() == true) {
            vModel.meetingSignNameFlow.value
        } else {
            null
        }


        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.edit_message_sign_title),
                hint = getString(R.string.message_sign_hint),
                editText = currentName,
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT,
                ),
                isCancellable = isCancellable
            ), response = callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun setMeetingSignText(text: String) {
        vBinding.tvMeetingSign.setSignText(text)
    }

    fun TextView.setSignText(text: String) {
        post {
            val availableWidth = width - paddingLeft - paddingRight
            val availableHeight = height - paddingTop - paddingBottom

            val words = text.split(" ")
            val paint = TextPaint(paint)

            var low = 10f
            var high = 1000f
            var bestSize = low

            while (low <= high) {
                val mid = (low + high) / 2
                paint.textSize = mid

                // Check each word fits width (prevents Angelic / a)
                val wordTooWide = words.any {
                    paint.measureText(it) > availableWidth
                }

                if (wordTooWide) {
                    high = mid - 1
                    continue
                }

                val layout = StaticLayout.Builder
                    .obtain(text, 0, text.length, paint, availableWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .build()

                if (layout.height <= availableHeight) {
                    bestSize = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }

            setTextSize(TypedValue.COMPLEX_UNIT_PX, bestSize)
            setText(text)
        }
    }

    var lastShiftStatus: Int? = null

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.meetingSignNameFlow.collect {
                        it?.let { customerName ->
                            if (customerName.isNotBlank()) {
                                //show name
                                setMeetingSignText(customerName)
                            } else {
                                //ask for name
                                openEditDialog(false)
                            }
                        }
                    }
                }
                launch {
                    sharedViewModel.shiftStatusFlow.collect {
                        if (vModel.checkShiftStatusChangeToHired(lastShiftStatus, it?.currentStatus)) {
                            activity?.let { activityNotNull ->
                                val navHostFragment =
                                    activityNotNull.supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                                val navController: NavController = navHostFragment.navController

                                val isInSettings = navController.currentDestination?.parent?.id == R.id.settings
                                if (!isInSettings && sharedViewModel.tripFlow.value?.fromDispatch == true) {
                                    Logs.d(TAG, "navigating back, shiftStatus changed to hired")
                                    iMainActivity.navigateTo(R.id.action_meetingSignFragment_to_infoDispatchFragment)

                                }
                            }
                        }
                        lastShiftStatus = it?.currentStatus
                    }
                }
            }
        }
    }
}