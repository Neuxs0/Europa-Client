# Modules
## Full Client
### Fullbright
- Description: Makes everything bright
- Settings: None
### Freecam
- Description: Detaches the camera from the player. In the Cheat client, freecam can clip through blocks.
- Settings:
    - speed \<float>
        - Default: 8.0
        - Minimum: 1.0
        - Maximum: 30.0
    - horizontalMovement \<boolean>
        - Default: true
        - Description: Moves forward and backward along the horizontal plane instead of camera pitch
    - playerInteraction \<boolean>
        - Default: true
        - Description: Use the player's view for block and entity interactions while freecam is enabled
    - disableOnDamage \<boolean>
        - Default: true
        - Description: Disable freecam when the local player takes damage
### No-Clip
- Description: Become a ghost, though one that can die again
- Settings:
    - speed \<float>
        - Default: 1.0
        - Minimum: 0.1
        - Maximum: 10.0
### Click-TP
- Description: Teleports you to the block you click while the module is enabled
- Settings:
    - button \<left|middle|right>
        - Default: middle
        - Description: Mouse button used to teleport
### Speed
- Description: Makes you go vroom
- Settings:
    - speed \<float>
        - Default: 1.5
        - Minimum: 0.1
        - Maximum: 10.0
### Reach
- Description: Slenderman?!?!?
- Settings:
    - distance \<float>
        - Default: 6.0
        - Minimum: 1.0
        - Maximum: 3.4028235e38
### Tracers
- Description: Draws lines from the bottom of the screen to targets
- Settings:
    - targetUser \<boolean>
        - Default: false
        - Description: Include your own player entity
    - targetEntities \<boolean>
        - Default: false
        - Description: Include non-player entities
### LiquidWalk
- Description: Lets you walk on liquid blocks
- Settings:
    - water \<boolean>
        - Default: true
        - Description: Walk on water blocks
    - lava \<boolean>
        - Default: true
        - Description: Walk on lava blocks
### InstaBreak
- Description: Instantly breaks blocks as if you are in creative mode
- Settings: None

## No-Cheat Client
### Fullbright
- Description: Makes everything bright
- Settings: None
### Freecam
- Description: Detaches the camera from the player without clipping through blocks.
- Settings:
    - speed \<float>
        - Default: 8.0
        - Minimum: 1.0
        - Maximum: 30.0
    - horizontalMovement \<boolean>
        - Default: true
        - Description: Moves forward and backward along the horizontal plane instead of camera pitch
    - playerInteraction \<boolean>
        - Default: true
        - Description: Use the player's view for block and entity interactions while freecam is enabled
    - disableOnDamage \<boolean>
        - Default: true
        - Description: Disable freecam when the local player takes damage
