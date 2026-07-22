# 🛡️ AllergyLens

### AI Food Safety Copilot

AllergyLens is an AI-powered food safety assistant that helps users identify allergens, analyze nutritional information, and make safer food choices by simply scanning food labels using Google's Gemini AI.

---

## 🚀 Problem Statement

Millions of people with food allergies struggle to understand complex food labels. Hidden allergens and unclear ingredient lists can lead to serious health risks.

AllergyLens simplifies this process by using AI to instantly analyze food products and provide personalized safety recommendations based on the user's allergy profile.

---

## ✨ Features

### 👤 User Profile
- Create personalized allergy profile
- Store dietary preference
- Personalized AI recommendations

### 📷 AI Food Scanner
- Scan food labels using AI
- Supports multiple images of the same product
- Detects ingredients automatically
- Identifies dangerous ingredients
- Personalized allergy detection

### 📦 Batch Scan
- Scan multiple different products in a single request
- AI analyzes every product separately
- Returns safe vs unsafe summary

### 🥗 Nutrition Analysis
- Estimated calories
- Protein
- Fat
- Carbohydrates
- Sugar

### ❤️ Health Analysis
- Health Score (0–100)
- Health Grade (A+ to F)
- Health Insights
- Risk Level

### 📊 Dashboard
- Total scans
- Safe vs Unsafe products
- Safety percentage
- Average health score
- Health grade
- Risk distribution
- Most triggered allergy
- Recent safe products
- Recent unsafe products

### 📜 Scan History
- Complete scan history
- Previous recommendations
- Triggered allergies
- Ingredients

### 🤖 AI Assistant
- Ask food-related questions
- Personalized responses based on allergy profile
- Diet-aware recommendations

---

# 🏗️ System Architecture

User
        │
        ▼
 Flutter Mobile App
        │
 REST APIs
        │
        ▼
 Spring Boot Backend
        │
 ├── Gemini AI
 └── PostgreSQL (Neon)
