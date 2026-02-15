import pytest
from fastapi.testclient import TestClient
from app.main import app, get_transcript_service
from app.services import VideoService
from unittest.mock import MagicMock

client = TestClient(app)

@pytest.fixture
def mock_service_dependency():
    mock_service = MagicMock(spec=VideoService)
    return mock_service

def test_get_transcript_endpoint_success(mock_service_dependency, sample_url):
    mock_response_data = {
        "metadata": {
            "video_id": "123",
            "title": "Test",
            "channel": "Channel",
            "description": "Desc",
            "tags": [],
            "video_url": sample_url,
            "thumbnail_url": "http://img.com",
            "duration_seconds": 60,
            "upload_date": "20230101"
        },
        "transcript": {
            "text": "Bla bla bla",
            "language_code": "en",
            "is_auto_generated": False,
            "word_count": 3
        },
        "status": "success"
    }

    mock_service_dependency.fetch_data.return_value = mock_response_data

    app.dependency_overrides[get_transcript_service] = lambda: mock_service_dependency

    response = client.post("/api/v1/transcript", json={"url": sample_url})

    assert response.status_code == 200
    data = response.json()
    assert data["metadata"]["video_id"] == "123"
    assert data["transcript"]["text"] == "Bla bla bla"

    app.dependency_overrides = {}

def test_get_transcript_endpoint_404(mock_service_dependency, sample_url):
    mock_service_dependency.fetch_data.return_value = None

    app.dependency_overrides[get_transcript_service] = lambda: mock_service_dependency

    response = client.post("/api/v1/transcript", json={"url": sample_url})

    assert response.status_code == 404

    app.dependency_overrides = {}

def test_get_transcript_endpoint_validation_error(mock_service_dependency):
    mock_service_dependency.fetch_data.side_effect = ValueError("Invalid URL")

    app.dependency_overrides[get_transcript_service] = lambda: mock_service_dependency

    response = client.post("/api/v1/transcript", json={"url": "https://valid.url"})

    assert response.status_code == 400
    assert "Invalid URL" in response.json()["detail"]

    app.dependency_overrides = {}