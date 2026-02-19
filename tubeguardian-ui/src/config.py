import os
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/api/v1")

DEFAULT_LOGO = "https://placehold.co/200x100?text=Brand+Logo"

POLL_INTERVAL_SECONDS = 5
MAX_POLL_RETRIES = 20