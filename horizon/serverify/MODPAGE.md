# Serverify for Horizon: the registry page

What to enter in the registry's submit form (playhorizon.dev/mods/submit). The description below is Markdown.

| Field | Value |
| --- | --- |
| Kind | Native mod |
| File | `dist/serverify-0.1.0.hnmod` (22,867 bytes, SHA-256 `f9ad0777f8fc7ea532f799da8a9eb72df2348c8d6bb5dc104f81b6ab47209501`) |
| Id | `serverify` |
| Version | `0.1.0` |
| Minecraft version | 26.3 |
| Name | Serverify |
| Authors | Louchatfroff |
| Licence | GPL-3.0-only |
| Source | https://github.com/Louchatfroff/serverify (folder `horizon/serverify`) |
| Icon | `docs/logo.png` (512 x 512 PNG, under 256 KiB) |

## One-line summary

Find Minecraft servers and add them to your Multiplayer list, from a page in your web browser.

## What changed (this version)

First release for Horizon, ported from the Java Edition mod.

## Description

Serverify is a server browser. Press **F8** (or type `/serverify web`, or press the button on the mod's settings page)
and your web browser opens a page with a search box, a category menu and cards showing each server's picture, address,
player count and country. Press **Add** and the server goes to the top of your Multiplayer list. Then join it from
Horizon's own Multiplayer screen.

It is the Horizon version of [Serverify](https://github.com/Louchatfroff/serverify), the server browser for Fabric,
Quilt, Forge and NeoForge, by the same author.

### What it does

- Opens a server browser page in your web browser: search, 22 categories, an offline-mode filter, and an **Add** button.
- Adds servers to `servers.dat` in the game folder, with the name cleaned of colour codes.
- Adds chat commands: `/serverify web`, `list [category]`, `search <words>`, `next`, `prev`, `add <number>` and `categories`.
- Adds a row in Options > Mods with a rebindable key (F8 by default) and a settings page with two buttons: open the
  browser, and **Offline-mode servers only** (on by default).

Horizon cannot join servers that check Minecraft accounts yet, which is most public servers, so by default the page
shows servers that accept offline-mode players. Turn the filter off to see all of them.

### What it contacts

- **Modrinth.** The game asks `api.modrinth.com` for the server list and `cdn.modrinth.com` for server pictures, only
  when you look for servers.
- **Three listing sites.** With the offline-mode filter off (and for top-mc-servers.net's list of cracked servers when it
  is on), the game fetches public pages of `top-mc-servers.net`, `minecraftservers.org` and `minecraft-mp.com`, and
  their server pictures. The game builds those addresses itself. The page reads the fetched pages with the browser's own
  HTML parser.
- **A page on your computer.** The first time you open the page, the mod starts a small web server on `127.0.0.1` (this
  computer only, never the network) with a random secret in its address, made for each session. The page talks only to
  that server. A request with another host name, another origin or no secret is refused. The server can show the list,
  send a picture and add one server to your Multiplayer list.
- It sends no data about you, and makes no request at any other time.

### What it does not do

- It does not run any code from the sites it reads, and it does not send your data anywhere.
- It does not change anything but `servers.dat` (one entry at a time) and its own settings in
  `horizon-mods/.horizon-mod-data/serverify/`.
- It does not draw screens or buttons in the game: Horizon's hook points have none, so the screen is your web browser.
- It cannot make Horizon join servers that check Minecraft accounts.
- Adding a server rewrites `servers.dat`, which drops the hidden direct-join entries Horizon keeps. Your saved servers
  stay.

### Install

Install it from the launcher's Mods page, then Settings > Native mods (experimental) > Build, and play the Native mods
instance. Its documentation is at https://serverify.louchat.site/docs/horizon.

Native mods are not sandboxed. The source is public and licensed GPL-3.0-only:
https://github.com/Louchatfroff/serverify/tree/main/horizon/serverify
