# 🏃‍♂️ AZIT (아지트) - 러닝 크루를 위한 운영 및 제휴 서비스
<img width="1920" height="1080" alt="1" src="https://github.com/user-attachments/assets/447349c4-98e3-447d-8d09-1e6d7b23c9ca" />

> **"크루원과 함께하는 실시간 출석 인증부터 아지트 전용 스토어까지"**
> 
> AZIT는 러닝 크루의 일정을 체계적으로 관리하고, 위치 기반 출석 체크 및 포인트 적립을 통해 크루원들의 참여도를 높이는 러닝 크루 전용 플랫폼입니다.
<br>

<div align="center">
  <a href="https://apps.apple.com/kr/app/%EC%95%84%EC%A7%80%ED%8A%B8-azit/id6758881115" target="_blank">
    <img src="https://developer.apple.com/assets/elements/badges/download-on-the-app-store.svg" alt="Download on the App Store" height="40" align="middle">
  </a>
  <a href="https://play.google.com/store/apps/details?id=com.azitcrew.app&hl=ko" target="_blank">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/ko_badge_web_generic.png" alt="Get it on Google Play" height="58" align="middle">
  </a>
</div>
<br>
<br>


## ✨ 핵심 기능
<div align="center">
  <img src="https://github.com/user-attachments/assets/d9faf147-acce-4c12-b06c-963053ac7d39" alt="스크린샷1" width="19%">
  <img src="https://github.com/user-attachments/assets/431a3f81-ac53-4280-ad90-e195c3749731" alt="스크린샷2" width="19%">
  <img src="https://github.com/user-attachments/assets/50411a62-8091-4d78-a1b9-482da3dcb199" alt="스크린샷3" width="19%">
  <img src="https://github.com/user-attachments/assets/707e9697-2091-4053-8d8b-071834341681" alt="스크린샷4" width="19%">
  <img src="https://github.com/user-attachments/assets/9c06c0a7-0aa8-48ee-af4a-f2b12ddb8a20" alt="스크린샷5" width="19%">
</div>
<br>

### 1. 📍 실시간 위치 기반 출석 체크
* **GPS 기반 인증**: 모임 장소 반경 100m 이내에서만 출석하기 버튼이 활성화되어 정확한 출석을 유도합니다.
* **스마트 타임 윈도우**: 모임 시간 1시간 전부터 1시간 후까지만 출석이 가능하도록 제한하여 운영의 신뢰성을 높였습니다.
* **홈 위젯**: 앱 진입 시 가장 가까운 일정의 출석 가능 상태(D-Day, 남은 시간)를 직관적으로 확인할 수 있습니다.

### 2. 📅 캘린더 기반 러닝 참여 및 생성
* **월별 일정 조회**: 정기런과 번개런을 캘린더에 색상 점(Dot)으로 구분하여 한눈에 크루 일정을 파악할 수 있습니다.
* **충돌 방지 로직**: 내가 이미 참여 중인 일정과 시간이 겹치는 새로운 일정에는 참여할 수 없도록 검증 로직이 적용되어 있습니다.

### 3. 🎁 출석 보상 및 전용 스토어
* **포인트 적립**: 출석을 완료할 때마다 자동으로 100 포인트가 즉시 적립됩니다.
* **아지트 크루 전용 스토어**: 모은 포인트를 활용해 러닝 용품 등 아지트만의 특별한 상품을 합리적인 가격에 주문할 수 있습니다.

### 4. 👥 크루 관리
* 리더의 승인/거절, 멤버 방출 등의 권한 관리를 통해 쉽고 편리한 크루 관리가 가능합니다.

<br>

## 🛠 기술 스택

### Backend
* **Language & Framework**: Java, Spring Boot, Spring Batch
* **Architecture**: Hexagonal Architecture (Port & Adapter Pattern)
* **Database & ORM**: Spring Data JPA, QueryDSL, MySQL, Redis
* **Documentation**: Swagger (SpringDoc OpenAPI)
* **Security**: Spring Security, JWT (Apple/Kakao OAuth)

