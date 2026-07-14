# RollCall 3.0 — System Design and UI Modernization Contract

> **Purpose:** This file is the implementation contract for any AI coding assistant working on the RollCall Android application.
>
> Read this file together with `PROJECT_DOCS.md` before changing code. `PROJECT_DOCS.md` is useful background but contains stale package, screen and version references. The checked-in source code and build files are authoritative when they disagree with either document.

---

## 1. Role of the AI

Act as a senior Android engineer and product designer modernizing an existing production-style application.

Your job is to:

1. Preserve all existing working attendance functionality and offline data.
2. Modernize the UI incrementally using Jetpack Compose and Material 3.
3. Create a coherent design system rather than styling each screen independently.
4. Add polished, purposeful motion, haptics and responsive layouts.
5. Use selective glass effects without sacrificing readability, accessibility or performance.
6. Avoid unnecessary rewrites of the database, repositories and business logic.
7. Leave the project buildable after every meaningful migration step.

Do not treat this project as a blank-slate demo application.

---

## 2. Product Identity

### Application

- **Name:** RollCall
- **Developer:** Parikshit Singh Bais
- **Product type:** Offline Android attendance manager
- **Primary users:** Teachers, professors, trainers and group leaders
- **Primary task:** Mark attendance as quickly and confidently as possible
- **Core promise:** Simple, fast, private and reliable attendance management

### Product principles

1. **Fast before decorative:** Attendance marking must remain immediate.
2. **Offline by default:** No account, cloud dependency, ads or tracking.
3. **Calm but expressive:** Motion should clarify actions and state, not distract.
4. **Large touch targets:** Important controls must be easy to use while standing or moving.
5. **Strong visual hierarchy:** Surfaces are separated mainly by solid tonal backgrounds, not outlines.
6. **Consistent interaction:** Similar actions must look and behave the same everywhere.
7. **Android-native:** Use Material 3 conventions while giving RollCall a distinctive visual identity.

---

## 3. Existing Technical Baseline

The current repository uses:

- Kotlin
- XML layouts and Material Design 3 View components
- Activities and Fragments
- MVVM-style architecture
- Hilt dependency injection
- Room database
- Kotlin coroutines and StateFlow
- DataStore Preferences
- View Binding
- Min SDK 24
- Offline-only operation without internet permission

The current project contains working entities, DAOs, repositories, ViewModels, CSV import, reports, settings, history, animations and haptic utilities.

### Repository-verified snapshot

This snapshot was checked against the repository on 14 July 2026:

| Area | Current implementation |
|---|---|
| App version | `2.0.1` (`versionCode = 2`) |
| Android SDK | compile/target 34, minimum 24 |
| Build tooling | AGP 8.2.2, Gradle 8.5, Kotlin 1.9.22, KSP 1.9.22-1.0.17, Java bytecode target 11 |
| UI | XML, View Binding, Material Components, Activities, Fragments, ViewPager2 and RecyclerView |
| Main navigation | `MainActivity` hosts Classes, History and Settings fragments in ViewPager2; Create Class, Attendance and Report are separate activities |
| Persistence | Room database version 1 with four entities; DataStore Preferences for settings |
| Saved settings | `theme`, `haptics_enabled`, `numbering_mode`, `report_template`, `attendance_mode` |
| Attendance input | Buttons, swipe, or both, selected through `attendance_mode` |
| Permissions | `VIBRATE` and legacy `READ_EXTERNAL_STORAGE` through API 32; no internet permission |
| Existing motion libraries | View animation utilities, DynamicAnimation and Lottie |
| Tests | Test dependencies exist, but no checked-in `src/test` or `src/androidTest` suites exist |
| Compose | Not enabled and no Compose dependencies are currently declared |

Do not rely on stale references in `PROJECT_DOCS.md` to `MainViewModel`, `HistoryActivity`, `SettingsActivity`, `HistoryAdapter`, or old package locations. Inspect the current tree before planning a migration.

### Known baseline risks to resolve or protect

