package ifac.td.taxi.ui.screen

import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentNewDestinationMapBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.NewDirectionsAdapter
import ifac.td.taxi.ui.model.RoutePointModel
import ifac.td.taxi.viewmodel.DestinationMapViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.getDispatchNumber
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class DestinationMapFragment : BaseFragment<FragmentNewDestinationMapBinding, DestinationMapViewModel>(R.layout.fragment_new_destination_map) {

    private val vModel: DestinationMapViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()

    private val TAG = "DestinationMapFragment"

    private lateinit var adapter: NewDirectionsAdapter

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentNewDestinationMapBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        initRecyclerView()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.multiDispatchFlow.collect { arDispatches ->
                        Logs.d(TAG, "dispatchFlow: Starting to collect from multiDispatchFlow.")
                        val dispatchColorMap = mutableMapOf<String, Int>()
                        var colorIndex = 0

                        Logs.d(TAG, "dispatchFlow: Collected new dispatch list with size: ${arDispatches?.size ?: 0}")

                        val routePoints = mutableListOf<RoutePointModel>()

                        val arrColors = intArrayOf(
                            ContextCompat.getColor(
                                requireContext(),R.color.routes_one
                            ),ContextCompat.getColor(
                                requireContext(),R.color.grey_divider
                            ),ContextCompat.getColor(
                                requireContext(),R.color.switch_thumb_on
                            ),ContextCompat.getColor(
                                requireContext(),R.color.routes_four
                            )
                        )

                        arDispatches?.forEachIndexed { index, infoDispatchModel ->
                            val dispatchNumber = infoDispatchModel.getDispatchNumber() ?: "Default"
                            val color = dispatchColorMap.getOrPut(dispatchNumber) {
                                val assignedColor = arrColors[colorIndex % arrColors.size]
                                Logs.d(TAG, "dispatchFlow: Assigned color $assignedColor to dispatchNumber: $dispatchNumber")
                                colorIndex++
                                assignedColor
                            }

                            val pickUpAddress = infoDispatchModel.pickUpAdress ?: ""
                            val pickUpCoordinates = infoDispatchModel.pickUpCoordinates ?: ""

                            if (pickUpAddress.isNotEmpty()) {
                                Logs.d(TAG, "dispatchFlow: Adding PICK_UP point for dispatchNumber: $dispatchNumber")
                                routePoints.add(
                                    RoutePointModel(
                                        type = RoutePointModel.RoutePointType.PICK_UP,
                                        address = pickUpAddress,
                                        coordinatesTag = pickUpCoordinates,
                                        order = infoDispatchModel.orderPickUp,
                                        dispatchNumber = infoDispatchModel.getDispatchNumber(),
                                        titleColor = color
                                    )
                                )
                            } else {
                                Logs.d(TAG, "dispatchFlow: pickUpAddress is empty for dispatchNumber: $dispatchNumber")
                            }

                            val destinyAddressList = infoDispatchModel.destinyAdress ?: emptyList()
                            val destinyCoordinatesList = infoDispatchModel.dropOffCoordinates ?: emptyList()

                            if (destinyAddressList.size == destinyCoordinatesList.size) {
                                destinyAddressList.forEachIndexed { addrIndex, address ->
                                    val coordinates = destinyCoordinatesList[addrIndex]
                                    Logs.d(TAG, "dispatchFlow: Adding DROP_OFF point #$addrIndex for dispatchNumber: $dispatchNumber")
                                    routePoints.add(
                                        RoutePointModel(
                                            type = RoutePointModel.RoutePointType.DROP_OFF,
                                            address = address,
                                            coordinatesTag = coordinates,
                                            order = infoDispatchModel.orderDropOff,
                                            dispatchNumber = infoDispatchModel.getDispatchNumber(),
                                            titleColor = color
                                        )
                                    )
                                }
                            } else {
                                Logs.e(TAG, "dispatchFlow: Mismatch between addressList and coordinatesList sizes for dispatchNumber: $dispatchNumber")
                            }
                        }

                        if (routePoints.isNotEmpty()) {
                            Logs.d(TAG, "dispatchFlow: Updating adapter with ${routePoints.size} route points.")
                            adapter.setRoutePoints(routePoints)
                        } else {
                            Logs.d(TAG, "dispatchFlow: routePoints list is empty. No update performed.")
                        }
                    }
                }

                launch {
                    vModel.navigatorFlow.collect {
                        it?.let { intent ->
                            iMainActivity.launchIntent(intent)
                        }
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        adapter =
            NewDirectionsAdapter(requireContext(), mutableListOf()) { routePoint ->
                Logs.d(TAG, "initRecyclerView: RoutePointModel = $routePoint")
                vModel.openNavigatorApp(routePoint.coordinatesTag, routePoint.address)
            }
        vBinding.rvDestinyAddress.layoutManager = LinearLayoutManager(requireContext())
        vBinding.rvDestinyAddress.adapter = adapter
    }
}