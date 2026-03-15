# Agent Knowledge Base - NpNg Workout Tracker

This document summarizes the architecture, key implementation details, and lessons learned during the development of the NpNg app.

## Project Overview
**NpNg** is a lightweight workout tracker focused on simplicity and progressive overload.
- **Stack**: Jetpack Compose, Room Database, ViewModel, Navigation Compose.
- **Architecture**: MVVM with a Repository pattern.

## Key Features & Logic

### 1. Progressive Overload Comparison
- **Logic**: When starting a "Legs" workout, the app queries the database for the most recent session of type "Legs" *excluding* the current one.
- **Implementation**: Uses a Room `@Transaction` query with `flatMapLatest` in the ViewModel to reactively update the "Last time" view when the session starts.

### 2. Session Management
- **Automated Start**: Sessions are created in the DB as soon as the `ActiveWorkoutScreen` is launched using `LaunchedEffect`.
- **Exit Strategy**: A `BackHandler` intercepts the system back button, and a `TopAppBar` back icon triggers a confirmation `AlertDialog`.
- **Discard Logic**: If "Discard" is chosen, the `WorkoutSession` (and its cascaded `ExerciseEntry` items) are deleted from the Room DB.

### 3. UI Patterns
- **Collapsible History**: Used `groupBy` on exercise names and `AnimatedVisibility` to create expandable cards for previous workout data.
- **Reusable Components**: `WorkoutButton` is used for large, consistent touch targets on the dashboard.

## Technical Lessons Learned

### Build & Lint Fixes
- **Navigation Parameters**: Always ensure `NavGraph` calls match the screen's constructor exactly (e.g., `onBack` vs `onWorkoutFinished`).
- **Room/Repository Sync**: Ensure any new DAO methods are also exposed through the `WorkoutRepository` before calling them in the `ViewModel`.
- **Compose UI**: `AlertDialog` buttons are styled using `ButtonDefaults.textButtonColors(contentColor = ...)` rather than a direct `color` parameter on the `TextButton`.
- **Coroutines**: Using `flatMapLatest` or other complex flow transformations often requires `@OptIn(ExperimentalCoroutinesApi::class)` in the ViewModel.

## Data Schema
- **WorkoutSession**: `id`, `type`, `timestamp`.
- **ExerciseEntry**: `id`, `sessionId` (FK), `exerciseName`, `weight`, `reps`, `setNumber`.
- **Relationship**: 1-to-Many (`WorkoutSession` -> `ExerciseEntry`) with `OnDelete = ForeignKey.CASCADE`.