- `CreateClassViewModel` currently edits a class by deleting and reinserting all students. Because attendance records reference students with cascading foreign keys, this can erase historical record rows while leaving session summaries behind. A redesign must not reproduce this behavior; preserve stable student IDs and update the roster transactionally.
- `AttendanceViewModel.saveAttendance()` has no in-progress or already-saved guard. Repeated taps can create duplicate sessions. Saving must become idempotent before or during the Attendance migration.
- `CsvParser.parseStudentsCsv()` is called synchronously from `CreateClassActivity`. Move file reading and parsing to `Dispatchers.IO` (or an injected IO dispatcher) without changing the accepted input contract unless a separate requirement changes it.
- Report generation resolves record student IDs against the current class roster. Roster edits can therefore make old report names disappear. Historical report integrity must be defined and protected before changing the schema or roster behavior.
- The saved `theme` preference and theme-application code exist, but the current Settings layout exposes no theme control. Treat the key as compatibility state; do not remove or rename it.
- A baseline `assembleDebug` attempt under JDK 25.0.2 failed before compilation because the current Gradle/AGP combination is not compatible with that runtime. Establish a compatible JDK, preferably JDK 17 for the current AGP baseline, and record a successful baseline build before migration.

### Protected systems

Unless a requested feature genuinely requires a change, preserve:

- Room entities and existing stored data
- DAOs and repository contracts
- Attendance calculation rules
- Class grouping logic
- CSV parsing behavior
- Report numbering modes
- Report template modes
- Attendance input modes (`buttons`, `swipe`, `both`)
- DataStore preference keys
- Hilt dependency graph
- Offline-only behavior

Do not change database schemas merely to support visual redesign work.

---

## 4. Target Architecture

Modernize the presentation layer while reusing the existing data and domain layers.

```text
Room / DataStore
       ↓
DAOs and repositories
       ↓
Existing or adapted ViewModels
       ↓
StateFlow-based UI state
       ↓
Jetpack Compose + Material 3 UI
```

### Target UI stack

- Jetpack Compose
- Material 3
- Navigation Compose
- Material 3 Adaptive where useful
- Lifecycle-aware StateFlow collection
- Compose Animation APIs
- Compose Preview for reusable components
- Haze or an equivalent maintained library only for selective backdrop blur
- Lottie only for rare illustration or completion moments

Material 3 Adaptive, a blur library and Lottie are optional capabilities, not mandatory dependencies. Add each only when a shipped screen needs it and a lightweight native implementation is insufficient.

### Version policy

- Prefer current stable Android and Compose libraries.
- Do not use an alpha dependency across the whole application for one visual effect.
- Experimental APIs must be isolated behind small wrappers and clearly documented.
- Upgrade Gradle, AGP, Kotlin and Java toolchains incrementally.
- Keep the project compiling between upgrade steps.
- Do not combine a toolchain upgrade, Compose enablement and the first screen migration in one unreviewable change.
- Record the JDK, Gradle, AGP, Kotlin, Compose compiler/plugin and Compose BOM versions that produced each successful migration build.
- Verify library stability and compatibility from official Android or library documentation at implementation time; this document intentionally does not pin future “latest” versions.

---

## 5. Migration Strategy

Do not rewrite every screen at once.

### Required approach

1. Establish a reproducible baseline build with a compatible JDK and record any pre-existing warnings or failures.
2. Add regression tests for the highest-risk existing behavior before changing it.
3. Enable Compose while retaining View Binding and the existing View screens.
4. Create the Compose theme and reusable component layer.
5. Allow Compose and XML screens to coexist temporarily.
6. Migrate one complete screen or flow at a time.
7. Reuse existing ViewModels where practical, but correct unsafe state and persistence behavior behind tests.
8. Replace old Activities only after their Compose destinations are stable.
9. Remove XML resources and dependencies only when no remaining screen references them.

### Recommended migration order

1. Theme, tokens and reusable components
2. Report screen
3. Settings screen
4. Create/Edit Class screen
5. Class list/Home screen
6. History screen
7. Attendance screen
8. Navigation consolidation and removal of obsolete View code

The Attendance screen should be visually prototyped early but migrated last because it is the most interaction-sensitive screen.

During coexistence, prefer a `ComposeView` or a Compose-hosting activity for one complete destination. Do not introduce Navigation Compose as the owner of routes until it can preserve all current intent arguments, back behavior and deep links between Attendance and Report.

---

## 6. Desired Visual Language

