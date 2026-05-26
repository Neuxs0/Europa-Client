# v2.0.0
Release time: 17:00 PDT, May 25, 2026
### Features
- Added an in-game Client GUI with Home, Utilities, Cheats, UI, Profiles, and Settings pages
- Added client profiles for saving, loading, and deleting per-variant module and settings setups
- Added persistent settings for modules, keybinds, GUI state, command prefix, HUD layout, and profile data
- Added configurable keybindings, including combo keybind support and a configurable client menu keybind
- Added a HUD editor with draggable, scalable HUD elements, snapping controls, and movable vanilla HUD modules
- Added FPS, TPS, ping, velocity, and connected-server HUD modules
- Added optional advanced FPS stats for average FPS, 1% lows, and RAM usage
- Added optional MSPT display to the TPS HUD
- Added Packet Inspector, No-Fog, Freecam, and Zoom utility modules
- Added HClip and VClip commands
- Added Fly, Xray, ESP, Tracers, No-Fall, Liquid Walk, Insta-Break, Click-TP, and Jetpack Height cheat modules
- Added jetpack speed control to the Speed cheat
- Added entity targeting controls to ESP and Tracers
### Fixes
- Fixed fullbright desync between module state and rendering state
- Improved fullbright lighting for chunks, entities, and item renders
### Misc
- Updated to Cosmic Reach Alpha v0.5.19
- Updated to require Puzzle Loader Cosmic v1.6.0-alpha
- Reworked the project into shared main code with Base and Cheat variant source sets
- Replaced the old multi-project Quilt/Puzzle layout with a Jigsaw-based Puzzle Loader build
- Added Base and Cheat jar build tasks from the shared source layout
- Added proper client icons, including a 256x scaled icon
- Refactored and cleaned up command, module, settings, rendering, and UI code
- Added custom UI rendering helpers for text, buttons, dropdowns, sliders, toggles, scrollbars, blur, and SDF shapes
- Added compatibility guards and smoke tests for command, module, and setting behavior

# v1.2.1
Release time: 16:00 PDT, March 16, 2025
### Fixes
- Fixed fullbright for Cosmic Reach Alpha v0.4.4
### Misc
- Updated to Cosmic Reach Alpha v0.4.4
- Updated to require Puzzle Loader v2.3.8

# v1.2.0
Release time: 19:00 PDT, March 15, 2025
### Fixes
- Fixed keybinds activating in chat
- Fix the Client's name not correctly showing in ModMenu or Puzzle Loader's Mod Menu
### Commands
- Client type command (#type, #clientType)
- Version command (#version, #clientVersion)
### Cheats
- Added Speed cheat
- Added Reach cheat
### Misc
- Updated to Cosmic Reach Alpha v0.4.3
- A no-cheat version
- A minor rework on commands centered around a module (#\<module> set \<setting> \<value> instead of #set\<module>\<setting> \<value>)
- Added an icon for ModMenu and Puzzle Loader

# v1.1.0
Release time: 16:00 PDT, March 10, 2025
### Fixes
- Fixed removal of no-clip on respawn
### Commands
- Disconnect command (#disconnect, #dc, #quit, #exit)
- Quit Game command (#quitGame, #closeGame, #exitGame)
- Fullbright command (#fullbright, #fb)
- Player List command (#playerList, #pl)
### Misc
- Puzzle Loader support
- Better client chat logging

# v1.0.0
Release time: 05:30 PDT, March 9, 2025
### Commands
- Help command (#help, #h, #?)
- NoClip command (#noclip, #nc)
- Set NoClip Speed command (#setNoClipSpeed, #sncs, #setncspeed)
- Say command (#say)
### Cheats
- No-clip cheat
