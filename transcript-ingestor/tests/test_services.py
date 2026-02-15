from unittest.mock import MagicMock, patch
from app.services import VideoTranscriptProvider
from youtube_transcript_api import NoTranscriptFound

class MockTranscript:
    def __init__(self, content):
        self.content = content
        self.language_code = 'en'

    def fetch(self):
        return [MagicMock(text=line) for line in self.content]

def test_transcript_provider_manual_success():
    with patch('app.services.YouTubeTranscriptApi') as MockApiClass:
        # Mock setup
        mock_instance = MockApiClass.return_value
        mock_list = MagicMock()

        expected_transcript = MockTranscript(["Hello", "World"])
        mock_list.find_manually_created_transcript.return_value = expected_transcript
        mock_instance.list.return_value = mock_list

        provider = VideoTranscriptProvider(languages=["en"])

        result = provider.fetch_transcript_data("video_123")

        assert result["text"] == "Hello World"
        assert result["is_auto_generated"] is False
        mock_instance.list.assert_called_once_with("video_123")

def test_transcript_provider_fallback_auto():
    with patch('app.services.YouTubeTranscriptApi') as MockApiClass:
        mock_instance = MockApiClass.return_value
        mock_list = MagicMock()
        mock_list.find_manually_created_transcript.side_effect = NoTranscriptFound("video_id", ["en"], "format")

        expected_transcript = MockTranscript(["Auto", "Generated"])
        mock_list.find_generated_transcript.return_value = expected_transcript

        mock_instance.list.return_value = mock_list

        provider = VideoTranscriptProvider(languages=["en"])

        result = provider.fetch_transcript_data("video_auto")

        assert result["text"] == "Auto Generated"
        assert result["is_auto_generated"] is True