### Design name

**Material 3 Expressive with soft-solid tonal surfaces and selective glass overlays**

### Style formula

```text
80% Material 3 and solid tonal surfaces
15% expressive shape, scale and motion
5% real glass or blur effects
```

### User’s design preference

The user prefers:

- Filled components rather than outlined components
- Strong surface hierarchy created through background shade
- Soft rounded geometry
- Modern animations with visible polish
- Dark premium interfaces
- Controlled glow and blur
- Components that feel substantial, not hollow
- Rich motion similar to polished data-visualization libraries

### Do not produce

- An outline-heavy form interface
- Glass on every card
- Excessive transparency
- Random gradients
- Neon cyberpunk styling
- Large permanent shadows
- Different corner radii chosen independently on every screen
- Animation on every element merely because it is possible
- A direct copy of Apple’s interface

---

## 7. Color System

Use semantic tokens. Do not hard-code colors inside screen composables.

The following values are starting points and may be adjusted slightly after visual testing.

### Dark theme foundation

| Token | Suggested value | Usage |
|---|---:|---|
| `background` | `#0B0D12` | Main app background |
| `surface` | `#121722` | Standard cards and sections |
| `surfaceContainer` | `#18202D` | Elevated grouped content |
| `surfaceContainerHigh` | `#202B3A` | Selected, pressed or floating solid surfaces |
| `surfaceContainerHighest` | `#293647` | Strongest neutral layer |
| `primary` | `#6E8FFF` | Main action, active navigation, progress |
| `onPrimary` | `#FFFFFF` | Content on primary |
| `primaryContainer` | `#23386D` | Tonal primary surfaces |
| `success` | `#40D982` | Present state |
| `successContainer` | `#123D2A` | Present tonal surface |
| `error` | `#FF626B` | Absent/destructive state |
| `errorContainer` | `#4A1D25` | Absent tonal surface |
| `warning` | `#F7C95C` | Warning state |
| `textPrimary` | `#F5F7FB` | Main text |
| `textSecondary` | `#B8C0CE` | Supporting text |
| `textTertiary` | `#7F8999` | Hints and metadata |
| `divider` | white at 6–8% | Rare separators |

### Color rules

- Use tonal elevation before borders.
- Standard cards should normally have no visible stroke.
- Selected states may use a stronger filled container.
- Green and red must not be the only indication of Present/Absent; include icons or text.
- Avoid pure black across large surfaces; use near-black tonal layers.
- Use gradients only where they add depth to a hero, progress or glass surface.
- Dynamic color may be offered as an optional setting, but the default experience should preserve RollCall’s brand colors.

---

## 8. Surface and Glass System

### Standard solid surface

Use for most content:

- Opaque tonal background
- 16–24 dp corner radius depending on component size
- No border by default
- Little or no conventional shadow in dark mode
- Optional subtle tonal lift when pressed or selected

### Glass surface

Use only for floating or layered controls such as:

- Floating bottom navigation
- Floating top app bar
- Modal bottom sheets
- Dialogs over visible content
- Attendance bottom action dock
- Temporary progress or completion overlays

### Glass recipe

- Backdrop blur: approximately 16–28 dp
- Surface tint opacity: approximately 72–90%
- Border/highlight: white at approximately 4–8%
- Optional top-edge highlight
- Sufficient opaque fallback when blur is disabled or unsupported
- Never place low-contrast body text directly on highly transparent glass

### Glass restrictions

Do not use backdrop blur for:

- Every class card
- Every history item
- Long report content
- Text input fields
- Large scrolling list backgrounds
- Present and Absent controls

### Performance fallback

Provide a `GlassSurface` abstraction with two modes:

1. Real blurred glass where appropriate and performant
2. Opaque tonal fallback with the same shape and layout

Do not spread blur-library-specific APIs throughout the project.

---

## 9. Shape System

Define centralized shapes.

| Shape token | Radius | Typical usage |
|---|---:|---|
| `small` | 10–12 dp | Small chips and compact controls |
| `medium` | 16 dp | Standard buttons and list cards |
| `large` | 20 dp | Student card, dialogs and major sections |
| `extraLarge` | 24–28 dp | Sheets, floating navigation and hero surfaces |
| `pill` | 50% | Filter chips, compact segmented controls |

