<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Changelog

## [2.0.1] - Feb 23, 2024

### Fixed

- Settings freeze fixed  

## [2.0.0] - Feb 22, 2024

### Changed

- Major code baseline overhaul

### Fixed

- Resolved Plugin SDK used deprecations

### Added

- Automatic settings migration from version 1.X
- Separate colors definition for light and dark themes
- Global configuration (defaults for every new and unconfigured project)
- Project specific configuration overriding the global one.

### Deprecated

- v1.x plugin configuration (automigrates on discovery)

## [1.5.2] - Sept 9, 2021

### Added

- Plugin icon added

## [1.5.1] - Aug 18, 2021

### Added

- Plugin icon added

### Fixed

- Plugin version (1.5.0) requires build 211.* or
  older [tobszarny/ActiveTabHighlighterPlugin#21](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/21)

## [1.5.0] - Jul 30, 2021

### Added

- IDEA Platform 2021.2 compatibility

### Changed

- Plugin project converted to Gradle.

## [1.4.0] - Apr 12, 2018

### Added

- Global configuration [tobszarny/ActiveTabHighlighterPlugin#7](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/7)
- Reset configuration changes [tobszarny/ActiveTabHighlighterPlugin#8](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/8)

### Changed

- Color picker offers the same user experience, as ones seen in IntelliJ code style
  settings [tobszarny/ActiveTabHighlighterPlugin#9](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/9)
- Improved readability of error messages.

### Fixed

- Active tab color reloads instantly after color change, no need to switch the tab anymore to update the highlight
  color [tobszarny/ActiveTabHighlighterPlugin#10](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/10)

## [1.3.1] - Sep 19, 2017

### Fixed

- Fixed issues with "distraction free" and "presentation
  mode" [tobszarny/ActiveTabHighlighterPlugin#6](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/6)

## [1.3.0] - Mar 15, 2017

### Fixed

- Fixed IDEA Platform 2017.X
  compatibility [tobszarny/ActiveTabHighlighterPlugin#4](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/4)

## [1.2.0] - Mar 8, 2017

### Added

- Now customizable and with persistent
  settings [tobszarny/ActiveTabHighlighterPlugin#2](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/2)
- Branch 145.0+ supporting added [tobszarny/ActiveTabHighlighterPlugin#3](https://github.com/tobszarny/ActiveTabHighlighterPlugin/issues/3)

## [1.1.0] - Jan 19, 2017

### Changed

- Minor refactoring

## [1.0.0] - Jan 19, 2017

### Added

- Initial release.
