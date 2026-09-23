package ifac.td.taxi.ui.screen

import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentShiftDetailBinding
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.ACTION_EXPORT_FILE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.ACTION_SEND_FILE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.EXTRA_INTENT_PURPOSE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_OPEN_CSV_FILE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_OPEN_XLS_FILE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_SEND_EMAIL
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.TripsInShiftAdapter
import ifac.td.taxi.viewmodel.ShiftDetailViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.viewmodel.model.ShiftOrderOptions
import ifac.td.taxi.viewmodel.model.SortState

class ShiftDetailFragment : BaseFragment<FragmentShiftDetailBinding, ShiftDetailViewModel>(
    R.layout.fragment_shift_detail
) {
    private val TAG = "ShiftDetailFragment"

    private val vModel: ShiftDetailViewModel by viewModel()
    private val shiftArgs: ShiftDetailFragmentArgs by navArgs()

    private lateinit var adapter: TripsInShiftAdapter

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentShiftDetailBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        vModel.getTripsFromShiftId(shiftArgs.shiftEntity.id)
        setupRecyclerView()

        setupSortButtons()

    }

    private fun setButtons() {
        vBinding.apply {
            btnExport.setAction {
                vModel.exportFile(shiftArgs.shiftEntity.id, ACTION_EXPORT_FILE)
            }

            btnEmail.setAction {
                vModel.exportFile(shiftArgs.shiftEntity.id, ACTION_SEND_FILE)
            }

            btnPrint.setAction {
                vModel.printShift(shiftArgs.shiftEntity.id)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TripsInShiftAdapter(requireContext(), R.layout.row_trip_in_shift) { trip ->

        }

        vBinding.rvTripsInShift?.layoutManager = LinearLayoutManager(requireContext())
        vBinding.rvTripsInShift?.adapter = adapter
    }

    private fun setupSortButtons() {
        vBinding.btnId.setOnClickListener {
            handleSortButtonClick(ShiftOrderOptions.ID, vBinding.ivSortId)
        }

        vBinding.btnTripAmount.setOnClickListener {
            handleSortButtonClick(ShiftOrderOptions.AMOUNT, vBinding.ivSortTripAmount)
        }

        vBinding.btnInitHour.setOnClickListener {
            handleSortButtonClick(ShiftOrderOptions.START_DATE, vBinding.ivSortInitHour)
        }

        vBinding.btnDistance.setOnClickListener {
            handleSortButtonClick(ShiftOrderOptions.DISTANCE, vBinding.ivSortDistance)
        }
    }

    private fun handleSortButtonClick(newOrder: ShiftOrderOptions, sortIcon: ImageView) {
        val nextState = when {
            vModel.currentSortState.option != newOrder -> {
                SortState(newOrder, true)
            }
            vModel.currentSortState.isAscending == true -> {
                SortState(newOrder, false)
            }
            vModel.currentSortState.isAscending == false -> {
                SortState(ShiftOrderOptions.NONE) // isAscending se queda en null
            }
            else -> {
                SortState(newOrder, true)
            }
        }

        refreshHeaderImages()
        when (nextState.isAscending) {
            true -> {
                sortIcon.setImageResource(R.drawable.round_arrow_drop_up_24)
                sortIcon.visibility = View.VISIBLE
            }
            false -> {
                sortIcon.setImageResource(R.drawable.round_arrow_drop_down_24)
                sortIcon.visibility = View.VISIBLE
            }
            null -> {
                sortIcon.visibility = View.INVISIBLE
            }
        }

        vModel.currentSortState = nextState
        if (nextState.option == ShiftOrderOptions.NONE) {
            vModel.sortShiftTripsBy(shiftArgs.shiftEntity.id, ShiftOrderOptions.NONE, true)
        } else {
            vModel.sortShiftTripsBy(shiftArgs.shiftEntity.id, nextState.option, nextState.isAscending!!)
        }

        Logs.d("ShiftDetailFragment", "Sort clicked: ${nextState.option}, ascending: ${nextState.isAscending}")
    }

    private fun refreshHeaderImages() {
        vBinding.apply {
            ivSortId.visibility = View.INVISIBLE
            ivSortInitHour.visibility = View.INVISIBLE
            ivSortDistance.visibility = View.INVISIBLE
            ivSortTripAmount.visibility = View.INVISIBLE
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.tripFlow.collect { shifts ->
                        shifts?.let { shiftList ->
                            adapter.setTripsInShift(shiftList)
                        }
                    }
                }

                launch {
                    vModel.intentFlow.collect { intent ->
                        when (intent.getStringExtra(EXTRA_INTENT_PURPOSE)) {
                            PURPOSE_OPEN_CSV_FILE, PURPOSE_OPEN_XLS_FILE -> {
                                Logs.d(TAG, "intentFlow: Attempting to open a CSV or XLS file")
                                try {
                                    context?.startActivity(intent)
                                    Logs.d(TAG, "intentFlow: Successfully opened the file")
                                } catch (e: Exception) {
                                    Logs.d(TAG, "intentFlow: PURPOSE_OPEN_CSV_FILE Exception: ${e.message}")
                                    iMainActivity.showToast(R.string.start_activity_error_toast)
                                }
                            }

                            PURPOSE_SEND_EMAIL -> {
                                Logs.d(TAG, "intentFlow: Attempting to send an email")
                                try {
                                    context?.startActivity(Intent.createChooser(intent, context?.getString(R.string.send_email_title)))
                                    Logs.d(TAG, "intentFlow: Email chooser started successfully")
                                } catch (e: Exception) {
                                    Logs.d(TAG, "intentFlow: PURPOSE_SEND_EMAIL Exception: ${e.message}")
                                    iMainActivity.showToast(R.string.start_activity_error_toast)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}