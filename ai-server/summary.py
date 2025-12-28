# // request body
#
# {
#   "content": "어제 회의에서는 신규 기능 출시 일정을 논의했습니다..."
# }


# // response body
# {
#   "message": "AI 요약이 완료되었습니다.",
#   "data": {
#     "record_id": 5,
#     "summary": "요약 내용..."
#   },
#   "statusCode": 200
# }

import os
from dotenv import load_dotenv
import google.generativeai as genai
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

load_dotenv()

# router
router = APIRouter()

# DTO
class SummaryRequest(BaseModel): # BaseModel을 상속받아 DTO 기능 수행
    record_id: int
    content: str

# Service 로직
class SummaryService:
    # 생성자
    def __init__(self):
        self.api_key = os.getenv("GOOGLE_API_KEY")
        genai.configure(api_key = self.api_key) # import한 genai 모듈에 api_key 저장
        self.model = genai.GenerativeModel("gemini-2.5-flash")

    def generate_summary(self, content: str) -> str:
        try:
            prompt = f"""
            너는 교육 자료를 요약해주는 유능한 조교야.
            다음 내용을 읽고 반드시 아래 형식을 지켜서 요약해줘:

            1. 명확하고 간결한 문장으로 작성할 것.
            2. 반드시 개조식(bullet points)을 사용하여 3줄로 요약할 것.
            3. 각 줄은 '- '로 시작할 것.
            4. 각 '-' 가 시작될 때마다 한줄씩 띄워서 가독성을 높일것.

            내용:
            {content}
            """

            # Gemini 호출
            response = self.model.generate_content(
                prompt,
                generation_config={"temperature": 0.0} # AI가 창의성을 배제하고 일관된 답변 생성
            )

            return response.text
        except Exception as e:
            print(f"SummaryService Error: {e}")
            raise e

summary_service = SummaryService() # 함수 변수에 저장

# controller 매핑
@router.post("/summary")
def generate_summary_api(request: SummaryRequest):
    try:
        # service 호출
        summary_text = summary_service.generate_summary(request.content)
        return {
            "message": "AI 요약이 완료되었습니다.",
            "data": {
                "record_id": request.record_id,
                "summary": summary_text
            },
            "statusCode": 200
        }

    # Service에서 넘어온 에러 처리
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI 서버 오류: {str(e)}")