import pytest

@pytest.fixture
def sample_url():
    return "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

@pytest.fixture
def mock_metadata_response():
    return {
        'id': 'dQw4w9WgXcQ',
        'title': 'Test Video',
        'uploader': 'Test Channel',
        'description': 'A test video description',
        'tags': ['test', 'video'],
        'webpage_url': 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
        'thumbnail': 'http://example.com/thumb.jpg',
        'duration': 120,
        'upload_date': '20230101'
    }

@pytest.fixture
def mock_transcript_item():
    return [
        {'text': 'Hello world', 'start': 0.0, 'duration': 1.0},
        {'text': 'This is a test', 'start': 1.0, 'duration': 2.0}
    ]