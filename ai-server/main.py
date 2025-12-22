# # 2. 요약 기능
# @app.post("/summary")
# async def summarize_text(request: TextRequest):
#     # GPT 호출 예시
#     # response = client.chat.completions.create(...)
#     return {"summary": f"'{request.text[:10]}...' 요약 완료"}

########################################################3
# AI 서버가 파일을 받아서 텍스트로 바꿔주는 기능
import os
import shutil
from fastapi import FastAPI, UploadFile, File, HTTPException
from openai import OpenAI
from dotenv import load_dotenv
#퀴즈 생성
#Pydantic 모델 추가
from pydantic import BaseModel
import google.generativeai as genai
# 환경 변수 로드 (.env 파일 읽기)
load_dotenv()

# 1. OpenAI 클라이언트 설정
api_key = os.environ.get("OPENAI_API_KEY")
client = OpenAI(api_key=api_key)

# Gemini API 설정 추가 ▼▼▼
google_api_key = os.environ.get("GOOGLE_API_KEY")
genai.configure(api_key=google_api_key)
# 퀴즈 생성용 Gemini 모델
gemini_model = genai.GenerativeModel('gemini-1.5-flash')

app = FastAPI()

print("[AI] LOADED FILE:", __file__)


# summary.py의 라우터를 summary_router로 등록
from summary import router as summary_router
app.include_router(summary_router)


@app.get("/")
def read_root():
    return {"message": "AI Server Running"}

# --- 핵심 기능: STT ---
@app.post("/stt")
async def speech_to_text(file: UploadFile = File(...)):
    temp_filename = f"temp_{file.filename}"

    try:
        # 1. 받은 파일을 잠시 서버에 저장 (OpenAI에 보내기 위해)
        with open(temp_filename, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
        # ▼▼▼▼ [추가할 코드] 파일이 진짜 잘 도착했는지 크기 확인 ▼▼▼▼
        file_size = os.path.getsize(temp_filename)
        print(f" [디버깅] 저장된 파일 크기: {file_size} bytes")

        if file_size == 0:
           raise Exception("파일 크기가 0입니다! 전송 실패!")

        # 2. OpenAI Whisper API 호출
        with open(temp_filename, "rb") as audio_file:
            transcript = client.audio.transcriptions.create(
                model="whisper-1",
#                 file=audio_file,
                # 파일 객체 대신 (파일명, 파일객체, 미디어타입) 튜플로 명시적으로 전달
                file=(temp_filename, audio_file, "audio/mpeg"),
                language="ko" # 한국어 지정
            )

        # 3. 임시 파일 삭제 및 결과 반환
        os.remove(temp_filename)
        return {"text": transcript.text}

    except Exception as e:
        print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        print(f"에러 내용: {e}")
        print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        # 에러 발생 시 임시 파일 지우고 에러 메시지
#         if os.path.exists(temp_filename):
#             os.remove(temp_filename)
        raise HTTPException(status_code=500, detail=str(e))

#퀴즈 생성
# main.py 상단에 Pydantic 모델 추가
from pydantic import BaseModel

class QuizRequest(BaseModel):
    text: str       # 요약된 텍스트 (또는 STT 원본)
    type: str       # MULTIPLE_CHOICE(객관식), SHORT_ANSWER(주관식/단답), ESSAY(서술형)
    difficulty: str # EASY, MEDIUM, HARD

# 3. 퀴즈 생성 기능
# @app.post("/quiz")
# async def generate_quiz(request: QuizRequest):
#     # GPT에게 보낼 프롬프트(명령어) 만들기
#     prompt = f"""
#     아래 텍스트를 바탕으로 {request.difficulty} 난이도의 {request.type} 문제 3개를 만들어줘.
#     결과는 반드시 JSON 형식으로만 출력해야 해. 불필요한 말(예: "여기 있습니다")은 하지 마.
#
#     [출력 형식 예시]
#     [
#       {{
#         "question": "문제 내용",
#         "options": ["보기1", "보기2", "보기3", "보기4"], (객관식일 때만 포함, 아니면 빈 리스트 [])
#         "answer": "정답",
#         "explanation": "해설"
#       }}
#     ]
#
#     [텍스트 내용]
#     {request.text}
#     """
#
#     try:
#         response = client.chat.completions.create(
#             model="gpt-3.5-turbo", # 또는 gpt-4
#             messages=[
#                 {"role": "system", "content": "너는 선생님이야. 주어진 텍스트를 보고 학생들을 위한 퀴즈를 만들어야 해."},
#                 {"role": "user", "content": prompt}
#             ],
#             temperature=0.7 # 창의성 조절
#         )
#
#         # GPT가 준 응답(JSON 문자열) 꺼내기
#         return {"quiz": response.choices[0].message.content}
#
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=str(e))
@app.post("/quiz")
async def generate_quiz(request: QuizRequest):
    # Gemini에게 보낼 프롬프트 (내용은 동일)
    prompt = f"""
    아래 텍스트를 바탕으로 {request.difficulty} 난이도의 {request.type} 문제 3개를 만들어줘.

    [중요 조건]
    1. 결과는 반드시 JSON 형식으로만 출력해야 해.
    2. 불필요한 마크다운 기호(예: ```json)나 잡담은 포함하지 마.

    [출력 형식 예시]
    [
      {{
        "question": "문제 내용",
        "options": ["보기1", "보기2", "보기3", "보기4"],
        "answer": "정답",
        "explanation": "해설"
      }}
    ]

    [텍스트 내용]
    {request.text}
    """

    try:
        # OpenAI 코드 삭제하고 Gemini 호출로 변경
        response = gemini_model.generate_content(prompt)

        # Gemini가 가끔 ```json ... ``` 같은 마크다운을 붙여서 줄 때가 있어서 제거해줌
        cleaned_text = response.text.replace("```json", "").replace("```", "").strip()

        return {"quiz": cleaned_text}

    except Exception as e:
        print(f"퀴즈 생성 에러: {e}")
        raise HTTPException(status_code=500, detail=str(e))

