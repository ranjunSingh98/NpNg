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

### 2. User Flow & UI Screens - [DONE]
- **Dashboard**: Quick access to 4 main workout types + Recent History preview.
- **Active Workout**: 
    - Real-time logging of sets.
    - Persistent weight/reps inputs for fast multi-set entry.
    - Collapsible "Last Time" view grouped by exercise for progressive overload tracking.
    - Intelligent exit logic: Auto-discards empty sessions, asks for confirmation if sets exist.
- **History**: Full scrollable history with expandable session details.

### 3. Navigation - [DONE]
- Integrated `Navigation Compose` for seamless screen transitions.
- Shared `ViewModel` architecture for consistent data state across screens.

## Future Roadmap (Planned Features)

### 1. Enhanced Customization
- **Custom Workout Types**: Implement the logic for the `+` button to allow users to define their own workout categories beyond the initial four.
- **Edit/Delete History**: Allow users to correct mistakes in past sessions or delete specific entries.

### 2. Analytics & Progress
- **Progress Charts**: Visual graphs showing weight/volume increase over time for specific exercises.
- **Personal Records (PRs)**: Highlight when a user hits a new max weight or rep count.

### 3. Settings & Utilities
- **Unit Selection**: Toggle between `lbs` and `kg`.
- **Data Export/Import**: Ability to backup workout history to a CSV or JSON file.
- **Rest Timer**: Optional simple timer between sets.

### 4. Polish
- **Exercise Auto-complete**: Suggest exercise names based on history to reduce typing.
- **Dark/Light Mode**: Full Material 3 theme support.
