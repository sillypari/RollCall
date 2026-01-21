<p align="center">
  <img src="PeskyLogo.png" alt="Pesky Logo" width="120" height="120">
</p>

<h1 align="center">Pesky</h1>

<p align="center">
  <strong>A Modern, Offline-First Password Manager for Android</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-blue.svg" alt="Min SDK">
  <img src="https://img.shields.io/badge/Language-Kotlin-purple.svg" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg" alt="UI Framework">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

---

## About Pesky

Pesky is a **completely offline**, privacy-focused password manager built with modern Android technologies. Your passwords never leave your device – no cloud sync, no servers, no tracking. Just you and your encrypted vault.

### Why Pesky?

- **100% Offline** - Your data stays on your device. Period.
- **Military-Grade Encryption** - AES-256 encryption with Argon2 key derivation
- **Beautiful UI** - Clean, modern Material 3 design with smooth animations
- **Fast & Lightweight** - No bloat, no unnecessary permissions
- **Free & Open Source** - No ads, no subscriptions, no data collection

---

## Features

### Password Management
- **Secure Vault** - Store unlimited passwords with strong encryption
- **Smart Search** - Instantly find entries by typing keywords like `weak`, `duplicate`, `expiring`, `favorites`, `recent`
- **Groups/Categories** - Organize passwords into custom groups (Social, Banking, Work, etc.)
- **Favorites** - Quick access to frequently used passwords
- **Notes** - Add secure notes to any entry
- **Password History** - Track previous passwords for each entry

### Security
- **AES-256 Encryption** - Industry-standard encryption for your vault
- **Argon2 Key Derivation** - Memory-hard function resistant to brute-force attacks
- **Master Password** - Single strong password protects everything
- **PIN Unlock** - Quick 6-digit PIN for faster access (after initial master password verification)
- **Biometric Authentication** - Fingerprint unlock support
- **Auto-Lock** - Automatically lock vault when switching apps
- **Screenshot Protection** - Prevent screenshots in sensitive screens
- **Clipboard Auto-Clear** - Automatically clears copied passwords (configurable: 10-90 seconds)

### Password Generator
- **Customizable Length** - Generate passwords from 4 to 64 characters
- **Character Options** - Include/exclude:
  - Uppercase letters (A-Z)
  - Lowercase letters (a-z)
  - Numbers (0-9)
  - Special characters (!@#$%^&*)
- **Easy-to-Read Mode** - Exclude ambiguous characters (0, O, l, 1, etc.)
- **Password Strength Meter** - Real-time strength analysis

### Password Health Analysis
- **Strength Analysis** - Visual strength indicator for each password
- **Weak Password Detection** - Identify passwords that need improvement
- **Duplicate Detection** - Find reused passwords across entries
- **Expiry Tracking** - Set and monitor password expiration dates
- **Security Dashboard** - Overview of your vault's security health

### Backup & Restore
- **Encrypted Backups** - Export your vault as an encrypted `.pesky` file
- **Easy Restore** - Import backups with your master password
- **No Cloud Required** - Store backups wherever you want (USB, SD card, etc.)

### User Experience
- **Material 3 Design** - Modern, clean interface following Google's design guidelines
- **Animated Icons** - Smooth, delightful animations throughout the app
- **Dark Theme** - Easy on the eyes, optimized for AMOLED screens
- **Quick Actions** - Long-press for fast copy username/password
- **Haptic Feedback** - Tactile responses for important actions

---

## Technical Details

### Built With
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt
- **Async Operations**: Kotlin Coroutines & Flow
- **Data Storage**: Encrypted file storage with DataStore for preferences
- **Background Tasks**: WorkManager for reliable clipboard clearing

### Encryption Stack
```
Master Password
      |
   Argon2id (memory-hard KDF)
      |
   256-bit Key
      |
   AES-256-GCM Encryption
      |
   Encrypted .pesky Database
```

### Permissions
| Permission | Usage |
|------------|-------|
| `USE_BIOMETRIC` | Fingerprint/Face unlock |
| `VIBRATE` | Haptic feedback |

**No internet permission** - Pesky cannot and will never access the network.

---

## Screenshots

*Coming soon*

---

## Getting Started

### Requirements
- Android 8.0 (API 26) or higher
- ~10 MB storage space

### Installation

#### Option 1: Build from Source
```bash
# Clone the repository
git clone https://github.com/sillypari/Pesky.git

# Open in Android Studio
# Build and run on your device
```

#### Option 2: Download APK
Download the latest release from the [Releases](https://github.com/sillypari/Pesky/releases) page.

### First Time Setup
1. Launch Pesky
2. Create a strong master password (this is the only password you need to remember!)
3. Optionally set up a 6-digit PIN for quick unlock
4. Start adding your passwords

---

## Security Best Practices

1. **Use a Strong Master Password** - At least 12 characters with mixed case, numbers, and symbols
2. **Enable Biometric Unlock** - For convenience without compromising security
3. **Regular Backups** - Export your vault periodically and store securely
4. **Use Generated Passwords** - Let Pesky create unique passwords for each account
5. **Check Password Health** - Regularly review weak and duplicate passwords

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- [Bouncy Castle](https://www.bouncycastle.org/) - Cryptography library
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system

---

## Support

If you encounter any issues or have suggestions:
- Open an [Issue](https://github.com/sillypari/Pesky/issues)
- Start a [Discussion](https://github.com/sillypari/Pesky/discussions)

---

<p align="center">
  <strong>Made with care for privacy</strong>
</p>

<p align="center">
  <em>Your passwords, your device, your control.</em>
</p>
