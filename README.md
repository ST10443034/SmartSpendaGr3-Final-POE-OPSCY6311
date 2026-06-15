# SmartSpenda – Personal Budget Tracker
**Spend Smarter, Live Better**

SmartSpenda is a full-featured Android personal budget tracking application designed to help users take control of their finances, set meaningful spending goals, and build better money habits — all in a fun, engaging way.

---

## 📖 Table of Contents
- [Overview](#-overview)
- [Key Features](#-key-features)
- [Screenshots](#-screenshots)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [Installation & Setup](#-installation--setup)
- [How to Use](#-how-to-use)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [APK Download](#-apk-download)
- [Team](#-team)
- [License](#-license)
- [Acknowledgements](#-acknowledgements)

---

## 🌟 Overview
Managing personal finances is often seen as stressful or tedious. **SmartSpenda** transforms budget tracking into an engaging and rewarding experience through beautiful design, real‑time insights, and gamification elements.

The app is built for the South African market (**ZAR currency**) and works entirely offline using a local **Room database** – no internet connection or bank login required. It respects user privacy while delivering powerful budgeting tools.

This project was developed as a Portfolio of Evidence (POE) for the module **OPSC6311**. It fulfills all requirements for Part 2 (App Prototype Development) and the final POE submission.

---

## ✨ Key Features

### Core Functionality (All Implemented)
- 🔐 **User Authentication** – Register and login with secure SHA‑256 password hashing.
- 📂 **Expense Categories** – Create, edit, delete custom categories (e.g., Groceries, Transport, Entertainment).
- ➕ **Add Expense** – Specify amount, date, start time, end time, description, category, and optionally attach a receipt photo.
- 💰 **Budget Goals** – Set both a monthly maximum budget and a monthly minimum spending goal (total and per category).
- 📋 **Expense History** – View all expenses with powerful filtering: Today, This Week, This Month, or any custom date range.
- 🖼️ **Receipt Viewer** – Tap any expense to view the attached receipt photo full‑screen.
- 📊 **Category Spending Summary** – For any selected period, see total money spent per category.
- 💾 **Local Database** – All data stored offline using Room (SQLite) – no cloud, no privacy concerns.
- 📈 **Spending Graph** – Visual daily spending trends (final POE) using **MPAndroidChart**.
- 🎯 **Progress Dashboard** – Real‑time overview of budget usage, remaining amount, and smart tips.
- 🏆 **Gamification** – Earn badges (Budget Master, Consistent Logger, Smart Spender) and unlock tiers (Bronze, Silver, Gold, Platinum) based on your logging habits.
- 🌙 **Dark Mode** – Toggle between light and dark themes.

### Additional Professional Touches
- **Material Design 3** – Modern UI with rounded cards, smooth animations, and adaptive layouts.
- **Custom Adaptive Icon** – Professional vector logo that adapts to any device shape.
- **Bottom Navigation** – Quick access to Home, History, Budget, Insights, and Profile.
- **Search & Filter** – Real‑time search across expense descriptions and categories.
- **Date & Time Pickers** – Intuitive selection of date and start/end times.

---

## 📸 Screenshots
*(Visuals available in the project documentation folder)*

---

## 🛠️ Tech Stack
| Category | Technologies |
| :--- | :--- |
| **Language** | Kotlin 1.9+ |
| **UI** | XML layouts, Material Design 3, ViewBinding, ConstraintLayout |
| **Database** | Room 2.8.4 with KSP (Kotlin Symbol Processing) |
| **Architecture** | MVVM with LiveData and Coroutines |
| **Image Loading** | Glide 4.16.0 |
| **Charts** | MPAndroidChart v3.1.0 |
| **Navigation** | Android Navigation Component |
| **Min SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 34 (Android 14) |

---

## 🏗️ Architecture
SmartSpenda follows the **Model-View-ViewModel (MVVM)** pattern:
- **Model**: Room entities and DAOs (data layer).
- **View**: Activities and Fragments (UI layer).
- **ViewModel**: Manages UI data and communicates with the repository.
- **Database**: Single source of truth using Room. All operations are suspended and run on background threads using `lifecycleScope.launch`.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Ladybug | 2024.1.1 or later)
- JDK 17
- Android SDK with API level 34

### Installation & Setup
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/SmartSpenda.git
   ```
2. **Open in Android Studio**:
   - Select "Open an existing project".
   - Navigate to the cloned folder and select it.
   - Wait for Gradle sync to complete.
3. **Build and Run**:
   - Connect an Android device or start an emulator.
   - Click the **Run** button.

---

## 📱 How to Use
1. **Registration**: Create an account with your email and a secure password.
2. **Setup Goals**: Navigate to the **Budget** tab to set your monthly spending limits.
3. **Log Expenses**: Use the floating action button to record a new transaction. Attach a photo of your receipt for digital record-keeping.
4. **Monitor Progress**: Check the **Dashboard** for real-time goal tracking and "Smart Tips."
5. **View Insights**: Use the **Insights** or **Category Summary** tabs to visualize your spending habits over time.

---

## 📂 Project Structure
```text
SmartSpenda/
├── app/
│   ├── src/main/java/com/example/smartspenda/
│   │   ├── adapters/          # RecyclerView adapters
│   │   ├── data/              # Data layer (Room Database, entities, DAOs)
│   │   ├── ui/                # UI layer (Activities, Fragments, ViewModels)
│   │   │   ├── auth/          # Login & Registration
│   │   │   ├── dashboard/     # Home screen & Goal tracking
│   │   │   ├── history/       # Transaction logs
│   │   │   ├── budget/        # Budgeting tools
│   │   │   └── profile/       # User profile & Gamification
│   │   └── utils/             # Helper classes (Date, Security)
│   └── src/main/res/          # Layouts, Drawables, Values
├── .github/workflows/         # CI/CD (GitHub Actions)
└── README.md
```

---

## 👥 Team
| Name | Student ID | Role |
| :--- | :--- | :--- |
| **Mufhumudzi Rasilingwana** | St10441516 | Research & Analysis |
| **Oluga Neluvhalani** | St10443034 | Development & Git Management |
| **Luthando Princess Mndawe** | St10446457 | UI/UX Design |

---

## 📄 License
This project is developed solely for educational purposes as part of a Portfolio of Evidence (POE) for the module **OPSC6311**. All rights reserved.

---

## 🙏 Acknowledgements
- **Material Design 3** – For modern UI guidelines.
- **MPAndroidChart** – For robust data visualization.
- **Room Persistence Library** – For seamless offline data.

Made with ❤️ for the Personal Budget Tracker – OPSC6311 POE
