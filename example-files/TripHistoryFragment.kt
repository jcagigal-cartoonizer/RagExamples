package ifac.td.taxi.ui.screen

import TripsHistoryAdapter
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentTripHistoryBinding
import ifac.td.taxi.framework.sdk.listeners.PaginationScrollListener
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.CustomDialog.CustomDialogModel
import ifac.td.taxi.viewmodel.TripHistoryViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class TripHistoryFragment :
    BaseFragment<FragmentTripHistoryBinding, TripHistoryViewModel>(R.layout.fragment_trip_history) {

    private val vModel: TripHistoryViewModel by viewModel()

    private val TAG = "TripHistoryFragment"

    private lateinit var adapter: TripsHistoryAdapter

    private var page: Int = 0

    private val PAGE_SIZE = 25

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentTripHistoryBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        setButtons()
        setupRecyclerView()
        createScrollListener()
        vModel.getTrips(page)
    }

    private fun setButtons() {
        vBinding.apply {

           btnBack.setAction {
                // iMainActivity.navigateBack()
               iMainActivity.navigateTo(TripHistoryFragmentDirections.actionTripHistoryFragmentToReceiptHistoryFragment(-1))
            }

           btnAll.setAction {
                adapter.selectAllItems()
            }

           btnDelete.setAction {
                val selectedTrips = adapter.getSelectedItems()
                vModel.deleteSelectedTrips(selectedTrips) {
                    adapter.removeSelectedItems()
                }

            }
        }
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            iMainActivity.navigateTo(TripHistoryFragmentDirections.actionTripHistoryFragmentToReceiptHistoryFragment(-1))
        }
    }

    private fun setupRecyclerView() {
        adapter = TripsHistoryAdapter(requireContext(), R.layout.row_trip) { trip, position ->
            if (trip.ticketBufferNoCopy.isNullOrBlank()) {
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                    Logs.d(TAG, "Trip without ticket dialog closed")
                    /*if (it.buttonPressed == ButtonType.ACCEPT) {
                        iMainActivity.navigateTo(
                            TripHistoryFragmentDirections.actionTripHistoryFragmentToReceiptHistoryFragment(
                                trip.id
                            )
                        )
                    }*/
                }

                iMainActivity.openDialog(
                    CustomDialogModel(
                        title = getString(R.string.warning),
                        buttons = arrayListOf(
                            ButtonType.CANCEL, ButtonType.ACCEPT
                        ),
                        description = getString(R.string.dialog_no_ticket_warning_description),
                    ),
                    callback
                )
            } else {
//                vModel.checkPrintTicket(trip, position == 0)
                iMainActivity.navigateTo(TripHistoryFragmentDirections.actionTripHistoryFragmentToReceiptHistoryFragment(trip.id))
            }
        }
        vBinding.rvTripsHistory.layoutManager = LinearLayoutManager(requireContext())
        vBinding.rvTripsHistory.adapter = adapter
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.tripListFlow.collect { trips ->
                        trips?.let { tripList ->
                            adapter.addTrips(tripList)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                launch {
                    vModel.loadingFlow.collect { isLoading ->
                        vBinding.pbTripsHistory.isVisible = isLoading
                    }
                }
            }
        }
    }

    private fun createScrollListener() {
        val listener = object : PaginationScrollListener(vBinding.rvTripsHistory.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems(totalItemCount: Int) {
                if (!vModel.loadingFlow.value && !vModel.isLastPage) {
                    page++
                    vModel.getTrips(page)
                }
            }


            override fun isLoading(): Boolean {
                return vModel.loadingFlow.value
            }

            override fun getPageSize(): Int {
                return PAGE_SIZE
            }
        }

        vBinding.rvTripsHistory.apply {
            addOnScrollListener(listener)
        }
    }
}