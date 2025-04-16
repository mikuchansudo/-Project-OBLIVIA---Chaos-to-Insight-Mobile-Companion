import os
from flask import Flask, request, jsonify
import openai
from model import generate_tip

# Set up OpenAI API key from environment variable
openai.api_key = os.getenv("OPENAI_API_KEY")

app = Flask(__name__)

@app.route('/analyze', methods=['POST'])
def analyze_chaos():
    data = request.json
    chaos_text = data.get('text', '')
    
    if not chaos_text:
        return jsonify({"error": "No chaos text provided"}), 400
    
    tip = generate_tip(chaos_text)
    
    return jsonify({"tip": tip})

if __name__ == '__main__':
    app.run(debug=True)
