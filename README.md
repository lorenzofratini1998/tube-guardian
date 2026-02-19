# TubeGuardian: Semantic Brand Suitability Engine
<p align="left"> 
<img src="https://img.shields.io/badge/Java_25-ED8B00?logo=openjdk&logoColor=white" /> 
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=Spring&logoColor=white" /> 
<img src="https://img.shields.io/badge/Python-3776AB?logo=python&logoColor=white" /> 
<img src="https://img.shields.io/badge/FastAPI-009688?logo=fastapi&logoColor=white" /> 

<img src="https://img.shields.io/badge/Google_Gemini-8E75B2?logo=google&logoColor=white" /> 
<img src="https://img.shields.io/badge/Qdrant-FD5273?logo=qdrant&logoColor=white" /> 

<img src="https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white" /> 
<img src="https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white" /> 
<img src="https://img.shields.io/badge/Flyway-CC0200?logo=flyway&logoColor=white" /> 

<img src="https://img.shields.io/badge/Streamlit-FF4B4B?logo=streamlit&logoColor=white" /> 

<img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white" /> 
<img src="https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=white" /> 
</p>


TubeGuardian is a scalable, automated content classification system designed to evaluate YouTube video inventory based on the **GARM (Global Alliance for Responsible Media)** framework.

Unlike traditional binary "Safe/Unsafe" filters, TubeGuardian leverages a Retrieval-Augmented Generation (RAG) architecture to generate an objective, brand-agnostic **Risk Profile**. This decoupled approach allows different advertisers (e.g., Disney vs. a Gaming Publisher) to apply granular Risk Tolerance rules to the exact same dataset. This architectural choice maximizes asset reuse, minimizes redundant LLM inference costs, and ensures deterministic filtering post-analysis.

## 🏗️ High-Level Architecture Flow
The system adopts a **Polyglot Microservices Architecture**, combining the robust enterprise patterns of Java/Spring Boot with the data-processing agility of Python.

<img alt="TubeGuardian Architecture" src="https://github.com/user-attachments/assets/f763b89d-dac8-4763-bcff-7160b25758bc"/>

1. **Smart Ingestion (Sync):** A Streamlit Web UI accepts YouTube URLs. The **Video Orchestrator** normalizes the URL and performs a cache lookup in PostgreSQL. On a cache hit, the Risk Profile is returned immediately ($0 cost).
2. **Polyglot Extraction:** On a cache miss, a dedicated FastAPI Python microservice asynchronously scrapes transcripts and metadata, bypassing strict API quotas.
3. **Event-Driven Dispatch (Async):** The Orchestrator pushes the extraction payload to a RabbitMQ queue (`video.analysis.jobs`), decoupling the fast ingestion phase from the high-latency AI processing phase.
4. **Semantic RAG & AI Analysis:** The **Policy Engine** (Java) consumes the message. It generates embeddings for the transcript and queries a **Qdrant Vector Database** to retrieve only the contextually relevant GARM policies. The prompt is then evaluated by Google Gemini (via Spring AI).
5. **Decoupled Persistence:** The Policy Engine persists the structured JSON Risk Profile directly to PostgreSQL and publishes a completion event to `video.analysis.results`.
6. **Client Polling & Filtering:** The UI polls the Orchestrator, which applies a deterministic Rules Engine to the final Risk Profile, yielding a strict "Approved/Rejected" status based on the specific brand's tolerances.

## 🎥 See it in Action (Demo)

Watch how TubeGuardian evaluates the exact same video payload against two radically different Brand Profiles (i.e., Disney vs. Red Bull), yielding deterministic results. The demo also highlights the immediate PostgreSQL cache hit when re-evaluating known content.

<video src="https://github.com/user-attachments/assets/a9d1dc02-1094-42f9-ae8c-bacef19b893f" controls="controls" muted="muted" style="max-height:640px; width:100%;"></video>

## 💻 Tech Stack

| Component | Technology | Justification |
| :--- | :--- | :--- |
| **Orchestrator & Policy Engine** | Java 25, Spring Boot 4.x | Type-safe, enterprise-grade scalability. |
| **AI Integration** | Spring AI, Google Gemini | Modern abstractions for LLMs; large context window handling. |
| **Data Ingestion** | Python, FastAPI | Optimal ecosystem for web scraping and media processing. |
| **Async Messaging** | RabbitMQ | Decouples services, handles backpressure, implements Dead Letter Queues (DLQ). |
| **Vector Database** | Qdrant | Fast, Rust-based vector search for the RAG implementation. |
| **Relational Database** | PostgreSQL, Flyway | OLTP storage using strict schemas and `JSONB` for flexible reporting. |
| **CI/CD & DevOps** | GitHub Actions, Docker | Matrix-strategy build pipelines, automated multi-container deployment. |
| **Frontend** | Python, Streamlit | Rapid, data-centric UI prototyping with state management. |

## 🧠 Key Design Decisions & Trade-offs

* **Decoupled Filtering (Compute Once, Filter Many):** LLM analysis is expensive and slow; CPU logic is cheap and fast. By storing a generic, exhaustive risk assessment, the system allows *N* different clients to filter the same video simultaneously with zero additional marginal cost.
* **Smart Policy Startup:** GARM Policies are loaded into Qdrant at application startup. The system computes a hash of the policy file; if the hash differs from the stored version, it seamlessly rebuilds the vector index, ensuring RAG consistency without manual intervention.
* **Sync Ingestion vs. Async Analysis:** Video metadata fetching is kept synchronous (<5s) to provide immediate feedback to the UI, while the unpredictable latency of LLM inference (30s - 2min) is safely handled by RabbitMQ workers.
* **Least Privilege Database Access:** Leveraging Flyway migrations, database access is strictly isolated. Microservices authenticate using dedicated, limited-privilege roles rather than a global admin user.

## 🚀 Getting Started

### Prerequisites
* **Docker** and **Docker Compose** installed.
* A valid Google **Gemini API Key** (from Google AI Studio).

### Local Setup
1. **Clone the repository:**
   ```bash
   git clone https://github.com/lorenzofratini1998/tubeguardian.git
   cd tubeguardian
   ```
2. **Configure the Environment Variables**<br>
   Copy the provided template and insert your API key.
    ```bash
   cp .env.example .env
    # Edit .env and add your GOOGLE_GENAI_API_KEY
   ```

3. **Run the Infrastructure (Development Mode)**: <br>
   If you want to run only the databases/brokers and boot the microservices from your IDE (e.g., IntelliJ/PyCharm):
    ```bash
   docker compose up -d
   ```
   
4. **Run the Full Stack (Production Mode)**: <br>
   To boot the entire architecture, including the pre-built application containers from Docker Hub:
    ```bash
   docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d
   ```
   
5. **Access the Application:** <br>
   * Web UI:[http://localhost:8501](http://localhost:8501)
   * RabbitMQ Management:[http://localhost:15672](http://localhost:15672)

## CI/CD Pipeline
The project uses advanced GitHub Actions workflows:
* **Continuous Integration**: Automated Maven testing (tg-common, tg-video-orchestrator, tg-policy-engine) on every Pull Request.
* **Continuous Deployment**: On merge to master, a Matrix Strategy pipeline dynamically reads the pom.xml version, builds the multi-module Docker images, and pushes them to Docker Hub with automated version tagging.

*Architected and developed by [@Lorenzo Fratini](https://www.github.com/lorenzofratini1998)*