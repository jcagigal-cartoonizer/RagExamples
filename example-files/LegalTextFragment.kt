package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentLegalTextBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.LegalTextViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LegalTextFragment() :
    BaseFragment<FragmentLegalTextBinding, LegalTextViewModel>(R.layout.fragment_legal_text) {

    private val vModel: LegalTextViewModel by viewModel()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentLegalTextBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        vBinding.btnAccept.setAction {
            vModel.clickOnAccept()
        }
        vModel.setLegalText()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    vModel.legalTextFlow.collect { legalText ->
                        legalText?.let {
                            vBinding.tvLegalText.text = legalText
                        }
                    }
                }
            }
        }

    }
/*
    override fun createScreenMenu(): List<ButtonModel> {
        return listOf(
            btnAccept
        )
    }

 */
}