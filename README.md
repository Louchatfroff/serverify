# Serverify

[![Build](https://github.com/Louchatfroff/serverify/actions/workflows/build.yml/badge.svg)](https://github.com/Louchatfroff/serverify/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Louchatfroff/serverify)](https://github.com/Louchatfroff/serverify/releases/latest)
[![License: GPL v3](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/minecraft-26.1%20%7C%2026.2%20%7C%2026.3-62b47a)

A client-side [Fabric](https://fabricmc.net) mod that adds a fast, native **server browser** to Minecraft's Multiplayer screen. Search, filter, check live status and join, without leaving the game.

**Website and downloads: <https://serverify.louchat.site/>**

![The Serverify server browser](docs/screenshots/browser.png)

## Features

- **Browser:** two columns of server cards with a colored banner, icon, name, version, country, player count and description. Endless scrolling, plus back and forward navigation between pages.
- **Categories and search:** 25 category checkboxes (Survival, Skyblock, Prison, PvP, Towny, Cobblemon and more), search with a short delay while you type, a sort menu (popular, newest, players, name), and an online-only filter.
- **Live status:** the server page uses Minecraft's own server ping for real latency, player count, version and the live colored MOTD, and refreshes every 30 to 60 seconds.
- **Join or save:** connect immediately, or add the server to your Multiplayer list in one click.
- **Favorites:** a local list stored as JSON in `config/serverify/`.
- **Sources:** [minecraft-list.info](https://minecraft-list.info) loads first, then other sites. The same server never appears twice.
- **Settings:** enable or disable each source and set the refresh interval, through [Mod Menu](https://modrinth.com/mod/modmenu).

| Server page | Live status | Settings |
| --- | --- | --- |
| ![Server page](docs/screenshots/detail.png) | ![Live status](docs/screenshots/status.png) | ![Settings](docs/screenshots/settings.png) |

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for your Minecraft version.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) for the same version.
3. Download the Serverify jar for your version from the [latest release](https://github.com/Louchatfroff/serverify/releases/latest).
4. Put all three jars in `.minecraft/mods`.
5. Start the game, open **Multiplayer** and press the **Serverify** button.

| Minecraft | Jar |
| --- | --- |
| 26.1, 26.1.1, 26.1.2 | `serverify-mc26.1-<version>.jar` |
| 26.2 | `serverify-mc26.2-<version>.jar` |
| 26.3 | `serverify-mc26.3-<version>.jar` |

Requires Java 25, which the official launcher installs for these versions. Mod Menu is optional.

## Build from source

You need JDK 25.

```bash
./gradlew build
```

The jar is written to `build/libs/`. Build for another Minecraft version by passing the values from [`.github/versions.json`](.github/versions.json):

```bash
./gradlew build -Pminecraft_version=26.3 -Pfabric_api_version=0.162.0+26.3 -Puniversalcraft_version=26.3 "-Pminecraft_range=>=26.3 <26.4"
```

Run the game with the mod, or run the scripted in-game tests that save screenshots. `runClientGameTest` uses the project classes, `runProductionClientGameTest` launches the real shaded jar:

```bash
./gradlew runClient
./gradlew runClientGameTest
./gradlew runProductionClientGameTest
```

## How it works

```
src/main/java/provider/    one ServerSource per site and a registry that loads them in order and removes duplicates
src/main/java/network/     async HTTP with timeout and retry
src/main/java/favorites/   local JSON storage
src/main/java/config/      settings stored in config/serverify.json
src/main/java/client/      entrypoint, Mod Menu hook, live ping and the Multiplayer integration
src/main/kotlin/gui/       the interface, built with Elementa
```

The interface is written in Kotlin on [Elementa](https://github.com/EssentialGG/Elementa) and [UniversalCraft](https://github.com/EssentialGG/UniversalCraft), which are bundled and relocated inside the jar. CI builds every Minecraft version listed in `versions.json`, and a weekly job checks that the newest Minecraft release still builds.

### Adding a Minecraft version

Add an entry to [`.github/versions.json`](.github/versions.json) with the Minecraft version, the matching Fabric API version, the UniversalCraft version and the version range. CI builds and releases it automatically.

## Releases

Pushing a tag such as `v0.2.0` builds a jar for every version in `versions.json` and publishes them to GitHub Releases.

## Notes

Server listings come from third-party sites. Some are read from public pages, so listings can change or stop working without notice. Serverify is not affiliated with Mojang, Microsoft or any listed server.

## Credits and inspiration

The interface is heavily inspired by [Resourcify](https://github.com/DeDiamondPro/Resourcify) by [DeDiamondPro](https://github.com/DeDiamondPro/): the navigation bar, the category sidebar, the search bar, the sort menu, the two-column cards and the project page layout all follow its design, adapted here for servers. Parts of its source code (the screen frame, navigation bar, drop-down, check boxes, card layout, color palette and a few layout helpers) were adapted under the GNU Lesser General Public License v3. Those files keep their original copyright notice and say that they were modified. Resourcify's image files were not used.

Serverify is built on these libraries, used under their own licenses:

- [Elementa](https://github.com/DeDiamondPro/Elementa) (DeDiamondPro's build of Essential's UI library) and [UniversalCraft](https://github.com/EssentialGG/UniversalCraft), LGPL-3.0, bundled in the jar. Elementa also brings two small images, a loading spinner and a scrollbar texture.
- [jsoup](https://jsoup.org), MIT license, bundled in the jar.
- [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin), Apache-2.0, installed separately.

## AI use and assets

Serverify's source code, tests, documentation and website text were written with the help of an AI coding assistant ([Claude](https://www.anthropic.com/claude), by Anthropic).

No assets are AI-generated or taken from other people's work. Serverify adds no images, textures, sounds, models or fonts of its own. The only images in the jar come from the Elementa library (see above). The screenshots in this repository are real captures of the game running the mod. They show Minecraft's interface and server names and icons from public listings, which belong to their owners. The website uses the open-licensed fonts Inter, Open Sans and Inconsolata. The test fixtures are short captured samples of public listing data, used only to test the parsers.

## License

Serverify is free software, copyright (C) 2026 [Louchatfroff](https://github.com/Louchatfroff). You can redistribute it and modify it under the terms of the [GNU General Public License, version 3](LICENSE). It comes with no warranty. The files adapted from Resourcify stay under the LGPL-3.0, as stated in their headers.

## A note to DeDiamondPro

I encourage [DeDiamondPro](https://github.com/DeDiamondPro/), the creator of Resourcify, to contact me if he does not like me using his UI design. I will redesign it myself when asked.
