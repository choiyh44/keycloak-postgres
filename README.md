# keycloak-postgres

keycloak user storage spi 샘플입니다.

postgresql DB 테이블에 있는 사용자 데이터 대상으로 로그인 처리되도록 구현했습니다.

## keycloak 버전

keycloak-21.0.1 버전 사용하여 개발되었습니다.

## jar 파일 생성 및 설치

maven 빌드를 실행해서 jar 파일을 생성합니다.
```
mvn clean package
```
jar 파일을 설치된 keycloak의 keycloak/provider 디렉토리에 복사합니다.

## postgresql table/data 생성

```
CREATE TABLE public.users (
	id serial4 NOT NULL,
	username varchar(32) NULL,
	"password" varchar(100) NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);

insert into users(username, password) values('testuser', 'admin')
```

## keycloak 설정

1. realm을 생성합니다.
2. realm에 client를 생성합니다.
3. realm의 User federations 메뉴에서 MyUserStorageProvider를 활성화합니다.
4. MyUserStorageProvider의 설정에서 DB 접속 정보를 등록합니다.

## 테스트

1. keycloak-sample-client의 application.yml에서 keycloak 설정을 적절히(?) 수정합니다.
2. keycloak-sample-client 를 실행하여 http://localhost/main 에 접속합니다. 화면이 keycloak 로그인 페이지로 이동됩니다.
3. 로그인을 수행합니다.
