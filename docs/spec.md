## Project Spec (Updated)

### Overview
NpNg is a simple intuitive bare-bones workout tracker app designed for the gym enthusiast who wants
no clutter and a simple easy to use every-day workout tracker. It has a simple input for the type
of workout day, and inputs for the exercise, sets, reps and weight. It will show a side by side view
of the previous workout of the same type, so that the gym enthusiast can make sure to target their 
progressive overload correctly.

## General Rules
 - We will use jetpack compose for the project out of a personal preference to learn the technology.
 - We will keep our code simple and concise and only add documentation where needed, the code should
be sufficiently self-explanatory.
 - We will adhere to our principle of keeping things lightweight and simple.

## Current Implementation Status

### 1. Data Architecture (Room Database) - [DONE]
- **WorkoutSession**: Tracks session type and timestamp.
- **ExerciseEntry**: Tracks specific sets with weight, reps, and exercise name.
- **Cascading Deletes**: Discarding a session removes all associated entries.
- **Database Versioning**: Incremented database version to 2 for schema changes.
- **Previous Workout Retrieval**: Refined query to accurately show the preceding workout of the same type.

### 2. User Flow & UI Screens - [DONE]
- **Dashboard**:
    - Quick access to 4 main workout types + Recent History preview.
    - **UI/UX Improvements**:
        - **Recent History**: Fixed items to be tappable and expandable (via `WorkoutSessionCard`).
        - **Custom Workouts**: FAB implemented with a name dialog to start custom sessions.
- **Active Workout**:
    - Real-time logging of sets.
    - Persistent weight/reps inputs for fast multi-set entry.
    - Collapsible "Last Time" view grouped by exercise.
    - **Exercise Autocomplete**: Refined to only suggest exercises from the *current session type* and deduplicate casing issues (e.g., "sQuat" and "SQUAT" both suggest "Squat").
- **History**: Full scrollable history with expandable session details.
    - **Shared Components**: Refactored `WorkoutSessionCard` for consistent look.

### 3. Navigation - [DONE]
- Integrated `Navigation Compose` with a shared `ViewModel`.

### 4. Component Refactoring - [DONE]
- Created `WorkoutSessionCard` as a shared component.

### 5. Dependency Updates - [DONE]
- All project dependencies updated to latest stable.

## Future Roadmap (Planned Features)

### 1. Enhanced Customization
- **Persistent Custom Workout Types**: Currently, custom workouts are session-based. Future update will allow saving them to the dashboard.
- **Edit/Delete History**: Allow users to correct mistakes in past sessions or delete specific entries.

### 2. Analytics & Progress
- **Progress Charts**: Visual graphs showing weight/volume increase over time.
- **Personal Records (PRs)**: Highlight when a user hits a new max weight or rep count.

### 3. Settings & Utilities
- **Unit Selection**: Toggle between `lbs` and `kg`.
- **Rest Timer**: Optional simple timer between sets.

### 4. Polish
- **Dark/Light Mode**: Full Material 3 theme support.


//TODO
1. Edit/Delete History entries.
2. Persistent Custom Workout Types (save to DB).
3. Rest Timer.
4. Unit Selection (lbs/kg).
