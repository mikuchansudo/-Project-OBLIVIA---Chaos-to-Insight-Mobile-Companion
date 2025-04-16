import openai

def generate_tip(chaos_text):
    # Generate a response using GPT model
    response = openai.Completion.create(
        engine="text-davinci-003",
        prompt=f"Analyze the following chaos and provide a helpful tip: {chaos_text}",
        max_tokens=150
    )
    
    # Extract the response and return the tip
    tip = response.choices[0].text.strip()
    return tip
