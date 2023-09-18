package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chuckerteam.chucker.R

private const val OVERVIEW_INDEX = 0
private const val REQUEST_INDEX = 1
private const val RESPONSE_INDEX = 2
private const val MOCK_INDEX = 3

internal class TransactionPagerAdapter(context: Context, fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles = arrayOf(
        context.getString(R.string.chucker_overview),
        context.getString(R.string.chucker_request),
        context.getString(R.string.chucker_response),
        context.getString(R.string.chucker_mock)
    )

    override fun getItem(position: Int): Fragment = when (position) {
        OVERVIEW_INDEX -> TransactionOverviewFragment()
        REQUEST_INDEX -> TransactionPayloadFragment.newInstance(PayloadType.REQUEST)
        RESPONSE_INDEX -> TransactionPayloadFragment.newInstance(PayloadType.RESPONSE)
        MOCK_INDEX -> TransactionPayloadFragment.newInstance(PayloadType.MOCK)
        else -> throw IllegalArgumentException("no item")
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence = titles[position]
}
