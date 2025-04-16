from flask import Flask, request, jsonify
import openai
import os

app = Flask(__name__)

# Set your OpenAI API key here
openai.api_key = "YOUR_OPENAI_API_KEY"

@app.route('/analyze', methods=['POST'])
def analyze_text():
    try:
        data = request.get_json()
        text = data.get('text', '')

        # Call GPT-3 model for analysis
        response = openai.Completion.create(
            engine="text-davinci-003",
            prompt=f"Provide insights and tips based on this text: {text}",
            max_tokens=100
        )

        tip = response.choices[0].text.strip()
        return jsonify({"tip": tip}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