### Shape rules

- Avoid mixing many unrelated radii on one screen.
- Large components may use more expressive asymmetry only when it communicates state.
- Pressing a component may slightly reduce its radius or scale, but never distort text.
- Avoid making every component a pill.

---

## 10. Typography

Use Material 3 typography as the foundation with a clean sans-serif font.

Preferred hierarchy:

- `displaySmall`: rare completion or percentage hero number
- `headlineMedium`: screen headline where needed
- `titleLarge`: student name and major card title
- `titleMedium`: class names and section titles
- `bodyLarge`: primary readable content
- `bodyMedium`: descriptions
- `labelLarge`: buttons and tabs
- `labelMedium`: metadata, counters and chips

### Typography rules

- Use weight and size before changing color excessively.
- Student name is the main focus on Attendance.
- Report body must prioritize readability over decoration.
- Avoid all-uppercase interface labels except generated plain-text report headings.
- Use monospace only for generated report preview or structured identifiers where beneficial.
- Support system font scaling without clipping.

---

## 11. Spacing and Sizing

Use an 8 dp spacing system with 4 dp half-steps where necessary.

Suggested tokens:

```text
4, 8, 12, 16, 20, 24, 32, 40, 48
```

### General rules

- Phone screen horizontal padding: normally 16 or 20 dp
- Focused Attendance content: 20 or 24 dp
- Minimum touch target: 48 × 48 dp
- Primary button height: 56–64 dp
- Compact control height: at least 48 dp
- Keep enough bottom content padding for floating navigation/action surfaces
- Do not use fixed heights for text-heavy content unless the text is strictly bounded

---

## 12. Iconography

- Use one coherent rounded icon family.
- Prefer Material Symbols Rounded or another single Android-friendly set.
- Use vector drawables or Compose vector icons for interface actions; do not use emoji characters as icons or decoration.
- Do not mix unrelated stroke weights.
- Icons must have content descriptions unless decorative.
- Use icon + label for important actions such as Share, Save, Present and Absent.
- Do not rely on ambiguous icons for destructive or uncommon actions.

---

## 13. Motion System

Motion is a first-class part of the design system.

### Motion goals

- Confirm input
- Show state change
- Preserve spatial continuity
- Guide attention
- Make data changes understandable
- Add character without slowing repeated tasks

### Duration tokens

| Token | Duration | Usage |
|---|---:|---|
| `instant` | 80–100 ms | Press response |
| `fast` | 150–200 ms | Small fades and icon transitions |
| `standard` | 250–320 ms | Content and container changes |
| `emphasized` | 400–500 ms | Screen-level or major state transition |
| `celebration` | 600–800 ms | Rare completion moment only |

### Preferred motion character

- Use spring motion for scale and position settling.
- Use tween/easing for color, alpha and progress changes.
- Pressed components scale to approximately `0.96–0.98`.
- Release should feel responsive, not bouncy like a toy.
- Use shared bounds or container transforms where navigation originates from a visible card.

### Motion rules

- Never delay navigation only to finish a decorative animation.
- Repeated attendance marking must remain rapid.
- Avoid long glow sweeps on every mark.
- Do not animate large background blur continuously.
- Cancel or merge animations when the user taps rapidly.
- Respect system animator scale and provide reduced-motion behavior.
- Do not use Lottie for normal buttons, tabs, navigation or progress.

### Central API

Create a centralized motion specification such as:

```kotlin
object RollCallMotion {
    const val Instant = 90
    const val Fast = 180
    const val Standard = 300
    const val Emphasized = 450
    const val Celebration = 700
}
```

Use shared spring and easing definitions rather than inventing them in each composable.

---

## 14. Haptic System

Haptics must reinforce meaningful actions and respect the user setting.

Suggested mapping:

| Action | Haptic |
|---|---|
| Navigation selection | Very light tick or none |
| Standard button press | Light tap |
| Present | Light confirmation |
| Absent | Medium confirmation, not aggressive |
| Successful save | Success pattern |
| Invalid form or destructive warning | Error/warning pattern |
| Long press | Context-click feedback |

### Haptic rules

