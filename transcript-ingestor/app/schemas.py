from datetime import datetime
from typing import Optional, List

from pydantic import BaseModel, HttpUrl, Field


class VideoRequest(BaseModel):
    url: HttpUrl


class VideoMetadata(BaseModel):
    video_id: str
    title: str
    channel: str
    description: str
    video_url: str
    thumbnail_url: str
    tags: List[str] = []
    duration_seconds: int
    upload_date: Optional[str] = None


class VideoTranscript(BaseModel):
    text: str
    language_code: Optional[str] = None
    is_auto_generated: Optional[bool] = None
    word_count: Optional[int] = None


class VideoResponse(BaseModel):
    metadata: VideoMetadata
    transcript: VideoTranscript
    status: str
    fetched_at: datetime = Field(default_factory=datetime.now)
