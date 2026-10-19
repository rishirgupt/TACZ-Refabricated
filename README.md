# Timeless and Classics Zero: Refabricated – Minecraft 26.2 port (Fabric), source code only

> **This is an unofficial community port of an unofficial port.**
> - **Timeless and Classics Zero (TaCZ)** was created by **MCModderAnchor** and the TaCZ team.
> - **TaCZ Refabricated** (the Fabric port for 1.21.1) was made by **Sh1roCu**.
> - This repository only adapts that Fabric port to **Minecraft 26.2**. The guns, animations, models, sounds and almost all of the code are
>   theirs. **Please support the original authors.**

## Original projects

| | |
|---|---|
| TaCZ (original mod, Forge/NeoForge) | <https://github.com/MCModderAnchor/TACZ> |
| TaCZ Refabricated (Fabric port this is based on) | <https://github.com/Sh1roCu/TACZ-Refabricated> |
| CurseForge | <https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero> |
| Pack upgrader | <https://www.curseforge.com/minecraft/mc-mods/tacz-pack-upgrader> |
| Required library | [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) |

This repository is a GitHub fork of TaCZ Refabricated. The port lives on the `26.2-fabric` branch.

## Licensing, and why there is no download

TaCZ Refabricated is licensed in two parts (see [`LICENSE`](LICENSE)):

- **Code: GNU GPL 3.0.** The code in this repository is distributed under the GPL 3.0, like the original.
- **Assets: CC BY-NC-ND 4.0** (textures, models, animations, sounds, icons and the bundled data). *No derivatives* means they may not be modified
  and redistributed.

Because the 26.2 build bundles the assets, **this repository contains no assets and there is no release jar.** Only source code and the shader I wrote
(`assets/tacz/shaders/core/scope_lens.fsh`) are included. To build the mod yourself, copy the `assets/`, `data/`, `icon.png` and `logo.png` of the
original TaCZ Refabricated 1.21.1 release into `src/main/resources/`, and place the libraries listed in `build.gradle` (Forge Config API Port,
Cardinal Components and similar) in `libs/`.

## What this port changes

- Fabric Loom 1.18 / Java 25 with the unobfuscated Mojang names and Fabric API 0.161.0+26.2.
- GUI, rendering, networking, item, data component and resource code ported to the 26.2 APIs.
- Item model types and special renderers for guns, ammo, attachments and the workbenches.
- A real magnified scope: the scope lens is rendered in a separate pass, both aimed and at the hip, and works with shaders (tested with Iris and Photon).
- Fixes to the workbench screens (resource counts and labels).

## Reporting problems

Problems that only happen on 26.2 belong in this repository's issues. Please do not report them to MCModderAnchor or Sh1roCu.

## Credits

- **MCModderAnchor and the TaCZ team** – the original Timeless and Classics Zero.
- **Sh1roCu** – TaCZ Refabricated.
- Everyone who made guns, attachments and packs for TaCZ.

## License

Code: GPL 3.0 (see [`LICENSE`](LICENSE)). Assets are not part of this repository; they are CC BY-NC-ND 4.0 and belong to their authors.
