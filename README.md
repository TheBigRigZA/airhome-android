# AirHome for Android TV

An Android TV application that bridges AirPlay with Google Home and other networked speakers, allowing Apple devices to stream audio to any compatible speaker on your home network.

## Features

- **AirPlay Bridge**: Enables Apple devices to stream audio to Google Home, networked speakers, and Android TV
- **Universal Compatibility**: Works with iPhones, iPads, Macs, and other AirPlay-capable devices
- **Easy Setup**: Simple configuration with minimal setup required
- **Background Service**: Runs silently in the background with automatic startup option
- **TV-Optimized Interface**: Designed specifically for Android TV with D-pad navigation

## How It Works

AirHome creates a virtual AirPlay receiver on your network. When your Apple device connects to this receiver, AirHome captures the audio stream and forwards it to your selected output device (Google Home, networked speaker, or the Android TV itself).

This bridges the gap between Apple's ecosystem and Google/Android devices, allowing for seamless audio streaming across platforms.

## Technical Details

- Based on the open-source AirConnect project
- Implements Apple's AirPlay protocol (focusing on audio streaming)
- Uses mDNS/Bonjour for service discovery
- Supports background operation with minimal resource usage

## Development Status

This application is currently under development. Contributions are welcome!

## License

This project is released under the MIT License - see the LICENSE file for details.

## Acknowledgements

- Inspired by the [AirConnect](https://github.com/philippe44/AirConnect) project by philippe44
- Built with Android TV development best practices
