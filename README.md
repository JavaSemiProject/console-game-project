# console-game-project

자바 세미프로젝트 콘솔 게임 — AI 딸깍으로 과제하던 영균이 참교육 당한 썰 푼다

---

## 실행 환경 요구사항

| 항목 | 버전 |
|------|------|
| Java | JDK 11 이상 |
| Docker | 최신 버전 (Docker Desktop 권장) |
| OS | Windows 10/11 |

---

## 실행 방법

### 1. 저장소 클론

```bash
git clone https://github.com/JavaSemiProject/console-game-project.git
cd console-game-project
```

### 2. Docker로 MySQL 실행

프로젝트 루트에서 아래 명령어를 실행합니다.

```bash
docker-compose up -d
```

- MySQL 8.0 컨테이너가 **포트 3307**에 실행됩니다.
- `init.sql`이 자동으로 적용되어 테이블과 초기 데이터가 세팅됩니다.
- 최초 실행 시 이미지 다운로드로 1~2분 소요될 수 있습니다.

DB 정상 실행 여부 확인:

```bash
docker ps
# mysql_console 컨테이너가 Up 상태여야 합니다.
```

### 3. 게임 실행

프로젝트 루트의 `run.bat`을 더블클릭하거나, **CMD**에서 실행합니다.

```cmd
run.bat
```

> **주의**: PowerShell이 아닌 **CMD(명령 프롬프트)** 에서 실행해야 합니다.  
> 한글이 깨지는 경우 CMD 창에서 우클릭 → 속성 → 글꼴을 `NSimSun` 또는 `Consolas`로 변경해보세요.

---

## DB 연결 정보

`src/db/DBConnection.java`에 하드코딩되어 있습니다.

| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 3307 |
| Database | console_game |
| User | root |
| Password | 1234 |

포트나 비밀번호를 바꾸려면 `docker-compose.yml`과 `DBConnection.java`를 함께 수정해야 합니다.

---

## 직접 컴파일하는 경우

```cmd
javac -encoding UTF-8 -cp lib\mysql-connector-j-9.6.0.jar @sources.txt -d out\cmd
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -cp out\cmd;lib\mysql-connector-j-9.6.0.jar Main
```

---

## 게임 재시작 (DB 초기화)

진행 데이터를 완전히 초기화하려면 컨테이너를 볼륨째 삭제하고 다시 올립니다.

```bash
docker-compose down -v
docker-compose up -d
```

---

## 트러블슈팅

| 증상 | 원인 | 해결 |
|------|------|------|
| `DB 연결 실패` 메시지 | Docker 컨테이너 미실행 | `docker-compose up -d` 재실행 |
| `Compilation failed!` | JDK 미설치 또는 PATH 미설정 | JDK 설치 후 `java -version` 확인 |
| 한글 깨짐 | 터미널 인코딩 불일치 | CMD에서 실행, 글꼴 변경 |
| 포트 충돌 (`3307`) | 다른 프로세스가 3307 사용 중 | `docker-compose.yml`에서 포트 변경 후 `DBConnection.java`도 동일하게 수정 |