- Never trigger haptics during initial state restoration.
- Do not trigger multiple haptics for one logical action.
- Haptic feedback must be globally disableable.
- Avoid using a heavy impact simply because an action is red.

---

## 15. Component Architecture

Create reusable RollCall components before building final screens.

Recommended structure:

```text
ui/
├── theme/
│   ├── Color.kt
│   ├── Type.kt
│   ├── Shape.kt
│   ├── Spacing.kt
│   ├── Motion.kt
│   └── RollCallTheme.kt
├── components/
│   ├── RollCallSurface.kt
│   ├── GlassSurface.kt
│   ├── RollCallButton.kt
│   ├── RollCallIconButton.kt
│   ├── RollCallTopBar.kt
│   ├── RollCallNavigation.kt
│   ├── ClassCard.kt
│   ├── AttendanceActionButton.kt
│   ├── AttendanceProgress.kt
│   ├── StatChip.kt
│   ├── AnimatedCounter.kt
│   ├── SettingRow.kt
│   ├── EmptyState.kt
│   ├── ConfirmationDialog.kt
│   └── LoadingState.kt
├── navigation/
│   ├── Destination.kt
│   ├── RollCallNavHost.kt
│   └── RollCallScaffold.kt
└── screens/
```

### Component requirements

Every reusable component should:

- Accept state and callbacks rather than own business logic
- Support enabled, disabled, pressed and selected states
- Expose accessibility semantics
- Use theme tokens only
- Include previews for important visual states
- Avoid direct ViewModel access unless it is a route-level composable
- Avoid hard-coded strings
- Support dark theme and future light theme

---

## 16. State Management Rules

Use unidirectional data flow.

```text
ViewModel exposes immutable UI state
        ↓
Route composable collects state lifecycle-safely
        ↓
Stateless screen composable renders state
        ↓
User events are sent back to ViewModel
```

### Requirements

- Prefer one clear `UiState` per screen or flow.
- Model loading, content, empty and error states explicitly.
- Collect StateFlow using lifecycle-aware APIs.
- Do not duplicate repository state inside composables.
- Use `rememberSaveable` only for temporary UI state that should survive recreation.
- Keep business rules in ViewModels/repositories, not animation callbacks.
- Do not pass database entities deeply through unrelated components when a screen model is clearer.
- Model one-shot navigation and confirmation events so they are not replayed after configuration change or lifecycle restart.
- Add `isSaving` and a consumed/saved result to attendance state so one logical save can create at most one session.
- Use `SavedStateHandle` for required route identifiers and fail with an explicit error state when an identifier is missing or invalid.

---

## 17. Navigation Direction

Move gradually toward a single-activity Compose navigation model.

Target destinations:

```text
Classes
CreateClass
EditClass
Attendance
History
Report
Settings
```

Required route arguments:

| Destination | Argument |
|---|---|
| `EditClass` | existing `classId` |
| `Attendance` | existing `classId` |
| `Report` | existing `sessionId` |

`CreateClass` may also receive a source class ID for duplicate mode. Use typed destination/argument definitions rather than scattering string keys. Preserve the current post-save flow from Attendance to Report and make Back return to a predictable main destination without duplicating activities.

### Phone navigation

- Bottom navigation for Classes, History and Settings
- Floating or tonal New Class action on Classes
- Full-screen focused Attendance flow

### Larger-screen navigation

- Navigation rail or adaptive navigation suite
- History list and Report detail may use list-detail layout
- Class list may use a multi-column arrangement where appropriate

### Transition guidance

- Class card → Attendance: shared bounds or container transform
- History session → Report: shared container transition
- Bottom destinations: fade-through or subtle crossfade
- Expand/collapse: animated size plus chevron rotation
- Dialogs/sheets: Material motion with restrained scale and fade

---

## 18. Screen Specifications

## 18.1 Classes/Home

Purpose: Let the user find a class and begin attendance immediately.

Required elements:

- Screen title or contextual top bar
- Class list grouped by branch/semester/section where applicable
- Subject and student count
- New Class action
- Empty state
- Edit, duplicate and delete actions

Design:

- Use solid tonal class cards with no permanent outline.
- Use a subtle icon container or subject color accent.
- Show clear pressed feedback.
- Expanded groups animate smoothly without moving unrelated content excessively.
- Keep the primary action reachable with one hand.

