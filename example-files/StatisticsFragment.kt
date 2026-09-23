package ifac.td.taxi.ui.screen

import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentStatisticsBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.pager.PagerAdapter
import ifac.td.taxi.viewmodel.StatisticsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatisticsFragment :
    BaseFragment<FragmentStatisticsBinding, StatisticsViewModel>(R.layout.fragment_statistics) {

    private val vModel: StatisticsViewModel by viewModel()

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentStatisticsBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        tabLayout = vBinding.tbStatistics
        viewPager = vBinding.vpStatistics

        val adapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        viewPager?.adapter = adapter
        viewPager?.isSaveEnabled = false

        tabLayout?.let { tab ->
            viewPager?.let { pager ->
                TabLayoutMediator(tab, pager) { tab, position ->
                    when (position) {
                        0 -> tab.text = getString(R.string.week)
                        1 -> tab.text = getString(R.string.month)
                        2 -> tab.text = getString(R.string.year)
                        else -> tab.text = getString(R.string.btn_unknown)
                    }
                }.attach()
            }
        }
    }

    override fun onDestroyView() {
        viewPager?.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

