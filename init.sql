DROP DATABASE IF EXISTS console_game;
CREATE DATABASE console_game;
USE console_game;

DROP TABLE IF EXISTS c_save;
DROP TABLE IF EXISTS i_save;
DROP TABLE IF EXISTS e_save;
DROP TABLE IF EXISTS save;
DROP TABLE IF EXISTS stage;
DROP TABLE IF EXISTS ending;
DROP TABLE IF EXISTS npc;
DROP TABLE IF EXISTS hero;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS particle;

create table particle( -- 조사 테이블
                         pp int primary key, -- 조사의 타입 확인, 0과 1 구분
                         sound_lg char(3) not null, -- 이, 가
                         sound_er varchar(3) not null, -- 을, 를
                         sound_en varchar(3) not null -- 은 는
);

create table ending ( -- 엔딩 목록 테이블
                        e_id varchar(10) primary key, -- 엔딩 id
                        e_name varchar(50) not null unique, -- 엔딩명
                        e_desc varchar(500) not null, -- 엔딩 설명
                        e_img text -- 엔딩 이미지
);

create table card( -- 카드 목록 테이블 (모든 카드의 기본 데이터)
                     c_id varchar(10) primary key, -- 카드 id
                     c_name varchar(30) not null unique, -- 카드 함수명
                     pp int not null , -- 조사 테이블 논리 값을 위한 컬럼
                     c_power int not null, -- 카드 공격력
                     c_desc varchar(500) not null unique, -- 카드에 대한 설명
                     c_use_msg varchar(500) not null unique, -- 카드 사용시 출력 물
                     c_img varchar(2000), -- 카드 수집시 보여줄 출력물
                     foreign key (pp) references particle(pp)
);

create table item( -- 아이템 목록 테이블 (모든 아이템의 기본 데이터)
                     i_id varchar(10) primary key, -- 아이템 id
                     i_name varchar(30) not null unique, -- 아이템 이름
                     pp  int default 0 not null, -- 조사 테이블 논리 값을 위한 컬럼
                     hp int, -- 체력 총량
                     i_power int, -- 공격력
                     heal int, -- 체력 회복 수치
                     i_use_msg varchar(500) not null, -- 아이템 사용시 출력 메세지
                     i_desc varchar(500) not null, -- 아이템에 대한 설명
                     i_img varchar(2000) not null, -- 아이템 이미지
                     foreign key (pp) references particle(pp)
);

create table npc( -- 게임에 나올 모든 캐릭터 및 몹 리스트
                    n_id varchar(10) primary key, -- npc id
                    n_name varchar(30) not null unique , -- npc 이름
                    pp int default 0 not null, -- npc 이름 뒤에 붙을 조사 타입
                    hp int not null, -- 체력 총량
                    power_min int default 0 not null, -- 최소 공격력
                    power_max int default 0 not null, -- 최대 공격력(치명타)
                    c_id varchar(10), -- 처치시 드랍할 카드 id
                    i_id varchar(10), -- 처치시 드랍할 아이템 id
                    is_boss int default 0 not null, -- 메인 보스인가 아닌가? 보스일 경우 1, 잡몹은 0
                    n_desc varchar(500) not null, -- npc 에 대한 설명
                    foreign key (pp) references particle(pp),
                    foreign key (c_id) references card(c_id),
                    foreign key (i_id) references item(i_id)
);

create table stage( -- 맵 테이블
                      s_id varchar(10) primary key, -- 맵의 id (좌표 형식 : 층_가로세로)
                      f_level int not null, -- 층수
                      s_column varchar(5) not null, -- 가로
                      s_row int not null, -- 세로
                      s_type varchar(20) not null, -- 맵의 타입(시작, 끝, 막힌구역, 이벤트, 몹, 보이드)
                      s_prob double not null, -- 맵 타입의 발동 확률(몹이나 이벤트 발생 확률)
                      n_id varchar(10), -- 맵에 나올 몹 번호
                      foreign key (n_id) references npc(n_id)
);


create table save( -- 세이브 로그 테이블
                     t_id varchar(10), -- 세이브 ID
                     try int primary key auto_increment, -- 게임 실행 번호
                     s_id varchar(10), -- 스테이지 ID
                     t_time timestamp default current_timestamp, -- 데이터 저장 시각
                     foreign key (s_id) references stage(s_id)
);

create table c_save( -- 카드 습득 로그 테이블 (컬렉션 반영할 테이블)
                       c_id varchar(10), -- 카드 번호
                       try int, -- 게임 실행 번호
                       c_count int, -- 카드 획득 횟수 카운트
                       foreign key (c_id) references card(c_id),
                       foreign key (try) references save(try)
);

create table i_save( -- 아이템 습득 로그 테이블 (컬렉션 반영할 테이블)
                       i_id varchar(10), -- 아이템 번호
                       try int, -- 게임 실행 번호
                       i_count int, -- 아이템 획득 횟수 카운트
                       foreign key (i_id) references item(i_id),
                       foreign key (try) references save(try)
);

create table e_save( -- 엔딩 해금 로그 테이블 (컬렉션 반영할 테이블)
                       e_id varchar(10), -- 엔딩 번호
                       try int, -- 엔딩 실행 번호
                       e_count int, -- 엔딩 해금 횟수 카운트
                       foreign key (e_id) references ending(e_id),
                       foreign key (try) references save(try)
);

create table n_save( -- NPC 만남 로그 테이블 (컬렉션 반영할 테이블)
                       n_id varchar(10), -- NPC 번호
                       try int, -- 게임 실행 번호
                       n_count int, -- NPC 만남 횟수 카운트
                       foreign key (n_id) references npc(n_id),
                       foreign key (try) references save(try)
);

INSERT INTO particle (pp, sound_lg, sound_er, sound_en) VALUES (0, '이', '을', '은');
INSERT INTO particle (pp, sound_lg, sound_er, sound_en) VALUES (1, '가', '를', '는');

INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c1', 'Scanner', 1, 0, '키보드 등에서 입력을 받을 때 사용하는 클래스.', '카드가 혜진이의 스킬을 흡수했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c2', 'random', 0, 20, '난수를 생성할 때 사용하는 클래스 또는 메서드.', '어디선가 거대한 주사위가 굴러왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c3', ';', 0, 0, '문장의 끝을 나타내는 기호.', '아무런 반응이 없다.', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c4', 'TreeNode', 1, 25, '트리 구조의 노드를 나타내는 클래스 이름으로 자주 쓰임.', '어딘가에서 ｀덩굴｀이 나타나 주변을 휘둘렀다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c5', 'while', 0, 22, '조건이 참인 동안 반복하는 반복문.', '카드에서 거대한 ｀풍차｀가 튀어나와 강한 바람을 날렸다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c6', 'if-else', 1, 30, '조건에 따라 코드 실행 경로를 바꾸는 제어문.', '카드에서 물음표들이 총알처럼 빠르게 나왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c7', 'new', 1, 30, '객체를 생성할 때 사용하는 키워드.', '카드에서 갖가지 모양의 ｀찰흙｀들을 던졌다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c8', 'sleep', 0, 29, '일정 시간 실행을 멈추는 메서드(java.lang.Thread의 static 메서드).', '어디선가 ｀양떼｀가 나타나 돌진했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c9', 'class', 1, 23, '클래스를 정의할 때 사용하는 키워드.', '하늘에서 ｀교실 책상｀이 떨어져 강타했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c10', '{ }', 1, 3500, '중괄호, 코드 블록(함수, 클래스, 조건문 등 범위 지정).', '카드에서 ｀새｀같이 생긴 것들이 날아가 강타했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c11', 'print', 1, 25, '화면에 내용을 출력하는 메서드(System.out.print 등).', '카드에서 ｀ ( ) ｀ 가 매섭게 날아왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c12', 'substring', 0, 27, '문자열의 일부를 잘라내는 메서드.', '카드에서 나온 커다란 ｀칼｀을 휘둘렀다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c13', 'import', 1, 25, '외부 클래스나 패키지를 불러올 때 쓰는 키워드.', '어디선가 ｀배달 오토바이｀가 달려왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c14', 'equals', 1, 35, '문자열이나 객체가 같은지 비교하는 메서드.', '하늘에서 거대한 ｀균형 저울｀이 나타나 지면을 내리쳤다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c15', 'Array', 1, 23, '배열을 나타내는 자료구조 혹은 클래스 이름.', '어디선가 거대한 ｀상자｀들이 쏟아져 나왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c16', 'return', 0, 20, '메서드의 결과 값을 반환하고 종료할 때 사용.', '카드에서 ｀부메랑｀이 날아갔다, 다시 카드 속으로 돌아갔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c17', 'format', 0, 30, '문자열을 특정 형식으로 변환할 때 쓰는 메서드.', '어디선가 거대한 ｀모양틀｀이 튀어나왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c18', 'boolean', 0, 20, '참(true) 또는 거짓(false) 값을 가지는 자료형.', '하늘에서 true와 false ｀도장｀이 나타나 지면을 세게 내려 찍었다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c19', 'sum', 0, 20, '합계를 구할 때 주로 사용하는 메서드나 변수명.', '주변에 있는 ｀0｀과 ｀1｀들이 ｀눈｀처럼 뭉쳐지며 굴러갔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c20', 'min', 0, 20, '최솟값을 구할 때 사용.', '주변에 있는 1｀들이 뽑혀 나와, ｀바늘｀처럼  찌르기 시작했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c21', 'max', 1, 20, '최댓값을 구할 때 사용.', '주변에 있는 가장 큰 ｀0｀과 ｀1｀ 이 튀어나와 타격했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c22', 'size', 1, 30, '컬렉션이나 리스트 등의 크기를 반환할 때 사용.', '카드에서 나온 ｀줄자｀를 채찍처럼 휘둘렀다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c23', 'static', 0, 24, '클래스 변수나 메서드를 정의할 때 사용, 객체 생성 없이 사용 가능.', '하늘에서 ｀압정｀이 떨어져 박혔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c24', 'try-catch', 1, 27, '예외 처리를 할 때 사용하는 블록.', '카드에서 나온 커다란 ｀잠자리채｀를 휘둘렀다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c25', 'throws', 1, 32, '메서드에서 예외를 던질 때 선언하는 키워드.', '카드에서 ｀폭탄｀이 튀어나왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c26', 'break', 1, 20, '반복문 또는 switch 문을 즉시 종료.', '카드에서 ｀정지 표지판｀이 날아왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c27', 'get', 0, 20, '객체의 값을 가져오는 메서드 이름에 자주 사용됨.', '하늘에서 거대한 ｀인형뽑기 집게｀가 내려왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c28', 'charAt', 0, 20, '문자열에서 특정 위치의 문자를 가져오는 메서드.', '하늘에서 거대한 ｀핀셋｀이 무언가를 집으려 날아왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c29', 'remove', 1, 11, '컬렉션 등에서 요소를 삭제하는 메서드.', '어디선가 커다란 ｀수정테이프｀가 나타나 빠르게 돌진했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c30', 'concat', 0, 20, '문자열을 이어 붙이는 메서드.', '하늘에서 커다란 ｀본드｀가 나타나 접착제를 뿌렸다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c31', 'length', 1, 20, '배열이나 문자열의 길이를 반환하는 속성 또는 메서드.', '하늘에서 거대한 ｀자｀가 튀어나와 지면을 내려쳤다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c32', 'List', 1, 20, '순서가 있는 컬렉션 인터페이스.', '카드에서 ｀두루마리｀가 빠르게 펼쳐지며 강타했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c33', 'trim', 0, 20, '문자열 앞뒤의 공백을 제거하는 메서드.', '카드에서 커다란 ｀가위｀가 나타나 가위질을 했다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c34', 'this', 1, 20, '현재 인스턴스 자신을 가리키는 참조변수.', '어디선가 ｀거울 조각｀들이 날아왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c35', 'Thread', 1, 20, '병렬 실행을 관리하는 클래스.', '카드에서 ｀실｀들이 여기저기로 뻗어 나왔다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c36', 'hashCode', 1, 20, '객체의 해시코드를 반환하는 메서드.', '하늘에서 ｀바코드 리더기｀가 내려와 내리 찍었다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c37', 'object', 1, 20, '모든 클래스의 최상위 부모 클래스 이름.', '하늘에서 ｀택배상자｀가 떨어졌다!', '이미지');
INSERT INTO card (c_id, c_name, pp, c_power, c_desc, c_use_msg, c_img) VALUES ('c38', 'continue', 1, 20, '반복문 내에서 현재 반복만 건너뛰고 다음 반복으로 진행.', '어디선가 ｀기관차｀가 빠르게 달려왔다!', '이미지');


