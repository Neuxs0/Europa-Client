# Commands

The default command prefix is shown as `#` here, but the client uses the configured command prefix at runtime. Commands can also be sent with a leading slash when the message after `/` matches a client command prefix.

## Base Client

### Misc

#### Help
- Usage: `#help`
- Aliases: `#h`, `#?`
- Description: Displays help information for all registered Europa Client commands.

#### Say
- Usage: `#say <message>`
- Aliases: None
- Description: Sends a public chat message.

#### Type
- Usage: `#type`
- Aliases: `#clientType`
- Description: Shows which Europa Client variant is running.

#### Version
- Usage: `#version`
- Aliases: `#clientVersion`
- Description: Shows the running Europa Client version.

#### Profile
- Usage:
    - `#profile list`
    - `#profile save <name> [all|modules|settings]`
    - `#profile load <name> [all|modules|settings]`
    - `#profile delete <name>`
- Aliases: `#profiles`
- Description: Manages profiles for the active variant.
- Sections:
    - `all`: modules and module settings.
    - `modules`: module enabled states.
    - `settings`, `module-settings`, `modules-settings`: module keybinds, custom settings, and HUD layout data.

### Utilities

#### Disconnect
- Usage: `#disconnect`
- Aliases: `#dc`, `#quit`, `#exit`
- Description: Disconnects from a server or singleplayer world and returns to the main menu.

#### Quit Game
- Usage: `#quitGame`
- Aliases: `#gameQuit`, `#closeGame`, `#exitGame`
- Description: Saves/disconnects, returns to the main menu, then exits the game.

#### Player List
- Usage: `#playerList`
- Aliases: `#pl`
- Description: Shows online players on the server.

### Utility Modules

#### Fullbright
- Usage: `#fullbright`
- Aliases: `#fb`
- Description: Toggles Fullbright.
- Settings: [Fullbright](./MODULES.md#fullbright)

#### NoFog
- Usage: `#nofog`
- Aliases: `#nf`
- Description: Toggles NoFog.
- Settings: [NoFog](./MODULES.md#nofog)

#### PacketInspector
- Usage: `#packetInspector`
- Aliases: None
- Description: Toggles PacketInspector, which outputs sent and received packets to the console.
- Settings: [PacketInspector](./MODULES.md#packetinspector)

#### Freecam
- Usage:
    - `#freecam`
    - `#freecam set speed <value>`
    - `#freecam set horizontalMovement <true|false>`
    - `#freecam set playerInteraction <true|false>`
    - `#freecam set disableOnDamage <true|false>`
- Aliases: `#fc`
- Description: Toggles Freecam or updates Freecam settings.
- Settings: [Freecam](./MODULES.md#freecam)

### Base Modules Without Commands

#### Zoom
- Command: None currently registered.
- Description: Zoom is registered as a Base client module and can be controlled through module settings/UI/keybinds, but there is no chat command in `ClientCommandRegistry`.
- Settings: [Zoom](./MODULES.md#zoom)

#### HUD Modules
- Command: None currently registered.
- Description: HUD modules are controlled through the HUD editor, module settings/UI, and keybinds where applicable.
- Settings: [HUD Modules](./MODULES.md#hud-modules)

## Cheat Client

The Cheat client includes every Base client command plus the following commands.

### Cheat Modules

#### NoClip
- Usage:
    - `#noclip`
    - `#noclip set speed <value>`
- Aliases: `#nc`
- Description: Toggles NoClip or sets NoClip speed.
- Settings: [NoClip](./MODULES.md#noclip)

#### NoFall
- Usage: `#nofall`
- Aliases: None
- Description: Toggles NoFall.
- Settings: [NoFall](./MODULES.md#nofall)
- Note: The source attempts to register `#nf`, but `#nf` is already used by NoFog, so it is not available for NoFall.

#### Fly
- Usage:
    - `#fly`
    - `#fly set speed <value>`
- Aliases: `#f`
- Description: Toggles Fly or sets Fly speed.
- Settings: [Fly](./MODULES.md#fly)

#### Click-TP
- Usage:
    - `#clicktp`
    - `#clicktp set button <left|middle|right>`
- Aliases: `#ctp`
- Description: Toggles Click-TP or changes the teleport mouse button.
- Settings: [Click-TP](./MODULES.md#click-tp)

#### Speed
- Usage:
    - `#speed`
    - `#speed set <speed|jetpackSpeed> <value>`
- Aliases: `#s`
- Description: Toggles Speed or changes Speed settings.
- Settings: [Speed](./MODULES.md#speed)

#### Reach
- Usage:
    - `#reach`
    - `#reach set distance <value>`
- Aliases: None
- Description: Toggles Reach or sets reach distance.
- Settings: [Reach](./MODULES.md#reach)

#### Xray
- Usage: `#xray`
- Aliases: `#xr`
- Description: Toggles Xray.
- Settings: [Xray](./MODULES.md#xray)

#### Tracers
- Usage: `#tracers`
- Aliases: `#tr`
- Description: Toggles Tracers.
- Settings: [Tracers](./MODULES.md#tracers)

#### LiquidWalk
- Usage:
    - `#liquidwalk`
    - `#liquidwalk set <water|lava> <true|false>`
- Aliases: `#lw`
- Description: Toggles LiquidWalk or changes which liquids can be walked on.
- Settings: [LiquidWalk](./MODULES.md#liquidwalk)
- Boolean values accepted by this command: `true`, `false`, `on`, `off`, `yes`, `no`, `1`, `0`.

#### InstaBreak
- Usage: `#instabreak`
- Aliases: `#ib`
- Description: Toggles InstaBreak.
- Settings: [InstaBreak](./MODULES.md#instabreak)

#### JetpackHeight
- Usage: `#jetpackheight`
- Aliases: `#jh`
- Description: Toggles unlimited jetpack height.
- Settings: [JetpackHeight](./MODULES.md#jetpackheight)

### Movement Clips

#### HClip
- Usage: `#hclip <distance>`
- Aliases: `#hc`
- Description: Clips horizontally by the provided distance in the current horizontal view direction.

#### VClip
- Usage: `#vclip <distance>`
- Aliases: `#vc`
- Description: Clips vertically by the provided distance.

### Cheat Modules Without Commands

#### ESP
- Command: None currently registered.
- Description: ESP is registered as a Cheat client module and can be controlled through module settings/UI/keybinds, but there is no chat command in `CheatVariant`.
- Settings: [ESP](./MODULES.md#esp)
