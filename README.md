# 제5회 국방오픈소스소프트웨어 캠프


## 소나기(소중한 나의 병영일기)  APP Release 1.0ver.
서버 소스는 <a href="https://github.com/JustKode/2019-OSAM-Backend">여기에 있습니다.</a>
![메인](https://user-images.githubusercontent.com/54613588/67464197-e849b400-f67d-11e9-88f2-3590114565ce.PNG)


### Developer
 #### 조찬민,박민재
훈련병 시절부터 적어온 소나기가 핸드폰 실시 이후 존재감이 많이 잊혀지고 있다...*****~~(설마 너도 소나기 안쓰고 있니??)~~*****
우리는 훈련병 때 그때 그 감성을 다시 되새겨보기 위해서 이 앱을 개발했다. 일기만 쓴다면 우리는 핸드폰에 내장되어 있는 켈린더 앱을 사용할 것이다. 하지만 "소나기" App은 사용자 중심의 InterFace 기반으로 휴가, 훈련 등의 일정관리도 쉽게 가능하고 군 생활을 하는 사용자가 편리하도록 자신의 군생활에 관련된 정보를 제공해준다.

## App Functions
- 계획 관리 (오늘 및 내일에 걸쳐있는 일정이 뭐가 있는지 알려 준다.)
- 일기장 기능 (일기를 작성한 부분은 초록색으로)
- 군 생활 카운터 (진급일, 제대일, D-Day 기능), 남은 휴가 일수 (정기, 포상, 위로 구분) 관리, 매일 6시마다 알람 기능

## Prerequisites
- SdkVersion 28(최소 16이상은 필수)
- Java Jdk 8.0
- AndroidStudio

## Front-End
- AndroidStudio


 ## Back-End
- Django Restful API, Amazon EC2
 
## Extern Library 
- Calendar: MaterialCalendarView [Github](https://github.com/prolificinteractive/material-calendarview)
  
  
  
## File Manifest

	**LoadingActivity** xml : (activity_loading.xml)

 	 ㄴLoginActivity xml : (activity_login.xml)
 
       	 ㄴRegistrActivity xml : (activity_info_register.xml)
 
 	
 
 
 	**MainActivty(BottomNavigationView)**  xml: (acitivity_main.xml)
 	 ㄴFirstFragement (fragement_first) xml :activity_fist.xml
 	   ㄴMaterialCalendarView
 
 	 ㄴSecondFragement (fragment_second.xml)
 	   ㄴ 가로 listview (com.sonagi.android.myapplication/today_tab) xml :today_tv.xml
	   ㄴ 일정 listview (com.sonagi.android.myapplication/row) xml : row_chkbox.xml
	   ㄴ 추가 Dialog xml : add_diaog
 	 ㄴThirdFragment (fragment_third.xml)
	   ㄴInfoModifyActivity xml : (activity_info_modify.xml)
         ㄴAlarmReceiver, AlarmService
	 
**Login Acting**

`LoadingActivity`에서 로그인에 대한 토큰 검사 후 토큰이 없거나, 만료되었을 시 `LoginActivity`로 이동합니다. 토큰 존재시 `MainActivty`로 넘어가 서비스를 이용할 수 있습니다.


## Installation Process
저희의 Github Repository를 Clone후 Android Studio에서 빌드 하면 됩니다.
안드로이드 클론 방법 [바로가기](https://webnautes.tistory.com/1175)
   
## Copyright / End User License
- MIT
   

## Contact Information
email: happyjarban@gmail.com or sobu0715@gmail.com
   
   
## Known Issues
오늘 & 내일 일정 `Notification`을 병사들의 휴대폰 불출 시간인 18시에 맞추어 테스트 해보고 싶었지만, 시간 관계상 구현만 하고, 테스트는 안해 본 상태입니다.
 
## Troubleshooting
- 비밀번호 패턴 체크, 이메일 패턴 체크 같은 경우에는 정규 표현식으로 해결
- User Check, Email Check 같은 경우에는 Backend 에서 오류 메세지를 Json으로 보낸 후 오류 메세지에 따라 중복 여부 호출
- `InfoRegisterActivity` 사용 중 앱이 강제 종료 되면, 다음 로그인 시에 정보를 다시 입력하게 끔 함.
- 

## Credit
- 박민재 : 서버 프로그래밍, 안드로이드-서버 간 데이터 처리 담당
- 조찬민 : 안드로이드 레이아웃, 달력 등 작동 로직등을 담당
  
  
## Change Log
 [2019.10.25] Relaese 1.0 version 
