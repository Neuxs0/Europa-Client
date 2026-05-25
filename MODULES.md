# Modules

Europa Client has shared modules in both variants and extra cheat modules in the Cheat variant.

All modules persist these base fields:
- `enabled` `<boolean>`
- `keybind` `<key combo string>`
- `notifications` `<boolean>`: show enabled/disabled chat messages for the module. Default: `true` for most modules, `false` for `PacketInspector`, `Zoom`, and HUD modules.

HUD modules also persist HUD editor data:
- `hud.x` `<float>`
- `hud.y` `<float>`
- `hud.scale` `<float>`: clamped from `0.5` to `4.0`
- `hud.locked` `<boolean>`

## Base Client

### Utility Modules

#### Fullbright
- Description: Remeshes loaded chunks so the world renders bright.
- Settings: None beyond base module fields.

#### NoFog
- Description: Removes fog rendering.
- Settings: None beyond base module fields.

#### PacketInspector
- Description: Logs sent and received packets to the console.
- Settings: None beyond base module fields.
- Notes: Module notifications default to `false`.

#### Zoom
- Description: Temporarily narrows the FOV for zooming. Mouse wheel changes the zoom amount while active.
- Settings:
    - `showHand` `<boolean>`
        - Default: `false`
        - Description: Show the player hand while zoomed.
    - `showHotbar` `<boolean>`
        - Default: `true`
        - Description: Show the hotbar while zoomed.
    - `smoothCamera` `<boolean>`
        - Default: `true`
        - Description: Enable smooth camera behavior while zoomed.
    - `saveZoom` `<boolean>`
        - Default: `false`
        - Description: Save the current zoom amount when zoom is disabled and restore it next time.
    - `equalZoom` `<boolean>`
        - Default: `true`
        - Description: Use equal-ratio zoom steps instead of fixed numeric steps.
    - `savedZoomAmount` `<float>`
        - Default: `3.0`
        - Description: Persisted zoom amount used when `saveZoom` is enabled.
- Notes: Module notifications default to `false`.

#### Freecam
- Description: Detaches the camera from the player. In the Base client, freecam collides with blocks; in the Cheat client, freecam can phase through blocks.
- Settings:
    - `speed` `<float>`
        - Default: `8.0`
        - Minimum: `1.0`
        - Maximum: `30.0`
    - `horizontalMovement` `<boolean>`
        - Default: `true`
        - Description: Move forward and backward along the horizontal plane instead of camera pitch.
    - `playerInteraction` `<boolean>`
        - Default: `true`
        - Description: Use the player's view for block and entity interactions while freecam is enabled.
    - `disableOnDamage` `<boolean>`
        - Default: `true`
        - Description: Disable freecam when the local player takes damage.

### HUD Modules

#### Vanilla Hotbar
- Description: Keeps the vanilla hotbar in the HUD editor layout.
- Default enabled: `true`
- Settings: HUD editor data only.
- Notes: Cannot be hidden in the HUD editor.

#### Vanilla Health Bar
- Description: Keeps the vanilla health bar in the HUD editor layout.
- Default enabled: `true`
- Settings: HUD editor data only.
- Notes: Cannot be hidden in the HUD editor.

#### FPS Counter
- Description: Displays FPS on the HUD.
- Default enabled: `false`
- Settings:
    - `advanced` `<boolean>`
        - Default: `false`
        - Description: Show FPS, average FPS, 1% low, and RAM usage.

#### TPS Counter
- Description: Displays server/local TPS on the HUD.
- Default enabled: `false`
- Settings:
    - `showMspt` `<boolean>`
        - Default: `true`
        - Description: Show milliseconds per tick under the TPS counter.

#### Ping Counter
- Description: Displays ping on the HUD.
- Default enabled: `false`
- Settings: HUD editor data only.

#### Velocity HUD
- Description: Displays local player speed in blocks per second.
- Default enabled: `false`
- Settings: HUD editor data only.

#### Connected Server
- Description: Displays the connected server on the HUD.
- Default enabled: `false`
- Settings: HUD editor data only.

## Cheat Client

The Cheat client includes every Base client module plus the following cheat modules.

### NoClip
- Description: Lets the local player move through blocks.
- Settings:
    - `speed` `<float>`
        - Default: `1.0`
        - Minimum: `1.0`
        - Maximum: `4.0`

### Fly
- Description: Lets the local player fly using normal movement controls, with jump/crouch for vertical movement.
- Settings:
    - `speed` `<float>`
        - Default: `8.0`
        - Minimum: `1.0`
        - Maximum: `20.0`

### Click-TP
- Description: Teleports the local player to the selected block when the configured mouse button is clicked.
- Settings:
    - `button` `<Left|Middle|Right>`
        - Default: `Middle`
        - Description: Mouse button used to teleport.

### Speed
- Description: Multiplies local player movement speed.
- Settings:
    - `speed` `<float>`
        - Default: `1.5`
        - Minimum: `1.0`
        - Maximum: `6.0`
    - `jetpackSpeed` `<float>`
        - Default: `1.5`
        - Minimum: `1.0`
        - Maximum: `6.0`
        - Description: Movement multiplier while the vanilla jetpack is active.

### Reach
- Description: Extends block/entity raycast reach.
- Settings:
    - `distance` `<float>`
        - Default: `6.0`
        - Minimum: `1.0`
        - Maximum: `24.0`

### Xray
- Description: Enables xray rendering for blocks.
- Settings: None beyond base module fields.

### ESP
- Description: Draws boxes around player targets and, optionally, non-player entities.
- Settings:
    - `threeDimensional` `<boolean>`
        - Default: `false`
        - Description: Draw target hitboxes in world space and ignore block depth.
    - `targetUser` `<boolean>`
        - Default: `false`
        - Description: Include your own player entity.
    - `targetEntities` `<boolean>`
        - Default: `false`
        - Description: Include non-player entities.

### Tracers
- Description: Draws lines from the center of the screen to player targets and, optionally, non-player entities.
- Settings:
    - `targetUser` `<boolean>`
        - Default: `false`
        - Description: Include your own player entity.
    - `targetEntities` `<boolean>`
        - Default: `false`
        - Description: Include non-player entities.

### NoFall
- Description: Prevents fall damage behavior by marking movement packets as on-ground while enabled.
- Settings: None beyond base module fields.

### LiquidWalk
- Description: Lets the local player walk on configured liquid blocks.
- Settings:
    - `water` `<boolean>`
        - Default: `true`
        - Description: Walk on water blocks.
    - `lava` `<boolean>`
        - Default: `true`
        - Description: Walk on lava blocks.

### InstaBreak
- Description: Breaks blocks instantly as if in creative mode.
- Settings: None beyond base module fields.

### JetpackHeight
- Description: Removes the vanilla jetpack height allowance limit.
- Settings: None beyond base module fields.
