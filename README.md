[![Build](https://github.com/sifrex19-blip/skyblock-qol-fabric/actions/workflows/build.yml/badge.svg)](https://github.com/sifrex19-blip/skyblock-qol-fabric/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/sifrex19-blip/skyblock-qol-fabric?label=release)](https://github.com/sifrex19-blip/skyblock-qol-fabric/releases)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

# Skyblock QoL — Holographic 3D Interface

A **client-side** Fabric mod for Hypixel Skyblock that replaces ordinary mod menus with
**floating holographic panels rendered as real geometry inside the Minecraft world**.
Panels have physical depth and thickness, glow, animate, and are operated with your
crosshair — walk around them, look at them from any angle, and click their 3D buttons.

> **Philosophy:** information and convenience only. No automation, no macros, no packets —
> everything is rendered and resolved on your own client, so it is multiplayer-safe.

---

## Features

### 🌐 Holographic 3D interface
- **Real world-space geometry** — panels are submitted into the world render pipeline
  (`LevelRenderEvents.COLLECT_SUBMITS`), not drawn as a 2D screen overlay. They occlude and
  are occluded by world geometry, have extruded side walls, and hold up under perspective.
- **Crosshair interaction** — aim to hover components (glow + sound feedback), left-click to
  press, toggle switches, and drag sliders. Clicks only win when the panel is closer than
  the vanilla raycast target, so you can still mine the block behind a panel.
- **Animations** — eased open/close with scaling, pulsing glow, hover lift on buttons,
  sliding toggle knobs, and a slowly sweeping scanline across the glass.
- **Multiple simultaneous panels** — a console that follows you and a world-anchored stats
  panel; panels face the player, render back-to-front, and never fight over draw order.
- **Modular by design** — the renderer never touches UI logic and components never touch
  the GPU: both sides meet at a tiny `HoloDraw` contract.

### 📊 Existing QoL tools
- **Skill tracker + HUD** — live XP, rate (XP/h), level progress and ETA per skill, with an
  animated in-world skill panel while you gain XP.
- **Timer board** — start 5/30-minute timers from the console; they tick down and expire.
- **Chat rules** — keyword rules (e.g. `sea creature`, `slayer`) that count matches from
  chat, with a master switch on the console.
- **Stats panel** — live readout of the current skill, level progress bar, XP rate,
  time-to-level and wheat/hour.

---

## Controls

| Input | Action |
|-------|--------|
| **H** | Toggle the holographic **QoL Console** (timers, chat-rule switch, opacity & glow) |
| **O** | Toggle the holographic **Skill Stats** panel (world-anchored where you opened it) |
| **Crosshair + Left click** | Interact with buttons, toggles and sliders |
| **Hold Left click on a slider** | Drag the value |
| Look-away / close panel | Panels animate out and stop rendering |

Both keybinds are rebindable in *Options → Controls → Skyblock QoL*.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) ≥ 0.19.5 for Minecraft **26.3**
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) into your `mods/` folder
3. Drop `skyblock-qol-0.1.0.jar` (from the [latest release](https://github.com/sifrex19-blip/skyblock-qol-fabric/releases)) into `mods/`
4. Launch — press **H** in a world to open the console

---

## Building

Requires **JDK 25**.

```bash
./gradlew build        # Windows: gradlew.bat build
```

The mod jar lands in `build/libs/skyblock-qol-0.1.0.jar`.

> The Gradle Wrapper is pinned to **9.9.2** because Fabric Loom `1.20.0-alpha.31`
> requires Gradle ≥ 9.9.0. GitHub Actions builds with Temurin 25 and the wrapper,
> so CI matches local builds exactly.

---

## Project structure

```
src/client/java/com/skyblockqol/
├── SkyblockQolClient.java      # entrypoint: keybinds, tick loop, renderer registration
├── SkillTracker.java           # XP parsing, rates, ETA
├── TimerBoard.java             # countdown timers
├── ChatRules.java              # keyword rules from chat
├── holo/                       # holographic interface (client-side, no packets)
│   ├── HoloManager.java        # panel lifecycle, hover/click/drag interaction
│   ├── HoloRaycast.java        # analytic crosshair ray vs. panel planes
│   ├── HoloStyle.java          # palette, sizes, timing constants
│   ├── HoloDraw.java           # the contract between UI logic and the renderer
│   ├── render/                 # HoloRenderer (world submission), HoloGeometry, HoloText
│   └── ui/                     # HoloPanel + components (button, toggle, slider,
│                               #   label, progress bar), ConsolePanel, StatsPanel
└── mixin/                      # HudMixin, ChatMixin, MinecraftMixin (input routing)
```

**Adding a new panel** is three steps: subclass `HoloPanel`, lay out components in the
constructor, and register a keybind that calls `HOLO.toggle...()`. Adding a new component
type means implementing `render(HoloDraw, HoloFrame)` — geometry and text both come for free.

---

## Roadmap ideas

- [ ] Holo **toast notifications** when a chat rule matches (animated cards flying in)
- [ ] **Theme presets** (cyan / amber / magenta) and persisted JSON config
- [ ] **Panel profiles** — save & restore layouts (mining, farming, dungeon…)
- [ ] **World waypoint markers** with distance labels
- [ ] **XP/hour bar chart** widget on the stats panel
- [ ] **ModMenu** integration for an in-game settings screen
- [ ] More languages (the keybind strings are already externalized)

---

## License

[MIT](LICENSE)
