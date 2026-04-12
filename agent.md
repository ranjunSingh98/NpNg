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
- **Dashboard Layout**: Uses a weighted `LazyColumn` for categories to allow separate scrolling while keeping the "Recent History" visible at the bottom.
- **Data Portability**: JSON-based Export/Import mechanism allows users to backup and restore their entire workout history. Importing wipes the existing database to maintain referential integrity.

### 4. Smart Autocomplete
- **Contextual Suggestions**: Exercise suggestions are filtered by the current `workoutType` (e.g., Legs only shows Legs exercises).
- **Normalization**: Suggestions are normalized to Title Case (e.g., "squat" -> "Squat") to deduplicate variations in casing or accidental typos in history.

## Technical Lessons Learned

### UI & UX
- **Material 3 Surface Tinting**: Avoid hardcoding `.background(color)` inside Material 3 containers like `ElevatedCard`. M3 cards apply elevation-based tints; adding a background to a child (even if it matches the theme surface color) can cause visible color "blocks" or mismatches.
- **Corner Consistency**: Standardize on `RoundedCornerShape(16.dp)` for all interactive elements (cards, menus, dialogs) to maintain a cohesive brand feel.
- **User-Facing Strings**: Use descriptive actions like "Import Data" instead of "Import JSON" to hide technical implementation details from the user.
- **Readable Filenames**: For backups, use `SimpleDateFormat("M_d_yy")` to create intuitive filenames like `gymapp_backup_4_12_26.json`.

### Build & Lint Fixes
- **Room Ordering**: `ORDER BY id ASC` is the most reliable for chronological logging.
- **Autocomplete**: Use `ExposedDropdownMenuBox` with `ExposedDropdownMenuAnchorType.PrimaryEditable`.
- **Data Flow**: Use `LOWER()` in SQL and `.distinct()` in Kotlin to clean up user-entered strings for UI suggestions.

### Data & Serialization
- **Kotlinx Serialization**: When using `@Serializable` on Room entities, ensure the serialization plugin is added to both the project and app-level Gradle files.
- **File Handling**: Use `ActivityResultContracts.CreateDocument` and `OpenDocument` with the `application/json` MIME type for robust file access without requiring legacy storage permissions.
- **Atomic Restore**: When importing, wrap the "delete all" and "insert all" operations in a single repository call to ensure the UI doesn't see a partially empty state.

## Data Schema
- **WorkoutSession**: `id`, `type`, `timestamp`.
- **ExerciseEntry**: `id`, `sessionId` (FK), `exerciseName`, `weight`, `reps`, `setNumber`.
- **Relationship**: 1-to-Many (`WorkoutSession` -> `ExerciseEntry`) with `OnDelete = ForeignKey.CASCADE`.

## Agent Operating Procedures
- After every change, run build and fix errors.
