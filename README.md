# BigBoardRestAPI
BigBoardRestAPI는 대규모 시스템에서 게시판을 구축할 때 고려해야 할 사항들을 학습하고 Redis를 활용한 성능 개선 및 부하 테스트를 실습하기 위해 개발된 프로젝트입니다.

## 주요 기능  
게시글 작성, 조회, 수정, 삭제 기능  
게시판 목록 및 상세 보기  
게시판 조회수 기능  
게시글에 대한 평가 및 평점 기능  
Redis를 활용한 캐싱 및 성능 최적화   

## 성능 테스트

### 테스트 목적
게시판 조회 API에 Redis 적용 전후의 성능을 비교하여 캐시 적용이 응답 속도와 처리량에 미치는 영향을 분석한다.

### 테스트 환경
JDK 버전: OpenJDK 11  
프레임워크: Spring Boot 3.4.1  
데이터베이스: MySQL 9.2.0   
캐시 시스템: Redis 3.4.1  
테스트 도구: nGrinder 3.5.9  

### 테스트 대상 API   
•	GET /before/board  --게시글 목록 조회 적용 전  
•	GET /board         --게시글 목록 조회 적용 후  
•	설명: 게시판 목록를 조회하는 API로, Redis 적용 전후의 응답 속도를 비교한다.  

•	GET /before/board/detail/{boardId}  --특정 게시글 조회 적용 전  
•	GET /board/detail/{boardId}         --특정 게시글 조회 적용 후  
•	설명: 특정 게시판 정보를 조회하는 API로 조회수 증가를 캐싱한 후 증가된 조회수를 DB에 일괄 적용, Redis 적용 전후의 응답 속도를 비교한다.  

•	PUT /before/board/evaluation  --게시글 평가 업데이트 적용 전  
•	PUT /board/evaluation         --게시글 평가 업데이트 적용 후  
•	설명: 게시판 평가 정보를 업데이트하는 API로 평가 점수를 캐싱한 후 DB에 일괄 적용, Redis 적용 전후의 응답 속도를 비교한다.  
 
### 테스트 조건
테스트 지속 시간: 3~5분
동시 접속자 수 (Vuser):99명

### 테스트 절차
MySQL 데이터베이스에 더미 데이터 100만 개 삽입  
Redis 적용 전, 적용 후 코드 분리   
nGrinder를 설정하여 부하 테스트 준비   


![image](https://github.com/user-attachments/assets/c03a1f57-01a7-4c95-a75e-a0e7e815f480)

### Redis 적용 전 테스트
#### GET before/board  
![image](https://github.com/user-attachments/assets/bc86a336-3aa0-4f43-b793-e2d9e18b44d6)

평균 TPS : 125  
평균 응답 시간(ms) : 786.97  
에러율(%) : 0   
  
#### GET /before/board/detail/{boardId}  
![image](https://github.com/user-attachments/assets/4c68c842-f70c-4688-978a-a84fa634b5ff)

평균 TPS : 2808  
평균 응답 시간(ms) : 33.64  
에러율(%) : 0  
  
#### PUT /before/board/evaluation  
![image](https://github.com/user-attachments/assets/39fd136e-18df-4c1c-af40-8a51376e3ec5)

평균 TPS : 2063.2  
평균 응답 시간(ms) : 45.8  
에러율(%) : 0  

### Redis 적용 후 테스트  
#### GET /board  
![image](https://github.com/user-attachments/assets/d192ad7b-acf2-45a5-8676-d625dd5aa4a8)

평균 TPS : 2473.5  
평균 응답 시간(ms) : 39  
에러율(%) : 0  
  
#### GET /board/detail/{boardId}  
![image](https://github.com/user-attachments/assets/b01554e6-b5aa-4d73-8cda-cb86c32ddc62)

평균 TPS : 2900.6  
평균 응답 시간(ms) : 32.6  
에러율(%) : 0  
  
#### PUT /board/evaluation
![image](https://github.com/user-attachments/assets/c5758112-7304-463a-8eb6-a216677cca8f)

평균 TPS : 2176.4  
평균 응답 시간(ms) : 43.5  
에러율(%) : 0  

#### 결과


#### GET /board (목록 조회)

TPS  
	•	적용 전: 125  
	•	적용 후: 2473.5  
	•	개선 정도: 약 19.8배 증가 (TPS 기준 약 1880% 상승)  
   
평균 응답 시간  
	•	적용 전: 786.97 ms  
	•	적용 후: 39 ms  
	•	개선 정도: 약 95% 단축  

#### GET /board/detail/{boardId} (상세 조회)

TPS  
	•	적용 전: 2808  
	•	적용 후: 2900.6  
	•	개선 정도: 약 92.6 TPS 상승 (약 3.3% 증가)  

평균 응답 시간  
	•	적용 전: 33.64 ms  
	•	적용 후: 32.6 ms  
	•	개선 정도: 약 1.04 ms 단축 (약 3.1% 개선)  
  
#### PUT /board/evaluation (평가 등록/갱신)

TPS  
	•	적용 전: 2063.2  
	•	적용 후: 2176.4  
	•	개선 정도: 113.2 TPS 상승 (약 5.5% 상승)	  

평균 응답 시간  
	•	적용 전: 45.8 ms  
	•	적용 후: 43.5 ms  
	•	개선 정도: 2.3 ms 약 (5% 개선)   

#### 평가
Redis를 적용시 조회에 대해서는 큰 폭으로 감소를 했으나 등록/갱신에 대해서는 개선은 되었으나 큰 차이가 없었다. 
조회수 증가, 평점 적용에 대해서는 메모리에 캐싱 후 DB에 일괄 저장하는 전략을 사용했으나 테스트 환경이 분산 시스템이 아니다보니 큰 이점을 가져가기는 어려웠다.
