import streamlit as st
from src.config import DEFAULT_LOGO
from src.client import TubeGuardianClient

_RISK_COLOR: dict[str, str] = {"HIGH": "red", "MEDIUM": "orange", "LOW": "green"}


def render_sidebar(client: TubeGuardianClient) -> str | None:
    with st.sidebar:
        st.header("Brand Profile")

        try:
            brands = client.get_brands()
        except Exception as e:
            st.error(f"Cannot connect to backend: {e}")
            return None

        if not brands:
            st.warning("No brands loaded from DB.")
            return None

        if "selected_brand_id" not in st.session_state:
            st.session_state.selected_brand_id = brands[0]["id"]

        st.markdown("Select Context")
        st.markdown(_CARD_CSS, unsafe_allow_html=True)

        cols = st.columns(2)
        for i, brand in enumerate(brands):
            with cols[i % 2]:
                _render_brand_card(brand)

        selected = st.session_state.selected_brand_id
        selected_name = next(b["displayName"] for b in brands if b["id"] == selected)
        st.caption(f"Apply rules for **{selected_name}**")
        return selected


def _render_brand_card(brand: dict) -> None:
    brand_id = brand["id"]
    is_selected = st.session_state.selected_brand_id == brand_id
    logo_url = brand.get("logoUrl", DEFAULT_LOGO)

    border_color = "#4CAF50" if is_selected else "#333"
    background = "#1a2a1a" if is_selected else "#1a1a1a"

    st.markdown(
        f"""
        <div class="brand-card" style="border: 2px solid {border_color}; background: {background};">
            <img src="{logo_url}" alt="{brand['displayName']}"/>
        </div>
        """,
        unsafe_allow_html=True,
    )
    if st.button(brand["displayName"], key=f"brand_{brand_id}", use_container_width=True):
        st.session_state.selected_brand_id = brand_id
        st.rerun()


_CARD_CSS = """
<style>
.brand-card {
    border-radius: 8px;
    padding: 10px;
    margin-bottom: 4px;
    text-align: center;
    cursor: pointer;
}
.brand-card img {
    width: 100%;
    max-height: 44px;
    object-fit: contain;
    filter: brightness(0) invert(1);
}

section[data-testid="stSidebar"] div[data-testid="stButton"] button {
    background: none !important;
    border: none !important;
    box-shadow: none !important;
    padding: 2px 0 !important;
    font-size: 0.75rem !important;
    color: #aaa !important;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
}
section[data-testid="stSidebar"] div[data-testid="stButton"] button:hover {
    color: #fff !important;
    background: none !important;
}
</style>
"""


def render_suitability_result(suitability: dict, brand_name: str) -> None:
    st.divider()
    _render_header(suitability, brand_name)
    st.write("")

    tab_violations, tab_debug = st.tabs(["🚫 Violations Breakdown", "🔍 Raw JSON Data"])

    with tab_violations:
        _render_violations(suitability)

    with tab_debug:
        with st.container(height=400, border=True):
            st.json(suitability)


def _render_header(suitability: dict, brand_name: str) -> None:
    if suitability["isSuitable"]:
        st.success(f"### ✅ Suitable for {brand_name}")
        st.markdown("Content passes all safety thresholds.")
    else:
        st.error(f"### 🛑 Not Suitable for {brand_name}")
        st.markdown("Content violates specific brand safety policies.")


def _render_violations(suitability: dict) -> None:
    violations = suitability.get("violations") or []

    if not violations:
        if suitability["isSuitable"]:
            st.info("✨ No violations detected. This content is safe.")
        else:
            st.warning("Unsuitable, but no specific violations list returned.")
        return

    st.caption("Scroll down to see all violations")
    with st.container(height=400, border=True):
        for v in violations:
            _render_violation_card(v)


def _render_violation_card(v: dict) -> None:
    with st.container(border=True):
        c1, c2 = st.columns([3, 1])
        c1.markdown(f"**{v['categoryId']}**")
        color = _RISK_COLOR.get(v["riskLevel"], "orange")
        c2.markdown(f":{color}[{v['riskLevel']}]")
        st.markdown(f"**Reasoning:** _{v['reasoning']}_")
        st.caption(f"Max Tolerance: {v['maxTolerance']}")