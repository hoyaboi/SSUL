package com.example.ssul.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ssul.R
import com.example.ssul.model.FavoriteModel
import com.example.ssul.model.StoreModel

class StoreAdapter(
    private var storeItems: MutableList<StoreModel>,
    private var favoriteItems: List<FavoriteModel>,
    private val onFavoriteClicked: (Int) -> Unit,
    private val onStoreClicked: (Int) -> Unit,
    private val isFavoriteMode: Boolean = false
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    inner class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storeContainer: LinearLayout = itemView.findViewById(R.id.store_container)
        private val storeImage: ImageView = itemView.findViewById(R.id.store_image)
        private val storeText: TextView = itemView.findViewById(R.id.store_text)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favorite_button)
        private val locationText: TextView = itemView.findViewById(R.id.location_text)
        private val filterGroup: TextView = itemView.findViewById(R.id.filter_group_image)
        private val filterDate: TextView = itemView.findViewById(R.id.filter_date_image)
        private val filterEfficiency: TextView = itemView.findViewById(R.id.filter_efficiency_image)
        private val filterPartner: TextView = itemView.findViewById(R.id.filter_partner_image)

        fun bind(item: StoreModel) {
            // 가게 정보 로드(이미지, 가게 이름, 주소)
            Glide.with(storeImage.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(storeImage)
            storeText.text = item.name
            locationText.text = item.address

            // 즐겨찾기 상태 조회
            val isFavorite = favoriteItems.any { it.storeId == item.id && it.isFavorite }

            // 즐겨찾기 가시성 조정
            favoriteButton.setImageResource(
                if (isFavorite) R.drawable.favorite_clicked else R.drawable.favorite_non_clicked
            )

            // 즐겨찾기 클릭 처리
            favoriteButton.setOnClickListener {
                onFavoriteClicked(item.id)
            }

            // 필터 칩 가시성 조정
            filterChipVisibility(filterPartner, item.isAssociated)
            filterChipVisibility(filterGroup, item.isFilterGroupChecked)
            filterChipVisibility(filterDate, item.isFilterDateChecked)
            filterChipVisibility(filterEfficiency, item.isFilterEfficiencyChecked)

            // 가게 클릭 처리 -> 세부 화면으로 이동
            storeContainer.setOnClickListener {
                onStoreClicked(item.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(storeItems[position])

        // 마지막 아이템인 경우 아래 마진 추가
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (position == storeItems.size - 1) {
            layoutParams.bottomMargin = 50.dpToPx(holder.itemView.context)
        } else {
            layoutParams.bottomMargin = 0
        }
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = storeItems.size

    // dp를 px로 변환하는 확장 함수
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    // 가게마다 필터 정보 따른 필터 칩 가시성 조정
    fun filterChipVisibility(view: View, isFilterTrue: Boolean) {
        view.visibility = if (isFilterTrue) View.VISIBLE else View.GONE
    }

    // 가게 리스트 업데이트
    fun updateItems(newItems: List<StoreModel>) {
        storeItems = if (isFavoriteMode) { // FavoriteFragment
            newItems.filter { store ->
                favoriteItems.any { favorite -> favorite.storeId == store.id && favorite.isFavorite }
            }.toMutableList()
        } else { // HomeFragment
            newItems.toMutableList()
        }
        notifyDataSetChanged()
    }

    // 즐겨찾기 상태 업데이트
    fun updateFavorites(favorites: List<FavoriteModel>) {
        favoriteItems = favorites
        if (isFavoriteMode) { // FavoriteFragment
            storeItems = storeItems.filter { store ->
                favoriteItems.any { favorite -> favorite.storeId == store.id && favorite.isFavorite }
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
