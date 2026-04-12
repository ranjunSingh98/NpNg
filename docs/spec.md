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
 - **Database Migrations**: We will always provide a manual or `AutoMigration` path when incrementing the database version to prevent user data loss. We will keep `exportSchema = true` enabled for version tracking.

## Style & Architectural Guidelines

### UI/UX Standards
- **Design System**: Strict adherence to **Material 3**.
- **Corner Radius**: Standardized `16.dp` rounded corners for all cards, dialogs, and menus.
- **Layout Spacing**:
    - Screen-level padding: `16.dp`.
    - Item spacing in lists: `8.dp` or `10.dp`.
- **Typography**:
    - Main titles: `headlineLarge`.
    - Card headers: `titleMedium` with `FontWeight.Bold`.
    - Metadata/Secondary info: `bodySmall` with `onSurfaceVariant` color.
- **Interactions**:
    - Use Haptic feedback for long-press actions (e.g., reordering).
    - Favor `AnimatedVisibility` for appearing/disappearing UI elements.
- **Edge-to-Edge**: Always handle system bars using `Scaffold` and `WindowInsets`.

### Architecture & Data
- **Pattern**: Clean **MVVM** with a Repository layer.
- **State**: Reactively expose data via `Flow` or `StateFlow` from Repository -> ViewModel -> UI.
- **Persistence**: Room for workout data; DataStore for simple user preferences.
- **Data Integrity**: 
    - Use `ForeignKey.CASCADE` to ensure entries are removed with their sessions.
    - All non-trivial DB/IO operations must run in `viewModelScope`.
- **Serialization**: Use `kotlinx.serialization` for JSON-based data portability.
- **App Updates**: When adding significant features, update the "What's New" dialog text and increment the app version. Clear old update notes to keep the message focused on the latest changes.

## Current Implementation Status

### 1. Data Architecture (Room Database) - [DONE]
- **WorkoutSession**: Tracks session type and timestamp.
- **ExerciseEntry**: Tracks specific sets with weight, reps, and exercise name.
- **Cascading Deletes**: Discarding a session removes all associated entries.
- **Database Versioning**: Incremented database version to 3 for schema changes.
- **Previous Workout Retrieval**: Refined query to accurately show the preceding workout of the same type.
- **Migration Path (2 to 3)**: Implemented manual migration to add `durationSeconds` to `exercise_entries`.

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
    - **Edit Mode**: Long-press any set in the current log to enter edit mode and delete accidental entries.
- **History**: Full scrollable history with expandable session details.
    - **Shared Components**: Refactored `WorkoutSessionCard` for consistent look.

### 3. Navigation - [DONE]
- Integrated `Navigation Compose` with a shared `ViewModel`.

### 4. Component Refactoring - [DONE]
- Created `WorkoutSessionCard` as a shared component.

### 5. Dependency Updates - [DONE]
- All project dependencies updated to latest stable.

### 5. Data Safety (JSON Backup) - [DONE]
- **JSON Export/Import**: Robust backup and restore mechanism.
    - **Implementation**: Uses `kotlinx.serialization` to package `WorkoutSession` and `ExerciseEntry` data.
    - **Export**: User-friendly filenames (`gymapp_backup_M_D_YY.json`) via `ActivityResultContracts.CreateDocument`.
    - **Import**: Full database restore (wipe then load) via `ActivityResultContracts.OpenDocument`.
    - **UI**: Integrated into the `TopAppBar` via a 3-dot overflow menu with `16.dp` rounded corners.

## Future Roadmap (Planned Features)

### 1. Enhanced Customization
- **Persistent Custom Workout Types**: Currently, custom workouts are session-based. Future update will allow saving them to the dashboard.
- **Edit/Delete History**: Allow users to correct mistakes in past sessions or delete specific entries in the history screen.

### 2. Analytics & Progress
- **Progress Charts**: Visual graphs showing weight/volume increase over time.
- **Personal Records (PRs)**: Highlight when a user hits a new max weight or rep count.

### 3. Settings & Utilities
- **Unit Selection**: Toggle between `lbs` and `kg`.
- **Rest Timer**: Optional simple timer between sets.

### 4. Polish
- **Dark/Light Mode**: Full Material 3 theme support.

### 5. Data Safety (JSON Backup) - [DONE]
- **JSON Export/Import**: Robust backup and restore mechanism.
    - **Implementation**: Uses `kotlinx.serialization` to package `WorkoutSession` and `ExerciseEntry` data.
    - **Export**: User-friendly filenames (`gymapp_backup_M_D_YY.json`) via `ActivityResultContracts.CreateDocument`.
    - **Import**: Full database restore (wipe then load) via `ActivityResultContracts.OpenDocument`.
    - **UI**: Integrated into the `TopAppBar` via a 3-dot overflow menu with `16.dp` rounded corners.

### 6. Data Insights (New)
- **Workout Heatmap**: A compact, GitHub-style contribution grid showing workout frequency.
    - **Visuals**: 7 rows (days) by ~5 columns (weeks) grid of small rounded blocks.
    - **Color**: Primary color for active days, subtle surface color for rest days.
    - **Layout**: High-density design that fits our Material 3 dark theme.
    - **Phased Rollout**:
        1. Navigation and Insights screen entry point.
        2. Monthly heatmap component.
        3. Statistical summaries (streaks, totals).

//TODO
1. Data Insights: Navigation & Screen setup.
2. Data Insights: Monthly Heatmap component.
3. Edit/Delete History entries.
4. Persistent Custom Workout Types (save to DB).
5. Rest Timer.
6. Unit Selection (lbs/kg).
