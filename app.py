import streamlit as st
import requests
import time
import re  # <--- NEW: Importiamo Regex

# --- CONFIGURAZIONE ---
API_BASE_URL = "http://localhost:8081/api/v1"
USE_MOCK_API = True

BRANDS = {
    "DISNEY": "Disney (Family Friendly)",
    "REDBULL": "Red Bull (High Action)",
    "HEINEKEN": "Heineken (Adult/Regulated)",
    "NYT": "The New York Times (News)",
    "DOVE": "Dove (Social Responsibility)"
}

# --- FUNZIONI UTILITY ---

# NEW: Funzione per validare se è un link YouTube valido
def is_valid_youtube_url(url):
    # Regex che accetta formati standard, short (youtu.be) ed embed
    youtube_regex = (
        r'(https?://)?(www\.)?'
        r'(youtube|youtu|youtube-nocookie)\.(com|be)/'
        r'(watch\?v=|embed/|v/|.+\?v=)?([^&=%\?]{11})'
    )
    return re.match(youtube_regex, url)

# --- FUNZIONI API (MOCK O REALI) ---
def submit_analysis(video_url):
    if USE_MOCK_API:
        time.sleep(1)
        return {"jobId": "job-123", "status": "PENDING"}
    try:
        payload = {"videoUrl": video_url}
        resp = requests.post(f"{API_BASE_URL}/analysis", json=payload)
        if resp.status_code in [200, 202]: return resp.json()
        return None
    except Exception as e:
        st.error(f"Error: {e}")
        return None

def poll_job_status(job_id):
    if USE_MOCK_API:
        time.sleep(1.5)
        return {"status": "COMPLETED", "result": {"videoId": "mock-id"}}
    for _ in range(10):
        try:
            resp = requests.get(f"{API_BASE_URL}/jobs/{job_id}")
            if resp.status_code == 200:
                data = resp.json()
                if data['status'] in ['COMPLETED', 'FAILED']: return data
            time.sleep(2)
        except: pass
    return {"status": "TIMEOUT"}

def get_suitability(video_id, brand_id):
    if USE_MOCK_API:
        # Mock responses
        if brand_id == "DISNEY":
            return {
                "brandId": "DISNEY", "isSuitable": False,
                "violations": [{"categoryId": "GARM-ARMS", "riskLevel": "MEDIUM", "maxTolerance": "LOW", "reasoning": "Gun detected"}]
            }
        return {"brandId": brand_id, "isSuitable": True, "violations": []}

    try:
        resp = requests.get(f"{API_BASE_URL}/analysis/videos/{video_id}/suitability", params={"brand_profile_id": brand_id})
        if resp.status_code == 200: return resp.json()
    except: pass
    return None

# --- UI ---

st.set_page_config(page_title="TubeGuardian", page_icon="🛡️", layout="wide")

st.title("🛡️ TubeGuardian AI")

with st.sidebar:
    st.header("⚙️ Settings")
    selected_brand_id = st.selectbox("Target Brand", options=list(BRANDS.keys()), format_func=lambda x: BRANDS[x])
    st.divider()
    st.caption("Backend: " + ("🟢 MOCK" if USE_MOCK_API else "🟠 LIVE"))

# --- NEW: INPUT SECTION MIGLIORATA ---
col1, col2 = st.columns([3, 1])

with col1:
    video_url = st.text_input("YouTube Video URL", placeholder="https://www.youtube.com/watch?v=...")

# Variabile di stato per abilitare il bottone
is_valid_url = False

# Logica di Validazione e Preview
if video_url:
    if is_valid_youtube_url(video_url):
        is_valid_url = True

        # --- MODIFICA QUI ---
        # Creiamo 3 colonne:
        # col_left (spazio vuoto), col_center (video), col_right (spazio vuoto)
        # I numeri [1, 2, 1] indicano le proporzioni: il video sarà largo metà dello spazio (2/4)
        _, col_video, _ = st.columns([1, 3, 1])

        with col_video:
            st.caption("📽️ Video Preview")
            st.video(video_url)
        # --------------------

    else:
        st.warning("⚠️ Please enter a valid YouTube URL")

with col2:
    st.write("")
    st.write("")
    # NEW: Il bottone è disabilitato se l'URL non è valido
    analyze_btn = st.button("🚀 Analyze", type="primary", disabled=not is_valid_url)


# --- MAIN LOGIC (Rimane uguale) ---
if analyze_btn and is_valid_url:
    with st.status("Running Analysis...", expanded=True) as status:
        st.write("📡 Ingesting Video...")
        submission = submit_analysis(video_url)

        if submission:
            job_id = submission.get('jobId') or submission.get('id')
            st.write(f"🧠 AI Processing (Job: `{job_id}`)...")

            job_result = poll_job_status(job_id)

            if job_result and job_result['status'] == 'COMPLETED':
                status.update(label="Done!", state="complete", expanded=False)
                video_id = job_result.get('result', {}).get('videoId')
                if USE_MOCK_API: video_id = "mock-id"

                if video_id:
                    suitability = get_suitability(video_id, selected_brand_id)
                    st.divider()

                    if suitability['isSuitable']:
                        st.success(f"## ✅ SUITABLE for {BRANDS[selected_brand_id]}")
                    else:
                        st.error(f"## 🛑 NOT SUITABLE for {BRANDS[selected_brand_id]}")

                    # Layout Colonne Risultati
                    c1, c2 = st.columns(2)
                    with c1:
                        if suitability['violations']:
                            st.subheader("Violations")
                            for v in suitability['violations']:
                                with st.expander(f"🚫 {v['categoryId']} ({v['riskLevel']})"):
                                    st.write(f"**Reasoning:** {v['reasoning']}")
                        else:
                            st.info("No violations found.")
                    with c2:
                        st.json(suitability)
            else:
                status.update(label="Failed", state="error")
                st.error("Analysis Failed.")