# ai-server/test_real.py

import os
import pytest
from fastapi.testclient import TestClient
from dotenv import load_dotenv

# 환경변수 로드 (API KEY 가져오기 위함)
load_dotenv()

from main import app

# 클라이언트 생성
client = TestClient(app)

# [안전장치] 만약 API 키가 없으면 이 테스트를 건너뛰어라 (스킵)
# CI/CD 서버나 키가 없는 환경에서 에러가 나는 것을 방지합니다.
@pytest.mark.skipif(os.getenv("GOOGLE_API_KEY") is None, reason="API Key가 없어서 실제 연동 테스트를 건너뜁니다.")
def test_real_gemini_api_call():
    """
    [통합 테스트] 실제 Google Gemini API를 호출하여 요약이 잘 되는지 검증
    """
    print("\n 실제 Google 서버로 요청을 보냅니다... (시간이 조금 걸립니다)")

    # 1. 테스트할 실제 데이터 (뉴스 기사 일부 등)
    real_content = """
    Python은 1991년 귀도 반 로섬이 발표한 프로그래밍 언어다.
    플랫폼에 독립적이며 인터프리터 방식이다.
    객체 지향적이며 동적 타이핑(dynamically typed) 대화형 언어이다.
    문법이 쉽고 간결하여 배우기 쉬운 언어로 꼽힌다.
    """

    request_data = {
        "record_id": 999,
        "content": real_content
    }

    # 2. API 호출 (진짜 서버로 나감)
    response = client.post("/summary", json=request_data)

    # 3. 검증 (Assertion)
    assert response.status_code == 200

    result = response.json()

    # 데이터 구조 확인
    assert result["message"] == "AI 요약이 완료되었습니다."
    assert result["data"]["record_id"] == 999

    # [중요] 실제 요약문 검증
    # AI의 답변은 매번 바뀌므로 "정확히 문장이 일치하는지" 검사할 수 없습니다.
    # 대신 니"빈 문자열이 아닌지", "문자열 타입인지" 등을 확인합다.
    summary = result["data"]["summary"]

    print(f"\n AI가 요약한 내용: {summary}")

    assert isinstance(summary, str) # 결과가 문자열인가?
    assert len(summary) > 10        # 내용이 10글자 이상인가? (제대로 된 문장인가)

    print(" 성공: 실제 Gemini API 연동 확인 완료")