import requests
import time
from src.config import API_BASE_URL, POLL_INTERVAL_SECONDS, MAX_POLL_RETRIES

class BackendError(Exception):
    pass


class TubeGuardianClient:
    def __init__(self):
        self.base_url = API_BASE_URL

    def _get(self, path: str, params: dict | None = None) -> dict | list:
        resp = requests.get(f"{self.base_url}{path}", params=params, timeout=5)
        resp.raise_for_status()
        return resp.json()

    def _post(self, path: str, payload: dict) -> dict:
        resp = requests.post(f"{self.base_url}{path}", json=payload, timeout=20)
        resp.raise_for_status()
        return resp.json()

    def get_brands(self) -> list[dict]:
        return self._get("/brands")

    def submit_analysis(self, video_url: str) -> dict:
        return self._post("/analysis", {"url": video_url})

    def get_job(self, job_id: str) -> dict:
        return self._get(f"/jobs/{job_id}")

    def get_suitability(self, video_id: str, brand_id: str) -> dict:
        return self._get(
            f"/analysis/videos/{video_id}/suitability",
            params={"brand_profile_id": brand_id},
        )

    def poll_until_complete(self, job_id: str, on_progress=None) -> dict | None:
        """Polls a job until COMPLETED or FAILED. Calls on_progress(pct, status) each tick."""
        for attempt in range(MAX_POLL_RETRIES):
            try:
                data = self.get_job(job_id)
                status = data.get("status", "UNKNOWN")
                pct = min(int((attempt + 1) / MAX_POLL_RETRIES * 90), 90)

                if on_progress:
                    on_progress(pct, status)

                if status == "COMPLETED":
                    if on_progress:
                        on_progress(100, status)
                    return data

                if status == "FAILED":
                    raise BackendError("Analysis job failed on server.")

            except BackendError:
                raise
            except requests.RequestException:
                pass

            time.sleep(POLL_INTERVAL_SECONDS)

        raise BackendError("Timeout: analysis took too long.")