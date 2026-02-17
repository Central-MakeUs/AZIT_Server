#!/bin/bash

# 현재 구동 중인 컨테이너 확인 (Blue가 떠있는지 체크)
IS_BLUE=$(sudo docker ps --filter "name=azit-blue" --filter "status=running" -q)

if [ -z "$IS_BLUE" ]; then
  TARGET_COLOR="blue"
  TARGET_PORT=8081
  IDLE_COLOR="green"
  IDLE_PORT=8082
else
  TARGET_COLOR="green"
  TARGET_PORT=8082
  IDLE_COLOR="blue"
  IDLE_PORT=8081
fi

echo "🚀 [무중단 배포 시작] 타겟: $TARGET_COLOR ($TARGET_PORT)"

# 새로운 버전의 컨테이너 실행
# docker-compose.yml에 정의된 서비스 이름을 인자로 전달
sudo docker compose up -d azit-$TARGET_COLOR

#  신규 컨테이너 헬스 체크
echo "🏥 헬스 체크를 시작합니다 (http://localhost:$TARGET_PORT/actuator/health)..."

for retry_count in {1..10}
do
  RESPONSE=$(curl -s http://localhost:$TARGET_PORT/actuator/health)
  UP_COUNT=$(echo $RESPONSE | grep 'UP' | wc -l)

  if [ $UP_COUNT -ge 1 ]; then
    echo "✅ 헬스 체크 성공! ($retry_count/10)"
    break
  else
    echo "⏳ 대기 중... ($retry_count/10)"
    sleep 5
  fi

  if [ $retry_count -eq 10 ]; then
    echo "❌ 헬스 체크에 실패하여 배포를 중단합니다."
    sudo docker stop azit-$TARGET_COLOR
    exit 1
  fi
done

# Nginx 포트 스위칭 (Nginx가 참조하는 설정 파일 업데이트)
echo "🔄 Nginx 연결 포트를 $TARGET_PORT 로 변경합니다."
echo "server 127.0.0.1:$TARGET_PORT;" | sudo tee /etc/nginx/conf.d/service-env.inc

# Nginx 설정 반영 (서비스 중단 없이 설정만 다시 읽음)
sudo nginx -s reload

# 이전 버전의 컨테이너 종료
echo "🧹 구버전 ($IDLE_COLOR) 컨테이너를 종료합니다."
sudo docker stop azit-$IDLE_COLOR


echo "✨ 배포가 완료되었습니다!"