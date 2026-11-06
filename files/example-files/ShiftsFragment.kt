package ifac.td.taxi.ui.screen

import ifac.td.taxi.ui.adapter.ShiftsAdapter
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentShiftsBinding
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.EXTRA_INTENT_PURPOSE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_OPEN_CSV_FILE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_OPEN_XLS_FILE
import ifac.td.taxi.framework.sdk.listeners.PaginationScrollListener
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.ShiftsViewModel
import ifac.td.taxi.viewmodel.model.ShiftOrderOptions
import ifac.td.taxi.viewmodel.model.SortState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShiftsFragment :
    BaseFragment<FragmentShiftsBinding, ShiftsViewModel>(
        R.layout.fragment_shifts
    ) {

    private val vModel: ShiftsViewModel by viewModel()
    private lateinit var adapter: ShiftsAdapter

    private val TAG = "ShiftsFragment"

    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabExport: FloatingActionButton
    private lateinit var fabTrash: FloatingActionButton

    private var loadingPage: Boolean = false
    private var page: Int = 0

    private val PAGE_SIZE = 25;


    private var translationYaxis: Float = 100f

    private var open: Boolean = false

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentShiftsBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setupRecyclerView()
        setupMenu()
        createScrollListener()
        checkLoading()
//        vModel.insertShifts()
        setupSortButtons()
    }

    override fun onResume() {
        super.onResume()
        vModel.checkShifts()
    }

    private fun setupMenu() {
        fabMain = vBinding.fabOptions
        fabExport = vBinding.fabExport
        fabTrash = vBinding.fabTrash

        fabExport.alpha = 0f
        fabTrash.alpha = 0f

        fabExport.translationY = translationYaxis
        fabTrash.translationY = translationYaxis

        fabMain.setImageResource(R.drawable.arrow_up)

        fabMain.setOnClickListener {
            Logs.d("ShiftsFragment", "fabMain onClick.")
            adapter.activateSelectionMode()
            if (open) {
                closeMenu()
            } else {
                openMenu()
            }

            vBinding.cbSelectedHidden.visibility = if (open) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        fabExport.setOnClickListener {
            Logs.d("ShiftsFragment", "fabExport onClick.")
            exportSelectedItems()
        }

        fabTrash.setOnClickListener {
            Logs.d("ShiftsFragment", "fabTrash onClick.")
            deleteSelectedItems()
        }
    }

    private fun openMenu() {
        open = !open
        fabMain.animate().rotation(180f).setDuration(300).start()
        fabExport.animate().translationY(0f).alpha(1f).setDuration(300).start()
        fabTrash.animate().translationY(0f).alpha(1f).setDuration(300).start()
    }

    private fun closeMenu() {
        open = !open
        fabMain.animate().rotation(0f).setDuration(300).start()
        fabExport.animate().translationY(translationYaxis).alpha(0f).setDuration(300).start()
        fabTrash.animate().translationY(translationYaxis).alpha(0f).setDuration(300).start()
    }

    private fun setupRecyclerView() {
        adapter =
            ShiftsAdapter(requireContext(), R.layout.row_shift) { shift ->
                val direction =
                    ShiftsFragmentDirections.actionShiftsFragmentToShiftDetailFragment(shift)
                iMainActivity.navigateTo(direction)
            }

        vBinding.rvShifts.layoutManager = LinearLayoutManager(requireContext())
        vBinding.rvShifts.adapter = adapter
//        (vBinding.rvShifts.adapter as ifac.td.taxi.ui.adapter.ShiftsAdapter).resetData()

    }

    private fun setupSortButtons() {
        vBinding.btnID.setOnClickListener {
            handleSortButtonClick(ShiftOrderOptions.ID, vBinding.ivSortId)
        }

        vBinding.btnAmount.setOnClickListener {
            handleSortButtonClick(ShiftOrderOptions.AMOUNT, vBinding.ivSortAmount)
        }
    }

    private fun handleSortButtonClick(newOrder: ShiftOrderOptions, sortIcon: ImageView) {
        vBinding.rvShifts.smoothScrollToPosition(0)
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
            vModel.sortShiftsBy(vModel.currentShifts, ShiftOrderOptions.NONE, true)
        } else {
            vModel.sortShiftsBy(vModel.currentShifts, nextState.option, nextState.isAscending!!)
        }

        Logs.d(TAG, "Sort clicked: ${nextState.option}, ascending: ${nextState.isAscending}")
    }

    private fun refreshHeaderImages() {
        vBinding.apply {
            ivSortId.visibility = View.INVISIBLE
            ivSortAmount.visibility = View.INVISIBLE
        }
    }

    private fun deleteSelectedItems() {
        val selectedItems = adapter.getSelectedItems()
        if (selectedItems.isNotEmpty()) {

            selectedItems.forEach { shift ->
                vModel.deleteShift(shift)
                Logs.d(TAG, "Deleted shiftStatus with ID: ${shift.id}")
            }

            adapter.removeSelectedItems()

        } else {
            iMainActivity.showToast(R.string.toast_selecciona_turno_eliminar)
        }
    }

    private fun exportSelectedItems() {
        if (adapter.getSelectedItems().isNotEmpty()) {
            vModel.exportShifts(adapter.getSelectedItems())
        } else {
            iMainActivity.showToast(R.string.toast_selecciona_turno_exportar)
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.shiftFlow.collect { shifts ->
                        Logs.d(TAG, "shiftFlow shifts = $shifts")
                        shifts?.let { shiftList ->
                            adapter.setShifts(shiftList)
//                            adapter.notifyItemRangeChanged((vBinding.rvShifts.layoutManager as LinearLayoutManager).findLastVisibleItemPosition(), shiftList.size)
                        }
                    }
                }

                launch {
                    vModel.loadingFlow.collect { isLoading ->
                        loadingPage = isLoading
                        checkLoading()
                    }
                }

                launch {
                    vModel.hasShiftsFlow.collect {
                        if (it) {
                            if (vModel.shiftFlow.value.isEmpty()) {
                                vModel.addShifts(page)
                            }
                        } else {
                            vModel._shiftFlow.emit(emptyList())
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
                                    Logs.e(TAG, "intentFlow: PURPOSE_OPEN_CSV_FILE Exception: ${e.message}")
                                    iMainActivity.showToast(R.string.start_activity_error_toast)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkLoading() {
        if (loadingPage) {
            vBinding.pbShifts.visibility = View.VISIBLE
        } else {
            vBinding.pbShifts.visibility = View.GONE
        }
    }

    private fun createScrollListener() {
        val listener = object :
            PaginationScrollListener(vBinding.rvShifts.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems(totalItemCount: Int) {
                if (!loadingPage) {
                    page++
                    vModel.addShifts(page)
                }
            }

            override fun isLoading(): Boolean {
                return loadingPage
            }

            override fun getPageSize(): Int {
                return PAGE_SIZE
            }
        }

        vBinding.rvShifts.addOnScrollListener(listener)
    }

}