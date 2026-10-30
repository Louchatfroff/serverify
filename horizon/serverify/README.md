# Serverify for Horizon (native mod)

Find Minecraft servers and add them to the Multiplayer list. It is a port of
[Serverify](https://github.com/Louchatfroff/serverify), the server browser mod for Fabric, Quilt, Forge and NeoForge, to
[Horizon](https://playhorizon.dev). Same author, same licence (GPL-3.0-only).

## What it does

**The server browser page.** Press the key chosen in Options > Mods (F8 by default, on any screen, the title screen and
Multiplayer included), type `/serverify web`, or use the **Open the server browser** button on the mod's settings page
(Options > Mods > Serverify > Settings). Your web browser opens a page with a search box, a category menu and cards
with the server's picture, address, player count and country. **Add** puts a server in your Multiplayer list.

**Chat commands**, for when you do not want to leave the game:

| Command | What it does |
| --- | --- |
| `/serverify` | Shows the help |
| `/serverify web` | Opens the server browser page |
| `/serverify list [category]` | The most played servers, optionally of one category |
| `/serverify search <words>` | Search servers by name |
| `/serverify next`, `/serverify prev` | The next or previous page of ten |
| `/serverify add <number>` | Adds a listed server to Multiplayer (`servers.dat`) |
| `/serverify categories` | Lists the categories |

The settings page also has **Offline-mode servers only** (on by default). Horizon cannot join servers that check
Minecraft accounts yet, so the list shows the servers that accept offline-mode players until you turn it off. With the
filter off, the page also lists servers from top-mc-servers.net, minecraftservers.org and minecraft-mp.com.

## What it contacts

- **Modrinth.** The game, never the page, asks `api.modrinth.com` for the list of Modrinth server projects (two requests
  per page: the search, then the servers' addresses and player counts) and `cdn.modrinth.com` for a server's picture. It
  does so only when you look for servers.
- **Three listing sites.** When the offline-mode filter is off (and, for top-mc-servers.net's list of cracked servers,
  when it is on), the game also fetches one public page of `top-mc-servers.net`, `minecraftservers.org` and
  `minecraft-mp.com` for each page of results, and their server pictures. The game builds those addresses itself from
  the category and page number; the page cannot ask for any other address. The browser page reads the fetched pages
  with the browser's own HTML parser and takes out names, addresses, player counts and pictures. Promoted and premium
  rows are skipped.
- **A page on your own computer.** The first time you open the browser page, the mod starts a small web server on
  `127.0.0.1` (this computer only, never the network). Its address holds a random secret made for that session. The
  page only talks to that server: it contacts no other site, and a request that comes with another host name, another
  origin or no secret is refused. The server can show the list, send a picture and add one server to `servers.dat` in
  the game folder, nothing else.
- It sends no data about you, and `/serverify add` and the page's **Add** write one entry to `servers.dat` and nothing else.

## What is different from the Java mod

The Java mod has its own screens (cards, a category sidebar, a detail page). Horizon's native hook points cover the
HUD, keys, commands and settings pages, not new screens or buttons on the title or Multiplayer screens, so this port
shows its screen in your web browser. The page uses Modrinth and the three listing sites; the chat commands use Modrinth
only. The minecraft-list.info source, the region filter, the featured servers, favorites and live ping are not part of
it.

## Build and test

It is a [horizon-mixins](https://github.com/horizon-rust/horizon-mixins) bundle that uses only Horizon's hook points
(`hooks: 1`): `native_tick`, `native_key`, `native_command`, `native_mods`, `native_settings` and
`native_settings_click`.

```sh
cargo test                                          # the logic (runtime.rs)
python3 native/tools/native.py check /path/to/serverify    # from a clone of horizon-mods
python3 native/tools/native.py package /path/to/serverify  # writes the .hnmod for the registry
```

| File | What it is |
| --- | --- |
| `mixins/serverify/horizon-mixin.toml` | The bundle: five modules and the HEAD hooks |
| `mixins/serverify/runtime.rs` | The logic: commands, the requests' URLs, parsing, chat lines, and the web server's request parsing and checks. Also this crate's library, for its tests |
| `mixins/serverify/net.rs` | The requests to Modrinth and the listing sites, on threads of their own |
| `mixins/serverify/web.rs` | The local web server |
| `mixins/serverify/page.rs` | The page: HTML and JavaScript that shows the lists and reads the listing pages, with no request of its own |
| `mixins/serverify/files.rs` | Reads and writes the Multiplayer list |
| `mixins/serverify/*.rs` | One small helper per hook |

## Licence

GPL-3.0-only. See `LICENSE`.