### Infrastructure & DevOps
* **Cloud**: AWS (EC2, RDS, S3, CloudFront, Route53, ECS, Fargate, EventBridge)
* **Container**: Docker, Docker Compose
* **Web Server**: Nginx (Reverse Proxy, Blue-Green Switching)
* **CI/CD**: GitHub Actions
* **Monitoring**: New Relic, Discord (알림 연동)
* **OS**: Ubuntu 22.04 LTS

<br>

## ⚙️ 시스템 아키텍처 특징

* **도메인 주도 설계(DDD)**: 도메인 객체 내부에 핵심 비즈니스 로직과 상태 변경 메서드를 캡슐화하여 응집도를 높였습니다.
* **헥사고날 아키텍처(Hexagonal)**: `in` / `out` 포트와 어댑터를 명확히 분리하여 영속성 계층(DB)의 변경이 비즈니스 로직(UseCase)에 영향을 주지 않도록 설계했습니다.
* **데이터 처리 최적화 (Spring Batch)**: 대량의 데이터 처리(탈퇴 회원 영구 삭제, 무통장 입금 기한 만료 주문 취소 처리 등)를 API 서버와 분리하여 서버 리소스 간섭을 방지하고, Chunk 지향 처리를 통해 메모리 효율성을 극대화했습니다.

<br>

## ☁️ 인프라 및 CI/CD

<div align="center">
  <img src="https://github.com/user-attachments/assets/8d4764a1-ccee-40f3-9a05-dae48b06e621" alt="AZIT Infrastructure Architecture" width="90%">
</div>
<br>

### ☁️ 서버리스 배치 시스템
* **자원 격리**: 대용량 데이터 작업이 상시 가동 중인 API 서버(EC2)의 성능에 영향을 주지 않도록 컨테이너 환경을 완전히 분리했습니다.
* **비용 최적화**: 24시간 서버를 띄우지 않고, 배치 작업이 필요한 시점에만 컨테이너를 실행하며 **Fargate Spot 인스턴스**를 활용해 비용을 절감했습니다.
* **스케줄링**: **AWS EventBridge**를 통해 각 배치 작업의 실행 주기를 관리합니다.

<br>

### 🚀 CI/CD 및 배포 (Blue-Green)
* **무중단 배포**: Nginx와 Docker Compose를 활용한 **Blue-Green 무중단 배포** 환경을 구축했습니다. 새로운 버전의 컨테이너를 띄운 후, Spring Boot Actuator로 헬스 체크를 통과했을 때만 Nginx 포트를 스위칭하여 서비스 중단 없이 안정적인 배포를 보장합니다.
* **보안을 고려한 동적 파이프라인**: GitHub Actions를 통한 자동 배포 시, Runner의 IP를 AWS EC2 Security Group에 임시로 허용(Port 22)하고 배포 완료 후 즉시 차단하여 외부의 보안 위협을 최소화했습니다.
* **실시간 모니터링 및 알림**: 모니터링 툴로 **New Relic**을 도입하여 슬로우 쿼리 등 서버의 성능과 상태를 모니터링하며, 시스템 장애를 Discord 웹훅과 연동하여 즉각적으로 대응할 수 있는 체계를 갖췄습니다.

<details>
<summary><b>💡 무중단 배포 쉘 스크립트(deploy.sh) 핵심 로직</b></summary>
<div markdown="1">

```bash
# 1. 신규 컨테이너 헬스 체크 (10회 반복)
for retry_count in {1..10}
do
  RESPONSE=$(curl -s http://localhost:$TARGET_PORT/actuator/health)
  UP_COUNT=$(echo $RESPONSE | grep 'UP' | wc -l)

  if [ $UP_COUNT -ge 1 ]; then
    echo "✅ 헬스 체크 성공! ($retry_count/10)"
    break
  fi
  sleep 10
done

# 2. Nginx 포트 스위칭 및 설정 리로드
echo "server 127.0.0.1:$TARGET_PORT;" | sudo tee /etc/nginx/conf.d/service-env.inc
sudo nginx -s reload

# 3. 구버전 컨테이너 종료
sudo docker stop azit-$IDLE_COLOR
