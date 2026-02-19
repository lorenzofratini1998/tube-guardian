import logging
from functools import lru_cache

from fastapi import Depends, FastAPI, HTTPException

from app.config import settings
from app.schemas import VideoRequest, VideoResponse
from app.services import VideoService

logging.basicConfig(
    level=settings.LOG_LEVEL,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

app = FastAPI(title=settings.APP_NAME, version="1.0.0")


@lru_cache()
def get_transcript_service() -> VideoService:
    return VideoService(languages=settings.SUPPORTED_LANGUAGES)


@app.post("/api/v1/transcript", response_model=VideoResponse)
async def get_transcript(
        request: VideoRequest,
        service: VideoService = Depends(get_transcript_service)
):
    logger.info(f"API: Received request for URL: {request.url}")

    try:
        result = service.fetch_data(str(request.url))
        if not result:
            logger.warning(f"API: Video not found or data missing for {request.url}")
            raise HTTPException(status_code=404, detail="Video not found or transcript unavailable")

        return result

    except ValueError as ve:
        logger.warning(f"API: Bad Request: {ve}")
        raise HTTPException(status_code=400, detail=str(ve))
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"API: Unexpected error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Internal Server Error")
