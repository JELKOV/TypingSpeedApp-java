# 타이핑 속도 테스트 앱

타이핑 속도 테스트 앱 (Typing Speed Test App)  
타이핑 속도 테스트 앱은 사용자가 타이핑 실력을 측정할 수 있는 데스크톱 애플리케이션입니다.  
GitHub API에서 문제를 가져와 사용자에게 제공하며, 정확도와 분당 타자 수(WPM)를 실시간으로 계산합니다.  

## 주요 기능

### 문제 제공:
- GitHub API를 통해 Java 코드 샘플을 동적으로 가져와 표시합니다.
- 샘플 코드는 [붕어빵 원정대 프로젝트](https://github.com/JELKOV/Deployfishshapedbread)에서 가져옵니다.
- 캐싱된 문제를 활용해 빠르게 문제를 제공할 수 있습니다.

### 실시간 성능 계산:
- 사용자가 입력한 텍스트와 문제를 비교해 정확도와 분당 타자 수(WPM)를 실시간으로 계산합니다.
- 입력 도중 틀린 부분은 하이라이트로 표시하여 피드백을 제공합니다.

### UX 개선:
- GitHub API에서 문제를 불러오는 동안 로딩 화면과 진행 바를 표시합니다.
- 완료 후 자동으로 타이핑 테스트가 시작됩니다.

### 결과 화면:
- 모든 문제가 완료되면 총 맞춘 문제 수를 요약해서 보여줍니다.

---

## 설치 및 실행

### 필수 조건
- Python 3.8 이상
- pip 패키지 매니저

### 설치 방법

#### 저장소 복제
```bash
git clone https://github.com/JELKOV/TypingSpeedApp-java.git
cd TypingSpeedApp-java
```

#### 필요한 패키지 설치
```bash
pip install -r requirements.txt
```

#### GitHub API 토큰 설정
`.env` 파일을 생성하여 GitHub API 토큰을 설정합니다.

`.env` 내용 예시:
```env
GITHUB_TOKEN=your_github_token_here
```

#### 앱 실행
```bash
python main.py
```

---

## 디렉토리 구조

```
Coding Typing Speed/
├── assets/          # 캐싱된 문제 파일이 저장되는 폴더
├── .env             # GitHub API 토큰을 저장하는 설정 파일
├── .gitignore       # git에 포함되지 않을 파일/폴더 지정
├── main.py          # 앱 메인 코드
├── README.md        # 프로젝트 설명 파일
└── requirements.txt # 패키지 종속성 목록
```

---

## 사용법
1. 앱 실행 후 첫 번째 문제가 자동으로 표시됩니다.
2. 문제 텍스트를 참고하여 입력 창에 정확히 입력합니다.
3. 입력 도중:
   - 정확도와 분당 타자 수(WPM)가 실시간으로 업데이트됩니다.
   - 틀린 부분은 하이라이트로 표시됩니다.
4. 모든 문제를 완료하면 최종 결과가 표시됩니다.

---

## 기술 스택
- **Python**: 애플리케이션 로직과 GUI 구현
- **Tkinter**: 사용자 인터페이스 구성
- **GitHub API**: 문제 데이터를 동적으로 가져오기
- **dotenv**: API 토큰 관리

---

## 주의사항

### GitHub API 토큰 보안:
- `.env` 파일에 개인 GitHub API 토큰을 저장하세요.
- 토큰을 절대 코드에 하드코딩하지 마세요.

### 캐싱 데이터 삭제:
- 앱 실행 중 생성되는 `assets/` 폴더는 필요 시 삭제해도 무방합니다.

---

## 참조 저장소
이 프로젝트는 [붕어빵 원정대 프로젝트](https://github.com/JELKOV/Deployfishshapedbread)의 코드를 GitHub API를 통해 가져와 문제로 활용하고 있습니다.  
해당 프로젝트에 대한 자세한 내용은 위 링크를 참조하세요.

---

## 제작자
- **JELKOV**
  - 프로젝트 설계 및 개발
  - GitHub: [AJH의 GitHub](https://github.com/JELKOV)

타이핑 실력을 테스트하며 즐거운 시간을 보내세요! 🎉

