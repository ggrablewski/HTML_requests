// Simple JS script sending a POST request

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

// Get __dirname equivalent in ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Load credentials
const credentialsPath = path.join(__dirname, '.credentials');
const credentialsContent = fs.readFileSync(credentialsPath, 'utf8');
const credentials = JSON.parse(credentialsContent);

const url = "https://api.generative.engine.capgemini.com/v2/llm/invoke";

async function callApi() {
    const payload = {
        action: "run",
        modelInterface: "langchain",
        data: {
            mode: "chain",
            text: "What are Banach spaces in practice used for?",
            files: [],
            modelName: "us.anthropic.claude-sonnet-4-5-20250929-v1:0",
            provider: "bedrock",
            systemPrompt: "You are a mathematician, a scientist.",
            modelKwargs: {
                maxTokens: 4096,
                temperature: 0.4,
                streaming: true,
                topP: 0.15
            }
        }
    };

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "accept": "application/json",
                "Content-Type": "application/json",
                "x-api-key": credentials.genai.token
            },
            body: JSON.stringify(payload)
        });

        const result = await response.json();
        console.log("API response:");
        console.log(result.content);

    } catch (err) {
        console.error("Error:", err);
    }
}

callApi();

// models:
// mistral.mistral-7b-instruct-v0:2
// us.anthropic.claude-sonnet-4-5-20250929-v1:0
// NO // anthropic.claude-sonnet-4-6
// gemini-2.5-flash
// NO // gemini-2.5-pro
// openai.gpt-5.2
// openai.gpt-5.2-chat
// openai.o3
// NO // grok-4
// NO // amazon.nova-pro-v1:0
// amazon.nova-2-lite-v1:0

