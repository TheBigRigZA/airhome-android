# Contributing to AirHome for Android TV

Thank you for your interest in contributing to AirHome for Android TV! This document provides guidelines and instructions for contributing to the project.

## Development Setup

1. Clone the repository:
   ```
   git clone https://github.com/TheBigRigZA/airhome-android.git
   ```

2. Open the project in Android Studio.

3. Ensure you have the following installed:
   - Android Studio (latest version recommended)
   - Android SDK (API level 21+)
   - Android TV emulator or device for testing

## Project Structure

The project follows a standard Android application structure:

- `app/src/main/java/com/mediabox/airhome/` - Java source code
  - `audio/` - Audio processing components
  - `service/` - Background service components
  - `ui/` - User interface components
  - `util/` - Utility classes
  - `receiver/` - Broadcast receivers
- `app/src/main/res/` - Resources
  - `layout/` - XML layout files
  - `values/` - String, color, and style resources
  - `drawable/` - Drawable resources

## Code Guidelines

1. **Coding Style**
   - Follow standard Java naming conventions
   - Use meaningful variable and method names
   - Add comments for complex logic
   - Keep methods focused on a single responsibility

2. **Commits**
   - Write clear, concise commit messages
   - Reference issue numbers when applicable
   - Keep commits focused on a single change

3. **Testing**
   - Test your changes on both emulator and real devices if possible
   - Ensure your changes don't break existing functionality
   - Add appropriate unit tests for new functionality

## Development Workflow

1. **Create a Branch**
   - Create a branch for your feature or bug fix
   - Use a descriptive name for your branch (e.g., `feature/add-speaker-selection` or `fix/audio-drops`)

2. **Make Changes**
   - Implement your changes following the code guidelines
   - Keep changes focused on the specific feature or bug

3. **Submit a Pull Request**
   - Push your branch to your fork
   - Create a pull request to the main repository
   - Describe your changes in the pull request description
   - Reference any related issues

## Key Areas for Contribution

1. **AirPlay Protocol**
   - Improving AirPlay protocol compatibility
   - Adding support for AirPlay 2 features
   - Enhancing metadata handling

2. **Audio Processing**
   - Improving audio quality and latency
   - Adding support for more audio formats
   - Enhancing transcoding capabilities

3. **User Interface**
   - Improving the Android TV interface
   - Adding visualizations for audio playback
   - Enhancing accessibility

4. **Device Support**
   - Adding support for more speaker types
   - Improving discovery of networked speakers
   - Enhancing Chromecast integration

## Resources

- [AirPlay Protocol Documentation](https://nto.github.io/AirPlay.html)
- [Android TV Developer Guidelines](https://developer.android.com/tv)
- [Android Audio Processing](https://developer.android.com/guide/topics/media/audio)

## Contact

If you have any questions or need assistance, please:
- Create an issue in the GitHub repository
- Reach out to the maintainers

Thank you for contributing to AirHome for Android TV!
