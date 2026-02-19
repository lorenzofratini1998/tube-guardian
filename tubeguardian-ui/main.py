import re

import streamlit as st

from src.client import TubeGuardianClient, BackendError
from src.ui_components import render_sidebar, render_suitability_result

_YOUTUBE_RE = re.compile(
    r"(https?://)?(www\.)?(youtube|youtu|youtube-nocookie)\.(com|be)/"
    r"(watch\?v=|embed/|v/|.+\?v=)?([^&=%\?]{11})"
)

st.set_page_config(page_title="TubeGuardian", layout="wide")
client = TubeGuardianClient()


def is_valid_youtube_url(url: str) -> bool:
    return bool(_YOUTUBE_RE.match(url))


def run_analysis(video_url: str, brand_id: str) -> None:
    try:
        submission = client.submit_analysis(video_url)
    except Exception as e:
        st.error(f"Submission error: {e}")
        return

    video_id = _resolve_video_id(submission)
    if not video_id:
        return

    try:
        suitability = client.get_suitability(video_id, brand_id)
        st.session_state.suitability_result = suitability
        st.session_state.suitability_brand = brand_id
    except Exception as e:
        st.error(f"Could not fetch suitability report: {e}")


def _resolve_video_id(submission: dict) -> str | None:
    if submission.get("status") == "COMPLETED":
        return submission.get("videoId")

    job_id = submission.get("jobId")
    if not job_id:
        st.error("Error: job ID missing but analysis is not complete.")
        return None

    st.info(f"New analysis started (Job: `{job_id}`). Processing...")
    progress_bar = st.progress(0)

    def on_progress(pct: int, status: str) -> None:
        progress_bar.progress(pct, text=f"Status: {status}...")

    try:
        with st.spinner("AI is analyzing video context..."):
            result = client.poll_until_complete(job_id, on_progress=on_progress)
        progress_bar.empty()
    except BackendError as e:
        progress_bar.empty()
        st.error(str(e))
        return None

    return result.get("videoId") or result.get("result", {}).get("videoId")


# --- Session state init ---
if "video_url" not in st.session_state:
    st.session_state.video_url = ""
if "suitability_result" not in st.session_state:
    st.session_state.suitability_result = None
if "suitability_brand" not in st.session_state:
    st.session_state.suitability_brand = None

# --- Page layout ---
st.title("TubeGuardian")
selected_brand_id = render_sidebar(client)

col_input, col_btn = st.columns([4, 1], vertical_alignment="bottom")
with col_input:
    def _on_url_change():
        st.session_state.video_url = st.session_state._url_input
        st.session_state.suitability_result = None

    st.text_input(
        "YouTube Video URL",
        placeholder="https://www.youtube.com/watch?v=...",
        key="_url_input",
        on_change=_on_url_change,
    )

with col_btn:
    url_is_valid = is_valid_youtube_url(st.session_state.video_url)
    analyze_btn = st.button(
        "🚀 Analyze",
        type="primary",
        use_container_width=True,
        disabled=not url_is_valid,
    )

if st.session_state.video_url:
    if url_is_valid:
        _, col_video, _ = st.columns([2, 2, 2])
        with col_video:
            st.video(st.session_state.video_url)
    else:
        st.warning("⚠️ Invalid YouTube URL")

if analyze_btn:
    run_analysis(st.session_state.video_url, selected_brand_id)

if st.session_state.suitability_result:
    render_suitability_result(
        st.session_state.suitability_result,
        st.session_state.suitability_brand,
    )