INSERT INTO item (i_id, i_name, pp, hp, i_power, heal, i_use_msg, i_desc, i_img) VALUES ('i1', '목화골드', 1, 0, 0, 20, '체력을 20 회복 했습니다!', '원조 커피 맛집. 체력을 20 회복한다.', '이미지');
INSERT INTO item (i_id, i_name, pp, hp, i_power, heal, i_use_msg, i_desc, i_img) VALUES ('i2', '뻑카스', 1, 0, 0, 30, '체력을 30 회복 했습니다!', '뻑가는 맛이다. 체력을 30 회복한다.', '이미지');
INSERT INTO item (i_id, i_name, pp, hp, i_power, heal, i_use_msg, i_desc, i_img) VALUES ('i3', '도레쓰비', 1, 0, 0, 20, '체력을 20 회복 했습니다!', '설탕이 왕창 들어간 맛이다. 체력을 20 회복한다.', '이미지');
INSERT INTO item (i_id, i_name, pp, hp, i_power, heal, i_use_msg, i_desc, i_img) VALUES ('i4', '헉식스', 1, 0, 10, 0, '공격력을 5 증가 시켰습니다!', '헉하고 놀랄 만큼 마시는 순간 잠이 깬다. 공격력을 5 증가시킨다.', '이미지');
INSERT INTO item (i_id, i_name, pp, hp, i_power, heal, i_use_msg, i_desc, i_img) VALUES ('i5', '리드불', 0, 0, 20, 0, '공격력을 10 증가 시켰습니다!', '밤을 새도록 리드 한다. 공격력을 10 증가시킨다.', '이미지');
INSERT INTO item (i_id, i_name, pp, hp, i_power, heal, i_use_msg, i_desc, i_img) VALUES ('i6', '쓰다벅스 아아', 1, 100, 0, 100, '체력이 100 올랐습니다!', '쓰지만 커피의 근본. 체력을 100 올려준다', '이미지');

INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n1', '영균', 0, 100, 10, 15, 'c1', null, 1, '머리가 나쁜편은 아니지만 게으르다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n2', '혜진', 0, 100, 25, 30, 'c2', null, 1, '영균과 같은 반이자 프로젝트를 같이하는 팀원이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n3', '선혁', 0, 30, 25, 35, 'c3', null, 1, '영균과 같은 반이자 프로젝트를 같이하는 팀원이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n4', '미주', 1, 100, 25, 30, 'c4', null, 1, '숲에서 나오는 보스이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n5', '솔민', 0, 100, 25, 35, 'c5', null, 1, '초원에서 나오는 보스이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n6', '제석', 0, 100, 25, 40, 'c6', null, 1, '사막에서 나오는 보스이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n7', '수지', 1, 100, 25, 45, 'c7', null, 1, '강물에서 나오는 보스이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n8', '봉민', 0, 100, 25, 50, 'c8', null, 1, '심해에서 나오는 보스이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n9', '민중', 0, 100, 25, 55, 'c9', null, 1, '화산에서 나오는 보스이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n10', '보경', 0, 100, 25, 60, 'c10', null, 1, '영균과 친구들을 가르치는 강사이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n11', '딸깍', 0, 10000, 50, 100, 'c11', null, 1, '일만 시키는 영균이에게 복수하기 위해, 영균이를 JVM 속으로 빨아들였다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n12', '캐시', 1, 100, 25, 30, null, 'i6', 1, '무언가를 많이 가지고 있다.');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n13', 'GC', 1, 100, 25, 55, 'c10', null, 1, '가비지 컬렉터');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n14', '빨간 무당벌래', 1, 50, 5, 15, 'c12', null, 0, '빨간색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n15', '주황 무당벌래', 1, 50, 5, 15, 'c13', null, 0, '주황색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n16', '노랑 무당벌래', 1, 50, 5, 15, 'c14', null, 0, '노란색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n17', '초록 무당벌래', 1, 50, 5, 15, 'c15', null, 0, '초록색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n18', '파랑 무당벌래', 1, 50, 5, 15, 'c16', null, 0, '파란색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n19', '남색 무당벌래', 1, 50, 5, 15, 'c17', null, 0, '남색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n20', '보라 무당벌래', 1, 50, 5, 15, 'c18', null, 0, '보라색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n21', '검정 무당벌래', 1, 50, 5, 15, 'c19', null, 0, '검정색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n22', '흰색 무당벌래', 1, 50, 5, 15, 'c20', null, 0, '흰색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n23', '빨강 나방', 0, 50, 5, 15, 'c21', null, 0, '빨간색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n24', '주황 나방', 0, 50, 5, 15, 'c22', null, 0, '주황색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n25', '노랑 나방', 0, 50, 5, 15, 'c23', null, 0, '노란색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n26', '초록 나방', 0, 50, 5, 15, 'c24', null, 0, '초록색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n27', '파랑 나방', 0, 50, 5, 15, 'c25', null, 0, '파란색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n28', '남색 나방', 0, 50, 5, 15, 'c26', null, 0, '남색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n29', '보라 나방', 0, 50, 5, 15, 'c27', null, 0, '보라색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n30', '검정 나방', 0, 50, 5, 15, 'c28', null, 0, '검정색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n31', '흰색 나방', 0, 50, 5, 15, 'c29', null, 0, '흰색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n32', '빨강 파리', 1, 50, 5, 15, 'c30', null, 0, '빨간색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n33', '주황 파리', 1, 50, 5, 15, 'c31', null, 0, '주황색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n34', '노랑 파리', 1, 50, 5, 15, 'c32', null, 0, '노란색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n35', '초록 파리', 1, 50, 5, 15, 'c33', null, 0, '초록색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n36', '파랑 파리', 1, 50, 5, 15, 'c34', null, 0, '파란색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n37', '남색 파리', 1, 50, 5, 15, 'c35', null, 0, '남색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n38', '보라 파리', 1, 50, 5, 15, 'c36', null, 0, '보라색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n39', '검정 파리', 1, 50, 5, 15, 'c37', null, 0, '검정색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n40', '흰색 파리', 1, 50, 5, 15, 'c38', null, 0, '흰색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n41', '분홍색 무당벌래', 1, 50, 5, 15, null, 'i1', 0, '분홍색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n42', '연두색 무당벌래', 1, 50, 5, 15, null, 'i2', 0, '연두색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n43', '하늘색 무당벌래', 1, 50, 5, 15, null, 'i3', 0, '하늘색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n44', '갈색 무당벌래', 1, 50, 5, 15, null, 'i4', 0, '갈색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n45', '회색 무당벌래', 1, 50, 5, 15, null, 'i5', 0, '회색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n46', '분홍색 나방', 0, 50, 5, 15, null, 'i1', 0, '분홍색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n47', '연두색 나방', 0, 50, 5, 15, null, 'i2', 0, '연두색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n48', '하늘색 나방', 0, 50, 5, 15, null, 'i3', 0, '하늘색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n49', '갈색 나방', 0, 50, 5, 15, null, 'i4', 0, '갈색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n50', '회색 나방', 0, 50, 5, 15, null, 'i5', 0, '회색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n51', '분홍색 파리', 1, 50, 5, 15, null, 'i1', 0, '분홍색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n52', '연두색 파리', 1, 50, 5, 15, null, 'i2', 0, '연두색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n53', '하늘색 파리', 1, 50, 5, 15, null, 'i3', 0, '하늘색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n54', '갈색 파리', 1, 50, 5, 15, null, 'i4', 0, '갈색이다');
INSERT INTO npc (n_id, n_name, pp, hp, power_min, power_max, c_id, i_id, is_boss, n_desc) VALUES ('n55', '회색 파리', 1, 50, 5, 15, null, 'i5', 0, '회색이다');


INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('1_a1', 1, 'a', 1, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('1_a5', 5, 'a', 1, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_a1', 1, 'a', 2, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_a2', 2, 'a', 2, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_a3', 3, 'a', 2, 'npc_i', 0, 'n41');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_a4', 4, 'a', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_a5', 5, 'a', 2, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_b1', 1, 'b', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_b2', 2, 'b', 2, 'npc_i', 0, 'n14');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_b3', 3, 'b', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_b4', 4, 'b', 2, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_b5', 5, 'b', 2, 'npc_i', 0, 'n19');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_c1', 1, 'c', 2, 'npc_i', 0, 'n46');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_c2', 2, 'c', 2, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_c3', 3, 'c', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_c4', 4, 'c', 2, 'npc_i', 0, 'n42');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_c5', 5, 'c', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_d1', 1, 'd', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_d2', 2, 'd', 2, 'npc_i', 0, 'n15');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_d3', 3, 'd', 2, 'npc_i', 0, 'n51');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_d4', 4, 'd', 2, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_d5', 5, 'd', 2, 'npc_i', 0, 'n18');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_e1', 1, 'e', 2, 'event_semicolon', 0, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_e2', 2, 'e', 2, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_e3', 3, 'e', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_e4', 4, 'e', 2, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('2_e5', 5, 'e', 2, 'event_comment', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_a1', 1, 'a', 3, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_a2', 2, 'a', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_a3', 3, 'a', 3, 'npc_i', 0, 'n17');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_a4', 4, 'a', 3, 'npc_i', 0, 'n41');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_a5', 5, 'a', 3, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_b1', 1, 'b', 3, 'npc_i', 0, 'n46');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_b2', 2, 'b', 3, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_b3', 3, 'b', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_b4', 4, 'b', 3, 'npc_i', 0, 'n20');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_b5', 5, 'b', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_c1', 1, 'c', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_c2', 2, 'c', 3, 'npc_i', 0, 'n16');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_c3', 3, 'c', 3, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_c4', 4, 'c', 3, 'npc_i', 0, 'n54');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_c5', 5, 'c', 3, 'event_engine', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_d1', 1, 'd', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_d2', 2, 'd', 3, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_d3', 3, 'd', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_d4', 4, 'd', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_d5', 5, 'd', 3, 'npc_i', 0, 'n28');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_e1', 1, 'e', 3, 'npc_i', 0, 'n52');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_e2', 2, 'e', 3, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_e3', 3, 'e', 3, 'npc_i', 0, 'n27');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_e4', 4, 'e', 3, 'event_door', 0, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('3_e5', 5, 'e', 3, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_a1', 1, 'a', 4, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_a2', 2, 'a', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_a3', 3, 'a', 4, 'npc_i', 0, 'n48');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_a4', 4, 'a', 4, 'npc_i', 0, 'n21');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_a5', 5, 'a', 4, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_b1', 1, 'b', 4, 'npc_i', 0, 'n51');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_b2', 2, 'b', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_b3', 3, 'b', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_b4', 4, 'b', 4, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_b5', 5, 'b', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_c1', 1, 'c', 4, 'npc_i', 0, 'n23');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_c2', 2, 'c', 4, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_c3', 3, 'c', 4, 'event_betrayal', 0, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_c4', 4, 'c', 4, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_c5', 5, 'c', 4, 'npc_i', 0, 'n22');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_d1', 1, 'd', 4, 'npc_i', 0, 'n41');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_d2', 2, 'd', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_d3', 3, 'd', 4, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_d4', 4, 'd', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_d5', 5, 'd', 4, 'npc_i', 0, 'n52');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_e1', 1, 'e', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_e2', 2, 'e', 4, 'npc_i', 0, 'n24');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_e3', 3, 'e', 4, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_e4', 4, 'e', 4, 'npc_i', 0, 'n43');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('4_e5', 5, 'e', 4, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_a1', 1, 'a', 5, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_a2', 2, 'a', 5, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_a3', 3, 'a', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_a4', 4, 'a', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_a5', 5, 'a', 5, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_b1', 1, 'b', 5, 'npc_i', 0, 'n25');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_b2', 2, 'b', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_b3', 3, 'b', 5, 'npc_i', 0, 'n47');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_b4', 4, 'b', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_b5', 5, 'b', 5, 'npc_i', 0, 'n46');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_c1', 1, 'c', 5, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_c2', 2, 'c', 5, 'npc_i', 0, 'n30');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_c3', 3, 'c', 5, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_c4', 4, 'c', 5, 'npc_i', 0, 'n26');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_c5', 5, 'c', 5, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_d1', 1, 'd', 5, 'npc_i', 0, 'n44');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_d2', 2, 'd', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_d3', 3, 'd', 5, 'npc_i', 0, 'n42');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_d4', 4, 'd', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_d5', 5, 'd', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_e1', 1, 'e', 5, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_e2', 2, 'e', 5, 'npc_i', 0, 'n31');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_e3', 3, 'e', 5, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_e4', 4, 'e', 5, 'npc_i', 0, 'n29');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('5_e5', 5, 'e', 5, 'npc_i', 100, 'n49');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_a1', 1, 'a', 6, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_a2', 2, 'a', 6, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_a3', 3, 'a', 6, 'npc_i', 0, 'n47');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_a4', 4, 'a', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_a5', 5, 'a', 6, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_b1', 1, 'b', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_b2', 2, 'b', 6, 'npc_i', 0, 'n46');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_b3', 3, 'b', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_b4', 4, 'b', 6, 'npc_i', 0, 'n48');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_b5', 5, 'b', 6, 'npc_i', 0, 'n36');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_c1', 1, 'c', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_c2', 2, 'c', 6, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_c3', 3, 'c', 6, 'npc_i', 0, 'n32');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_c4', 4, 'c', 6, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_c5', 5, 'c', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_d1', 1, 'd', 6, 'npc_i', 0, 'n52');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_d2', 2, 'd', 6, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_d3', 3, 'd', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_d4', 4, 'd', 6, 'npc_i', 0, 'n37');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_d5', 5, 'd', 6, 'npc_i', 0, 'n42');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_e1', 1, 'e', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_e2', 2, 'e', 6, 'npc_i', 0, 'n33');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_e3', 3, 'e', 6, 'npc_i', 0, 'n54');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_e4', 4, 'e', 6, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('6_e5', 5, 'e', 6, 'event_cache', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_a1', 1, 'a', 7, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_a2', 2, 'a', 7, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_a3', 3, 'a', 7, 'npc_i', 0, 'n53');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_a4', 4, 'a', 7, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_a5', 5, 'a', 7, 'npc_i', 0, 'n45');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_b1', 1, 'b', 7, 'npc_i', 0, 'n50');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_b2', 2, 'b', 7, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_b3', 3, 'b', 7, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_b4', 4, 'b', 7, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_b5', 5, 'b', 7, 'npc_i', 0, 'n35');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_c1', 1, 'c', 7, 'npc_i', 0, 'n38');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_c2', 2, 'c', 7, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_c3', 3, 'c', 7, 'npc_i', 0, 'n39');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_c4', 4, 'c', 7, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_c5', 5, 'c', 7, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_d1', 1, 'd', 7, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_d2', 2, 'd', 7, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_d3', 3, 'd', 7, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_d4', 4, 'd', 7, 'npc_i', 0, 'n40');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_d5', 5, 'd', 7, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_e1', 1, 'e', 7, 'event_heap', 0, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_e2', 2, 'e', 7, 'npc_i', 0, 'n55');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_e3', 3, 'e', 7, 'npc_i', 0, 'n34');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_e4', 4, 'e', 7, 'npc_i', 0, 'n43');
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('7_e5', 5, 'e', 7, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('heap_a1', 0, '1', 0, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('heap_a2', 0, '2', 0, 'start', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('heap_a3', 0, '3', 0, 'v', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('heap_b1', 0, '1', 0, 'finish', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('heap_b2', 0, '2', 0, 'w', 100, null);
INSERT INTO stage (s_id, s_row, s_column, f_level, s_type, s_prob, n_id) VALUES ('heap_b3', 0, '3', 0, 'finish', 100, null);


INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e1', '바이트코드가 된 영균', '영균이의 HP가 0이 되었다.', '
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣄⣤⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢀⣤⠿⠉⠉⣽⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢠⣾⡟⢁⡠⣶⣾⠟⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠿⣿⢷⢿⠾⠛⣁⠤⠦⠴⢀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠈⠉⢁⡄⠉⠀⠀⠀⠀⣀⠈⠑⠤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⡌⠀⠀⣀⠀⠀⠀⠻⠇⠀⠀⠱⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠆⠀⠀⢿⡷⠀⢠⣄⠀⠀⠀⠀⠱⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠘⡄⠀⠀⠀⠀⠀⢿⡆⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠢⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡎⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠑⠢⡀⠀⠀⠀⠀⠀⢠⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢆⠀⠀⠀⢀⠎⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⡠⠒⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢅⠀⢣⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢆⡀⠉⠲⡀⠀⠀⠀⠀⠀⠀⠀⡀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠢⡸⠀⠀⠀⢀⡜⠊⠉⠉⠉⢉⠉⠉⠑⠚⠒⠶⠤⣄⣠⣀⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⠧⠴⠚⠊⠁⢰⠂⠀⠀⡶⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠈⠉⠳⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠎⠁⠀⠀⠀⠀⠀⣽⠀⠀⢰⠃⠀⠀⠀⠀⠀⠀⠀⢀⡠⠛⠀⠀⠀⠀⢹⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡄⠀⠀⠀⡀⠀⢔⡇⠀⠀⡟⢦⢦⡴⠤⠦⠴⠤⢄⡜⠁⠀⠀⠀⠀⣠⠞⠈⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠱⣤⣢⣐⡽⠋⢹⠀⠀⢸⠁⠀⡑⡇⠀⠀⠀⠀⡎⠀⠀⠀⠀⠀⣰⠃⠀⠀⠸⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣠⠤⠖⠚⠀⢀⡏⠀⠀⡼⠃⠀⠀⠀⡼⠁⠀⠀⠀⠀⣰⡿⡀⢁⠀⠀⠘⢦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⢶⠲⠶⠤⠴⠓⠋⠁⠀⢀⢀⣠⣠⡞⠀⢀⡞⠁⠀⠀⠀⢸⠁⠀⠀⠀⢀⡼⠁⠙⢷⣀⠄⠀⠀⠀⢳⡄⢀⢀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢾⣿⠿⠕⠒⠦⠦⠒⠚⠚⠉⢉⡝⣊⡁⠀⡴⠋⠀⠀⠀⠀⠀⡏⠀⠀⠀⠦⠾⢄⣀⠀⠀⠙⠲⣄⠀⠀⠀⠑⠉⠉⠉⠉⠙⠚⠒⠦⠴⣀⣄⣠⣀⣄⣠⣀⡀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⢝⣮⣶⢠⠇⠀⠀⠀⠀⠀⠀⢗⡀⠀⠀⠀⠀⠀⠈⠉⠓⠦⢄⣈⠳⢦⣀⡀⢀⠀⡀⠀⡀⢀⣀⣀⣀⣀⣀⣀⡀⠀⠀⠀⠻⣄⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⠓⠲⠶⢤⣤⣀⡀⠀⠀⠈⠑⠦⢤⣉⣉⣉⠉⠉⠉⠉⠁⠀⠁⠀⠀⠈⠈⠙⠲⡄⠂⡼⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠙⠓⠲⠦⢤⣄⠆⠉⢉⡷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠪⠃⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⠏⠀⡼⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⠤⠞⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
');
INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e2', '미안해 영균아', '영균이가 혜진이한테 쳐맞아 HP가 0이 되었다.', '

⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠀⣀⣄⣠⠤⠤⠦⠴⠚⠒⠓⠓⠓⠓⠓⠓⠓⠒⢦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣠⡤⠴⠓⠚⠉⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⠛⢆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣄⠼⠊⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡴⠃⠀⠘⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡴⠖⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡼⠁⣠⣀⡀⠘⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡴⠚⠉⠀⠀⠀⠀⠀⠀⠀⠀⣴⣿⣷⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡞⠀⢾⣿⣿⣿⡆⠘⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠴⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣿⣿⡿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⡴⠋⠀⠀⠘⠿⡿⠿⠁⠀⠘⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡴⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠁⠀⠀⠀⠀⠀⠀⠀⡀⣠⣠⠶⠶⠛⠋⠉⢷⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢀⡴⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣄⣠⣦⠼⠾⠚⠛⠉⠁⠀⠀⠀⠀⠀⠈⢳⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⣠⠞⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⣠⡤⠶⠚⠛⠉⠉⠁⠀⠀⠀⠀⠀⠀⢀⣀⠀⠀⠀⠀⠀⠀⠀⢷⠀⠀⠀⠀⠀⠀⡀⠀⠀⠀⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢠⣧⣤⣄⣄⣤⣤⣤⣴⣤⠶⠴⠾⠒⠚⠚⠉⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⣿⣿⣆⠀⣰⡆⣀⠀⠸⣧⠀⠀⠀⢰⣿⣿⣷⡄⠀⠈⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠸⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠿⣿⣿⡃⠀⡇⠹⢹⢶⠀⠹⣆⠀⠀⠸⣿⣿⡿⠇⠀⠀⠈⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡹⣐⠇⠀⠀⠘⣻⠀⢻⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢻⠀⠀⠀⠀⠀⠀⣠⣦⣦⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢧⡙⠦⠀⠀⠀⡏⠀⠈⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⡄⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠸⡆⠀⠀⠀⠀⠐⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣹⠀⠀⠀⡼⠁⠀⠀⠸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠃⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢷⠀⠀⠀⠀⠀⠉⠛⠛⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣺⠀⠀⡼⠁⠀⠀⠀⠀⢿⡀⠀⢀⣴⣤⣆⠀⠀⠀⡾⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠸⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢾⠀⠠⡇⠀⠀⠀⠀⠀⠘⣧⠀⢾⣿⣿⣿⡇⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⣿⣿⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⡏⠀⢐⡇⠀⠀⠀⠀⠀⠀⢻⡄⠈⠛⠟⠋⠁⠀⢸⠁⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢿⣿⡿⠇⠀⠀⠀⠀⠀⠀⠀⠀⢀⡇⠀⣸⠀⠀⠀⠀⠀⠀⠀⠘⣧⠀⠀⠀⠀⠀⠀⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⠇⠀⢼⠀⠀⠀⠀⠀⠀⠀⠀⢿⡂⠀⠀⠀⠀⣸⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣤⢤⣀⡀⠀⠀⠀⢸⠁⠀⡽⠀⠀⠀⠀⠀⠀⠀⠀⢸⣧⠀⠀⠀⢠⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠞⠁⠀⠀⠀⠉⢳⡀⠀⡾⠀⠀⣫⠀⣼⣿⣷⣆⠀⠀⠀⠀⣿⡆⠀⠀⡞⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⡂⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⠃⠀⠀⠀⠀⠀⠀⠀⠹⡄⡏⠀⠀⢼⠈⣿⣿⣿⡿⠀⠀⠀⠀⢹⣷⠀⡼⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣗⠀⠀⠀⠀⠀⠀⣰⣤⣦⣀⠀⠀⠀⠀⠀⠀⠀⠠⡇⢀⡀⡴⠀⠀⢠⡀⡖⠀⢷⠇⠀⠀⢺⠀⠀⠉⠁⠀⠀⠀⠀⠀⢸⣿⡾⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⡄⠀⠀⠀⠀⠐⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠨⡇⠀⣹⢧⡀⠀⠀⡼⠳⠄⣹⠀⠀⠀⣹⠀⠀⠀⠀⣀⣠⣤⣶⣾⠿⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢷⠀⠀⠀⠀⠀⠙⠛⠛⠁⠀⠀⠀⠀⠀⠀⠀⣤⣇⠀⠃⠀⢀⣀⠀⠀⠀⠀⡏⠀⠀⠀⣼⣠⣦⣾⣾⣿⡿⠛⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⡴⠟⠀⠸⣄⠀⠀⡼⠁⢳⡀⠀⡼⠁⣀⣴⣶⣿⣿⣿⠿⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠹⡄⠀⠀⠀⠀⠀⠀⠀⠀⣤⠞⠉⠀⠀⠀⠀⠀⢘⣦⣀⡉⠀⣀⣡⣾⣷⣿⣿⡿⠿⠋⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠹⣶⣤⣀⣄⣀⣀⣀⡞⠁⠀⠀⠀⠀⢀⣴⠴⠿⠿⠿⠿⠟⠛⠛⠋⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⠻⠿⠿⠛⣻⠁⠀⠀⠀⠀⠓⠚⠒⠲⠤⢆⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣄⡀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠓⢦⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠛⠒⠒⠒⠳⠤⠴⣄⣠⣀⠀⠈⠙⢦⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⣆⠀⠀⠘⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠦⢤⠀⡼⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠺⠜⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

');
INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e3', '당기시오', '혜진과 선혁 그리고 영균은 카공을 하기 위해 ｀쓰다벅스｀ 로 향했다. 너무 졸린 탓이었는지, 영균이는 그만 ｀당기시오｀를 밀어버리는 바람에 문에 강하게 부딫히며 쓰러졌다. ', '
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⠛⠟⠟⣿⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣻⠅⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣹⡆⠀⠀⢿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡇⠀⠀⢹⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣇⠀⠀⢸⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣯⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⠀⠀⠸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⣄⣼⣿⣤⣦⣶⣿⣧⣤⣤⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣠⣦⣾⠿⠛⠛⠉⠉⣿⡅⠀⠀⣿⡆⠀⠉⠉⠛⠻⢶⣦⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣴⣾⠿⠛⠉⠁⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠉⠛⠷⣶⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣴⡿⠛⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⡧⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⢷⣦⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⣾⠟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣿⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⢷⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⡿⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢺⣿⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⣶⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⣿⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣽⣿⠀⠀⣿⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢀⣾⡟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣾⣿⠀⠀⢿⣗⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢿⣄⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢀⣾⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣽⣿⠀⠀⢽⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢻⣆⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢀⣾⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣽⣿⠀⠀⢺⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⣇⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢀⣼⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⡆⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢀⣼⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⢸⣿⠆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣿⡄⠀⠀⠀⠀
⠀⠀⠀⠀⣼⡿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⢘⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣷⠀⠀⠀⠀
⠀⠀⠀⢰⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⡆⠀⠀⠀
⠀⠀⠀⣿⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⣿⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣷⠀⠀⠀
⠀⠀⢠⣿⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⠀⠀⠀⣿⣯⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣏⠀⠀⠀
⠀⠀⢸⡿⠀⠀⠀⠀⠀⢀⠀⡀⢀⠀⡀⡀⣀⢀⡀⣀⢀⡀⡀⠠⢀⠀⡀⢀⠀⠀⠀⠀⠸⣿⠀⢀⠀⢻⣿⠀⡀⢀⠀⡀⢄⢀⠀⡠⢀⡀⣠⣠⣤⣶⣶⣶⣶⣶⣶⣦⣴⣤⣤⣄⣤⣀⣿⣀⣀⡀
⣴⣷⣶⣾⣾⣷⣿⣾⣿⣿⣿⣿⡿⣿⢿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠟⢟⢛⣛⣋⣉⣥⣤⣤⣄⣈⣀⣉⠉⢉⠉⡉⠉⠉⠋⢹⡏
⢰⣿⡇⠀⢀⠀⡀⣀⣀⣄⣠⣄⣤⣤⣤⣤⣦⣴⣤⣦⣴⣤⣦⣦⣶⣴⣶⣶⣶⣶⣶⣶⣾⡾⠿⠿⠿⠿⣿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠿⠻⠻⠛⠛⠛⠋⠉⠛⠛⠛⠛⠻⠿⠿⠿⢿⡿⠿⠾⠾⠇
⠘⣿⣷⣿⡿⣿⡿⠿⠿⠛⠛⠛⠛⠛⠙⠉⠋⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠀⠀⠀⠀⠀⢹⡇⠀⠀⠀⠐⣿⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⡇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣷⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣿⠄⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢿⣯⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⡀⠀⢀⠀⠀⠀⢀⠀⣀⣀⣐⣀⣄⣐⣤⣔⣄⣠⣸⡿⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⣹⣿⠀⠀⠀⡀⠠⢀⣀⣀⣄⣠⣠⣠⣤⣴⣤⣶⣴⣶⣶⣶⣶⣶⣶⣶⣶⣶⣿⣷⣶⣶⣶⣿⣷⣾⣶⣷⣿⣿⣿⣶⣷⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡗⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢸⣿⣧⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠇⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠘⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⡿⡿⣿⢿⢿⠿⡿⢿⠿⠿⠿⠿⠛⠛⠛⠻⠿⠿⠿⠿⠿⣿⣿⣿⣦⣦⠀⠀⠀⠀
⠀⠀⠀⠀⣦⣶⣾⣿⣿⣿⣿⣿⠿⠿⠿⠿⠿⠿⠻⠛⠟⠛⠛⠛⠛⠛⠛⠉⠉⠉⠉⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⡟⠀⠀⠀⠀
⠀⠀⠀⠀⣻⡆⠀⠈⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣿⠃⠀⠀⠀⠀
⠀⠀⠀⠀⢸⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢼⡿⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣿⡇⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠐⣿⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⠁⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠈⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⣿⡿⠁⠀⠀⠀⠀⣰⣾⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣺⡟⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢻⡧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢁⣾⣿⡿⠁⠀⠀⠀⠀⣼⣿⡿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣀⣄⣠⣠⣠⣤⣤⣴⣤⣌⠀⠀⣠⣀⡀⠄⠀⠀⠀⢀⣿⡇⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢹⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣿⣿⡿⠁⠀⠀⠀⠀⣼⣿⡿⠁⠀⠀⠀⠀⠀⠀⠀⠈⢰⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠆⢰⣿⣿⡇⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠠⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⣿⣿⡿⠁⠀⠀⠀⠀⣼⣿⣿⣿⣦⣀⠀⠀⠀⠀⠀⠀⠀⠀⢩⣿⣿⠉⠈⠀⠁⠀⠀⠀⠀⠀⠀⢸⣿⣿⠁⠀⠀⠀⠀⣼⡿⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢿⣗⠀⠀⠀⠀⠀⠀⠀⠀⣰⣿⣿⣿⣷⣦⡀⠀⠀⣼⣿⡿⠉⠻⢿⣿⣧⡄⠀⠀⠀⠀⠀⠀⣼⣿⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢸⣿⡀⠀⠀⠀⠀⠀⠀⣰⣿⣿⡿⠻⣿⣿⣿⣦⣸⣿⡿⠁⠀⠀⠀⠹⢿⡿⠀⠀⠀⠀⠀⢠⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣴⣾⣾⣶⡆⣿⠇⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠈⣿⡆⠀⠀⠀⠀⠀⣰⣿⣿⡿⠁⠀⠀⠙⠿⠟⢿⡿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⠿⠟⠋⠁⢸⣿⠁⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⣿⡧⠀⠀⠀⠀⣰⣿⣿⡿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣽⣿⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⠃⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢿⣿⠀⠀⠀⠀⠻⠟⠛⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣧⣤⣤⣦⣴⣤⣷⣾⣶⣄⠀⣸⣿⣿⠁⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢸⣿⡀⠀⠀⠀⠀⠀⢠⣦⣤⣥⣶⣴⣶⣶⣾⣶⣶⣤⣦⣴⣤⣤⣬⣄⡀⠀⠀⠀⠀⠀⠉⠛⠿⠿⠿⠿⠛⠛⠛⠛⠛⠋⢀⣾⣿⡿⠀⠀⠀⠀⠀⣽⡗⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠸⣿⡇⠀⠀⠀⠀⠀⠹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⣿⣇⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢺⣿⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠐⣿⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢼⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠈⣿⣿⠀⠀⠀⠀⠀⠀⠀⠄⠀⠀⠀⠀⠀⡀⠠⡀⡀⢄⣠⣀⣄⣠⣀⣄⣠⣠⣠⣠⣄⣤⣠⣄⣄⣄⣠⣠⣠⣤⣴⣤⣶⣾⣦⣾⣶⣤⣶⣶⣾⡗⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⣿⣶⣷⣾⣶⣷⣾⣿⣿⣷⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⠈⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠩⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡗⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⠙⠙⠋⠛⠙⠋⠋⠋⠛⠙⠋⠙⠉⠉⠉⠉⠙⠙⠛⠛⠛⠛⠛⠛⠿⠿⠿⠿⠿⠿⠿⠿⠿⢿⠿⡿⡿⠿⠿⠿⠛⠛⠛⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
');
INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e4', '엘리트 이선혁', '평소와 다르게 똑똑하고 재빨라진 선혁이가 영균이를 흠씬 두둘겨 팼다', '

⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⣴⣴⣤⣤⣤⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⠞⠛⠉⠁⠀⠀⠀⠀⠉⠉⠻⢷⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣠⣀⡀⠀⠀⠀⠀⠀⠀⡀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣴⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢿⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⡏⣠⡼⢧⠀⠀⠀⠀⠠⣞⡉⠙⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⠃⠀⠀⠀⠀⠀⠀⠀⢀⣄⠀⠀⠀⠀⠀⢠⣤⡝⢷⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣯⠉⣥⠟⠀⠀⠀⣄⡴⠫⣇⠀⢷⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⡇⠀⠀⠀⠀⠀⠀⠀⡴⠋⠹⠀⠀⠀⠀⠀⠞⠀⠁⠘⣿⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⠀⠸⣆⡀⠀⣰⠋⠀⠀⠈⢻⡀⠙⣦⣠⣤⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⣽⠀⠀⠀⠀⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⡄⠀⠀⠀⠀⢸⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡇⠀⠯⠉⠛⣯⢀⡀⣸⠃⠘⣧⠀⠈⢳⣄⠙⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⠀⠀⠀⠀⠀⠀⠀⠀⡀⣠⣠⣤⣀⠀⠀⢸⣄⣤⣤⣀⠘⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠛⠒⠲⠶⠶⠼⠯⣧⡉⣀⣴⡼⢦⡀⠀⠙⠀⣿⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⡄⠀⠀⠀⠀⠀⠐⠚⠉⠉⠉⠈⡉⠁⠀⠈⢫⡉⠀⠀⢨⡿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠹⣭⡼⠁⠈⠻⣄⠀⣴⠃⠹⣆⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢻⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⠃⠶⠓⢦⣤⠟⠀⠀⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣆⠀⠀⠀⠈⠛⠁⠀⠀⠹⣆⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠻⣦⠀⠀⠀⠀⠀⠀⣦⢴⡼⢶⠶⡶⠾⡶⠖⣆⠀⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢶⣄⠀⠀⠀⠀⠀⠀⠀⢹⡆⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢷⣄⠀⠀⠀⠀⠘⢷⡖⠺⠦⠷⠴⠗⢾⡇⣼⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⣆⠀⠀⠀⠀⠀⠀⠀⢻⡀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠻⣦⣀⠀⠀⠀⠹⣶⣒⡗⡿⢒⣿⢳⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⡄⠀⠀⠀⠀⠀⠀⠸⡇⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣻⠙⠻⣦⣄⠀⠀⠉⠛⠋⠉⢠⣿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⠃⢀⠀⠀⠀⠀⠀⠀⢿⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢀⢀⣄⣤⣤⣤⣤⣴⢤⡿⠀⠀⠀⠉⠻⠶⢦⣴⣤⡶⠟⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠞⠁⢀⡼⠁⠀⠀⠀⠀⠀⢘⡇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⣠⡴⠟⠋⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⠟⠁⠀⢠⡞⠁⠀⠀⠀⠀⠀⠀⢰⡇⠀⠀⠀⠀
⠀⠀⠀⢀⣼⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠓⠚⠛⠚⠖⠶⠶⣦⣤⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡶⠋⠀⠀⠀⢠⡟⠀⠀⠀⠀⠀⢀⣤⠞⠋⠀⠀⠀⠀⠀
⠀⠀⠀⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠻⣦⡄⠀⠀⠀⠀⣠⠞⠁⠀⠀⠀⠀⣠⠟⠀⠀⠀⠀⣀⡦⠞⠁⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢸⡅⠀⠀⠀⠀⠀⠀⠀⠀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⣆⠀⠀⠀⢣⡀⠀⠁⠀⠐⣴⠏⠀⠀⢰⣤⠞⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣇⠈⣿⠀⠀⠀⠀⠳⣄⠀⠀⠀⢿⠀⠀⠀⠸⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⡄⢸⡇⠀⠀⠀⠀⠈⠳⢄⠀⠘⢧⡀⠀⠀⢹⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢹⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⣧⠀⣷⠀⠀⠀⠀⠀⠀⠈⠳⣄⠈⢷⡀⠀⠀⢻⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢸⣏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢽⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⠀⢺⡄⠀⠀⠀⠀⠀⠀⠀⠈⢣⡈⣧⠀⠀⠀⢷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠸⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣺⠀⠸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠳⡈⢷⠀⠀⠘⣧⡀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠀⠐⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⠘⣧⠀⠀⠀⠙⣦⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡠⠏⣠⡿⠀⠀⢠⡟⠁⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠓⠒⢚⣵⠃⢀⢀⡼⠁⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

');
INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e5', '수업시작', '수업에 성실히 임하지 않은 영균이는 선생님에게 호되게 혼나고 말았다', '

⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠐⡖⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡤⣄⠼⣒⠧⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠠⣄⡀⠓⣠⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⢫⠜⣡⠞⢁⠖⡹⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠧⢀⣄⠳⡈⠂⠀⠀⠀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡤⠋⠀⠀⠐⠃⡜⢁⠞⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠰⡓⠀⡘⢆⠑⠀⢀⡤⠖⠋⠉⠉⠉⠙⢦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⠞⠀⠀⠀⠀⠀⠀⣤⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠙⠀⣏⠃⠀⢠⠏⠀⠀⠀⠀⠀⠀⠀⠀⢳⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠴⠚⠁⠀⢠⠤⠖⠒⠒⠾⠥⠤⠤⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠈⠁⠀⡞⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀⣤⠜⠊⠁⠀⠀⠀⠀⢀⠀⣀⣄⣠⠤⠴⠤⠖⠚⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠐⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠄⠀⠀⠀⠀⠀⣀⠜⠋⠀⠀⠀⠀⠀⣀⡰⠖⠉⠉⠀⠀⠀⠀⡀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠈⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠅⠀⠀⠀⠀⡴⠃⠀⠀⠀⠀⠀⣠⠖⠉⠀⠀⠀⠀⠀⣠⠖⠋⠉⠀⠈⠉⠳⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⡃⠀⠀⠀⡜⠀⠀⠀⣀⡤⠴⠋⠁⠀⠀⠀⠀⠀⠀⣰⠁⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⠀⠀⠀⠹⡄⠀⠀⠀⠀⠘⢦⣀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⡤⠀⠀⠀⠀⢹⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠹⡄⠀⠀⠀⠀⠀⠀⢀⣀⣼⠤⡤⣀⡀⠀⠙⢦⠀⠀⠀⠀⠀⠈⠉⠓⠲⢄⡀⣸⠑⡄⡀⠘⢒⣁⠀⠀⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢀⣤⠤⠦⠾⠒⠲⠦⠀⠀⠰⠋⠀⠀⠀⠀⠀⠉⠳⣄⠀⠙⢦⡀⠀⠀⠀⠀⠀⠀⠀⠱⡇⢀⡼⠁⠐⠋⠀⠀⠀⠀⠀⣸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢐⠔⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠈⢣⡀⠀⠀⠀⠀⠀⠀⣇⠘⠒⠂⠑⢆⠀⠀⠀⠀⢀⡞⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢠⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡀⠀⢹⡀⠀⠀⠀⠙⣄⠀⠀⠀⠀⠀⢱⠠⠞⠒⢦⡈⠀⠀⠀⣠⠞⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠸⡅⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡀⠀⠀⠀⣇⠀⠀⡧⠀⠀⠀⠀⠸⡄⠀⠀⠀⠀⠈⣇⠀⠀⠀⢁⣀⡴⠚⠁⠀⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡇⠰⡀⠀⠀⠀⠀⠀⠀⠀⠀⣹⠑⣆⠀⡞⠱⡄⠀⠀⠸⡄⠀⢸⠀⠀⠀⠀⠀⢷⠀⠀⠀⠀⠀⠘⠦⠴⠋⠉⠀⠀⠀⠀⠀⠈⢣⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢯⠀⢳⠀⠀⠀⠀⠀⢠⡤⡀⢸⡀⠈⢦⡇⠀⡧⢀⡀⠀⢳⠀⠸⡅⠀⠀⠀⠀⢸⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢸⡄⠈⢧⠀⠀⠀⠀⠀⢧⠘⠢⣳⡀⠈⠃⠀⠸⠃⢣⠀⠈⣇⠐⡇⠀⠀⠀⠀⠠⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢧⠀⠈⢦⠀⠀⠀⠀⠈⠳⣀⠈⠓⠀⠀⠀⠀⠀⢸⠀⠀⠸⡄⡇⠀⠀⠀⠀⢸⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠳⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠘⣆⠀⠈⢆⠀⠀⠀⠠⡤⠬⣢⡄⠀⠀⠀⠀⠀⡏⠀⠀⠀⢳⠣⠀⠀⠀⠀⣸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠹⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠘⣆⠀⠘⡆⠀⠀⠀⠓⢤⣄⡀⠀⠀⠀⣠⠜⠁⠀⠀⠀⢸⡇⠀⠀⠀⠀⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠄⠀⠀⠀⠀⠀⠀⢹⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠸⡄⠀⢹⡀⠀⠀⠀⠀⠀⠉⠉⠙⠉⠁⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⢰⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠃⠀⠀⠀⠀⠀⠀⠀⡝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⣆⠀⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡼⠁⠀⠀⠀⠀⠀⠀⠀⡼⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠓⢺⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡜⠁⠀⠀⠀⠀⠀⠀⢀⡼⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡅⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡴⢒⣋⡭⠍⠙⠒⠲⠤⢯⣀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠞⠀⠀⠀⠀⠀⠀⢀⡴⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠁⠀⠀⠀⠀⠀⠀⢄⠀⠀⠠⣜⣫⡴⢒⡚⠀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠛⠒⠒⠲⠢⠖⠋⠀⠀⠀⠀⠀⣠⠔⡻⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡏⠀⠀⠀⠀⠀⠀⠀⠈⢳⡀⠀⠀⠸⠔⢹⢡⠞⠚⠦⢄⣀⠀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⢀⡦⠚⠁⢀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡼⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣧⠀⠀⠀⠀⠈⠉⠘⡆⠀⠠⡇⠉⠉⠉⠓⠚⠤⠤⠴⠤⠴⠤⠤⠴⠊⠁⠀⠀⠀⠐⡅⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢼⠀⠀⠀⠀⠀⠀⠀⢹⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠹⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⡼⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠇⠀⠀⠀⠀⠀⠀⠀⢸⠀⡼⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⡼⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡏⠀⠀⠀⠀⠀⠀⠀⠀⡺⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⡞⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡴⠁⠹⠀⠀⠀⠀⠀⠀⠀⢰⠃⢐⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

');
INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e6', 'GC에게 수거된 영균', '집에서 키우는 반려 로봇 청소기의 배신', '

⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢠⠜⠀⠀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡀⣀⣄⠤⠦⠖⠒⠓⠋⠉⠉⠉⠉⠉⠉⠉⠙⠓⠚⠒⠦⠤⢤⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢀⡴⠃⠀⣠⠞⠁⠀⠀⠀⠀⢀⣠⠴⠚⠊⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠑⠲⠤⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢀⠎⠀⢀⡞⠁⠀⠀⠀⢀⡠⠗⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠲⢤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠜⠀⢀⡏⠀⠀⠀⢀⣠⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⠤⠦⠦⠦⠤⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠳⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠘⠀⠀⠀⣰⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡴⠋⡤⠆⡀⠀⡖⠒⠀⠈⠙⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠳⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⢾⢇⠘⠤⠭⣇⠀⠳⠤⠔⠀⠀⣸⡙⢆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢣⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢠⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣏⢸⣮⡳⢤⣀⣈⡀⣀⣀⣠⠤⢞⣽⡇⣸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢣⠀⠀⠀⠀⣇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠦⡹⢿⣷⣶⣥⣭⣬⣤⣴⣾⣿⠟⡱⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⡇⠀⠀⠀⢸⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢻⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠓⠬⣍⣛⣛⡛⣛⣛⡩⠴⠊⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣯⠀⠀⠀⠘⡄⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢹⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡴⠒⠚⠲⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡴⢤⠀⠀⠀⠀⢰⡯⠀⠀⠀⠀⠇⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢸⡘⣄⠀⠀⠀⠀⠀⠀⠀⠀⡼⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡤⠦⣄⠀⠀⢼⠔⠋⣠⠞⠀⣠⠏⡗⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠈⢶⡀⠀⠀⠀⠀⠀⠀⢳⠀⠰⠤⡦⠄⠀⠀⡀⠀⠀⠀⠀⡀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣄⠴⠛⡄⣀⡘⠦⠜⠁⢠⡴⠁⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀⠀⠙⢦⡀⠀⠀⠀⠀⠘⠒⠲⢬⠃⠀⠀⡖⠉⢳⡀⠀⠀⢧⡠⠚⠀⠀⡇⣠⠤⣤⠀⡴⠋⣲⢤⣀⠀⠀⡖⢫⠋⠀⠀⠀⣠⠔⠋⠀⠀⢰⠃⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠀⠉⠲⣄⡀⠀⠀⠀⠀⠸⠀⠀⠈⠧⠤⠞⠙⠦⠀⢸⠁⠀⠀⠀⢼⠁⣠⠇⠀⠧⡤⠃⠀⠈⠁⠀⠉⠁⢀⣀⠴⢊⣥⠀⠀⠀⠀⣸⠁⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⢂⡶⣶⣶⣷⣾⣾⣶⣿⡄⠀⠀⠀⠀⠀⠙⠲⢤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⠀⠀⠀⠙⠊⠁⠀⠀⠀⠀⠀⠀⠀⡀⣀⡴⢖⡫⠕⠚⠁⣸⠀⠀⠀⣰⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢦⡀⠀⠀⠀⠀⠀⠀⠈⠙⠚⠒⠢⠴⣄⣀⡀⢀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠀⢀⢀⣄⡠⠦⣜⣒⠭⠖⠊⠁⠀⠀⠀⠀⢸⡀⢀⡜⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢱⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠉⠉⠉⠉⠉⣉⣉⣉⣉⠭⠭⠑⠒⠉⣉⣀⣤⣴⣶⣿⡆⠀⡀⣠⠔⢋⡴⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⣿⡉⠲⢄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⡄⠀⠀⠀⠀⠀⠀⠀⢿⣿⡿⠿⢛⣋⠥⠚⢉⣠⠖⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣇⠀⠀⠉⠓⠦⢄⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀⡀⢀⠀⣀⣠⡠⠴⠔⠒⠊⢉⢀⡤⠖⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢹⠀⠀⠀⠀⠀⠀⠈⠉⠒⠦⣄⣀⡀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠉⠉⠉⠁⢀⣀⡤⠤⠦⠒⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠋⠒⠒⠒⠒⠒⠒⠒⠒⠋⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

');
INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e7', '살려줘 영균아', '현실세계에서도 혜진이 보이지가 않는다. 아무래도….', '
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢀⠶⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⢦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡞⠀⠀⣀⣀⣀⣀⣀⣀⣀⣀⣄⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣄⣀⣀⣀⣀⣀⣄⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣄⠀⠀⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡭⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢺⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⣎⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣹⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⢎⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢼⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡣⠀⠀⢀⣤⡀⠀⠀⠀⠀⣰⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⠇⠀⠀⠀⠀⠀⠀⣶⣦⣴⣶⣷⣦⣄⠀⠀⠀⠀⢺⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡕⠀⠀⢸⣿⡃⠀⠀⠀⠀⣼⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⡀⠀⠀⠀⠀⠀⠀⣹⣿⠟⠋⠉⠹⣿⣧⠀⠀⠀⣹⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⢎⠀⠀⣸⣿⠂⠀⠀⠀⠀⢺⣿⠀⠀⠀⠀⠀⠀⣀⣀⡀⠀⠀⠀⠀⠸⣿⡇⠀⠀⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⣿⣿⠀⠀⠀⢼⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡣⠀⠀⢸⣿⡅⣠⣤⣶⣶⣾⣿⡂⠀⠀⢀⣼⡾⠟⢿⣿⡇⠀⠀⠀⠈⣿⣇⠀⠀⠀⠀⠀⠀⢸⣿⠀⠀⠀⢀⣿⣿⠀⠀⠀⢺⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡕⠀⠀⢸⣿⣿⡿⠟⠋⠉⢹⣿⡇⠀⠀⣼⣿⣤⣶⣿⣿⡇⠀⠀⠀⠀⣿⣿⠀⠀⠀⠀⠀⠀⠘⣿⣧⣴⣴⣿⡿⠃⠀⠀⠀⣹⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⢎⠀⠀⠈⣿⣷⠀⠀⠀⠀⠀⣿⣷⠀⠀⣿⣿⡿⠿⠛⠉⠀⠀⠀⠀⠀⢸⣿⡆⠀⠀⠀⠀⠀⠀⢻⡟⠛⠋⠁⠀⠀⠀⠀⠀⢼⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡣⠀⠀⠀⢻⣿⡄⠀⠀⠀⠀⢹⣿⡆⠀⠹⣿⣧⣤⣤⣴⣾⡶⠀⠀⠀⠀⢿⣿⠀⠀⠀⠀⠀⠀⢹⣿⠀⠀⠀⠀⠀⠀⠀⠀⢺⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡕⠀⠀⠀⠘⣿⣷⠀⠀⠀⠀⠈⠻⠃⠀⠀⠛⠿⠿⠛⠛⠉⠀⠀⠀⠀⠀⠈⠉⠀⠀⠀⠀⠀⠀⠈⣿⡄⠀⠀⠀⠀⠀⠀⠀⣹⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⢎⠀⠀⠀⠀⠈⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠀⠀⠀⠀⠀⠀⠀⠀⢼⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⡣⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢺⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⠙⠒⠒⠒⠒⠚⠒⠒⠓⠒⠓⠒⠓⠚⠒⠚⠒⠓⠒⠒⠒⠚⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠓⠒⠒⠒⠒⠒⠒⠒⠛⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⣧⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⢤⠤⡤⣼⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣶⣶⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠻⠟⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡼⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠈⠉⠓⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⢲⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⡖⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠋⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠗⠚⠒⢻⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡗⠓⠓⠳⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡴⠁⠀⠀⠀⠘⠓⠲⠦⠤⠤⠦⠴⠴⠒⠚⠃⠀⠀⠀⠈⢦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡴⢏⢤⣄⣄⣄⣠⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣠⣀⣄⣤⡠⡤⡹⢦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢗⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⡺⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⢲⠖⢶⣒⠶⠶⡒⢖⡲⣒⣒⣒⣒⣒⢖⢶⢒⣒⡒⣲⠲⣒⠶⢖⢲⠶⢶⣒⡲⢖⣒⣒⣒⣒⣒⣒⠲⣄⠀⠀⠀⠀⢀⣠⠤⠴⣒⠲⠤⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⠃⣚⣒⣚⡚⣒⣒⡛⣴⣤⢏⣦⣤⡎⣧⣴⡜⣳⣤⣇⣷⣴⢛⣦⣾⢸⠶⣚⢓⣒⢃⠧⣤⣼⠡⣤⢦⣃⠘⡆⠀⠀⢠⢏⠀⠀⠈⠿⡇⠀⣘⣆⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⡏⢰⣧⣤⣤⡛⣓⣒⢣⣧⣔⣙⣤⣤⣻⣤⣤⣟⣤⣤⣻⢤⣜⣸⣤⣼⡮⣤⣼⣘⣒⢾⠸⡤⠼⠸⠤⢤⡎⠀⢳⠀⠀⠈⣏⢣⣀⡤⠴⠚⠊⠁⠸⡆⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢨⠃⣸⣂⣲⣂⣛⣓⡚⣘⣢⡊⣧⣤⢿⣤⣇⣧⣤⣻⣤⣼⣧⣤⣜⣧⣤⢇⣧⣤⢟⣒⣜⠈⣧⢭⠥⣭⣤⡼⠀⠸⡆⠀⠀⠘⡎⢦⡀⠀⠀⠀⠀⠀⣇⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⣺⠠⠧⠤⠴⠸⠤⡤⠇⣧⠤⠇⣇⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣄⣸⢸⣀⣼⢸⣠⡸⠈⠧⠼⠀⣧⠤⡼⠀⠀⣇⠀⠀⠀⠘⣆⠙⠦⣄⣠⠴⠚⡇⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣺⠀⠀⠀⠀⠈⠓⠢⠤⠤⠖⠊⠁⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢸⣄⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣠⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
');
INSERT INTO ending (e_id, e_name, e_desc, e_img) VALUES ('e8', '아 Tlqkf 꿈', '꿈인지 현실인지 모를 JVM에서 생존형 학습을 마친 영균이가 스스로 코딩을 하기 시작했다.', '
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣤⠀⠀⠀⠀⠀⠀⢀⡀⣀⣤⣤⠄⠀⠀⣄⣠⣴⡶⠾⠟⡁⡀⡀⢀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⠄⠀⠀⠀⣰⡖⠀⠀⠀⠀⠀⠀⢾⣇⠀⠀⠀⠼⠿⠿⠛⣿⠏⠉⠁⠀⠀⣻⣏⠭⠒⠊⠉⠉⠉⠉⠉⠉⠉⠓⠢⢤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⣠⣶⡶⠶⠟⠛⠁⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⢻⣿⠀⠀⠀⣠⠤⠒⠚⣿⣀⣀⠀⣠⠔⣿⡇⣀⣠⣤⣴⠶⠟⠁⠀⠀⠀⠀⠀⠀⠈⠙⠲⢤⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⣾⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣷⠀⠀⠀⠀⠀⠀⠘⣿⡧⠖⠋⠀⠀⠀⢈⣿⡤⠉⠉⠁⠀⣿⡟⠛⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⠦⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⣿⡗⠀⠀⠀⠀⢀⢀⡀⠀⠀⢸⣿⠀⠀⠀⠀⠀⠀⡤⢻⣿⠀⠀⠀⠀⢠⠔⣿⡇⠀⠀⠀⠀⣿⡧⠀⠀⠀⣠⣠⡤⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠳⡄⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢹⣯⣤⣴⠾⠾⠟⠋⠀⠀⠀⠸⣿⡇⠀⠀⠀⢀⡞⠁⠘⣿⡇⠀⢀⠴⠁⠀⢿⡯⠀⠀⠀⠀⢺⣿⣤⠾⠛⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⡄⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢸⣿⡁⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⣀⣤⡾⠾⠃⠀⠛⠁⠐⠁⢀⡤⠦⠚⠒⡒⠑⠒⠒⠮⢍⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣇⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠘⣿⡇⢀⣤⣴⡶⠂⠀⠀⠀⠀⠻⢿⠟⠋⠉⡉⠳⡄⠀⠀⠀⣠⠞⠁⣀⡴⠖⣩⠥⠚⠉⣀⡤⠴⠊⠑⢆⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⣿⠗⠋⠉⠀⠀⠀⠀⠀⠀⠀⢠⡏⠲⣄⠀⠙⣄⠙⢆⢀⠞⠁⣠⢞⡡⠔⠉⣀⣀⣔⣋⣁⠀⠀⠀⠀⠈⠳⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣇⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⠀⠀⠀⢱⠖⠊⠦⠬⣝⡀⡜⠁⠈⠉⠁⠈⠀⠀⠀⠀⠀⠉⠉⠉⠋⠒⠦⢜⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡆⠀⠀⣰⠃⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⢧⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⢠⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⡄⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⢺⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⡇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣇⠀⠸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡄⠀⠀⢀⡤⠖⠒⠲⣄⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⢀⣠⡤⠤⠄⠀⠀⠀⡇⠀⣤⠋⢠⢤⢄⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡆⡇⢀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⠴⠔⢓⢉⡉⡀⠀⠀⠀⠀⠀⠀⢀⣷⣾⠁⣴⣿⣵⢾⠀⣸⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⢡⣾⡿⣿⣿⣿⣦⣄⡀⠀⠀⠀⣠⣶⣾⣿⣿⣿⣿⣿⣿⣿⢿⣿⣿⣷⣆⠀⣠⣴⣿⠟⠁⣼⣷⣿⡿⡝⢠⠏⠀⠀⠀⠀⠀⠀⠀⢸⠃⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡼⠉⢹⡀⠈⢾⣿⡆⠀⠀⠉⠙⣻⣿⣦⣀⣠⣿⣿⠋⠉⠁⠁⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⠟⠁⠀⠀⠰⡽⣿⡃⡰⠋⠀⠀⠀⠀⠀⠀⠀⢀⡏⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⠃⠀⠀⡇⠀⠘⣿⣧⠀⣤⢠⠟⠀⠈⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⣿⡟⠁⠀⠀⠀⢀⡴⢃⡔⡱⠁⠀⠀⠀⠀⠀⠀⠀⠀⡞⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⠀⠀⡇⠀⠀⠙⣿⡿⠀⠀⢇⠀⣰⡃⠀⠀⠻⢿⣶⣶⣶⣶⣶⣤⣦⣴⣤⣤⣼⡿⠋⠀⠀⠀⠀⠀⠨⠎⠁⡴⠁⠀⠀⠀⠀⠀⠀⠀⠀⡼⠁⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡜⠀⠀⢰⠇⠀⠀⠀⢰⠃⠀⠀⣹⢠⠃⡇⠀⠀⠀⠀⠈⠉⠙⠙⠛⠛⠛⠛⠛⠛⠋⠁⠀⠀⠀⠀⠀⠀⡢⠒⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⡼⠁⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠃⠀⠀⣸⠀⠀⠀⡠⢾⠀⠀⠀⡺⠃⢸⠂⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡇⠀⢀⠀⠀⠀⠀⠀⠀⠀⢀⠞⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡚⠀⠀⠀⡏⠀⣀⠞⠁⡗⠀⠀⢀⣇⠀⡜⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣠⠔⣋⡠⢴⠊⠀⠀⠀⠀⠀⠀⢠⠎⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡴⠒⠦⡀⢐⠇⠀⠀⢰⠇⡴⠁⠀⡼⠁⠀⠀⣸⢈⡖⢣⣀⠀⣀⡤⠤⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⢉⡜⠁⡏⠀⠀⠀⠀⠀⠀⠀⣺⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡇⠀⠀⢳⡜⠀⠀⠀⣸⠎⠀⠀⡴⠁⠀⠀⣠⠗⠉⠀⢀⠏⠉⠉⡉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⠴⠋⠀⢘⡇⠀⣲⠀⢀⡀⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡗⠀⠀⢰⠃⠀⠀⣠⠋⠀⠀⣰⠁⠀⣠⠋⠁⠀⠀⡤⠋⠳⡀⠀⠮⡭⠤⠤⣄⣠⣠⠆⠀⠀⠀⠀⠀⢀⣠⠞⠁⠀⠀⠀⠈⡇⢸⠹⡀⢸⢱⡀⢠⡬⢇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⣏⠀⢠⠏⠀⠀⡰⠃⠀⠀⢠⠇⠀⣰⠃⠀⠀⣠⠋⠀⠀⠀⠙⣄⠀⠈⠑⠒⠊⠁⠀⠀⠀⠀⠀⢀⡴⠊⠀⠀⠀⠀⠀⠀⠀⢧⡍⠀⢧⡸⠀⢣⡝⠀⠀⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡇⢀⡞⠀⢀⠞⠁⠀⠀⢀⡞⠀⢀⠇⠀⠀⢠⠏⠀⠀⠀⠀⠀⠈⢣⡈⠙⠒⠉⠀⠀⠀⠀⣠⠔⠋⠀⢀⣀⣀⣠⠤⠴⠒⠒⠉⠇⠀⠀⠀⠀⠐⠒⠒⠶⠤⠤⣤⣀⡀⠀⢀⠀⠀⠀
⠀⠀⠀⠀⠀⡇⡼⠀⠀⠉⠀⠀⠀⢀⡞⠀⠀⣸⠀⠀⠀⢸⠀⠀⠀⠀⣠⠴⠋⠁⠙⠦⣄⣀⢀⣠⠴⠚⠑⠋⢉⣉⣉⣤⡤⠀⢀⡄⠀⢀⡀⠀⠀⠀⠀⠀⠀⡀⣀⣠⡆⠀⠾⠿⠿⠿⣿⡇⠀⠀
⠀⠀⠀⠀⠀⡇⡇⠀⠀⠀⠀⠀⠀⠞⠀⠀⠀⢼⠀⠀⠀⠸⡄⠀⣠⠎⠁⠀⠀⠀⠀⠀⠀⠉⠉⠀⠀⠀⠀⢠⣿⠛⠋⠉⠀⠀⣿⡃⢠⣿⡅⠀⠀⢠⡾⠿⣿⣿⠋⣿⠃⠀⠀⠀⠀⠀⣿⡇⠀⠀
⠀⠀⠀⠀⠀⡷⡁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡘⠀⠀⠀⠈⡇⡜⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣯⠀⠀⠀⠀⠀⣿⠆⠘⣿⡇⠀⠀⣿⡅⠀⣾⠿⢛⣿⠁⠀⠀⠀⠀⠀⣿⣧⣀⠀
⠀⠀⠀⠀⢸⠀⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢼⡷⠀⠀⠀⠀⠀⣿⣷⠶⣿⣇⠀⠀⢿⣿⡶⠋⢀⣐⡋⠀⠀⠰⠿⣿⡿⢿⣿⡁⠀
⠀⠀⠀⠀⠸⡁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⢀⣀⣀⡀⠀⣿⡆⠀⢻⡿⠀⠀⠀⠀⢠⡶⠿⠿⢿⣿⠂⣰⡄⠿⠃⢸⠟⡆⠀
⠀⠀⠀⠀⠀⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡛⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⠛⠛⠛⠋⠀⠀⣿⠇⠀⠻⠋⠀⠀⠀⠀⣿⣷⣄⣀⡼⠁⠘⣿⣇⣀⣀⣀⣀⣇⣀
⠀⠀⠀⠀⠀⠈⢧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣸⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⠛⠋⠀⠀⠀⠛⠛⠛⠛⠛⢻⠛⠁
');
