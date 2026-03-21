# JustEnoughFakepixel — Feature List

Use `/jef` to open the mod config in game.

<details open><summary>

## General

</summary>

### Damage Splashes
+ Option to hide crit damage nametags (✧ stars)
+ Option to hide gray and fire-aspect (non-crit) damage numbers

### Enchant Parser (beta)
+ Ability to change color of enchants (supports chorma)
+ Three layout modes: Normal (2 per line), Compress (pack to fit), Expand (one per line with descriptions)

### Gyrokinetic Wand Helper
+ Shows the area of effect ring when holding the Gyrokinetic Wand
    + Color changes to indicate cooldown state
+ Cooldown timer overlay showing remaining cooldown duration
    + Option to show the timer even when the wand is not held

### Tooltip Tweaks
+ Roman Numerals — converts Roman numerals to integers in tooltips and the tab list
+ Skyblock ID — shows the internal Skyblock item ID at the bottom of item tooltips
+ Missing Enchants — hold Shift on an enchanted item to see all enchants that could be applied but aren't

### Visual Tweaks
+ Disable Enchant Glint — removes the enchant glint from items
+ Prevent Cursor Reset — stops the mouse cursor jumping back to center when opening GUIs
+ Brewing Stand Highlighter — highlights brewing stands when they finish brewing

</details>

<details open><summary>

## Scoreboard

</summary>

+ Custom Scoreboard — (beta)
    + Drag-and-drop line ordering, including: Server, Season, Time, Profile Type, Island, Location, Purse, Bank, Bits, Gems, Active Event, Cookie Buff, Power, Fetchur item, Slayer quest, and empty separator lines
    + Adjustable background color (with alpha), corner radius, and scale
    + Option to hide when Tab is held

</details>

<details open><summary>

## Misc

</summary>

### Performance HUD
+ An overlay that displays FPS, server TPS, and current ping

### Search Bar
+ Adds a search bar to supported inventory GUIs
+ Highlights matching items (configurable)

### Current Pet Overlay
+ Shows your active pet

### Item Pickup Log
+ Shows a live HUD list of recently picked up or dropped items

### Inventory Buttons
+ Adds clickable command shortcut buttons on any inventory GUI
+ open editor with: (`/jefbuttons`)
    + Clipboard import/export for sharing button layouts as base64

### Item Stack Tips
+ Shows enchantment book level as stack size
+ Shows Catacombs floor number as stack size

### Skill XP Display
+ Hold Shift on a skill item to see the total XP remaining to max level

### Visual Tweaks
+ No Item Swap Animation — removes the lowering animation when switching hotbar slots
+ Show Own Nametag — makes your own nametag visible in third-person view
+ Disable Entity Fire — hides the fire overlay rendered on burning entities

</details>

<details open><summary>

## Mining

</summary>

### Fetchur Overlay
+ Shows today's Fetchur item on screen while in SkyBlock

### Gemstone Powder Tracker
+ Tracks all drops from Crystal Hollows powder chests in a customizable overlay
+ Tracks: chests opened (with /h rate), gemstone powder (with /h rate), diamond essence, gold essence, oil barrels, ascension ropes, wishing compasses, jungle hearts, enchanted hard stone mined (with /h rate and compact count), all 12 gemstone types displayed as flawless–fine–flawed–rough,
+ Reads 2x Powder active status and time remaining directly from the tab list
+ Drag-and-drop display line ordering
+ Toggleable via `/pdt toggle` or a configurable keybind — shows `[Paused]` in the title when paused.
+ Reset via `/pdt reset` or the config Reset button

</details>

<details open><summary>

## Diana

</summary>

+ Diana stat tracking — records playtime, burrow digs, mob spawns, inquisitor kills, loot share, and rare drops
+ toggle tracking with `/diana toggle`, reset with `/diana reset`
+ Party command `!help`

### Event Overlay
+ Shows playtime, burrows per hour, mob counts, inquisitor chance, and loot share info as an overlay

### Loot Overlay
+ Shows total chimeras, rare drops, and coin-equivalent loot

### Inquisitor HP Overlay
+  HP Overlay for the nearest active Minos Inquisitor

### Diana Mob HP Overlay
+  HP Overlay for the nearest Diana mob (only own)

</details>

<details open><summary>

## Dungeons

</summary>

### Blood Mob Highlight
+ Highlights blood room mobs with a colored box or outline (configurable)

### Dungeon Phase timer Overlay
+ Shows phase timers during dungeon runs
+ Records personal best times per floor and displays end-of-run stats in chat
+ Party command `!pb`

</details>

<details open><summary>

## Farming

</summary>

### Mouse Lock
+ Locks yaw and pitch so the camera can't move while farming
+ Toggle via a configurable keybind or with `/lockmouse`

</details>

<details open><summary>

## Waypoints

</summary>

+ Create and manage named waypoint groups via the in-game GUI (keybind or `/waypoint`)
+ Renders an ESP box and tracer line to the next active waypoint in loaded group
+  distance labels above each waypoint (name + distance)
+ Auto-advance — automatically moves to the next waypoint when you get within a configurable range and stay for a configurable delay
+ Setup mode to position all waypoints in a group individually
+ colors (box, tracer, label, distance label) are configurable

</details>

<details open><summary>

## Commands

</summary>

+ `/jef [category]` — opens the JEF config, optionally jumping to a category
+ `/diana <reset|toggle>` — resets or pauses Diana tracking
+ `/pdt <reset|toggle>` (`/powdertracker`) — resets or pauses the powder tracker
+ `/lockmouse` — toggles mouse lock
+ `/waypoint` — opens the waypoint group manager
+ `/jefbuttons` — opens the inventory button editor

</details>