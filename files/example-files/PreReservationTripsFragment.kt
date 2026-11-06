package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.interfacom.sdk.taximeter.bravocomm.models.prereservations.Prereservation
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPreReservationTripsBinding
import ifac.td.taxi.repository.connections.receivers.utils.getDateTimeFromISO8601
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.PreReservationTripsAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.PreReservationTripsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreReservationTripsFragment :
    BaseFragment<FragmentPreReservationTripsBinding, PreReservationTripsViewModel>(
        R.layout.fragment_pre_reservation_trips
    ) {

    private val TAG = "PreReservationTripsFragment"

    private val vModel: PreReservationTripsViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    override fun getViewModel(): PreReservationTripsViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentPreReservationTripsBinding {
        return FragmentPreReservationTripsBinding.inflate(layoutInflater)
    }

    private val preReservationAdapter: PreReservationTripsAdapter by lazy {
        val callback: (Prereservation) -> Unit = {
            if (it.isAssignedToMyCar) {
                if (it.isTooLateToCancelAssignment) {
                    context?.let { context ->
                        iMainActivity.showToast(R.string.can_not_cancel_reservation)
                    }
                } else {
                    openCancelPreReserveTripDialog(it)
                }
            } else if (it.isAvailable) {
                openPreReserveTripDialog(it)
            }
        }
        PreReservationTripsAdapter(requireContext(), callback)
    }

    private fun openPreReserveTripDialog(preReservation: Prereservation) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
            if (it.buttonPressed == ButtonType.ACCEPT) {
                vModel.preReserveTrip(preReservation, false)
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.dialog_asignar_trip_title),
                description = getString(
                    R.string.pickup_time_in_zone_placeholder_str,
                    preReservation.pickupTime.getDateTimeFromISO8601(),
                    preReservation.pickupZone
                ),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }

    private fun openCancelPreReserveTripDialog(preReservation: Prereservation) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
            if (it.buttonPressed == ButtonType.ACCEPT) {
                vModel.preReserveTrip(preReservation, true)
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.dialog_desasignar_trip_title),
                description = getString(
                    R.string.pickup_time_in_zone_placeholder_str,
                    preReservation.pickupTime.getDateTimeFromISO8601(),
                    preReservation.pickupZone
                ),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT,
                )
            ),
            callback,
            fragmentManager = childFragmentManager
        )
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        vBinding.rvPreReservationTrips.apply {
            adapter = preReservationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.preReservationTripsListFlow.collect {
                        it?.let { list ->
                            preReservationAdapter.submitList(list)
                        }
                    }
                }

                launch {
                    vModel.bidFlow.collect { (wasCompleted, cancelPreReservation) ->
                        Logs.d(TAG, "bidFlow called from PreReservationTripsFragment wasCompleted: $wasCompleted, requestType: ${if (!cancelPreReservation) "PreReserve" else "Cancel PreReservation" }")

                        val dialogCallback: (CustomDialog.CustomDialogResponse) -> Unit = {
                            if (it.buttonPressed == ButtonType.ACCEPT) { }
                        }

                        if (wasCompleted && !cancelPreReservation) {
                            iMainActivity.openDialog(
                                CustomDialog.CustomDialogModel(
                                    title = getString(R.string.prereservation),
                                    description = getString(R.string.prereservation_dialog_assigned),
                                    buttons = arrayListOf(ButtonType.ACCEPT)
                                ),
                                dialogCallback
                            )
                        } else if (wasCompleted && cancelPreReservation) {
                            iMainActivity.openDialog(
                                CustomDialog.CustomDialogModel(
                                    title = getString(R.string.prereservation),
                                    description = getString(R.string.prereservation_dialog_unassigned),
                                    buttons = arrayListOf(ButtonType.ACCEPT)
                                ),
                                dialogCallback
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        vModel.removeHandlerCallback()
        super.onPause()
    }
}