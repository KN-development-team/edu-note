# # ai-server/main.py
# from fastapi import FastAPI, UploadFile, File
# from pydantic import BaseModel
# import os
# from openai import OpenAI
#
# # 환경변수에서 키 가져오기 (Docker가 주입해줌)
# api_key = os.environ.get("OPENAI_API_KEY")
# client = OpenAI(api_key=api_key)
#
# app = FastAPI()
#
# class TextRequest(BaseModel):
#     text: str
#
# @app.get("/")
# def read_root():
#     return {"message": "AI Server is running!"}
#
# # 1. STT (녹음 -> 텍스트)
# @app.post("/stt")
# async def speech_to_text(file: UploadFile = File(...)):
#     # 실제 오디오 파일을 받아서 처리하는 로직
#     # (임시로 파일명만 반환)
#     return {"filename": file.filename, "text": "변환된 텍스트입니다."}
#
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
# 환경 변수 로드 (.env 파일 읽기)
load_dotenv()

# 1. OpenAI 클라이언트 설정
api_key = os.environ.get("OPENAI_API_KEY")
client = OpenAI(api_key=api_key)

app = FastAPI()

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