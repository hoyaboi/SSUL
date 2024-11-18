package com.example.ssul

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.ssul.adapter.TabAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var messageBox: ConstraintLayout
    private lateinit var messageBoxTextView: TextView
    private lateinit var messageBoxYesButton: TextView
    private lateinit var messageBoxNoButton: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 상태표시줄 색상 변경
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_background)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        setupViews()
        viewPager.adapter = TabAdapter(this)
        viewPager.isUserInputEnabled = false
        viewPager.setCurrentItem(1, false) // 메인 화면 설정(홈)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.customView = createTabView("즐겨찾기", R.drawable.ic_favorites)
                1 -> tab.customView = createTabView("홈", R.drawable.ic_home)
                2 -> tab.customView = createTabView("지도", R.drawable.ic_map)
            }
        }.attach()

        // 메시지 박스 표시된 경우 메시지 박스 컨테이너 외에는 터치가 안 되게 설정
        messageBox.setOnClickListener {}
    }

    override fun onBackPressed() {
        if (messageBox.visibility == View.VISIBLE) {
            messageBox.visibility = View.GONE
            return
        }

        // 홈 화면(HomeFragment)에서의 뒤로가기 클릭 처리
        val currentFragment = (viewPager.adapter as? TabAdapter)?.getFragment(viewPager.currentItem)
        if (currentFragment is HomeFragment && currentFragment.handleBackPressed()) {
            return
        }

        // 기본 뒤로가기 클릭 처리 (앱 종료)
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            super.onBackPressed()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViews() {
        messageBox = findViewById(R.id.message_box)
        messageBoxTextView = findViewById(R.id.message_box_text)
        messageBoxYesButton = findViewById(R.id.message_box_yes_button)
        messageBoxNoButton = findViewById(R.id.message_box_no_button)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
    }

    // 탭바 UI 설정
    private fun createTabView(title: String, icon: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.tab_item, null)
        val tabIcon = view.findViewById<ImageView>(R.id.tab_icon)
        val tabText = view.findViewById<TextView>(R.id.tab_text)

        tabIcon.setImageResource(icon)
        tabText.text = title
        return view
    }

    // 프래그먼트 갱신 메서드
    fun refreshFragment(position: Int) {
        viewPager.adapter?.notifyItemChanged(position)
    }

    fun showMessageBox(message: String, onYesClicked: () -> Unit) {
        messageBoxTextView.text = message
        messageBox.visibility = View.VISIBLE

        messageBoxYesButton.setOnClickListener {
            messageBox.visibility = View.GONE
            onYesClicked()
        }

        messageBoxNoButton.setOnClickListener {
            messageBox.visibility = View.GONE
        }
    }
}