# ai-server/test_main.py

from fastapi.testclient import TestClient
from main import app
from summary import summary_service

# 가짜 클라이언트 생성
client = TestClient(app)

def test_generate_summary_api_success():
    """
    정상적인 요약 요청 테스트 (Mocking 사용)
    """
    original_method = summary_service.generate_summary # 나중에 원상복구를 위해 저장

    # 가짜 함수 정의
    def mock_generate_summary(content: str):
        return "이것은 테스트용 가짜 요약문입니다."

    # 서비스의 메서드를 가짜 함수로 교체 (Mocking)
    summary_service.generate_summary = mock_generate_summary

    try:
        # [Mockito] when: API 호출
        request_data = {
            "record_id": 100,
            "content": "테스트를 위한 긴 문장입니다..."
        }
        response = client.post("/summary", json=request_data)

        # [Mockito] then: 결과 검증
        assert response.status_code == 200 # 상태 코드가 200인가?

        result = response.json()
        assert result["message"] == "AI 요약이 완료되었습니다."
        assert result["data"]["record_id"] == 100
        assert result["data"]["summary"] == "이것은 테스트용 가짜 요약문입니다." # 가짜 응답이 잘 왔나?

        print("\n 성공: 요약 API 정상 동작 확인")

    finally:
        # 테스트 끝나면 원래 메서드로 복구 (다른 테스트에 영향 안 주게)
        summary_service.generate_summary = original_method


def test_validation_error():
    """
    유효성 검사 실패 테스트 (record_id에 문자를 넣었을 때)
    """
    # when: record_id에 숫자가 아닌 문자열을 보냄
    invalid_data = {
        "record_id": "숫자아님",
        "content": "내용"
    }
    response = client.post("/summary", json=invalid_data)

    # then: 422 Unprocessable Entity 에러가 나야 함 (Pydantic이 잡아냄)
    assert response.status_code == 422
    print("성공: 유효성 검사 에러 처리 확인")