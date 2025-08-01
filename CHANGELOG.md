# Changelog

## [0.0.32-v3] - 0.0.31-v3

### Summary
This release upgrades the project to SDK 35, introduces edge-to-edge UI support, updates dependencies, and resolves theme compatibility issues.

### Added
- Edge-to-edge UI support for Android 35.

### Changed
- **Android SDK:** Bumped `compileSdk` and `targetSdk` to **35**.
- **Library dependencies:** Upgraded `chat-core` to `3.0.0-beta.33`.
- **Themes:** Simplified `AppTheme` by removing redundant attributes.

### Fixed
- Updated activity themes and exported flags in `AndroidManifest.xml`.
- Fixed edge-to-edge UI rendering issues on Android 35 devices.

### How to upgrade
- Update your project's `compileSdk` and `targetSdk` to 35.
- Sync project to fetch updated dependencies.

---

For a full list of changes, see the commit history between versions `0.0.32-v3` and `0.0.33-v3`.


## [0.0.31-v3] - 0.0.30-v3

### Summary
This release includes major upgrades to the build system, dependencies, and several code improvements for compatibility and stability.

### Changed
- **Build system:**
  - Upgraded Gradle plugin from `4.1.3` to `8.3.1` and Gradle wrapper from `6.5` to `8.4`.
  - Increased `kotlin_version` from `1.6.0` to `1.7.21`.
  - Increased `org.gradle.jvmargs` from `-Xmx1536m` to `-Xmx4608m`.
  - Added new Gradle properties: `android.defaults.buildfeatures.buildconfig`, `android.nonTransitiveRClass`, and `android.nonFinalResIds`.
- **Library dependencies:**
  - Updated `chat-core` from `3.0.0-beta.28` to `3.0.0-beta.31`.
  - Updated `jupuk` from `1.4.2` (previously `1.5.2`) to `1.5.9`.
  - Added `de.hdodenhof:circleimageview:3.1.0`.
  - Updated test dependencies and removed `com.mikhaellopez:circularimageview`.
- **Android SDK:**
  - Increased `compileSdkVersion` and `targetSdkVersion` from `32` to `34`.
  - Increased `minSdkVersion` from `16` to `21`.
- **Manifest:**
  - Added `tools` namespace in `AndroidManifest.xml` for the widget module.

### Fixed
- Fixed APK build issues.
- Refactored code for compatibility with AGP 8.
- Improved event handling in `ChatRoomActivity` and `ChatRoomPresenter`.

### UI
- Replaced `CircularImageView` with `CircleImageView` in chat room layout.

### How to upgrade
- Ensure your project uses Gradle 8.4 and Android Gradle Plugin 8.3.1 or later.
- Update your dependencies as listed above.

---

For a full list of changes, see the commit history between versions `0.0.30-v3` and `0.0.31-v3`.
