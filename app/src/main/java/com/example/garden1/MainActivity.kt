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
import androidx.core.text.set
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.garden1.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        //  **  적정값 설정 **
        var origin_humidity = 50.0; var origin_temperature = 50.0; var origin_air = 50.0
        //var origin_humidity: Double = (getString(R.string.origin_humidity)).toDouble() //@values/strings에서 데이터 가져오는 법(getString()은 리소스의 id를 받음을 주의)

        //  **  현재값 설정 **
        var current_humidity = 40.0; var current_temperature = 45.0; var current_air = 10.0
        var current_air_string = "-"

        var progress_humidity: Double = 100*(current_humidity/origin_humidity)
        var progress_temperature: Double = 100*(current_temperature /origin_temperature)
        var progress_air: Double = 100*(current_air /origin_air)
        var progress_total = (progress_humidity + progress_temperature + progress_air)/3

        when (progress_air) { //공기질 수치에 따른 공기질 지수 표현
            in 80.0 .. 100.0 -> { current_air_string = "좋음"}
            in 50.0 .. 80.0 -> { current_air_string = "보통" }
            else -> { current_air_string = "나쁨" }
        }

        //  **  Bottom Sheet Collapsed **
        fun colBtn(part1: String, part2: String, percent: Double): SpannableString {
            val s1 = part1
            val s2 = part2
            val fullText = "$s1\t$s2"
            val spannable = SpannableString(fullText)

            var color_s2 = ForegroundColorSpan(Color.BLACK)
            when (percent) {
                in 80.0 .. 100.0 -> { color_s2 = ForegroundColorSpan(Color.BLUE)}
                in 50.0 .. 80.0 -> { color_s2 = ForegroundColorSpan(Color.GREEN) }
                else -> { color_s2 = ForegroundColorSpan(Color.RED) }
            }
            spannable.setSpan(color_s2, s1.length+1, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }
        binding.bottomSheet.btnHumidity.text = colBtn("습도  ", "$current_humidity%", progress_humidity)
        binding.bottomSheet.btnTemperature.text = colBtn("온도  ", "$current_temperature℃", progress_temperature)
        binding.bottomSheet.btnAir.text = colBtn("공기질 ", current_air_string, progress_air)
        binding.bottomSheet.btnIssue.text = getString(R.string.issue)+"\t\t-"
        // TODO: 이상상태 알림 개수 표현으로 수정

        //  **  Bottom Sheet Expanded **
        fun exBtn(part0: String, part1: String, part2: String, sizeSpan1: Float, sizeSpan2: Float, percent: Double): SpannableString {
            val s0 = part0
            val s1 = part1
            val s2 = part2
            val fullText = "$s0\n$s1$s2"
            val spannable = SpannableString(fullText)

            //val color_s1 = ForegroundColorSpan(colorSpan1)
            val size_s1 = RelativeSizeSpan(sizeSpan1)
            var color_s1 = ForegroundColorSpan(Color.BLACK)
            when (percent) {
                in 80.0 .. 100.0 -> { color_s1 = ForegroundColorSpan(Color.BLUE)}
                in 50.0 .. 80.0 -> { color_s1 = ForegroundColorSpan(Color.GREEN) }
                else -> { color_s1 = ForegroundColorSpan(Color.RED) }
            }
            spannable.setSpan(color_s1, s0.length+1, s0.length + s1.length+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(size_s1, s0.length+1, s0.length + s1.length+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val size_s2 = RelativeSizeSpan(sizeSpan2)
            spannable.setSpan(size_s2, s0.length + s1.length+1, s0.length+s1.length+s2.length+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

        binding.bottomSheet.btnHumidityEx.text = exBtn("습도 ", "${current_humidity}%\t", " /${origin_humidity}%", 1.3f, 1.0f, progress_humidity) //습도
        binding.bottomSheet.btnTemperatureEx.text = exBtn("온도 ", "${current_temperature}%\t", " /${origin_temperature}%", 1.3f, 1.0f, progress_temperature) //온도
        binding.bottomSheet.btnAirEx.text = exBtn("공기질 ", "${current_air_string}", " ${progress_air}%", 1.3f, 1.0f, progress_air) //공기질
        binding.bottomSheet.btnIssueEx.text = "알림내역\n확인하기"
        // TODO: 이상상태 알림 개수 표현으로 수정 + 이상상태 로그 페이지 띄우기
        binding.bottomSheet.btnWater.text = getString(R.string.water)
        // TODO: 물주기
        binding.bottomSheet.btnTempChange.text = getString(R.string.temp_change)
        // TODO: 온도 조절

        //  **  Middle Box Expanded (=Bottom Sheet Collapsed) **
        binding.middleBox.progressBar1.progress = progress_humidity.roundToInt()
        binding.middleBox.progressBar2.progress = progress_temperature.roundToInt()
        binding.middleBox.progressBar3.progress = progress_air.roundToInt()
        binding.middleBox.progressBar1Text2.text = String.format("%.0f", progress_total) + "%"

        //progressBar1_text4 퍼센트별 문구 적용
        when (progress_total) {
            in 80.0 .. 100.0 -> { binding.middleBox.progressBar1Text4.text = "잘 자라고 있어요!"}
            in 50.0 .. 80.0 -> { binding.middleBox.progressBar1Text4.text = "조금만 더 노력해주세요!" }
            else -> { binding.middleBox.progressBar1Text4.text = "분발해주세요..!" }
        }

        //  **  Middle Box Collapsed (=Bottom Sheet Expanded) **
        binding.middleBox.progressBarTotal.progress = progress_total.roundToInt() // 막대 그래프(middle box collapsed)의 퍼센트 설정
        binding.middleBox.progressBarTotalText.text = "초록이는 " + String.format("%.0f", progress_total) + "% 쾌적해요!"

        //이상상태 로그 페이지 띄우기
        val intent = Intent(this, IssueActivity::class.java)
        binding.bottomSheet.btnIssueEx.setOnClickListener {
            startActivity(intent)
        }

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false // 새로고침 애니메이션 중지
            startProgressAnimation(progress_humidity.toInt(), progress_temperature.toInt(), progress_air.toInt()) //원형 그래프 로딩 애니메이션
        }

        startProgressAnimation(progress_humidity.toInt(), progress_temperature.toInt(), progress_air.toInt()) //화면 처음 로딩시 원형 그래프 로딩 애니메이션

        binding.middleBox.middleBoxBtn.setOnClickListener { //중앙 박스 클릭시
            startProgressAnimation(progress_humidity.toInt(), progress_temperature.toInt(), progress_air.toInt()) //원형 그래프 로딩 애니메이션
        }

        bottomEvent() //binding.BottomSheet 슬라이드

    }

    private fun startProgressAnimation(progress_humidity: Int, progress_temperature: Int, progress_air: Int) {
        val animator1 = ObjectAnimator.ofInt(binding.middleBox.progressBar1, "progress", 0, progress_humidity)
        val animator2 = ObjectAnimator.ofInt(binding.middleBox.progressBar2, "progress", 0, progress_temperature)
        val animator3 = ObjectAnimator.ofInt(binding.middleBox.progressBar3, "progress", 0, progress_air)
        animator1.duration = 1000 // 1 seconds
        animator1.interpolator = LinearInterpolator()
        animator2.duration = 1000 // 1 seconds
        animator2.interpolator = LinearInterpolator()
        animator3.duration = 1000 // 1 seconds
        animator3.interpolator = LinearInterpolator()

        animator1.start()
        animator2.start()
        animator3.start()
    }
    private fun bottomEvent() {
        val bottomBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)

        val layoutParams = binding.middleBox.middleBoxBtn.layoutParams
        var maxHeight = binding.middleBox.middleBoxBtn.maxHeight
        var minHeight = binding.middleBox.middleBoxBtn.minHeight

        //binding.middleBox.layoutMiddleBox.layoutParams = layoutParams

        bottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) { // 슬라이드 중, 접힌 상태=0, 펼친 상태=1

                binding.middleBox.middleBoxBtn.apply { // 중앙 박스(버튼) 크기 조절
                    //layoutParams.height = (maxHeight * (1.0 - slideOffset)).toInt()
                    layoutParams.height = (minHeight + (maxHeight - minHeight) * (1.0 - slideOffset)).toInt()
                    binding.middleBox.middleBoxBtn.layoutParams = layoutParams
                }

                if (slideOffset <= 0.1) { // 접힌 상태
                    binding.bottomSheet.sixBtnCollapsed.isVisible=true
                    binding.bottomSheet.sixBtnExpanded.visibility= GONE

                    binding.middleBox.progressBarTotalText.visibility = GONE // 막대그래프(middle box collapsed) 텍스트 숨기기
                    binding.middleBox.progressBar1.isVisible = true // 원형그래프1(middle box expanded) 보이기
                    binding.middleBox.progressBar2.isVisible = true // 원형그래프1(middle box expanded) 보이기
                    binding.middleBox.progressBar3.isVisible = true // 원형그래프1(middle box expanded) 보이기
                    binding.middleBox.progressBar1Text1.isVisible = true // 원형그래프(middle box expanded) 텍스트1 보이기
                    binding.middleBox.progressBar1Text2.isVisible = true // 원형그래프(middle box expanded) 텍스트2 보이기
                    binding.middleBox.progressBar1Text3.isVisible = true // 원형그래프(middle box expanded) 텍스트3 보이기
                    binding.middleBox.progressBar1Text4.isVisible = true // 원형그래프(middle box expanded) 텍스트4 보이기

                } else if (slideOffset<0.15) {
                    binding.bottomSheet.sixBtnCollapsed.isVisible=true
                    binding.bottomSheet.sixBtnExpanded.visibility= GONE

                    binding.middleBox.progressBar1.visibility = GONE // 원형그래프(middle box expanded) 숨기기
                    binding.middleBox.progressBar2.visibility = GONE // 원형그래프(middle box expanded) 숨기기
                    binding.middleBox.progressBar3.visibility = GONE // 원형그래프(middle box expanded) 숨기기

                } else if (slideOffset<0.9) {
                    binding.bottomSheet.sixBtnCollapsed.visibility= GONE
                    binding.bottomSheet.sixBtnExpanded.isVisible=true

                    binding.middleBox.progressBarTotal.visibility = GONE // 막대그래프(middle box collapsed) 숨기기
                    binding.middleBox.progressBar1.visibility = GONE // 원형그래프(middle box expanded) 숨기기
                    binding.middleBox.progressBar2.visibility = GONE // 원형그래프(middle box expanded) 숨기기
                    binding.middleBox.progressBar3.visibility = GONE // 원형그래프(middle box expanded) 숨기기

                } else if (0.9 <= slideOffset) { // 펼친 상태
                    binding.bottomSheet.sixBtnCollapsed.visibility= GONE
                    binding.bottomSheet.sixBtnExpanded.isVisible=true

                    binding.middleBox.progressBar1Text1.visibility = GONE // 원형그래프(middle box expanded) 텍스트1 숨기기
                    binding.middleBox.progressBar1Text2.visibility = GONE // 원형그래프(middle box expanded) 텍스트2 숨기기
                    binding.middleBox.progressBar1Text3.visibility = GONE // 원형그래프(middle box expanded) 텍스트3 숨기기
                    binding.middleBox.progressBar1Text4.visibility = GONE // 원형그래프(middle box expanded) 텍스트4 숨기기
                    binding.middleBox.progressBarTotal.isVisible = true // 막대그래프(middle box collapsed) 보이기
                    binding.middleBox.progressBarTotalText.isVisible = true // 막대그래프(middle box collapsed) 텍스트 보이기
                }
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) { // 뷰의 상태에 따른 변화
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED-> { } // 접힌 상태
                    BottomSheetBehavior.STATE_EXPANDED-> { } // 펼쳐진 상태
                    BottomSheetBehavior.STATE_HALF_EXPANDED-> { } // 반쯤 펼쳐진 상태
                    BottomSheetBehavior.STATE_DRAGGING-> { } // 드래그 중일 때
                }
            }
        }) //bottomBehavior.addBottomSheetCallback 설정
    }

}
