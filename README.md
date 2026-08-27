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

```text
app/src/main/
├── java/
│   └── .../werewolf/
│       ├── WerewolfPlugin.java
│       ├── Game.java
│       ├── Role.java
│       ├── WWPlayer.java
│       ├── HousingManager.java
│       ├── VotingDisplayManager.java
│       ├── WerewolfVoiceChat.java
│       └── ...
└── resources/
    └── plugin.yml
```

## Status

This project is actively developed and has been used to run live multiplayer Werewolf sessions. Additional roles, game mechanics, and quality-of-life improvements are planned.
