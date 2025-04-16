# OBLIVIA - Chaos-to-Insight Mobile Companion

OBLIVIA is a cloud-powered Android app (APK) that learns from your digital chaos (clipboard, notifications, etc.) and gives you personalized, intelligent tips. These insights help with personal growth, mood tracking, productivity, creativity, and financial advice. 

It’s built entirely with **free tools** and hosted on **Render** and **GitHub**. 

---

## 🗂️ Project Structure

oblivia/ ├── app/ │ ├── src/ │ │ ├── main/ │ │ │ ├── java/com/oblivia/ │ │ │ │ ├── MainActivity.kt │ │ │ │ ├── ClipboardListener.kt │ │ │ │ ├── NotificationListener.kt │ │ │ │ ├── Tip.kt │ │ │ │ ├── TipDao.kt │ │ │ │ ├── TipDatabase.kt │ │ │ ├── res/ │ │ │ │ ├── layout/ │ │ │ │ │ └── activity_main.xml │ │ │ │ ├── values/ │ │ │ │ │ └── strings.xml │ ├── AndroidManifest.xml ├── backend/ │ ├── main.py │ ├── model.py │ ├── requirements.txt │ └── README.md ├── README.md

markdown
Copy
Edit

---

## 🚀 Setup and Deployment

### 1. **Setting up OpenAI API Key**

- **Sign up** or **log in** to [OpenAI](https://platform.openai.com/account/api-keys).
- Create a **new API Key** and copy it.
- **Store it securely**:
  - On **Render**: Add the `OPENAI_API_KEY` as a secret environment variable.
  - **Locally**: Save it in your `.env` file for development (not to be uploaded to GitHub).

### 2. **Backend Deployment on Render**

To deploy the backend on **Render**:

1. Go to [Render](https://render.com/) and sign up/log in.
2. Click **New Web Service** and select **Python**.
3. Connect your **GitHub repository** (where the code is stored).
4. For **Build Command**, use:
   ```bash
   pip install -r requirements.txt
