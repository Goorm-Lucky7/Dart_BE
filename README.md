# 💛 Web 프로젝트 feat.D'art

## 🗂️ 패키지 구조
```text
dart/
├─ api/
│  ├─ application/
│  │  ├─ member/
│  │  ├─ gallery/
│  │  ├─ review/
│  │  ├─ payment/
│  │  └─ chat/
│  │
│  ├─ domain/
│  │  ├─ member/
│  │  │  ├─ repo/
│  │  │  └─ entity/
│  │  ├─ gallery/
│  │  │  ├─ repo/
│  │  │  └─ entity/
│  │  ├─ review/
│  │  │  ├─ repo/
│  │  │  └─ entity/
│  │  ├─ payment/
│  │  │  ├─ repo/
│  │  │  └─ entity/
│  │  └─ chat/
│  │     ├─ repo/
│  │     └─ entity/
│  │
│  └─ dto/
│     ├─ member/
│     ├─ gallery/
│     ├─ review/
│     ├─ payment/
│     └─ chat/
│
├─ infrastructure/
│  ├─ redis/
│  ├─ s3/
│  └─ payment/
│
├─ presentation/
│  ├─ MemberController/
│  └─ ChatController/
│
├─ global/
│  ├─ auth/
│  ├─ common/
│  ├─ config/
│  └─ error/-> dartException
│
└─ admin/ (optional)
```

<br/><br/>

## ✔️ 코드 컨벤션
팀 내에서 지켜야 할 코드 컨벤션을 명시합니다. 이 컨벤션들은 코드의 가독성을 높이고, 효율적인 협업 및 유지 보수를 도모하기 위해 정립되었습니다.

### 일반 규칙
- **인텔리제이 네이버 코드 컨벤션 사용** : 가독성 향상과 오류 발생 위험을 줄이기 위해 사용합니다.
- **코드 길이** : 한 줄의 코드 길이는 최대 120자를 넘지 않도록 합니다.
- **클래스 구조** : 클래스는 상수, 멤버 변수, 생성자, 메서드 순으로 작성합니다.

### 네이밍 규칙
- **메서드 이름** : 메서드는 동사+명사의 형태로 명확하게 작성합니다. 예) `saveOrder`, `deleteUser`
- **불린 반환 메서드** : 반환 값이 불린 타입인 경우 메서드 이름은 'is'로 시작합니다. 예) `isAdmin`, `isAvailable`
- **검증 메서드** : 검증에 관한 메서드는 `validate`로 시작합니다. 예) `validateInput`, `validateUser`

### 아키텍처 및 설정
- **계층형 아키텍처** : 프로젝트는 계층형 아키텍처 구조를 따릅니다.
- **BaseTimeEntity** : 날짜 정보가 자동으로 등록되도록 `BaseTimeEntity`를 적용합니다.
- **YML 파일 분리** : 개발 환경에 맞게 `local`, `develop`, `main` 등으로 yml 설정 파일을 분리합니다.

### 특별한 규칙
- **정적 팩토리 메서드 사용** : 객체 생성 시 정적 팩토리 메서드를 사용하여 가독성과 유저 친화성을 높입니다.
- **빌더 패턴 사용** : 생성자의 매개변수가 4개 이상일 경우 빌더 패턴을 사용해 가독성을 높입니다.
- **레코드 활용** : DTO 등 간단한 목적의 클래스에는 Java의 record를 활용하여 코드를 간소화합니다.

<br/><br/>

## 💬 커밋 메시지 규칙
- **`refactor`**: 코드 리팩터링 시 사용합니다.
- **`feat`**: 새로운 기능 추가 시 사용합니다.
- **`fix`**: 버그 수정 시 사용합니다.
- **`chore`**: 빌드 업무 수정, 패키지 매니저 수정 시 사용합니다.
- **`style`**: 코드 포맷 변경, 세미콜론 누락, 코드 수정이 없는 경우 사용합니다.
- **`docs`**: 문서 수정 시 사용합니다.
- **`test`**: 테스트 관련 코드 시 사용합니다.
- **`Move`**: 코드 또는 파일의 이동이 있을 경우 사용합니다.
- **`Rename`**: 파일명(or 폴더명)을 수정한 경우 사용합니다.
- **`Remove`**: 코드(파일)의 삭제가 있을 경우 사용합니다.
- **`Comment`**: 주석 추가 및 변경이 있을 경우 사용합니다.
- **`Add`**: 코드나 테스트, 예제, 문서 등의 추가 생성이 있을 경우 사용합니다.
