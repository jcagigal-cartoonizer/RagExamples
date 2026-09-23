package ifac.td.taxi.ui.screen

import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPointsOfInterestBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.PointsOfInterestAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.PointsOfInterestViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PointsOfInterestFragment :
    BaseFragment<FragmentPointsOfInterestBinding, PointsOfInterestViewModel>(
        R.layout.fragment_points_of_interest
    ) {

    private val vModel: PointsOfInterestViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentPointsOfInterestBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        setButtons()
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }

            btnSearch.setAction {
                vModel.getPOIs(vBinding.etPOISearchBar.text.toString())
                hideKeyboard()
            }

            etPOISearchBar.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    vModel.getPOIs(vBinding.etPOISearchBar.text.toString())
                    hideKeyboard()
                }
                false
            }
        }

    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.poiFlow.collect { poiList ->
                        val adapter = context?.let { context ->
                            PointsOfInterestAdapter(context, poiList) { poi ->
                                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        when (response.buttonPressed) {
                                            ButtonType.UBICAR_DESTINO -> {
                                                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                                    { responseLocate ->
                                                        when (responseLocate.buttonPressed) {
                                                            ButtonType.ACCEPT -> vModel.locateOnHired(
                                                                poi.zone
                                                            )

                                                            else -> {}
                                                        }
                                                    }

                                                iMainActivity.openDialog(
                                                    CustomDialog.CustomDialogModel(
                                                        title = poi.zone.nombreZone,
                                                        description = getString(R.string.ask_locate),
                                                        buttons = if (poi.zone != null) {
                                                            arrayListOf(
                                                                ButtonType.CANCEL,
                                                                ButtonType.ACCEPT
                                                            )
                                                        } else {
                                                            arrayListOf(
                                                                ButtonType.CANCEL,
                                                                ButtonType.ACCEPT
                                                            )
                                                        },
                                                    ),
                                                    callBack,
                                                    fragmentManager = childFragmentManager
                                                )
                                            }

                                            ButtonType.NAVEGAR -> {
                                                vModel.openNavigatorApp(poi.lat, poi.lon, poi.street)
                                            }

                                            ButtonType.UBICAR_DESTINO_NAVEGAR -> {
                                                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                                    { responseLocate ->
                                                        when (responseLocate.buttonPressed) {
                                                            ButtonType.ACCEPT -> {
                                                                vModel.locateOnHired(poi.zone)
                                                                vModel.openNavigatorApp(poi.lat, poi.lon, poi.street)
                                                            }

                                                            else -> {}
                                                        }
                                                    }

                                                iMainActivity.openDialog(
                                                    CustomDialog.CustomDialogModel(
                                                        title = poi.zone.nombreZone,
                                                        description = getString(R.string.ask_locate),
                                                        buttons = arrayListOf(
                                                            ButtonType.CANCEL,
                                                            ButtonType.ACCEPT
                                                        ),
                                                    ),
                                                    callBack,
                                                    fragmentManager = childFragmentManager
                                                )


                                            }

                                            else -> {}
                                        }
                                    }

                                val zoneDescription = poi.zone?.nombreZone
                                    ?: resources.getString(R.string.poi_out_of_zone_short)
                                val streetDescription = poi.street?.let {
                                    "\n" + getString(
                                        R.string.dialog_poi_description_street,
                                        it
                                    ) } ?: ""
                                val description = "\n" + getString(
                                    R.string.dialog_poi_description,
                                    zoneDescription,
                                    streetDescription
                                )

                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = poi.poi,
                                        description = description,
                                        buttons = if (poi.zone != null) {
                                            arrayListOf(
                                                ButtonType.UBICAR_DESTINO,
                                                ButtonType.NAVEGAR,
                                                ButtonType.UBICAR_DESTINO_NAVEGAR,
                                            )
                                        } else {
                                            arrayListOf(
                                                ButtonType.NAVEGAR,
                                            )
                                        },
                                    ),
                                    callBack,
                                    fragmentManager = childFragmentManager
                                )
                            }
                        }

                        val llm = LinearLayoutManager(context)
                        llm.orientation = LinearLayoutManager.VERTICAL

                        vBinding.rvPOIResults.layoutManager = llm
                        vBinding.rvPOIResults.adapter = adapter
                    }
                }


                launch {
                    vModel.locationOnHiredSentFlow.collect {
                        sharedViewModel.saveHiredZone(it)
                            iMainActivity.navigateBack()
                            iMainActivity.navigateBack()
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
}