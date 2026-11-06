package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentRequirementsBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.RequirementsAdapter
import ifac.td.taxi.viewmodel.RequirementsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RequirementsFragment :
    BaseFragment<FragmentRequirementsBinding, RequirementsViewModel>(R.layout.fragment_requirements) {

    private val TAG = "RequirementsFragment"
    private val vModel: RequirementsViewModel by viewModel()

    override fun getViewModel(): RequirementsViewModel = vModel

    override fun getViewBinding() = FragmentRequirementsBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showBottomBar(true)
        iMainActivity.showHeader(true)

        vModel.getRequirements()
        showLoading(true)
    }

    private fun showLoading(isLoading: Boolean) {
        vBinding.pbDriverRequirements.visibility = if (isLoading) View.VISIBLE else View.GONE
        vBinding.pbVehicleRequirements.visibility = if (isLoading) View.VISIBLE else View.GONE

        vBinding.rvDriverRequirements.visibility = if (isLoading) View.GONE else View.VISIBLE
        vBinding.rvVehicleRequirements.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showNoDataMessage(isEmpty: Boolean, textView: View) {
        textView.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.requirementsFlow.collect { requirements ->
                        requirements?.let {
                            Logs.d(TAG, "requirementsFlow: $requirements")

                            showLoading(false)

                            requirements.driverReqs?.let { driverRequirements ->
                                showNoDataMessage(driverRequirements.isEmpty(), vBinding.tvNoDriverRequirements)
                                if (driverRequirements.isNotEmpty()) {
                                    val driverRequirementsAdapter = RequirementsAdapter(driverRequirements) {}
                                    vBinding.rvDriverRequirements.layoutManager = LinearLayoutManager(context)
                                    vBinding.rvDriverRequirements.adapter = driverRequirementsAdapter
                                }
                            }

                            requirements.vehicleReqs?.let { vehicleRequirements ->
                                showNoDataMessage(vehicleRequirements.isEmpty(), vBinding.tvNoVehicleRequirements)
                                if (vehicleRequirements.isNotEmpty()) {
                                    val vehicleRequirementsAdapter = RequirementsAdapter(vehicleRequirements) {}
                                    vBinding.rvVehicleRequirements.layoutManager = LinearLayoutManager(context)
                                    vBinding.rvVehicleRequirements.adapter = vehicleRequirementsAdapter
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
