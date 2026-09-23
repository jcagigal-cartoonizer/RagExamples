package ifac.td.taxi.ui.screen

import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentToolsBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.ToolsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ToolsFragment(
) : BaseFragment<FragmentToolsBinding, ToolsViewModel>(
    R.layout.fragment_tools
) {

    private val TAG = "ToolsFragment"
    private val vModel: ToolsViewModel by viewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentToolsBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
    }

    private fun setButtons() {
        vBinding.apply {
            btnRequirements.setAction {
                vModel.clickRequirements()
            }
            btnMeetingSign.setAction {
                iMainActivity.navigateTo(ToolsFragmentDirections.actionToolsFragmentToMeetingSignFragment(
                    vModel.meetingSignColors.value.first, vModel.meetingSignColors.value.second
                ))
            }
        }
    }
}