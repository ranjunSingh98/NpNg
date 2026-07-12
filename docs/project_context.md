# Project Context: NpNg / GymApp

Last updated: 2026-05-27

## What this project is

This is an Android workout tracker app called `NpNg` ("No pain No gain"). It is a local-first Jetpack Compose app focused on quick set logging, progressive overload, and minimal UI clutter.

Core user flow:

1. Start from the dashboard by selecting a workout category or creating a custom workout.
2. Enter sets during an active workout session.
3. Compare against the previous workout of the same type in the "Last time" section.
4. Review past sessions in History.
5. View a monthly workout heatmap and summary stats in Insights.
6. Export/import all workout data as JSON backups.

## Stack and architecture

- UI: Jetpack Compose + Material 3
- Navigation: Navigation Compose
- State: Single shared `WorkoutViewModel`
- Persistence:
  - Room for workout sessions and exercise entries
  - DataStore for lightweight user preferences
- Serialization: `kotlinx.serialization`
- Build: AGP 9.1.0, Kotlin 2.3.10, compile/target SDK 36, min SDK 24

The app is organized as MVVM:

- `ui/*`: screens, shared composables, theme
- `ui/viewmodel/WorkoutViewModel.kt`: app state orchestration and user actions
- `data/repository/*`: repository wrappers around Room and DataStore
- `data/dao/WorkoutDao.kt`: Room queries
- `data/model/*`: entities and transfer models
- `data/WorkoutDatabase.kt`: Room database and migration

## Data model

### `WorkoutSession`

- Table: `workout_sessions`
- Fields:
  - `id`
  - `type`
  - `timestamp`

### `ExerciseEntry`

- Table: `exercise_entries`
- Fields:
  - `id`
  - `sessionId`
  - `exerciseName`
  - `weight`
  - `reps`
  - `setNumber`
  - `durationSeconds`

Notes:

- `ExerciseEntry.sessionId` has `ForeignKey.CASCADE` back to `WorkoutSession`.
- Schema version is `3`.
- Migration `2 -> 3` adds nullable `durationSeconds`.
- Export/import format is `GymAppData(version, sessions, entries)`.

## Screens and behavior

### Dashboard

File: `app/src/main/java/com/example/gymapp/ui/screens/DashboardScreen.kt`

- Shows reorderable workout categories.
- Shows last workout date per category.
- Shows recent session cards.
- Has custom workout dialog.
- Has export/import actions in overflow menu.
- Shows a "What's New in v0.3" dialog gated by DataStore.

### Active Workout

File: `app/src/main/java/com/example/gymapp/ui/screens/ActiveWorkoutScreen.kt`

- Creates a new session immediately when opened, unless resuming an existing session.
- Shows previous workout entries for the same workout type.
- Supports exercise autocomplete using historical exercises for the current workout type.
- Handles standard strength logging with weight + reps.
- Handles cardio logging with duration in minutes stored as `durationSeconds`.
- Long-pressing current log rows enters edit mode and reveals delete buttons.
- Back handling:
  - empty new session gets discarded
  - populated new session asks save vs discard
  - resumed session asks save vs don't save

### History

File: `app/src/main/java/com/example/gymapp/ui/screens/HistoryScreen.kt`

- Lists all sessions using the shared `WorkoutSessionCard`.
- Also duplicates export/import actions.
- Swipe-to-resume is available through the shared session card.

### Insights

File: `app/src/main/java/com/example/gymapp/ui/screens/InsightsScreen.kt`

- Horizontal pager over the last 24 months.
- Monthly heatmap based on distinct workout days and workout types per day.
- Summary cards:
  - total workouts in month
  - workout days in month
  - average workouts per week
- Can share the rendered heatmap as a PNG through a `FileProvider`.
- Contains a developer-facing `Seed Data` action that wipes/replaces data via `viewModel.seedData()`.

## Shared components

### `WorkoutSessionCard`

- Expandable session card that loads entries reactively.
- Supports swipe from start to end to "resume" a session.
- Used in both Dashboard and History.

