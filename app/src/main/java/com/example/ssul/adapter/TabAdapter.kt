package com.example.ssul.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ssul.FavoritesFragment
import com.example.ssul.HomeFragment
import com.example.ssul.MapFragment
import com.example.ssul.StoreItem

//class TabAdapter(fragmentActivity: FragmentActivity, private val storeItems: MutableList<StoreItem>) :
//    FragmentStateAdapter(fragmentActivity) {
//
//    private val fragmentCache = mutableMapOf<Int, Fragment>() // Fragment 캐시
//
//    private val fragmentCreators: Map<Int, () -> Fragment> = mapOf(
//        0 to { FavoritesFragment().also { it.setStoreItems(storeItems) } },
//        1 to { HomeFragment().also { it.setStoreItems(storeItems) } },
//        2 to { MapFragment().also { it.setStoreItems(storeItems) }  }
//    )
//
//    override fun createFragment(position: Int): Fragment {
//        // 캐시에 저장된 Fragment가 있으면 반환, 없으면 생성 후 저장
//        return fragmentCache[position] ?: fragmentCreators[position]?.invoke()?.also {
//            fragmentCache[position] = it
//        } ?: throw IllegalStateException("Invalid position")
//    }
//
//    override fun getItemCount(): Int = fragmentCreators.size
//
//    fun getFragment(position: Int): Fragment? {
//        return fragmentCache[position]
//    }
//}

class TabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragmentList = listOf(
        FavoritesFragment(),
        HomeFragment(),
        MapFragment()
    )

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }
}