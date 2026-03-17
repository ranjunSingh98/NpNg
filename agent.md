# Agent Knowledge Base - NpNg Workout Tracker

This document summarizes the architecture, key implementation details, and lessons learned during the development of the NpNg app.

## Project Overview
**NpNg** is a lightweight workout tracker focused on simplicity and progressive overload.
- **Stack**: Jetpack Compose, Room Database, ViewModel, Navigation Compose.
- **Architecture**: MVVM with a Repository pattern.

## Key Features & Logic

### 1. Progressive Overload Comparison
- **Logic**: When starting a "Legs" workout, the app queries the database for the most recent session of type "Legs" *excluding* the current one.
- **Implementation**: Uses a Room `@Transaction` query with `flatMapLatest` (or manual state management in VM) to reactively update the "Last time" view.
- **Ordering**: Queries use `ORDER BY id ASC` for entries to ensure sets appear in chronological order.

### 2. Session Management
- **Automated Start**: Sessions are created in the DB as soon as the `ActiveWorkoutScreen` is launched.
- **Exit Strategy**: A `BackHandler` intercepts the system back button.
- **Discard Logic**: Empty sessions are auto-discarded. If sets exist, a confirmation `AlertDialog` allows Saving or Discarding.

### 3. UI Patterns
- **Double-Collapsible History**: In the Active Workout view, the "Last time" section is collapsible. Inside, exercises are also collapsible.
- **Persistent Inputs**: Weight and Reps fields persist after adding a set.
- **Unified Scrolling**: Active Workout screen uses `verticalScroll` on a `Column` to avoid nested scroll issues.
- **Edit Mode**: Long-press on a log entry to reveal delete buttons for all entries in the current session. Tapping anywhere outside the log entries dismisses Edit Mode.

### 4. Smart Autocomplete
- **Contextual Suggestions**: Exercise suggestions are filtered by the current `workoutType` (e.g., Legs only shows Legs exercises).
- **Normalization**: Suggestions are normalized to Title Case (e.g., "squat" -> "Squat") to deduplicate variations in casing or accidental typos in history.

## Technical Lessons Learned

### Build & Lint Fixes
- **Room Ordering**: `ORDER BY id ASC` is the most reliable for chronological logging.
- **Autocomplete**: Use `ExposedDropdownMenuBox` with `ExposedDropdownMenuAnchorType.PrimaryEditable`.
- **Data Flow**: Use `LOWER()` in SQL and `.distinct()` in Kotlin to clean up user-entered strings for UI suggestions.

## Data Schema
- **WorkoutSession**: `id`, `type`, `timestamp`.
- **ExerciseEntry**: `id`, `sessionId` (FK), `exerciseName`, `weight`, `reps`, `setNumber`.
- **Relationship**: 1-to-Many (`WorkoutSession` -> `ExerciseEntry`) with `OnDelete = ForeignKey.CASCADE`.

## Agent Operating Procedures
- After every change, run build and fix errors.