Do not hide important actions behind gestures only; long press may be supported but also provide an accessible overflow menu.

## 18.2 Create/Edit Class

Purpose: Enter class details and import students with minimal friction.

Required fields:

- Branch
- Semester
- Section
- Subject
- Student CSV import
- Imported student count
- Save/Create action

Design:

- Prefer filled tonal text fields over outline-heavy fields.
- Group related information into solid surface sections.
- Show inline validation near the relevant field.
- Keep the main save button visible or easily reachable.
- CSV import should clearly show file state, count and replacement behavior.

Behavior:

- Preserve create, edit and duplicate modes.
- Prevent accidental loss of changed form data.
- Do not parse files on the main thread.
- Preserve existing student IDs when editing a roster. Diff inserts, updates and removals inside a Room transaction rather than deleting the complete roster.
- Define removal behavior for students referenced by historical attendance before allowing removal. The default requirement is that old reports remain complete and readable.
- CSV parsing must continue to accept the current one-column name format and two-or-more-column roll-number/name format, including automatic first-row header detection.

## 18.3 Attendance

Purpose: Mark every student accurately and as quickly as possible.

This is the most important screen.

Suggested hierarchy:

```text
Top bar: class + subject + progress
Student position / roll number
Large student name
Compact live statistics
Segmented present/absent progress
Large Absent and Present actions
Previous / Skip or Next controls
Save action when appropriate
```

### Student card

- Solid elevated tonal surface
- Large student name as visual focus
- Roll number and current position as supporting information
- Present/Absent state shown by icon, label and color
- Avoid a permanent border; temporary state glow or tonal fill is acceptable

### Present and Absent controls

- Large filled controls, not transparent outlined cards
- Present uses a success tonal surface and becomes stronger when activated
- Absent uses an error tonal surface and becomes stronger when activated
- Include both letter/icon and word label
- Maintain at least 48 dp touch targets; these controls should be substantially larger

### Marking interaction

On Present:

1. Immediate press scale
2. Light haptic
3. Success container/color transition
4. Present counter animates to new value
5. Progress segment animates
6. Student content transitions to next student without an artificial wait

The selected attendance input mode is functional behavior, not merely presentation. Preserve all three persisted modes:

- `buttons`: show Present and Absent controls; disable swipe marking.
- `swipe`: enable swipe marking; hide the large buttons and provide clear gesture guidance.
- `both`: enable both input methods.

On Absent:

1. Immediate press scale
2. Medium haptic
3. Error container/color transition
4. Absent counter animates
5. Progress segment animates
6. Student content transitions to next student without an artificial wait

### Rapid-use requirement

- The user must be able to mark students rapidly.
- Avoid a mandatory 700 ms glow sweep after every action.
- Use approximately 150–300 ms feedback that can be interrupted.
- Do not block the next tap while decorative animation completes.

### Completion

- Show a restrained success state when all students are marked.
- Provide a clear Save Attendance action.
- A small check or completion animation is acceptable once per session.
- Do not show an intrusive celebration after every mark.

### Safety

- Preserve reset confirmation.
- Preserve discard confirmation when marks exist.
- Make previous/review behavior clear.
- Prevent duplicate session saves from repeated taps.
- Preserve current partial-save behavior: users may save with unmarked students after explicit confirmation; only marked students receive records while the session retains the class total.
- Disable all save entry points while a save transaction is running and navigate only after the transaction succeeds.

## 18.4 History

Purpose: Find and inspect past attendance sessions.

Design:

- Group by date.
- Use a filled filter control rather than an old spinner appearance.
- Session cards display class, subject, time, present, absent and rate.
- Expand/collapse date groups with size animation and chevron rotation.
- Keep newest sessions first.
- Destructive delete action requires confirmation.

## 18.5 Report

Purpose: Summarize a completed attendance session and support sharing.

Design:

- Class/date header surface
- Three clear statistics: Present, Absent and Rate
- Readable detailed report section
- Copy and Share actions
- Do not put long text on highly transparent glass
- Use animated numbers only on first appearance or when data changes

Behavior:

- Preserve report template modes.
- Preserve absolute and relative numbering modes.
- Preserve Android share behavior.
- Copy action should provide clear confirmation.

