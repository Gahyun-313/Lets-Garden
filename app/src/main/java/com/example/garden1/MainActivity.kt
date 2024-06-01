package com.example.garden1

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.ViewGroup.GONE
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.garden1.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var currentHumidity: Double = 40.0
    private var currentTemperature: Double = 45.0
    private var currentAir: Double = 10.0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        // 초기 UI 설정
        updateUI()

        //이상상태 로그 페이지 띄우기
        val intent = Intent(this, IssueActivity::class.java)
        binding.bottomSheet.btnIssueEx.setOnClickListener {
            startActivity(intent)
        }

        //화면 끌어내려 새로고침
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener { // 화면 끌어내려 새로고침

            // 데이터를 새로 갱신
            fetchData()

            swipeRefreshLayout.isRefreshing = false // 새로고침 애니메이션 중지
            startProgressAnimation() //그래프 애니메이션 실행
        }

        //화면 처음 로딩시 원형그래프 애니메이션 실행
        startProgressAnimation()

        //중앙 박스 클릭 이벤트
        binding.middleBox.middleBoxBtn.setOnClickListener {
            startProgressAnimation() //그래프 애니메이션 실행
        }

        bottomEvent() //binding.BottomSheet 슬라이드

    }
    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            val data = fetchFromDatabase()
            withContext(Dispatchers.Main) {
                currentHumidity = data.first
                currentTemperature = data.second
                currentAir = data.third
                updateUI()
            }
        }
    }
    
    private suspend fun fetchFromDatabase(): Triple<Double, Double, Double> {
        // TODO: 무슨 함수인지 공부
        return withContext(Dispatchers.IO) {
            val dbUrl = "jdbc:mariadb://<YourDBHost>:<Port>/<DatabaseName>"
            val dbUser = "<YourDBUser>"
            val dbPassword = "<YourDBPassword>"
            var humidity = 0.0
            var temperature = 0.0
            var air = 0.0

            try {
                Class.forName("org.mariadb.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
                val statement = connection.createStatement()
                val resultSet: ResultSet = statement.executeQuery("SELECT humidity, temperature, air FROM your_table_name ORDER BY id DESC LIMIT 1")

                if (resultSet.next()) {
                    humidity = resultSet.getDouble("humidity")
                    temperature = resultSet.getDouble("temperature")
                    air = resultSet.getDouble("air")
                }

                resultSet.close()
                statement.close()
                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Triple(humidity, temperature, air)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        //  **  적정값 설정 **
        val originHumidity = 50.0
        val originTemperature = 50.0
        val originAir = 50.0

        // 적정값 대비 현재값 수치(백분율)
        val progressHumidity: Double = 100*(currentHumidity/originHumidity)
        val progressTemperature: Double = 100*(currentTemperature /originTemperature)
        val progressAir: Double = 100*(currentAir /originAir)
        val progressTotal = (progressHumidity + progressTemperature + progressAir)/3

        val currentAirString: String = when (progressAir) { //공기질 수치에 따른 공기질 지수 표현
            in 80.0 .. 100.0 -> { "좋음" }
            in 50.0 .. 80.0 -> { "보통" }
            else -> { "나쁨" }
        }

        binding.bottomSheet.btnHumidity.text = colBtn("습도  ", "$currentHumidity%", progressHumidity)
        binding.bottomSheet.btnTemperature.text = colBtn("온도  ", "$currentTemperature℃", progressTemperature)
        binding.bottomSheet.btnAir.text = colBtn("공기질 ", currentAirString, progressAir)
        binding.bottomSheet.btnIssue.text = getString(R.string.issue)+"\t\t-"
        // TODO: 이상상태 알림 개수 표현으로 수정

        binding.bottomSheet.btnHumidityEx.text = exBtn("습도 ", "${currentHumidity}%\t", " /${originHumidity}%", 1.3f, 1.0f, progressHumidity) //습도
        binding.bottomSheet.btnTemperatureEx.text = exBtn("온도 ", "${currentTemperature}%\t", " /${originTemperature}%", 1.3f, 1.0f, progressTemperature) //온도
        binding.bottomSheet.btnAirEx.text = exBtn("공기질 ",
            currentAirString, " ${progressAir}%", 1.3f, 1.0f, progressAir) //공기질
        binding.bottomSheet.btnIssueEx.text = "알림내역\n확인하기"
        // TODO: 이상상태 알림 개수 표현으로 수정 + 이상상태 로그 페이지 띄우기
        binding.bottomSheet.btnWater.text = getString(R.string.water)
        // TODO: 물주기
        binding.bottomSheet.btnTempChange.text = getString(R.string.temp_change)
        // TODO: 온도 조절

        //  **  Middle Box Expanded (=Bottom Sheet Collapsed) **
        binding.middleBox.progressBar1.progress = progressHumidity.roundToInt()
        binding.middleBox.progressBar2.progress = progressTemperature.roundToInt()
        binding.middleBox.progressBar3.progress = progressAir.roundToInt()
        binding.middleBox.progressBar1Text2.text = String.format("%.0f", progressTotal) + "%"

        //progressBar1_text4 퍼센트별 문구 적용
        when (progressTotal) {
            in 80.0 .. 100.0 -> { binding.middleBox.progressBar1Text4.text = "잘 자라고 있어요!"}
            in 50.0 .. 80.0 -> { binding.middleBox.progressBar1Text4.text = "조금만 더 노력해주세요!" }
            else -> { binding.middleBox.progressBar1Text4.text = "분발해주세요..!" }
        }

        //  **  Middle Box Collapsed (=Bottom Sheet Expanded) **
        binding.middleBox.progressBarTotal.progress = progressTotal.roundToInt() // 막대 그래프(middle box collapsed)의 퍼센트 설정
        binding.middleBox.progressBarTotalText.text = "초록이는 " + String.format("%.0f", progressTotal) + "% 쾌적해요!"

    }

    //  **  Bottom Sheet Collapsed **
    private fun colBtn(part1: String, part2: String, percent: Double): SpannableString {
        val fullText = "$part1\t$part2"
        val spannable = SpannableString(fullText)

        val color2 = when (percent) {
            in 80.0 .. 100.0 -> { ForegroundColorSpan(Color.BLUE) }
            in 50.0 .. 80.0 -> { ForegroundColorSpan(Color.GREEN) }
            else -> { ForegroundColorSpan(Color.RED) }
        }
        spannable.setSpan(color2,
            part1.length + 1, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    //  **  Bottom Sheet Expanded **
    private fun exBtn(part0: String, part1: String, part2: String, sizeSpan1: Float, sizeSpan2: Float, percent: Double): SpannableString {
        val fullText = "$part0\n$part1$part2"
        val spannable = SpannableString(fullText)

        //val color1 = ForegroundColorSpan(colorSpan1)
        val size1 = RelativeSizeSpan(sizeSpan1)
        val color1 = when (percent) {
            in 80.0 .. 100.0 -> { ForegroundColorSpan(Color.BLUE) }
            in 50.0 .. 80.0 -> { ForegroundColorSpan(Color.GREEN) }
            else -> { ForegroundColorSpan(Color.RED) }
        }
        spannable.setSpan(color1,
            part0.length + 1, part0.length + part1.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(size1,
            part0.length + 1, part0.length + part1.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val size2 = RelativeSizeSpan(sizeSpan2)
        spannable.setSpan(size2,
            part0.length + part1.length + 1,
            part0.length + part1.length + part2.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // middle box의 삼중 그래프 애니메이션
    private fun startProgressAnimation() {
        val progressHumidity = (100 * (currentHumidity / 50.0)).toInt()
        val progressTemperature = (100 * (currentTemperature / 50.0)).toInt()
        val progressAir = (100 * (currentAir / 50.0)).toInt()

        val animator1 = ObjectAnimator.ofInt(binding.middleBox.progressBar1, "progress", 0, progressHumidity)
        val animator2 = ObjectAnimator.ofInt(binding.middleBox.progressBar2, "progress", 0, progressTemperature)
        val animator3 = ObjectAnimator.ofInt(binding.middleBox.progressBar3, "progress", 0, progressAir)

        animator1.duration = 1000
        animator1.interpolator = LinearInterpolator()
        animator2.duration = 1000
        animator2.interpolator = LinearInterpolator()
        animator3.duration = 1000
        animator3.interpolator = LinearInterpolator()

        animator1.start()
        animator2.start()
        animator3.start()
    }
    // Bottom Sheet Slide Event
    private fun bottomEvent() {
        val bottomBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)

        val layoutParams = binding.middleBox.middleBoxBtn.layoutParams
        val maxHeight = binding.middleBox.middleBoxBtn.maxHeight
        val minHeight = binding.middleBox.middleBoxBtn.minHeight

        bottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            // 슬라이드 중일 때,
            override fun onSlide(bottomSheet: View, slideOffset: Float) { // slide offset: 접힌 상태=0, 펼친 상태=1

                // 중앙 박스(버튼) 크기 조절
                binding.middleBox.middleBoxBtn.apply {
                    layoutParams.height = (minHeight + (maxHeight - minHeight) * (1.0 - slideOffset)).toInt()
                    binding.middleBox.middleBoxBtn.layoutParams = layoutParams
                }

                if (slideOffset <= 0.1) { // 접힌 상태
                    binding.bottomSheet.sixBtnCollapsed.isVisible=true
                    binding.bottomSheet.sixBtnExpanded.visibility= GONE
                    //막대그래프 텍스트 숨기기
                    binding.middleBox.progressBarTotalText.visibility = GONE
                    //원형그래프 보이기
                    binding.middleBox.progressBar1.isVisible = true
                    binding.middleBox.progressBar2.isVisible = true
                    binding.middleBox.progressBar3.isVisible = true
                    //원형그래프 텍스트 보이기
                    binding.middleBox.progressBar1Text1.isVisible = true
                    binding.middleBox.progressBar1Text2.isVisible = true
                    binding.middleBox.progressBar1Text3.isVisible = true
                    binding.middleBox.progressBar1Text4.isVisible = true

                } else if (slideOffset<0.15) {
                    binding.bottomSheet.sixBtnCollapsed.isVisible=true
                    binding.bottomSheet.sixBtnExpanded.visibility= GONE
                    //원형그래프 숨기기
                    binding.middleBox.progressBar1.visibility = GONE
                    binding.middleBox.progressBar2.visibility = GONE
                    binding.middleBox.progressBar3.visibility = GONE

                } else if (slideOffset<0.9) {
                    binding.bottomSheet.sixBtnCollapsed.visibility= GONE
                    binding.bottomSheet.sixBtnExpanded.isVisible=true
                    //막대그래프 숨기기
                    binding.middleBox.progressBarTotal.visibility = GONE
                    //원형그래프 숨기기
                    binding.middleBox.progressBar1.visibility = GONE
                    binding.middleBox.progressBar2.visibility = GONE
                    binding.middleBox.progressBar3.visibility = GONE

                } else if (0.9 <= slideOffset) { // 펼친 상태
                    binding.bottomSheet.sixBtnCollapsed.visibility= GONE
                    binding.bottomSheet.sixBtnExpanded.isVisible=true
                    //원형그래프 텍스트 숨기기
                    binding.middleBox.progressBar1Text1.visibility = GONE
                    binding.middleBox.progressBar1Text2.visibility = GONE
                    binding.middleBox.progressBar1Text3.visibility = GONE
                    binding.middleBox.progressBar1Text4.visibility = GONE
                    //막대그래프 보이기
                    binding.middleBox.progressBarTotal.isVisible = true
                    //막대그래프 텍스트 보이기
                    binding.middleBox.progressBarTotalText.isVisible = true
                }
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) { // 뷰의 상태에 따른 변화
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED-> { } // 접힌 상태
                    BottomSheetBehavior.STATE_EXPANDED-> { } // 펼쳐진 상태
                    BottomSheetBehavior.STATE_HALF_EXPANDED-> { } // 반쯤 펼쳐진 상태
                    BottomSheetBehavior.STATE_DRAGGING-> { } // 드래그 중일 때
                    BottomSheetBehavior.STATE_HIDDEN -> { }
                    BottomSheetBehavior.STATE_SETTLING -> { }
                }
            }
        })
    }

}
