# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


### Technical Details
- Java version: 21 (source and target)
- Minecraft API: 1.21
- Server compatibility: Bukkit 1.21.x, Spigot 1.21.x, Paper 1.21.x, Purpur 1.21.x and Folia 1.21.x
- Build system: Maven

---

## [2.1.3-ALPHA] - 2026-02-03

- Converted the project to Kotlin
- Fixed some deprecated entries
- Fixed a NullPointer issue
- Fixed public API exposure
- Fixed an unused variable
- Fixed a generic exception handling issue
- Removed a unused import
- Cleaned up the code a bit
- Added automatic config update for convenience at future updates

## [2.0.2-SNAPSHOT] - 2026-02-01

- Removed unused command "/heartdrops help".
- Fixed some minor issues with permission system.

## [2.0.1-SNAPSHOT] - 2026-02-01

- Initial release of version 2 of HeartDrops for Minecraft 1.21.11+ (took only about ten years for that)
- Multi-platform support: Bukkit, Paper, Spigot, Purpur, Folia
- Fixed issues with the pickup sound not working
- Fixed problems with ProtocolLib integration and displaying the heart particles
- Fixed an issue with heart containers floating in the air after killing flying mobs.