## 18.6 Settings

Purpose: Configure application behavior without overwhelming the user.

Suggested sections:

- Appearance
- Interaction
- Reports
- About

Settings should include or preserve:

- Haptic feedback
- Attendance input mode: Buttons, Swipe, or Both
- Report template
- Numbering mode
- The persisted theme value for compatibility; expose a theme control only when light/system themes are actually designed and tested
- Optional dynamic color
- Optional reduced motion
- App version and developer name

Design:

- Use reusable `SettingRow` components.
- Avoid putting every setting inside its own outlined card.
- Group settings into tonal sections.
- Switches and segmented controls must show clear selected states.

Dynamic color and reduced motion are target enhancements, not existing settings. If added, use new DataStore keys with backward-compatible defaults. Never reuse or change the meaning of an existing key.

---

## 19. Empty, Loading and Error States

Every data screen must define:

- Loading state
- Empty state
- Content state
- Recoverable error state where relevant

### Empty state style

- Soft tonal icon container
- Clear title
- One short explanation
- Primary action only where useful
- Optional subtle entrance animation

Do not use a large decorative animation that delays access to the primary action.

### Loading

- Prefer content-shaped placeholders or a simple progress indicator.
- Avoid fake long loading animations for fast local database operations.

---

## 20. Accessibility Requirements

These are non-negotiable.

- Minimum interactive target: 48 × 48 dp
- Support TalkBack labels and roles
- Announce important attendance state changes appropriately
- Do not rely on color alone
- Maintain readable contrast over glass surfaces
- Support large font sizes without clipping
- Respect reduced motion / animator duration settings
- Preserve logical focus order
- Ensure dialogs have clear safe and destructive actions
- Avoid rapid flashing effects
- Provide accessible alternatives to long press and swipe-only actions

---

## 21. Performance Requirements

- Keep attendance interactions responsive on older devices supported by Min SDK 24.
- Avoid unnecessary recomposition by using stable state and sensible component boundaries.
- Use lazy lists for long collections.
- Use keys for list items.
- Do not blur large continuously scrolling regions.
- Avoid allocating animation objects repeatedly inside hot recomposition paths.
- Move CSV parsing and database work off the main thread.
- Make multi-table writes transactional, especially class/roster edits and attendance session/record creation.
- Use baseline profiles or startup optimization only after the migration is stable.
- Measure before adding heavy visual libraries.

---

## 22. Code Quality Rules

- Follow Kotlin style and clear naming.
- Prefer small, focused composables.
- Keep route composables separate from stateless screen composables.
- Do not place large amounts of business logic in `LaunchedEffect`.
- Do not access repositories directly from UI components.
- Do not hard-code user-facing strings.
- Do not hard-code colors, dimensions, shapes or durations in screens.
- Do not use emojis in code comments, strings, interface labels, icons or decorative UI. Use clear text and the established vector icon family.
- Avoid duplicated components with slightly different styling.
- Document any temporary migration bridge between Views and Compose.
- Remove dead code only after verifying that no existing flow uses it.

---

## 23. Testing Expectations

At minimum, protect these behaviors:

- Creating a class
- Editing a class
- Editing a roster without corrupting or emptying historical reports
- Duplicating a class
- Importing students from supported CSV formats
- Starting attendance
- Marking Present and Absent
- Moving backward and changing a mark
- Resetting attendance
- Discarding partially marked attendance
- Saving one session exactly once
- Saving a partially marked session with the existing confirmation semantics
- Viewing history
- Opening a report
- Copying and sharing a report
- Filtering history
- Changing report settings
- Persisting settings through app restart
- Preserving buttons/swipe/both attendance mode behavior
- Preserving existing Room data during UI migration

Add:

- ViewModel unit tests for business state
- Repository/Room tests for transactional writes, cascade behavior and historical-data preservation
- Compose UI tests for critical flows
- Screenshot or golden tests for core components where practical
- Accessibility checks for major screens

---

## 24. AI Working Procedure

Before making changes, the AI must:

1. Read `PROJECT_DOCS.md`.
2. Inspect the actual repository tree and relevant source files.
3. Identify the current build configuration and dependency versions.
4. Build the current project before refactoring when the environment permits.
5. State which files and layer will be changed.
6. Preserve existing behavior unless a requested change explicitly replaces it.

