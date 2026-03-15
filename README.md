# NpNg (No pain No gain) - Workout Tracker

NpNg is a lightweight, bare-bones workout tracker designed for gym enthusiasts who value simplicity and efficiency. Built with modern Android development practices, it eliminates clutter to focus on what matters: tracking sets and ensuring progressive overload.

## 🚀 Key Features

- **Progressive Overload Tracking**: Instantly compare your current exercise against your previous session's performance of the same workout type.
- **Collapsible History**: Historical sets are grouped by exercise and collapsible, keeping your screen clean and focused on your current set.
- **Rapid Data Entry**: Weight and reps persist between sets, allowing for extremely fast logging.
- **Smart Session Management**:
    - **Auto-Discard**: Accidentally started a workout? Empty sessions are discarded automatically on back.
    - **Confirmation Dialogs**: Prevents data loss by asking for confirmation before discarding active sessions.
- **Full History**: Browse all past workouts with expandable details to see exactly how you've progressed over time.
- **Intuitive Dashboard**: Large touch targets for major muscle groups (Legs, Back, Chest, Arms) and a quick preview of recent history.

## 🛠 Tech Stack

- **Jetpack Compose**: Modern declarative UI.
- **Room Database**: Robust local data persistence.
- **Kotlin Coroutines & Flow**: Reactive data streams for real-time UI updates.
- **ViewModel & Repository Pattern**: Clean MVVM architecture.
- **Navigation Compose**: Smooth transitions between screens.
- **Material 3**: Modern design system and components.

## 🏗 Architecture

The app follows a standard MVVM (Model-View-ViewModel) architecture:
- **UI (Compose)**: Displays state and sends events to the ViewModel.
- **ViewModel**: Manages UI state, handles business logic, and communicates with the Repository.
- **Repository**: Abstracts the data source (Room) from the rest of the app.
- **Data (Room)**: Defines entities (`WorkoutSession`, `ExerciseEntry`) and DAOs for database interaction.

## 🛤 Future Roadmap

- [ ] **Custom Workout Types**: Define your own categories beyond the standard four.
- [ ] **Progress Analytics**: Visual charts to track volume and strength gains over time.
- [ ] **Personal Records (PRs)**: Highlight and celebrate new weight/rep milestones.
- [ ] **Exercise Auto-complete**: Faster entry with name suggestions based on history.
- [ ] **Unit Toggle**: Support for both `lbs` and `kg`.

## 📦 Getting Started

1. Clone the repository.
2. Open the project in **Android Studio (Hedgehog or newer)**.
3. Sync Gradle and run the app on an emulator or physical device (API 24+).

---
*NpNg - Simplify your gains.*
