# Minecraft Werewolf

A custom multiplayer **Werewolf social deduction game built as a PaperMC plugin**, designed to automate full live games inside Minecraft.

The plugin manages the game from start to finish, including role assignment, timed day/night phases, discussion, voting and trials, nighttime abilities, player elimination, and win conditions. It was built for live community events with 20+ players.

## Features

* Automated **day, dusk, night, and voting phases**
* Randomized assignment of Werewolf, Town, and special roles
* Role-specific nighttime abilities and interactions
* Deterministic resolution of interdependent nighttime actions
* In-game voting and vote visualization
* Automated player elimination and win-condition checking
* Per-player role information and inventory management
* Custom housing and player-location mechanics
* Team and identity visibility controls
* Integration with proximity voice chat
* Spectator/dead-player handling

## Technical Highlights

### Role Action Resolution

Nighttime abilities can interact with one another, making execution order important. Role actions are assigned explicit priorities so dependent actions resolve deterministically rather than according to player input or event timing.

This allows interactions such as protection, investigation, attacks, and other role effects to resolve consistently each night.

### Game State Management

The plugin maintains game state across a complete match, including:

* active and eliminated players
* role assignments
* Werewolf membership
* game phases
* voting state
* nighttime targets and effects
* win conditions

Timed transitions automatically advance the game through its phases while coordinating world state, player inventories, UI elements, and role behavior.

### Multiplayer Systems

The plugin also implements several systems around the core game logic, including:

* scoreboard-based voting displays
* controlled inventories
* player name and identity visibility
* skin masking
* custom housing regions
* voice-chat integration
* timed UI and boss-bar feedback

## Tech Stack

* **Java**
* **PaperMC API**
* **Gradle**
* **Adventure API**
* **PacketEvents**
* **Simple Voice Chat API**

## Project Structure

The game is split so the Werewolf rules never touch Minecraft:

```text
core/   pure game logic, no Paper dependency, unit tested
  Game, PhaseController     players, phases, votes, trials, win checks
  role/                     Role base class + one class per role, Roles registry
  night/                    night actions resolved in priority order
  view/Views                what each player should look like for the current state
app/    the Paper plugin
  WerewolfPlugin            wires services, listeners and commands
  sync/PlayerSync           applies Views to Minecraft (the "update player" step)
  game/                     GameEffects (one-shot effects), clock, placement, guillotine, world
  listener/                 join/quit, combat, chat, protection, trial buttons
  housing/ scene/ display/ inventory/ voice/ commands/
```

The flow is always: change game state in `core`, then call `PlayerSync.syncAll()`
(the game does this for you through `GameListener.onStateChanged`). `PlayerSync`
works out each player's visibility, name tag, ghost state, skin, tab entry,
judgment buttons and the house doors from `Views`, and only touches what changed.

Adding things:

* **A role:** subclass `core/role/Role`, override `onNight` (schedule actions with an
  `ActionPriority`) or `onDusk`, and register it in `Roles`.
* **A phase effect:** add a case to `GameEffects.onPhaseStarted` or `onTick`.
* **Something players should look like:** add a field to `PlayerView`, compute it
  in `Views.of`, and apply it in `PlayerSync`.

Build with `./gradlew build`; run the game logic tests with `./gradlew :core:test`.
PacketEvents is a required server plugin and Simple Voice Chat is optional.

## Status

This project is actively developed and has been used to run live multiplayer Werewolf sessions. Additional roles, game mechanics, and quality-of-life improvements are planned.
