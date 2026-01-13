# HyIrcBridge

A Hytale server plugin that bridges game chat with an IRC channel.

## Features

- **Chat Synchronization**: Bidirectional chat between Hytale and IRC.
- **Connection Events**: Announcements on IRC when players join or leave the server.
- **Kill Feed**: Death messages are relayed to IRC.
- **Configurable**: Easily configure IRC server, channel, and bot details.

## Installation

1. Download the latest `.jar` from the [Releases](https://github.com/Stonebound/hyircbridge/releases) page.
2. Place the jar in your Hytale server's `mods` directory.
3. Restart the server.
4. Configure the plugin in `mods/Stonebound_HyIrcBridge/HyIrcBridge.json`.

## Configuration

The configuration file is located at `run/mods/Stonebound_HyIrcBridge/HyIrcBridge.json` and supports the following options:

- `host`: IRC server address.
- `port`: IRC server port.
- `channel`: IRC channel to join.
- `nick`: Bot's nickname.
- `ssl`: Whether to use SSL for the connection.

## Development

This project uses Gradle. To build the project:

```bash
./gradlew build
```

The resulting jar will be in `build/libs/`.

### Dependencies

Note: This mod requires the `HytaleServer.jar` to be present for compilation. The `build.gradle` is configured to look for it in the default Hytale installation path.