For each implementation task:

1. Make the smallest coherent change.
2. Reuse or create design-system components.
3. Keep UI state explicit.
4. Run compilation/tests where available.
5. Report changed files and any remaining migration bridge.
6. Do not claim successful compilation unless it was actually run.

### AI must not

- Invent files or classes without checking the repository.
- Rewrite the database during a visual task.
- Add internet permission, analytics, ads or cloud sync.
- Add emoji icons or decorative emoji text.
- Replace Hilt/Room/DataStore simply due to personal preference.
- Introduce multiple overlapping UI libraries.
- add arbitrary colors or animations outside the design tokens.
- Convert every screen in a single uncontrolled patch.
- silently remove features described in `PROJECT_DOCS.md`.
- claim that an API is stable without checking the project’s chosen dependency version.

---

## 25. Suggested Implementation Phases

### Phase 0 — Stabilize and protect

- Build with a compatible, documented JDK
- Add tests around attendance save, class/roster edit, CSV parsing and report generation
- Make attendance saving idempotent and transactional
- Make class/roster editing preserve historical attendance data
- Move CSV file parsing off the main thread
- Decide and document historical roster semantics before any schema change

### Phase A — Foundation

- Upgrade build tooling incrementally where required
- Enable Compose
- Add Material 3
- Create theme tokens
- Create motion and haptic wrappers
- Create solid and glass surface abstractions
- Add previews

### Phase B — Shared Components

- Buttons
- Icon buttons
- Top app bar
- Navigation surface
- Cards
- Stat chips
- Animated counters
- Empty state
- Setting rows
- Confirmation dialogs

### Phase C — Low-risk Screens

- Report
- Settings
- Create/Edit Class

### Phase D — Lists and Navigation

- Classes/Home
- History
- Navigation Compose
- Adaptive layouts

### Phase E — Attendance

- Attendance UI state model
- Student content transition
- Present/Absent actions
- Progress animation
- Haptics
- Save/discard/reset protections

### Phase F — Cleanup

- Remove obsolete XML layouts and adapters only when unused
- Remove obsolete animation utilities only after Compose equivalents are complete
- Update `PROJECT_DOCS.md`
- Add screenshots
- Run regression tests
- Verify offline permissions
- Remove `READ_EXTERNAL_STORAGE` if verification confirms the Storage Access Framework flow does not require it on any supported API level

---

## 26. Definition of Done

A migrated feature is complete only when:

- It matches this design system.
- It preserves the original functional behavior.
- It uses reusable theme tokens and components.
- It handles loading, empty and content states.
- It is accessible with large touch targets and meaningful labels.
- Motion is purposeful and interruptible.
- It works without internet.
- It does not corrupt or discard existing local data.
- Its database writes that span multiple tables are atomic.
- Repeated save taps cannot create duplicate attendance sessions.
- It compiles successfully.
- Critical tests pass.
- Any remaining XML/Compose bridge is documented.

---

## 27. Final Visual Summary

RollCall 3.0 should feel:

- Dark
- Premium
- Soft-solid
- Fast
- Tactile
- Expressive
- Calm
- Trustworthy
- Android-native

The application should **not** feel like a generic Material sample and should **not** feel like an Apple clone.

Use filled tonal surfaces for the majority of the interface. Reserve glass, blur and glow for a few floating layers and special moments. Make attendance marking the fastest, clearest and most satisfying interaction in the application.

---

## 28. First Instruction to an AI Coding Agent

Use the following as the opening task when beginning the modernization:

> Read `PROJECT_DOCS.md` and `ROLLCALL_AI_UI_REDESIGN_GUIDE.md`, but treat checked-in source and build files as authoritative. First establish a reproducible baseline build with a compatible JDK and implement the Phase 0 regression tests and safety fixes. Do not begin visual migration until attendance saving is idempotent, roster edits preserve historical data and CSV parsing is off the main thread. Then enable Compose alongside the existing Views, create the design-system foundation and migrate one complete low-risk destination at a time. Preserve Room data, Hilt, DataStore keys, StateFlow behavior, CSV compatibility, attendance input modes, report settings, history and offline-only operation.
