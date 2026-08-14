# Tesseract OCR 실행 환경 설정

문서 OCR은 Tess4J를 통해 운영체제에 설치된 Tesseract와 `kor`, `eng` 학습 데이터를 사용합니다.
애플리케이션은 대표적인 설치 경로를 자동 탐색하며, 경로가 다르면 아래 환경변수를 설정합니다.

| 환경변수 | 설명 | 예시 |
|---|---|---|
| `TESSERACT_DATA_PATH` | `kor.traineddata`, `eng.traineddata`가 들어 있는 `tessdata` 경로 | `C:\Program Files\Tesseract-OCR\tessdata` |
| `TESSERACT_NATIVE_LIBRARY_PATH` | Tesseract DLL/dylib/so가 들어 있는 디렉터리 | `C:\Program Files\Tesseract-OCR` |
| `TESSERACT_LANGUAGE` | OCR 언어 | `kor+eng` |

## Windows

1. UB Mannheim Windows Tesseract 설치 프로그램으로 64비트 Tesseract를 설치합니다.
2. 설치할 때 Korean language data를 선택합니다.
3. 다음 파일이 존재하는지 확인합니다.

```text
C:\Program Files\Tesseract-OCR\libtesseract-5.dll
C:\Program Files\Tesseract-OCR\tessdata\kor.traineddata
C:\Program Files\Tesseract-OCR\tessdata\eng.traineddata
```

기본 경로에 설치했다면 별도 설정 없이 자동 탐색합니다. 다른 위치에 설치했다면 PowerShell에서 설정합니다.

```powershell
$env:TESSERACT_DATA_PATH = "D:\tools\Tesseract-OCR\tessdata"
$env:TESSERACT_NATIVE_LIBRARY_PATH = "D:\tools\Tesseract-OCR"
$env:TESSERACT_LANGUAGE = "kor+eng"
.\gradlew.bat bootRun
```

IntelliJ에서는 Run/Debug Configuration의 Environment variables에 같은 값을 등록합니다.
Java와 Tesseract는 모두 같은 아키텍처(일반적으로 64비트)를 사용해야 합니다.

## macOS

MacPorts 기본 경로(`/opt/local`)와 Apple Silicon Homebrew 기본 경로(`/opt/homebrew`)는 자동 탐색합니다.

```bash
sudo port install tesseract tesseract-kor
./gradlew bootRun
```

설치 위치가 다르면 다음과 같이 지정합니다.

```bash
TESSERACT_DATA_PATH=/custom/share/tessdata \
TESSERACT_NATIVE_LIBRARY_PATH=/custom/lib \
./gradlew bootRun
```

## Ubuntu/Debian

```bash
sudo apt-get update
sudo apt-get install -y tesseract-ocr tesseract-ocr-kor tesseract-ocr-eng libtesseract-dev
./gradlew bootRun
```

## 확인

```bash
tesseract --version
tesseract --list-langs
```

`--list-langs` 출력에 `kor`와 `eng`가 모두 있어야 합니다.
