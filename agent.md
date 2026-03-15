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
- **Ordering**: Queries use `ORDER BY id ASC` to ensure sets appear in the exact chronological order they were recorded.

### 2. Session Management
- **Automated Start**: Sessions are created in the DB as soon as the `ActiveWorkoutScreen` is launched using `LaunchedEffect`.
- **Exit Strategy**: A `BackHandler` intercepts the system back button.
- **Discard Logic**: Empty sessions are auto-discarded. If sets exist, a confirmation `AlertDialog` allows Saving or Discarding.

### 3. UI Patterns
- **Double-Collapsible History**: In the Active Workout view, the "Last time" section is collapsed by default. Expanding it reveals exercise cards, which are *also* collapsible to show/hide specific sets.
- **Persistent Inputs**: Weight and Reps fields persist after adding a set to facilitate rapid logging of multiple sets.
- **Unified Scrolling**: The Active Workout screen uses a single `verticalScroll` on a `Column` to avoid nested scroll issues with `LazyColumn`.

## Technical Lessons Learned

### Build & Lint Fixes
- **Room Ordering**: Relying on `setNumber` for ordering can be inconsistent; `ORDER BY id ASC` is more reliable for chronological set logging.
- **Compose UI**: Use `weight(1f)` with `verticalScroll` for flexible layouts that include a fixed footer button.
- **Data Flow**: `flatMapLatest` is essential for reactive queries that depend on a dynamic ID (like the current session).

## Data Schema
- **WorkoutSession**: `id`, `type`, `timestamp`.
- **ExerciseEntry**: `id`, `sessionId` (FK), `exerciseName`, `weight`, `reps`, `setNumber`.
- **Relationship**: 1-to-Many (`WorkoutSession` -> `ExerciseEntry`) with `OnDelete = ForeignKey.CASCADE`.

## Agent Operating Procedures
- After every change, run build and fix errors.
