# Roll Call - Complete Project Documentation

> Single-source-of-truth document covering architecture, data model, UI/UX design system, screen-by-screen breakdown, interaction patterns, and file reference. Written so an AI or new developer can understand the entire project in one reading.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Architecture](#3-architecture)
4. [Package Structure](#4-package-structure)
5. [Data Model](#5-data-model)
6. [Design System](#6-design-system)
7. [Screens & Navigation Flow](#7-screens--navigation-flow)
8. [Screen-by-Screen UI/UX Breakdown](#8-screen-by-screen-uiux-breakdown)
9. [Component Patterns](#9-component-patterns)
10. [Animations & Micro-Interactions](#10-animations--micro-interactions)
11. [Haptic Feedback System](#11-haptic-feedback-system)
12. [State Management](#12-state-management)
13. [Settings & Preferences](#13-settings--preferences)
14. [File Reference](#14-file-reference)

---

## 1. Project Overview

**Roll Call** is a 100% offline Android attendance management app for teachers, professors, and group leaders. Users create classes, add students (manually or via CSV), take attendance with large Present/Absent buttons, and view reports with statistics.

**Key characteristics:**
- No internet permission. All data stored locally via Room database.
- Dark-theme-only. Apple-inspired dark color palette optimized for AMOLED.
- Material Design 3 components (cards, buttons, switches, bottom nav, toolbar).
- MVVM architecture with Hilt dependency injection, Kotlin coroutines, StateFlow.
- Version 2.0.0. Min SDK 24 (Android 7.0). Target SDK 34.

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin (100%) |
| UI Framework | XML layouts + Material Design 3 |
| Architecture | MVVM with Clean Architecture layers |
| Database | Room (SQLite) |
| DI | Hilt (Dagger) |
| Async | Kotlin Coroutines + StateFlow |
| Preferences | DataStore (Preferences) |
| Build | Gradle KTS, AGP 8.2.2, Kotlin 1.9.22, KSP |
| View Binding | Enabled (`viewBinding = true`) |
| ProGuard | Enabled in both debug and release builds |

---

## 3. Architecture

```
UI Layer (Activities + Fragments)
    |
ViewModel Layer (StateFlow-based state)
    |
Repository Layer (single source of truth)
    |
Data Layer (Room DAOs + DataStore)
```

- **Activities**: MainActivity (host with ViewPager2), CreateClassActivity, AttendanceActivity, ReportActivity, HistoryActivity, SettingsActivity.
- **Fragments**: ClassListFragment (tab 0), HistoryFragment (tab 1), SettingsFragment (tab 2) — all hosted inside MainActivity's ViewPager2.
- **ViewModels**: MainViewModel, AttendanceViewModel, ReportViewModel, HistoryViewModel, SettingsViewModel. All use `MutableStateFlow` / `StateFlow` for reactive state.
- **Repositories**: AttendanceRepository (all DB operations), SettingsRepository (DataStore preferences).
- **DI Modules**: DatabaseModule (provides Room DB + DAOs), DataStoreModule (provides DataStore).

---

## 4. Package Structure

```
com.simpleattendance/
  RollCallApplication.kt          -- Hilt app entry point (@HiltAndroidApp)
  data/
    local/
      AppDatabase.kt              -- Room database, version 1, 4 entities
      dao/
        ClassDao.kt               -- CRUD for classes table
        StudentDao.kt             -- CRUD for students table
        AttendanceDao.kt          -- CRUD for sessions + records tables
      entity/
        ClassEntity.kt            -- classes table
        StudentEntity.kt          -- students table (FK to classes)
        AttendanceSessionEntity.kt -- attendance_sessions table (FK to classes)
        AttendanceRecordEntity.kt  -- attendance_records table (FK to sessions + students)
    repository/
      AttendanceRepository.kt     -- Singleton, wraps all 3 DAOs
      SettingsRepository.kt       -- Singleton, wraps DataStore
  di/
    DatabaseModule.kt             -- Provides AppDatabase, ClassDao, StudentDao, AttendanceDao
    DataStoreModule.kt            -- Provides DataStore<Preferences>
  ui/
    main/
      MainActivity.kt             -- Host: ViewPager2 + BottomNavigation + FAB
      MainPagerAdapter.kt         -- FragmentStateAdapter: 3 fragments
      MainViewModel.kt            -- Loads classes, manages grouping/expansion
      ClassListFragment.kt        -- Tab 0: class list RecyclerView
      HistoryFragment.kt          -- Tab 1: history list with date grouping
      SettingsFragment.kt         -- Tab 2: tabbed settings (General/Reports/About)
      ClassAdapter.kt             -- Multi-type adapter: single class card OR expandable group
    attendance/
      AttendanceActivity.kt       -- Take attendance screen
      AttendanceViewModel.kt      -- Manages marking state, saves session
    createclass/
      CreateClassActivity.kt      -- Create/edit class form
      CreateClassViewModel.kt     -- Handles class creation logic
    report/
      ReportActivity.kt           -- Attendance report display
      ReportViewModel.kt          -- Builds report text, loads session data
    history/
      HistoryActivity.kt          -- Standalone history (also available as fragment)
      HistoryViewModel.kt         -- Loads sessions, filters by class
      HistoryAdapter.kt           -- Flat session list adapter
      GroupedHistoryAdapter.kt    -- Date-grouped adapter with expandable headers
    settings/
      SettingsActivity.kt         -- Standalone settings (also available as fragment)
      SettingsViewModel.kt        -- Reads/writes DataStore preferences
  util/
    AnimationUtils.kt             -- scaleIn/Out, slideIn, fadeIn/Out, pulse, shake
    HapticUtils.kt                -- lightTap, mediumImpact, heavyImpact, successPattern, errorPattern
    CsvParser.kt                  -- Parses CSV files into StudentCsvRow list
```

---

## 5. Data Model

### Entity Relationship Diagram

```
ClassEntity (classes)
  |-- 1:N --> StudentEntity (students)
  |-- 1:N --> AttendanceSessionEntity (attendance_sessions)
                  |-- 1:N --> AttendanceRecordEntity (attendance_records)
                                    |-- N:1 --> StudentEntity (students)
```

### ClassEntity (`classes` table)

| Column | Type | Notes |
|---|---|---|
| id | Long (PK, auto) | Primary key |
| branch | String | e.g. "Computer Science" |
| semester | String | e.g. "4th" |
| section | String | e.g. "A" |
| subject | String | e.g. "Data Structures" |
| createdAt | Long | Epoch millis, defaults to now |

**Computed properties:**
- `displayName` = `"$branch $semester-$section"` (e.g. "Computer Science 4th-A")
- `fullDisplayName` = `"$branch $semester-$section ($subject)"` (e.g. "Computer Science 4th-A (Data Structures)")
- `batchKey` = `"$branch|$semester|$section"` — used to group classes with same branch/semester/section into expandable groups

### StudentEntity (`students` table)

| Column | Type | Notes |
|---|---|---|
| id | Long (PK, auto) | Primary key |
| classId | Long (FK) | References classes.id, CASCADE delete |
| rollNo | String | Roll/enrollment number, can be empty |
| name | String | Student full name |

**Computed:** `displayName` = `"$rollNo - $name"` or just `name` if rollNo is empty.

### AttendanceSessionEntity (`attendance_sessions` table)

| Column | Type | Notes |
|---|---|---|
| id | Long (PK, auto) | Primary key |
| classId | Long (FK) | References classes.id, CASCADE delete |
| date | Long | Epoch millis, defaults to now |
| presentCount | Int | Snapshot at save time |
| absentCount | Int | Snapshot at save time |
| totalCount | Int | Total students in class at save time |

**Computed:** `percentage` = `(presentCount / totalCount) * 100`

### AttendanceRecordEntity (`attendance_records` table)

| Column | Type | Notes |
|---|---|---|
| id | Long (PK, auto) | Primary key |
| sessionId | Long (FK) | References attendance_sessions.id, CASCADE delete |
| studentId | Long (FK) | References students.id, CASCADE delete |
| status | String | "P" (present) or "A" (absent) |

---

## 6. Design System

### Color Palette

The app uses a **dark-only** Apple-inspired color system with opacity-based hierarchy.

**Backgrounds (darkest to lightest):**
| Name | Hex | Usage |
|---|---|---|
| background_primary | #1C1C1E | Main screen background, status bar |
| background_secondary | #2C2C2E | Card backgrounds, bottom nav |
| background_tertiary | #3A3A3C | Hovered states, progress track |
| background_quaternary | #48484A | Pressed states |

**Primary accent (Apple Blue):**
| Name | Hex | Usage |
|---|---|---|
| primary | #0A84FF | Buttons, links, active nav, FAB, progress bar |
| primary_dark | #0066CC | Primary container |
| primary_light | #409CFF | Lighter variant |
| primary_subtle | #260A84FF | Subtle background tint (20% opacity) |
| on_primary | #FFFFFF | Text/icons on primary surfaces |

**Status colors (Apple Semantic):**
| Name | Hex | Usage |
|---|---|---|
| success_green | #30D158 | Present button, present chips, green borders |
| success_green_light | #1A30D158 | Present stat card background (10% opacity) |
| error_red | #FF453A | Absent button, absent chips, red borders |
| error_red_light | #1AFF453A | Absent stat card background (10% opacity) |
| warning_yellow | #FFD60A | Warning states |
| info_blue | #0A84FF | Info states (same as primary) |

**Text hierarchy (opacity-based on white):**
| Name | Hex | Opacity | Usage |
|---|---|---|---|
| text_primary | #FFFFFF | 100% | Headlines, important text |
| text_secondary | #B3FFFFFF | 70% | Body text, descriptions |
| text_tertiary | #80FFFFFF | 50% | Hints, labels, timestamps |
| text_quaternary | #4DFFFFFF | 30% | Disabled, subtle counts |
| text_disabled | #40FFFFFF | 25% | Truly disabled elements |

**Glass morphism:**
| Name | Hex | Usage |
|---|---|---|
| glass_background | #D92C2C2E | Semi-transparent card overlay |
| glass_background_light | #BF3A3A3C | Lighter glass variant |
| glass_border | #14FFFFFF | Subtle white border (8% opacity) on cards |

**Other:**
| Name | Hex | Usage |
|---|---|---|
| card_background | #2C2C2E | Standard card fill |
| card_background_hovered | #3A3A3C | Card hover state |
| card_background_pressed | #48484A | Card pressed state |
| divider | #14FFFFFF | Horizontal dividers |
| ripple | #33FFFFFF | Touch ripple overlay (20% opacity) |
| nav_item_active | #0A84FF | Active bottom nav item |
| nav_item_inactive | #99FFFFFF | Inactive bottom nav item (60% opacity) |

### Typography

Uses Material 3 type scale. Key styles used throughout:
- `TextAppearance.Material3.HeadlineSmall` — Empty state headings
- `TextAppearance.Material3.HeadlineMedium` — Stat numbers in History
- `TextAppearance.Material3.TitleLarge` — Report class name, attendance student name (22sp, bold)
- `TextAppearance.Material3.TitleMedium` — Card titles, student name in class card
- `TextAppearance.Material3.BodyMedium` — Body text in reports, descriptions
- `TextAppearance.Material3.BodySmall` — Subtitles, hints, developer info
- `TextAppearance.Material3.LabelLarge` — Section headers ("Class Details", "Students"), tab text
- `TextAppearance.Material3.LabelMedium` — Counter text, progress labels, tab text
- `TextAppearance.Material3.LabelSmall` — Student number (12sp), chips, timestamps, date headers

### Spacing & Dimensions

- Screen horizontal padding: 24dp (attendance screen), 16dp (list screens)
- Card corner radius: 16dp (standard), 20dp (student card, buttons, date pill), 12dp (filter cards)
- Card stroke: 1dp `glass_border` (#14FFFFFF)
- Card elevation: 0dp (flat design throughout)
- Button corner radius: 16dp
- Button height: 56dp (primary full-width buttons)
- FAB margin bottom: 100dp (above bottom nav)
- Progress bar height: 12dp, corner radius 6dp
- Student attendance card: 160dp fixed height
- A/P attendance buttons: 130dp height, 20dp corner radius, 2dp stroke
- Chip padding: 12dp horizontal, 4dp vertical
- RecyclerView bottom padding: 160dp (to clear FAB + bottom nav)

### Card Styles

Defined in `themes.xml`:

1. **Standard Card** (`Widget.RollCall.Card`): `card_background` fill, 16dp corners, 0dp elevation, 1dp `glass_border` stroke.
2. **Glass Card** (`Widget.RollCall.Card.Glass`): `glass_background` fill, `glass_border` stroke.
3. **Stat Chip Green** (`stat_chip_background_green`): #1A4CAF50 fill, 16dp corners.
4. **Stat Chip Red** (`stat_chip_background_red`): Similar red-tinted fill.
5. **Chip Green** (`chip_background_green`): #1A30D158 fill, 16dp corners (used for "P: X" in history).
6. **Chip Red** (`chip_background_red`): Similar red-tinted fill (used for "A: X" in history).

### Tab Design

Settings tab bar is a `TabLayout` inside a `MaterialCardView` with `background_secondary` fill, 14dp corners. Selected tab gets a solid primary-blue pill background (`tab_item_background.xml`), unselected is transparent. Uses `LabelMedium` text style.

---

## 7. Screens & Navigation Flow

### Navigation Architecture

```
MainActivity (single activity host)
  |
  +-- ViewPager2 (swipeable, 3 pages)
  |     |
  |     +-- Page 0: ClassListFragment  <-- Bottom Nav: "Attendance"
  |     +-- Page 1: HistoryFragment    <-- Bottom Nav: "History"
  |     +-- Page 2: SettingsFragment   <-- Bottom Nav: "Settings"
  |
  +-- ExtendedFloatingActionButton (visible only on Page 0)
  +-- BottomNavigationView (3 items: Attendance, History, Settings)
  +-- MaterialToolbar (title animates on tab switch)
```

**Bottom Navigation Items:**
1. Attendance (ic_class icon) -> Page 0
2. History (ic_history icon) -> Page 1
3. Settings (ic_settings icon) -> Page 2

**Toolbar title animation:** When switching tabs, the toolbar title fades out (100ms), changes text, then fades in (150ms) using `ObjectAnimator` on the alpha property.

**FAB visibility:** The "New Class" extended FAB is shown only on Page 0 (Attendance). It is hidden on Pages 1 and 2 via `fab.hide()` / `fab.show()`.

### Screen Transitions

```
MainActivity
  |-- FAB tap --> CreateClassActivity (create new class)
  |-- Class card tap --> AttendanceActivity (take attendance for that class)
  |-- Class long-press --> MaterialAlertDialog (Edit / Duplicate / Delete)
  |
AttendanceActivity
  |-- Back (with marks) --> MaterialAlertDialog ("Discard Attendance?")
  |-- Back (no marks) --> finish()
  |-- Save --> ReportActivity (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP)
  |-- Toolbar Reset --> MaterialAlertDialog confirmation
  |
ReportActivity
  |-- Back --> returns to calling activity (MainActivity or HistoryActivity)
  |-- Share button --> Android share intent
  |-- Copy button --> copies report text to clipboard
  |
HistoryFragment (inside MainActivity)
  |-- Session tap --> ReportActivity (with fromHistory=true)
  |-- Session long-press --> MaterialAlertDialog (Delete Session)
  |
ClassListFragment (inside MainActivity)
  |-- Class tap --> AttendanceActivity
  |-- Class long-press --> MaterialAlertDialog (Edit / Duplicate / Delete)
```

---

## 8. Screen-by-Screen UI/UX Breakdown

### 8.1 Main Activity (Home)

**Layout:** `activity_main.xml`
- `CoordinatorLayout` with `background_primary` (#1C1C1E), edge-to-edge.
- `AppBarLayout` with transparent status bar, `MaterialToolbar` with "Roll Call" title.
- `ViewPager2` fills remaining space with scrolling view behavior.
- `ExtendedFloatingActionButton` at bottom-end, 100dp above bottom nav. Text "New Class", blue fill, white text, ic_add icon.
- `BottomNavigationView` at bottom, `background_secondary` fill, labeled items.

**Behavior:**
- Swiping between ViewPager2 pages syncs with bottom nav selection.
- Bottom nav tap switches ViewPager2 page with smooth scroll.
- Haptic light tap on every tab switch/page change.
- Toolbar title animates: "Roll Call" -> "History" -> "Settings" with fade transition.
- FAB hides on History/Settings tabs, shows on Attendance tab.

### 8.2 Class List Fragment (Tab 0)

**Layout:** `fragment_class_list.xml`
- `FrameLayout` with `background_primary`.
- `RecyclerView` with 16dp horizontal padding, 160dp bottom padding (for FAB + nav).
- Progress bar (centered, primary-tinted, shown during loading).
- Empty state: circular card (96dp, 48dp radius, tertiary background) with ic_class icon, "No classes yet" heading, "Tap + to create your first class" subtext.

**Class Adapter (`ClassAdapter.kt`):**

Two view types based on grouping:
1. **TYPE_GROUP_SINGLE** (`item_class.xml`): Used when a batchKey has exactly one class.
   - `MaterialCardView`: card_background fill, 16dp corners, 1dp glass_border stroke, ripple.
   - Horizontal layout: primary-subtle icon container (48dp, 12dp radius, ic_class icon) + vertical text (className, subjectName, studentCount) + chevron right icon.
   - Vertical margin: 6dp between cards.
   - Click -> start attendance. Long-press -> show options dialog.

2. **TYPE_GROUP_MULTIPLE** (`item_class_group.xml`): Used when a batchKey has 2+ classes.
   - Expandable group card with folder icon, group name (e.g. "Computer Science 4th-A"), subject count ("2 subjects"), expand/collapse chevron.
   - When expanded: child `ItemClassBinding` cards indented (24dp start padding), showing subject name only (subjectName hidden).
   - Click on header -> toggles expansion with rotation animation on chevron.

**Grouping Logic (`MainViewModel.groupClasses()`):**
- Classes are grouped by `batchKey` (branch|semester|section).
- Groups sorted alphabetically by displayName.
- Classes within each group sorted by subject name.
- Expansion state tracked in `expandedGroups: MutableSet<String>`.

### 8.3 Create Class Activity

**Layout:** `activity_create_class.xml`
- Toolbar with "Create Class" title, back navigation icon.
- `NestedScrollView` with 24dp padding.

**Section 1: "Class Details"**
- Blue label "Class Details" (primary color, LabelLarge style).
- `MaterialCardView` with 4 outlined `TextInputLayout` fields:
  - Branch (e.g. "Computer Science")
  - Semester (e.g. "4th")
  - Section (e.g. "A")
  - Subject (e.g. "Data Structures")
- All use `Widget.Material3.TextInputLayout.OutlinedBox` style, primary stroke color.

**Section 2: "Students"**
- Blue label "Students".
- `MaterialCardView` containing:
  - "Import from CSV" title + description.
  - Two buttons: "Select CSV File" (tonal button, ic_file icon) + "Format Info" (text button).
  - Student count text (green, shown after import, initially gone).

**Save Button:**
- Full-width primary button "Create Class", 56dp height, 16dp corner radius.

**Behavior:**
- CSV parsing via `CsvParser.parseStudentsCsv()`: reads CSV, skips header rows (detects keywords like "roll", "name", "student"), supports 2-column (rollNo, name) or 1-column (name only) format.
- Supports editing existing class (receives `classId` intent extra).
- Supports duplicating class (receives `duplicateClassId` intent extra).

### 8.4 Take Attendance Activity (Marquee Screen)

**Layout:** `activity_attendance.xml`
- Toolbar with class displayName as title, subject as subtitle, back icon, overflow menu (Sort + Reset).
- Content below toolbar with 24dp horizontal padding.

**Section 1: Student Card (160dp fixed height)**
- `MaterialCardView`: card_background fill, 20dp corners, 1dp glass_border stroke.
- Contains centered vertical layout:
  - Student number (12sp, LabelSmall, tertiary text): "3 / 32"
  - Roll number (14sp, LabelMedium, secondary text, 0.1 letterSpacing): "CS001"
  - Student name (22sp, HeadlineLarge, bold, primary text, max 2 lines): "John Smith"
- Card border color changes based on status: green for P, red for A, glass_border for unmarked.
- Name color changes based on status: success_green for P, error_red for A, text_primary for unmarked.

**Section 2: Live Stats Row**
- Horizontal layout centered with three stat indicators:
  - **Present chip**: green circle dot (8dp) + "X Present" text (13sp, bold, success_green), inside `stat_chip_background_green` pill (16dp horizontal, 8dp vertical padding).
  - **Divider**: 1dp wide, 20dp tall, glass_border color.
  - **Absent chip**: red circle dot + "X Absent" text (error_red), inside `stat_chip_background_red` pill.
  - **Divider**.
  - **Remaining**: "X Left" text (13sp, text_tertiary).

**Section 3: Animated Progress Bar**
- Progress text centered: "12/32 marked" (13sp, LabelMedium, secondary).
- Custom progress bar (12dp height, 6dp radius):
  - Track background (tertiary color).
  - Green present fill (success_green), width proportional to presentCount/totalCount.
  - Red absent fill (error_red), positioned after present fill.
  - Glow overlay (40dp wide, gradient from transparent to white to transparent, 12dp radius) sweeps across bar on each mark with 700ms animation, tinted with action color.

**Section 4: A/P Buttons (130dp height)**
- Two large square-ish `MaterialCardView` buttons side by side:
  - **Absent (A)**: 2dp red outline, 20dp corners, transparent background. "A" text (40sp, bold, error_red). Red ripple on touch. Fill overlay (error_red at 0 alpha initially).
  - **Present (P)**: 2dp green outline, 20dp corners, transparent background. "P" text (40sp, bold, success_green). Green ripple on touch. Fill overlay (success_green at 0 alpha initially).
- On tap: fill overlay animates to 0.85 alpha (350ms, AccelerateDecelerateInterpolator), text color changes to white.

**Section 5: Navigation & Save**
- Previous/Next text buttons (secondary color, with back/chevron icons).
- Full-width "Save Attendance" primary button (56dp, 16dp radius).

**Attendance Flow:**
1. User taps P or A.
2. Haptic fires (light for P, heavy for A).
3. Button fill animates (350ms).
4. Student name + card border flash green/red.
5. Progress bar glow sweeps across (700ms).
6. After 200ms delay, advances to next student.
7. When all students marked: completion dialog ("All Students Marked!").
8. Save: creates AttendanceSessionEntity + AttendanceRecordEntity rows, navigates to ReportActivity.

**Toolbar Menu:**
- Sort: "Alphabetical (A-Z)" or "Original Order (As in list)"
- Reset: clears all marks, resets to first student (with confirmation dialog).

**Back Navigation:**
- If marks exist: shows "Discard Attendance?" dialog with marked count summary.
- If no marks: finishes immediately.

### 8.5 Report Activity

**Layout:** `activity_report.xml`
- Toolbar with "Attendance Report" title, back icon.
- `NestedScrollView` with 24dp padding.

**Section 1: Class Info Card**
- Card with className (TitleLarge, primary text) and date (BodyMedium, tertiary text).

**Section 2: Stats Row (3 cards)**
- Three cards side by side with equal weight:
  - **Present card**: success_green_light background, "X" (28sp bold green) + "Present" (12sp green).
  - **Absent card**: error_red_light background, "X" (28sp bold red) + "Absent" (12sp red).
  - **Percentage card**: primary_subtle background, "XX%" (28sp bold blue) + "Rate" (12sp blue).

**Section 3: Detailed Report Card**
- Card with "Detailed Report" title + copy icon button (40dp).
- Report text in monospace font, secondary text color, 4dp line spacing.
- Report format (generated by `ReportViewModel.buildReportText()`):
  ```
  ATTENDANCE REPORT
  ==============================
  Class: Computer Science 4th-A (Data Structures)
  Date: 15 Jan 2024, 10:30 AM

  Summary:
     Present: 28
     Absent: 4
     Total: 32
     Percentage: 87.5%

  ABSENT STUDENTS:
  --------------------
  1. Alice Johnson
  2. Bob Smith
  ...

  PRESENT STUDENTS:
  --------------------
  1. Carol White
  2. David Brown
  ...

  ==============================
  Generated by Roll Call App
  ```

**Section 4: Share Button**
- Centered primary button with ic_share icon, "Share" text, horizontal padding 48dp.

**Numbering modes:**
- Absolute: student position in original class list (e.g., 3, 7, 12).
- Relative: sequential 1, 2, 3... within the present/absent list.

**Report template modes:**
- Both: shows both ABSENT and PRESENT sections.
- Absent Only: shows only ABSENT section.
- Present Only: shows only PRESENT section.

### 8.6 History Fragment (Tab 1)

**Layout:** `fragment_history.xml`
- `LinearLayout` vertical with `background_primary`.

**Filter Section:**
- Card with filter icon + Spinner ("All Classes" or specific class names).

**Session List:**
- `RecyclerView` with `GroupedHistoryAdapter`.

**Empty State:**
- Circular card (80dp) with ic_history icon, "No History" heading, "Take attendance to see history here" subtext.

**GroupedHistoryAdapter:**

Two view types:

1. **DateHeader** (`item_date_header.xml`):
   - Pill-shaped card (background_secondary, 20dp corners, glass_border stroke).
   - Horizontal: date text ("Today" or "15 Jan 2024", LabelLarge bold, primary text) + session count ("2 sessions", LabelSmall, tertiary) + chevron down icon (rotates 180deg when expanded).
   - Click toggles expansion with 200ms rotation animation.
   - "Today" is expanded by default on first load.

2. **Session** (`item_history_session.xml`):
   - `MaterialCardView`: card_background, 16dp corners, glass_border stroke, ripple.
   - Vertical content:
     - Top row: className (TitleMedium, primary) + time ("10:30 AM", LabelSmall, tertiary).
     - Subject text (BodySmall, tertiary).
     - Stats row: green "P: 28" chip + red "A: 4" chip + bold percentage "88%" (TitleMedium, primary).
   - Click -> ReportActivity. Long-press -> delete confirmation.

**Data flow:**
- `HistoryViewModel` loads all sessions via `repository.getAllSessions()`, joins with class info.
- Filter spinner filters by classId (null = all classes).
- Sessions grouped by date (calendar day), sorted descending (newest first).

### 8.7 Settings Fragment (Tab 2)

**Layout:** `fragment_settings.xml`
- Tab bar at top (pill design in card, 3 tabs: General, Reports, About).
- `NestedScrollView` below with tab-specific content containers.

**General Tab:**
- **Dark Mode card**: "Dark Mode" title + "Use dark theme" subtitle + `MaterialSwitch` (checked by default). Note: app is currently locked to dark theme regardless of this setting.
- **Haptic Feedback card**: "Haptic Feedback" title + "Vibration on button presses" subtitle + `MaterialSwitch`.

**Reports Tab:**
- **Report Template card**: Radio group with 3 options: "Show Both (Present & Absent)" (default), "Show Absent Only", "Show Present Only".
- **Numbering Mode card**: Radio group: "Absolute (Position in class list)" (default), "Relative (1, 2, 3...)".

**About Tab:**
- Card with centered content: app icon (80dp, 20dp radius), app name (HeadlineSmall, bold), version ("Version: 2.0"), developer ("Parikshit Singh Bais").

**Behavior:**
- Tab switching shows/hides the 3 containers (generalSettingsContainer, reportsSettingsContainer, aboutSettingsContainer).
- `isInitializing` flag prevents haptic feedback during initial state load from DataStore.
- All changes immediately persisted to DataStore via SettingsViewModel.

---

## 9. Component Patterns

### Empty State Pattern
Every list screen has a consistent empty state:
- Circular card (80-96dp, tertiary background, 50% radius corners) with a relevant icon (ic_class, ic_history).
- Heading text (secondary color, HeadlineSmall or TitleMedium).
- Subtext (tertiary color, BodyMedium).
- Example: "No classes yet" / "Tap + to create your first class".

### Card Pattern
All cards follow:
- `MaterialCardView` with `Widget.RollCall.Card` style.
- 16dp corners, 0dp elevation, 1dp `glass_border` stroke.
- `card_background` (#2C2C2E) fill.
- 16dp internal padding.
- Ripple on clickable cards (`#33FFFFFF`).

### Button Patterns
- **Primary**: Full-width, 56dp height, `primary` (#0A84FF) fill, white text, 16dp corner radius.
- **Text**: `Widget.Material3.Button.TextButton`, secondary text color, optional leading/trailing icon.
- **Tonal**: `Widget.Material3.Button.TonalButton`, used for CSV import.
- **Attendance A/P**: Custom `MaterialCardView` with 2dp colored outline, 20dp corners, 130dp height, 40sp bold single letter.

### Dialog Pattern
All confirmations use `MaterialAlertDialogBuilder`:
- Title: action description ("Delete Class?", "Reset Attendance?").
- Message: consequence explanation with relevant counts.
- Positive button: destructive action ("Delete", "Reset", "Discard", "Save Anyway").
- Negative button: safe action ("Cancel", "Continue", "Continue Taking").

---

## 10. Animations & Micro-Interactions

### Attendance Screen Animations
1. **Button fill**: `ObjectAnimator` on fill overlay alpha (0 -> 0.85, 350ms, AccelerateDecelerateInterpolator).
2. **Name flash**: Student name `TextView` color transitions to green/red instantly.
3. **Card border flash**: `studentCard.strokeColor` changes to match action color.
4. **Progress glow**: White gradient overlay (40dp wide) sweeps across progress bar (translationX animation, 700ms, AccelerateDecelerateInterpolator, alpha fades from 0.5 to 0).
5. **Progress bar width**: Present and absent bar widths animate (200ms) with `setUpdateListener`.

### Toolbar Title Animation
- Fade out (100ms) -> change text -> fade in (150ms) using `ObjectAnimator` on toolbar alpha.

### Tab Switch Animations
- Settings tab chevron: `ObjectAnimator` rotation (0 -> 180deg or vice versa, 200ms).
- Date header chevron: Same rotation animation.

### AnimationUtils (`AnimationUtils.kt`)
Reusable animation helpers:
- `scaleIn(view)`: 0.8 -> 1.0 scale + 0 -> 1 alpha, 300ms.
- `scaleOut(view)`: 1.0 -> 0.8 scale + 1 -> 0 alpha, 200ms.
- `slideInFromRight(view)`: Translation from +width to 0 + fade, 300ms.
- `slideInFromLeft(view)`: Translation from -width to 0 + fade, 300ms.
- `fadeIn(view)`: Alpha 0 -> 1, 300ms default.
- `fadeOut(view)`: Alpha 1 -> 0, 300ms default, sets GONE on end.
- `pulse(view)`: Scale 1.0 -> 1.1 -> 1.0, 100ms each way.
- `shake(view)`: TranslationX oscillation (+10, -10, 0), 50ms each step.

---

## 11. Haptic Feedback System

`HapticUtils` is a Hilt singleton that wraps Android's `Vibrator` / `VibratorManager`. Reads `haptics_enabled` from DataStore to check if haptics are on.

| Method | Vibration Effect | Usage |
|---|---|---|
| `lightTap()` | EFFECT_TICK (10ms) | Navigation taps, tab switches, sort selection, spinner selection |
| `mediumImpact()` | EFFECT_CLICK (20ms) | Long-press menus, FAB tap, class group toggle, delete confirmations |
| `heavyImpact()` | EFFECT_HEAVY_CLICK (30ms) | Absent button press, save button, reset action |
| `successPattern()` | Waveform (0, 50, 100, 50ms) | Attendance completion dialog, save confirmation |
| `errorPattern()` | Waveform (0, 100, 50, 100ms) | Error states |

On API < 29: falls back to `VibrationEffect.createOneShot()` with default amplitude.
On API < 26: falls back to deprecated `vibrator.vibrate(duration)`.

---

## 12. State Management

### Pattern
Every ViewModel exposes a `StateFlow<UiState>` where `UiState` is a data class containing all screen state. Activities/Fragments collect this flow in `lifecycleScope.launch { repeatOnLifecycle(STARTED) { ... } }`.

### MainViewModel State (`MainUiState`)
```kotlin
data class MainUiState(
    classes: List<ClassEntity>,          // All classes from DB
    groupedClasses: List<ClassGroup>,   // Grouped by batchKey for display
    isLoading: Boolean,                 // Show/hide progress bar
    isEmpty: Boolean                    // Show/hide empty state
)
```

### AttendanceViewModel State (`AttendanceUiState`)
```kotlin
data class AttendanceUiState(
    classEntity: ClassEntity?,           // Current class info
    students: List<StudentAttendance>,  // Students with their mark status
    currentIndex: Int,                  // Currently displayed student index
    isLoading: Boolean,                 // Loading state
    isComplete: Boolean,                // All students marked?
    savedSessionId: Long?,              // Set after successful save (triggers navigation)
    presentCount: Int,                  // Running count
    absentCount: Int                    // Running count
)
// Computed: currentStudent, progress, markedCount, allMarked
```

### ReportViewModel State (`ReportUiState`)
```kotlin
data class ReportUiState(
    classDisplayName: String,
    formattedDate: String,
    presentCount: Int,
    absentCount: Int,
    totalCount: Int,
    percentage: Float,
    presentStudents: List<StudentEntity>,
    absentStudents: List<StudentEntity>,
    reportText: String,                 // Pre-formatted monospace report
    isLoading: Boolean
)
```

### HistoryViewModel State
Loads sessions, supports class filtering. Exposes `uiState` with `sessions`, `classes`, `isEmpty`, `isLoading`.

### SettingsViewModel State
Exposes `settings: Flow<UserSettings>` directly from DataStore. Methods: `setTheme()`, `setHapticsEnabled()`, `setNumberingMode()`, `setReportTemplate()`.

---

## 13. Settings & Preferences

Stored in DataStore (`rollcall_settings` preferences file):

| Key | Type | Default | Description |
|---|---|---|---|
| theme | String | "system" | Theme choice (dark/light/system) - currently locked to dark |
| haptics_enabled | Boolean | true | Enable/disable vibration feedback |
| numbering_mode | String | "relative" | Student numbering in reports (absolute/relative) |
| report_template | String | "detailed" | What to show in reports (both/absent_only/present_only) |

---

## 14. File Reference

### Activities
| File | Purpose |
|---|---|
| `ui/main/MainActivity.kt` | Main host: ViewPager2 + BottomNav + FAB |
| `ui/attendance/AttendanceActivity.kt` | Take attendance with A/P buttons |
| `ui/createclass/CreateClassActivity.kt` | Create/edit class with CSV import |
| `ui/report/ReportActivity.kt` | View/share attendance report |
| `ui/history/HistoryActivity.kt` | Standalone history view |
| `ui/settings/SettingsActivity.kt` | Standalone settings view |

### Fragments
| File | Purpose |
|---|---|
| `ui/main/ClassListFragment.kt` | Tab 0: class list with grouping |
| `ui/main/HistoryFragment.kt` | Tab 1: date-grouped history |
| `ui/main/SettingsFragment.kt` | Tab 2: tabbed settings |

### ViewModels
| File | Purpose |
|---|---|
| `ui/main/MainViewModel.kt` | Class loading, grouping, expansion |
| `ui/attendance/AttendanceViewModel.kt` | Marking logic, session saving |
| `ui/report/ReportViewModel.kt` | Report text generation |
| `ui/history/HistoryViewModel.kt` | Session loading, filtering |
| `ui/settings/SettingsViewModel.kt` | DataStore read/write |

### Adapters
| File | Purpose |
|---|---|
| `ui/main/ClassAdapter.kt` | Multi-type: single class card or expandable group |
| `ui/history/HistoryAdapter.kt` | Flat session list |
| `ui/history/GroupedHistoryAdapter.kt` | Date-grouped sessions with expandable headers |

### Data Layer
| File | Purpose |
|---|---|
| `data/local/AppDatabase.kt` | Room DB, version 1, 4 entities |
| `data/local/dao/ClassDao.kt` | Class CRUD queries |
| `data/local/dao/StudentDao.kt` | Student CRUD queries |
| `data/local/dao/AttendanceDao.kt` | Session + record CRUD queries |
| `data/local/entity/ClassEntity.kt` | Class data with computed display names |
| `data/local/entity/StudentEntity.kt` | Student data with FK to class |
| `data/local/entity/AttendanceSessionEntity.kt` | Session snapshot with percentage |
| `data/local/entity/AttendanceRecordEntity.kt` | Individual P/A record |
| `data/repository/AttendanceRepository.kt` | Singleton wrapping all DAOs |
| `data/repository/SettingsRepository.kt` | Singleton wrapping DataStore |

### DI Modules
| File | Purpose |
|---|---|
| `di/DatabaseModule.kt` | Provides AppDatabase + DAOs |
| `di/DataStoreModule.kt` | Provides DataStore<Preferences> |

### Utilities
| File | Purpose |
|---|---|
| `util/AnimationUtils.kt` | Reusable view animations (scale, slide, fade, pulse, shake) |
| `util/HapticUtils.kt` | Haptic feedback with 5 intensity levels |
| `util/CsvParser.kt` | CSV file parsing for student import |

### Layouts
| File | Used By |
|---|---|
| `activity_main.xml` | MainActivity |
| `activity_attendance.xml` | AttendanceActivity |
| `activity_create_class.xml` | CreateClassActivity |
| `activity_report.xml` | ReportActivity |
| `activity_history.xml` | HistoryActivity |
| `activity_settings.xml` | SettingsActivity |
| `fragment_class_list.xml` | ClassListFragment |
| `fragment_history.xml` | HistoryFragment |
| `fragment_settings.xml` | SettingsFragment |
| `item_class.xml` | ClassAdapter (single class) |
| `item_class_group.xml` | ClassAdapter (expandable group) |
| `item_history_session.xml` | GroupedHistoryAdapter / HistoryAdapter |
| `item_date_header.xml` | GroupedHistoryAdapter (date headers) |

### Resources
| File | Purpose |
|---|---|
| `values/colors.xml` | Full color palette (30+ colors) |
| `values/themes.xml` | Material 3 dark theme + custom card/FAB/bottom nav styles |
| `values/strings.xml` | All user-facing strings |
| `values/dimens.xml` | Card stroke width |
| `color/bottom_nav_colors.xml` | State list: active (primary) / inactive (60% white) |
| `menu/bottom_navigation_menu.xml` | 3 bottom nav items |
| `menu/attendance_menu.xml` | Sort + Reset overflow menu |
| `menu/class_options_menu.xml` | Edit + Delete context menu |
| `menu/main_menu.xml` | Settings overflow (legacy) |
| `anim/slide_in_left.xml` | Slide-in animation |
| `anim/slide_in_right.xml` | Slide-in animation |
| `anim/slide_out_left.xml` | Slide-out animation |
| `anim/slide_out_right.xml` | Slide-out animation |
| `anim/scale_in.xml` | Scale-in animation |
| `anim/scale_out.xml` | Scale-out animation |
| `drawable/stat_chip_background_green.xml` | Green stat chip pill |
| `drawable/stat_chip_background_red.xml` | Red stat chip pill |
| `drawable/chip_background_green.xml` | Green "P: X" chip |
| `drawable/chip_background_red.xml` | Red "A: X" chip |
| `drawable/progress_track_background.xml` | Progress bar track (tertiary, 6dp radius) |
| `drawable/progress_present_fill.xml` | Progress bar present fill (green, 6dp radius) |
| `drawable/progress_absent_fill.xml` | Progress bar absent fill (red, 6dp radius) |
| `drawable/progress_glow.xml` | Progress bar glow (white gradient, 6dp radius) |
| `drawable/tab_item_background.xml` | Settings tab selector (blue pill / transparent) |
| `drawable/tab_background.xml` | Tab background |
| `drawable/circle_background.xml` | Circle background for group icons |
| `drawable/circle_green.xml` | Green dot for present indicator |
| `drawable/circle_red.xml` | Red dot for absent indicator |
| `drawable/pulse_ring.xml` | Pulse ring animation drawable |
| `drawable/ic_*.xml` | Various vector icons (add, back, check, class, close, copy, etc.) |

---

## Known Issues / Notes for Future Work

1. **Dark mode switch exists but is non-functional**: The app theme is hardcoded to `Theme.Material3.Dark.NoActionBar`. The switch in settings persists the preference but does not trigger an `AppCompatDelegate` theme change.
2. **Two settings entry points**: Both `SettingsFragment` (tab 2) and `SettingsActivity` (standalone) exist with overlapping content. The fragment has more complete settings (haptics, dark mode, report template, numbering). The activity has report template, numbering, haptics, and about info but lacks the tabbed structure.
3. **No onboarding**: First-time users see an empty state but no guided walkthrough.
4. **No last-session indicator on class cards**: Class list doesn't show when attendance was last taken or overall attendance percentage.
5. **CSV import only**: No manual student entry UI (one-by-one). Students can only be added via CSV file or by editing the database directly.
6. **No attendance editing after save**: Once saved, attendance records cannot be modified from the UI.
7. **No data export**: Reports can be shared/copied as text but not exported as CSV/PDF.
8. **No attendance trends/analytics**: Individual student attendance history per class is not surfaced in the UI despite the data being available in the database.
