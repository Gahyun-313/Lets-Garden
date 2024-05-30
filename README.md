2024.05.30 🕐13:12

- Bottom sheet을 제외한 부분을 아래로 swipe 시 새로고침 (Swipe refresh layout)
- Bottom sheet의 Slide에 맞춰 Middle Box 축소/확장
- 앱 시작/새고로침 시 삼중 그래프 애니메이션
- Bottom sheet Expanded 상태에서 "알림내역 확인하기" 버튼 클릭 시 서브 액티비티인 IssueActivity가 실행됨으로써 activityMainBinding(activity_issue.xml) 실행
- 

- 각 수치(progress_humidity, progress_temperature, progress_air)에 따라 각 버튼 내 text color 변경(SpannableString() 이용함으로써 text 전체의 색깔을 바꾸는 것이 아닌 일부분만 변경)

![device-2024-05-30-130948](https://github.com/Gahyun-313/Garden1/assets/78289372/0d484207-00bd-4707-a9f0-e68894c0973e)
