# SimpleAttendance - Lightweight Offline Attendance App

A simple, offline-first Android attendance management application designed for teachers and educators. Built with Java and SQLite for reliability and ease of use.

## Features

### 📚 Class Management
- Create and manage multiple classes (Branch, Semester, Section)
- Import student lists via CSV files
- Support for roll numbers and names

### 👥 Student Management
- CSV import with flexible formats:
  - `Roll Number, Student Name`
  - `Student Name` only
- Automatic header detection and parsing
- Manual student entry capability

### ✅ Attendance Taking
- Simple Present/Absent (P/A) interface
- Large, color-coded buttons for easy marking
- Forward/backward navigation for corrections
- Real-time student counter (current/total)
- Subject selection for each session

### 📊 Sorting & Organization
- Toggle between CSV order and alphabetical sorting
- Maintain original import order when needed
- Easy navigation through student lists

### 📈 Report Generation
- Comprehensive attendance reports
- Present/Absent counts and percentages
- List of absent students
- Date and time stamps
- Easy sharing via email, messaging, or other apps

### 💾 Offline-First Design
- Complete SQLite database storage
- No internet connection required
- All data stored locally on device
- Fast performance and reliability

## Technical Specifications

- **Platform**: Android (API Level 21+)
- **Language**: Java
- **Database**: SQLite
- **Architecture**: MVVM pattern with Repository
- **UI**: Material Design components
- **File Support**: CSV import/export

## CSV Format Examples

### Option 1: Roll Number and Name
```csv
101,John Doe
102,Jane Smith
103,Bob Johnson
```

### Option 2: Name Only
```csv
John Doe
Jane Smith
Bob Johnson
```

*Note: Headers are automatically detected and skipped*

## Database Schema

The app uses a normalized SQLite database with the following main tables:
- `classes` - Store class information
- `students` - Store student details linked to classes
- `subjects` - Predefined and custom subjects
- `attendance_sessions` - Attendance session metadata
- `attendance_records` - Individual attendance records

## Installation & Setup

1. Clone or download this project
2. Open in Android Studio
3. Sync Gradle files (Android Studio will prompt this automatically)
4. Build and run on your Android device or emulator
5. Grant file access permissions for CSV import functionality

### App Icon
The app includes a custom clipboard-style icon with checkmarks that represents the attendance functionality:
- **Design**: Blue circular background with white clipboard and green checkmarks
- **Format**: Vector drawables for crisp display on all screen densities
- **Adaptive**: Supports Android 8.0+ adaptive icon system
- **Fallback**: Compatible with older Android versions

The icon is automatically configured and will appear in:
- App launcher
- Recent apps screen
- Settings and app info screens

## Usage Workflow

1. **Create Class**: Add branch, semester, and section details
2. **Import Students**: Select CSV file with student data (optional)
3. **Take Attendance**: Select class → Choose subject → Mark attendance
4. **Generate Report**: View summary and share results

## Key Benefits

- **Lightweight**: Minimal storage footprint and system requirements
- **Fast**: Offline operation with instant response times
- **Simple**: Intuitive interface designed for quick attendance taking
- **Reliable**: Local storage ensures data integrity
- **Flexible**: Support for various class structures and CSV formats
- **Shareable**: Easy report generation and distribution

## Development

This project follows Android development best practices:
- Material Design UI guidelines
- SQLite database management
- Proper lifecycle management
- Error handling and user feedback
- File permission handling

## Requirements

- Android 5.0 (API level 21) or higher
- Storage permission for CSV file imports
- Approximately 10-20MB storage space

---

**SimpleAttendance** - Making attendance management simple, fast, and reliable.