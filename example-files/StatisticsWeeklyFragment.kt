package ifac.td.taxi.ui.screen

import android.graphics.Color
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentStatisticsWeeklyBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.receivers.utils.convertHmToKm
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.chart.CustomChart
import ifac.td.taxi.viewmodel.StatisticsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatisticsWeeklyFragment :
    BaseFragment<FragmentStatisticsWeeklyBinding, StatisticsViewModel>(R.layout.fragment_statistics_weekly) {

    private val vModel: StatisticsViewModel by viewModel()

    private var pbBilling: ProgressBar? = null
    private var pbState: ProgressBar? = null

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentStatisticsWeeklyBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(true)

        vModel.get7LastDays()

        vModel.getTimeLast7Days()
    }

    override fun setupObservers() {
        pbBilling = vBinding.pbBilling
        pbState = vBinding.pbState

        val lineChartBilling = vBinding.lcBillingWeekly
        val lineChartState = vBinding.lcStateWeekly

        lineChartBilling.setNoDataText("")
        lineChartState.setNoDataText("")

        pbBilling?.visibility = View.VISIBLE
        pbState?.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.billingValuesFlow.collect {
                        it?.let { values ->

                            val labels = mutableListOf<String>()

                            val totals = mutableListOf<Double>()

                            for (value in values) {
                                val date = value.date
                                labels.add(date)

                                val total = value.totalAmount
                                totals.add(total)
                            }

                            setUpBillingChart(labels, totals)

                            pbBilling?.visibility = View.GONE
                        }
                    }
                }

                launch {
                    vModel.timeValuesFlow.collect {
                        it?.let { values ->

                            val labels = mutableListOf<String>()

                            val distanceFreeValues = mutableListOf<Double>()

                            val distanceHiredValues = mutableListOf<Double>()

                            for (value in values) {
                                val date = value.label
                                labels.add(date)

                                val distanceFree = value.distanceFree.convertHmToKm()
                                distanceFreeValues.add(distanceFree)

                                val distanceHired = value.distanceHired.convertHmToKm()
                                distanceHiredValues.add(distanceHired)
                            }

                            setUpStateChart(labels, distanceHiredValues, distanceFreeValues)

                            pbState?.visibility = View.GONE
                        }
                    }
                }
            }
        }


    }

    private fun setUpBillingChart(dates: List<String>, totals: List<Double>) {
        val lineChartBilling = vBinding.lcBillingWeekly

        if (dates.isEmpty() || totals.isEmpty()) {
            showNoDataMessage(lineChartBilling)
        } else {

            lineChartBilling.xAxis.spaceMin = 0.5f
            lineChartBilling.xAxis.spaceMax = 0.5f

            if (dates.size != totals.size) {
                Logs.e(
                    "StatisticsFragment",
                    "Las listas de fechas y totales tienen longitudes diferentes"
                )
            } else {
                val initialDataEntries = mutableListOf<Entry>()
                for (i in dates.indices) {
                    val xValue = i.toFloat()
                    val yValue = totals[i].toFloat()
                    initialDataEntries.add(Entry(xValue, yValue))
                }

                val chartDescription = ""

                val chartModel =
                    CustomChart.CustomChartModel(
                        resources.getString(R.string.statistics_billing),
                        initialDataEntries,
                        null,
                        null,
                        dates,
                        chartDescription,
                        color = resources.getColor(R.color.green_statistics),
                        null,
                        CustomChart.ChartType.BILLING
                    )
                context?.let { CustomChart(it, lineChartBilling) }?.initChart(chartModel)
            }
        }
    }

    private fun setUpStateChart(
        dates: List<String>,
        distanceFreeValues: List<Double>,
        distanceHiredValues: List<Double>,
    ) {
        val lineChartState = vBinding.lcStateWeekly

        if (dates.isEmpty() || distanceFreeValues.isEmpty() || distanceHiredValues.isEmpty()) {
            showNoDataMessage(lineChartState)
        } else {

            lineChartState.xAxis.spaceMin = 0.5f
            lineChartState.xAxis.spaceMax = 0.5f

            if (dates.size != distanceFreeValues.size || dates.size != distanceHiredValues.size) {
                Logs.e(
                    "StatisticsFragment",
                    "Las listas de fechas y totales tienen longitudes diferentes"
                )
            } else {
                val dataEntries = mutableListOf<Entry>()
                for (i in dates.indices) {
                    val xValue = i.toFloat()
                    val yValue = distanceFreeValues[i].toFloat()
                    dataEntries.add(Entry(xValue, yValue))
                }

                val dataEntries2 = mutableListOf<Entry>()
                for (i in dates.indices) {
                    val xValue = i.toFloat()
                    val yValue = distanceHiredValues[i].toFloat()
                    dataEntries2.add(Entry(xValue, yValue))
                }

                val chartDescription = ""

                val chartModel =
                    CustomChart.CustomChartModel(
                        resources.getString(R.string.strOccupied),
                        dataEntries,
                        resources.getString(R.string.strVacant),
                        dataEntries2,
                        dates,
                        chartDescription,
                        color = resources.getColor(R.color.blue_statistics),
                        secondLineColor = resources.getColor(R.color.green),
                        CustomChart.ChartType.STATE
                    )
                context?.let { CustomChart(it, lineChartState) }?.initChart(chartModel)
            }
        }
    }

    private fun showNoDataMessage(lineChart: LineChart) {
        lineChart.clear()
        lineChart.setNoDataText(getString(R.string.statistics_no_data))
        lineChart.setNoDataTextColor(Color.RED)
        lineChart.invalidate()
    }

}