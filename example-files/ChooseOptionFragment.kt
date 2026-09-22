package ifac.td.taxi.ui.screen

import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentChooseOptionBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.ClassicAdapter
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.ChooseOptionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ChooseOptionFragment :
    BaseFragment<FragmentChooseOptionBinding, ChooseOptionViewModel>(R.layout.fragment_choose_option) {

    private val TAG = "ChooseOptionFragment"

    private val vModel: ChooseOptionViewModel by viewModel()
    private val args: ChooseOptionFragmentArgs by navArgs()

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentChooseOptionBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showBottomBar(true)
        iMainActivity.showHeader(true)

        iMainActivity.setChooseOptionFragmentActive(true)

        setUpAdapter()
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            goBackCancellingShift()
        }
    }

    override fun updateTopBarIcon() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.BACK, true) {
            goBackCancellingShift()
        }
    }

    private fun goBackCancellingShift() {
        // Cancelamos la selección de turno cerrando la sesión del driver. Si no lo
        // hiciéramos, el servidor Bravo seguiría reenviando openBravoShifts (volviendo
        // a navegar aquí) y el siguiente login no funcionaría.
        iMainActivity.setChooseOptionFragmentActive(false)
        vModel.logoffDriver()
        iMainActivity.navigateBack()
    }

    private fun setUpAdapter() {
        Logs.d(TAG, "setUpAdapter: ${args.bravoListIDs.toList()} ${args.bravoListStrings.toList()}")

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL

        vBinding.rvChooseOption.layoutManager = llm
        vBinding.rvChooseOption.adapter = context?.let { context ->
            ClassicAdapter(context, Pair(args.bravoListIDs.toList(), args.bravoListStrings.toList())) { (selectedId, selectedString) ->
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    CoroutineScope(Dispatchers.IO).launch {
                        when (response.buttonPressed) {
                            ButtonType.ACCEPT -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    selectedId.let { vModel.sendShiftSelectedAnswer(selectedId) }
                                    iMainActivity.setChooseOptionFragmentActive(false)
                                    vModel.loginDriver()
                                    iMainActivity.navigateBack()
                                }
                            }

                            else -> {}
                        }
                    }
                }

                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = context.getString(R.string.warning),
                        description = getString(R.string.dialog_send_option_confirm, selectedString),
                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                    ),
                    callback,
                    fragmentManager = childFragmentManager
                )
            }
        }
    }
}