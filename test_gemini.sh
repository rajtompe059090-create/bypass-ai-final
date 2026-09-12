#!/bin/bash
curl -s -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${GEMINI_API_KEY}" \
-H 'Content-Type: application/json' \
-d '{
  "contents": [{
    "parts":[{"text": "Hello, introduce yourself."}]
  }],
  "systemInstruction": {
    "parts": [{"text": "You are Bypass AI, an expert Android Developer Assistant."}]
  }
}'
