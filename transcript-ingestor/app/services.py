import logging
from typing import Optional, Dict, Any, List

from youtube_transcript_api import YouTubeTranscriptApi, TranscriptsDisabled, NoTranscriptFound
from yt_dlp import YoutubeDL

logger = logging.getLogger(__name__)


class VideoMetadataProvider:
    """
    Responsible for extracting video metadata.
    """

    def __init__(self):
        self.ydl_opts = {
            'quiet': True,
            'skip_download': True,
            'extract_flat': False,
            'no_warnings': True,
        }

    def fetch_metadata(self, url: str) -> Dict[str, Any]:
        try:
            with YoutubeDL(self.ydl_opts) as ydl:
                return ydl.extract_info(url, download=False)
        except Exception as e:
            logger.error(f"MetadataProvider: Failed for {url}: {str(e)}")
            raise ValueError(f"Metadata extraction failed: {str(e)}")


class VideoTranscriptProvider:
    """
    Responsible for extracting transcript text.
    """

    def __init__(self, languages: List[str]):
        self.languages = languages
        self.api = YouTubeTranscriptApi()

    def fetch_transcript_data(self, video_id: str) -> Dict[str, Any]:
        base_response = {
            "text": "",
            "language_code": None,
            "is_auto_generated": False
        }

        try:
            transcript_list = self.api.list(video_id)

            # Try manually created transcript first, fallback to auto-generated
            try:
                transcript = transcript_list.find_manually_created_transcript(self.languages)
                is_auto_generated = False
            except NoTranscriptFound:
                transcript = transcript_list.find_generated_transcript(self.languages)
                is_auto_generated = True

            transcript_text = " ".join([snippet.text for snippet in transcript.fetch()])

            return {
                "text": transcript_text,
                "language_code": transcript.language_code,
                "is_auto_generated": is_auto_generated
            }

        except (TranscriptsDisabled, NoTranscriptFound) as e:
            logger.warning(f"Transcript unavailable for {video_id}: {e}")
            return base_response
        except Exception as e:
            logger.error(f"Error fetching transcript for {video_id}: {e}")
            return base_response


class VideoService:
    """
    Orchestrator Service.
    """

    def __init__(self, languages: List[str]):
        self.metadata_provider = VideoMetadataProvider()
        self.transcript_provider = VideoTranscriptProvider(languages)

    def fetch_data(self, url: str) -> Optional[Dict[str, Any]]:
        logger.info(f"Service: Processing URL {url}")

        try:
            metadata = self.metadata_provider.fetch_metadata(url)
            video_id = metadata.get('id')

            if not video_id:
                raise ValueError("Could not determine Video ID from URL")

            transcript_data = self.transcript_provider.fetch_transcript_data(video_id)

            return self._format_response(metadata, transcript_data, url)

        except ValueError as e:
            logger.error(f"Service: Validation error: {str(e)}")
            raise e
        except Exception as e:
            logger.error(f"Service: Unexpected error: {str(e)}")
            raise e

    @staticmethod
    def _format_response(info: Dict[str, Any], transcript_data: Dict[str, Any], original_url: str) -> Dict[str, Any]:
        metadata = {
            "video_id": info.get('id'),
            "title": info.get('title', 'Unknown'),
            "channel": info.get('uploader', 'Unknown'),
            "description": info.get('description', ''),
            "tags": info.get('tags', []),
            "video_url": info.get('webpage_url', original_url),
            "thumbnail_url": info.get('thumbnail', ''),
            "duration_seconds": info.get('duration', 0),
            "upload_date": info.get('upload_date'),
        }

        transcript_text = transcript_data.get("text", "")
        transcript = {
            "text": transcript_text,
            "language_code": transcript_data.get("language_code"),
            "is_auto_generated": transcript_data.get("is_auto_generated", False),
            "word_count": len(transcript_text.split()) if transcript_text else 0
        }

        return {
            "metadata": metadata,
            "transcript": transcript,
            "status": "success" if transcript_text else "no_transcript"
        }