### `WorkoutHeatmap`

- Calendar-style month grid.
- A day becomes "active" if any workout exists that day.
- Up to 3 colored side strips indicate the workout types performed that day.

### `WorkoutCategory`

- Central list of built-in categories:
  - Legs
  - Back
  - Chest
  - Arms
  - Shoulders
  - Push
  - Pull
  - Abs
  - Cardio

## Preferences and persistence details

`UserPreferencesRepository` stores:

- `CATEGORY_ORDER`: comma-separated category names
- `HAS_SEEN_UPDATE_03`: whether the v0.3 dialog has been dismissed

The default theme is hardcoded dark in `NpNgTheme(darkTheme = true)`.

## Important implementation details

- Previous-workout comparison is query-based, not computed in memory.
- Exercise autocomplete normalizes historical exercise names to title case.
- Export/import is full-database backup and restore, not merge.
- The import path currently trusts the JSON payload and restores sessions/entries directly.
- The app shares one `WorkoutViewModel` across all destinations by creating it in `NavGraph`.

## Files that look important for future work

- `app/src/main/java/com/example/gymapp/ui/viewmodel/WorkoutViewModel.kt`
- `app/src/main/java/com/example/gymapp/ui/screens/ActiveWorkoutScreen.kt`
- `app/src/main/java/com/example/gymapp/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/example/gymapp/ui/screens/InsightsScreen.kt`
- `app/src/main/java/com/example/gymapp/data/dao/WorkoutDao.kt`
- `app/src/main/java/com/example/gymapp/data/WorkoutDatabase.kt`

## Current repo state and cleanup notes

At the time of this note, the worktree is not clean. Modified/untracked app files already exist and should be read carefully before editing:

- `app/src/main/java/com/example/gymapp/ui/components/WorkoutHeatmap.kt`
- `app/src/main/java/com/example/gymapp/ui/screens/InsightsScreen.kt`
- `app/src/main/java/com/example/gymapp/ui/viewmodel/WorkoutViewModel.kt`
- `app/schemas/`
- `app/src/main/res/xml/file_paths.xml`

There is also an accidental tracked file:

- `app/src/main/java/com/example/gymapp/app/src/main/java/com/example/gymapp/ui/viewmodel/WorkoutViewModel.kt`

Its contents say it was created by mistake and should be deleted.

## High-signal risks and likely bugs

1. Resuming a session is not a real draft/resume model.
   - History and dashboard allow swiping any old session to reopen it in `ActiveWorkoutScreen`.
   - New entries can be appended to historical sessions, but there is no explicit session status, lock, or resume marker.
   - "Don't Save" on a resumed workout just navigates back; it does not undo edits made during that reopen.

2. Import/restore may break primary-key assumptions.
   - `restoreData()` deletes then reinserts sessions and entries with serialized ids.
   - This depends on Room/SQLite behavior with autoincrement ids and assumes imported foreign keys remain valid exactly as stored.
   - There is no version-gated migration logic for backup payloads beyond `version = 3`.

3. `seedData()` is destructive and exposed in production UI.
   - Insights exposes a top-bar button that replaces live user data.

4. Category last-workout dates are fetched sequentially in `DashboardScreen`.
   - This causes one suspend query per category from UI code instead of exposing a single aggregated stream.

5. Export/import code is duplicated in Dashboard and History.
   - This increases drift risk and should probably move into a shared helper or shared UI action.

6. Theme setup is partly inconsistent.
   - Compose theme colors are custom, but resource XML colors/themes still contain mostly template/default values.
   - `Theme.GymApp` inherits `android:Theme.Material.Light.NoActionBar` while Compose forces dark styling.

7. There are almost no real tests.
   - Only template unit/instrumentation tests exist.

## Environment note

I could not verify the app with Gradle in this shell because Java was unavailable:

- `./gradlew :app:assembleDebug` failed with "Unable to locate a Java Runtime."

Future sessions should either restore a JDK in the shell environment or use Android Studio's configured JBR/JDK before relying on build verification.
