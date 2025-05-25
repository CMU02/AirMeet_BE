# Spring Data Redis Document
```text
https://docs.spring.io/spring-data/redis/reference/redis/getting-started.html
```
# Spring WebSocket API Document
```text
https://docs.spring.io/spring-framework/reference/web/websocket/server.html
```
# Redis Key Structure 요약
| Key                                 | 설명                 |
| ----------------------------------- |--------------------|
| `code:{joinCode}` → roomId          | joinCode로 방 찾기     |
| `room:{roomId}` → MeetingRoom       | 회의방 데이터            |
| `user:{userId}` → User              | 유저 정보              |
| `room:{roomId}:users` → Set<userId> | 해당 방에 누가 있는지       |
| `user:{userId}:rooms` → Set<roomId> | 해당 유저가 참여 중인 방 리스트 |
| `chat-room:{roomId}:messages` -> List<roomId> | 해당하는 방에 채팅 내역 리스트  | 

