# DailyTag

> 오늘 하루 @누구 와 보냈나요?


![가로이미지](https://github.com/gyuch-an02/madcamp2024S/assets/146503043/f4157c68-f66e-4119-b741-56a5e4c59913)


DailyTag는 태그를 통해 사람들과의 기록을 모아보고, 나의 하루를 관리할 수 있는 앱입니다. 

---

### Team

---

하은수 (숙명여대)
안규찬 (KAIST)

### Tech Stack

---

**Front-end** : Java

**IDE** : Android Studio

### 태그

---

- 연락처에 저장된 사람을 태그 가능
- 본문 텍스트 입력 시 자동완성. dropdown에서 클릭 시 해당 컨텐츠의 태그로 자동 추가
- + 버튼으로도 태그 추가 가능
- 태그를 누르면 태그 상세 페이지로 이동
- 태그 상세 페이지 : 연락처, 태그된 할일, 일기, 사진 표시

### Tab 1 : 연락처

---

- 기기에 저장된 연락처를 볼 수 있는 탭
- 프로필 이미지, 이름, 전화번호를 표시
- 가나다순 정렬. 첫글자 header로 가독성을 높임
- 연락처 검색 가능
- 연락처 추가, 수정, 삭제 가능
- 각 연락처를 누르면 태그 상세 페이지로 연결

### Tab 2 : 사진

---

- 기기에 저장된 사진을 볼 수 있는 탭
- 날짜 header를 표시
- 기간 필터 기능 :
    - 오늘, 최근 7일, 최근 30일, 전체 버튼
    - 직접 기간 설정 가능
- 각 이미지를 누르면 이미지 이름, 날짜, 확대한 이미지 표시
    - 태그 가능

### Tab 3 : 나의하루

---

- 날짜별로 할일과 일기를 작성할 수 있는 탭
- 월 단위로 표시 (singleRowCalendar)
- ‘할일’과 ‘일기’ 라벨로 편집기를 전환 가능
- 할일
    - checkbox로 완료 여부 표시 (완료된 할일은 하단으로 이동)
    - 각 할일마다 사람 태그 가능
- 일기
    - 연락처 정보를 기반으로 사람 태그 가능
    - 버튼으로 되돌리기, 재실행 가능

### 시연 영상

---

![dailyTag](https://github.com/gyuch-an02/madcamp2024S/assets/127263741/49437b3f-f5fd-47a8-bc7a-2ea9843cfed5)
