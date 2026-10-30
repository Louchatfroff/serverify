// The server browser page. It asks the game (its own address) for every list, listing page and
// picture, reads the listing pages with the browser's own HTML parser, and tells the game which
// server to add; it contacts no other site.
// This file becomes the module crate::game::serverify_page.
pub const PAGE: &str = r####"<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Serverify</title>
<style>
:root { color-scheme: dark; --bg: #181818; --card: #242424; --line: #3a3a3a; --text: #ececec; --dim: #a8a8a8; --accent: #6dbf6d; }
* { box-sizing: border-box; }
body { margin: 0; background: var(--bg); color: var(--text); font: 15px/1.45 system-ui, sans-serif; }
header, main, nav { max-width: 960px; margin: 0 auto; padding: 16px; }
h1 { margin: 0 0 4px; font-size: 26px; }
p.note { margin: 0 0 12px; color: var(--dim); }
form { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
input[type=search], select { background: var(--card); color: var(--text); border: 1px solid var(--line); border-radius: 6px; padding: 8px 10px; font: inherit; }
input[type=search] { flex: 1 1 220px; }
button { background: var(--card); color: var(--text); border: 1px solid var(--line); border-radius: 6px; padding: 8px 14px; font: inherit; cursor: pointer; }
button:hover:not(:disabled) { border-color: var(--accent); }
button:disabled { color: var(--dim); cursor: default; }
#list { display: grid; grid-template-columns: repeat(auto-fill, minmax(420px, 1fr)); gap: 12px; }
.card { background: var(--card); border: 1px solid var(--line); border-radius: 8px; padding: 12px; display: grid; grid-template-columns: 64px 1fr auto; gap: 12px; align-items: start; }
.card img { width: 64px; height: 64px; border-radius: 6px; background: #111; object-fit: cover; }
.card h2 { margin: 0; font-size: 17px; overflow-wrap: anywhere; }
.card p { margin: 4px 0 0; color: var(--dim); overflow-wrap: anywhere; }
.card .meta { color: var(--text); font-family: ui-monospace, monospace; font-size: 13px; }
nav { display: flex; gap: 12px; align-items: center; justify-content: center; }
#status { text-align: center; color: var(--dim); min-height: 1.5em; }
#hint { color: var(--dim); font-size: 13px; margin: 8px 0 0; }
</style>
</head>
<body>
<header>
<h1>Serverify</h1>
<p class="note">Browse servers and add them to your Multiplayer list. The game fetches the lists, and this page, which works on this computer only, shows them.</p>
<form id="form">
<input type="search" id="q" placeholder="Search servers" autocomplete="off">
<select id="category"></select>
<label><input type="checkbox" id="offline" checked> Offline-mode only</label>
<button type="submit">Search</button>
</form>
<p id="hint"></p>
</header>
<main>
<div id="status"></div>
<div id="list"></div>
</main>
<nav>
<button id="prev" type="button">Previous</button>
<span id="page"></span>
<button id="next" type="button">Next</button>
</nav>
<script>
"use strict";
const base = location.pathname.replace(/\/$/, "");
const CATEGORIES = [["Any category", ""], ["Survival", "survival"], ["SMP", "smp"], ["Vanilla", "vanilla"], ["SkyBlock", "skyblock"], ["Prison", "prison"], ["Factions", "factions"], ["PvP", "pvp"], ["KitPvP", "kitpvp"], ["Lifesteal", "lifesteal"], ["OneBlock", "oneblock"], ["Towny", "towny"], ["Economy", "economy"], ["RPG", "rpg"], ["Creative", "creative"], ["Minigames", "minigames"], ["Bed Wars", "bedwars"], ["Parkour", "parkour"], ["Pokemon", "cobblemon"], ["Modded", "modded"], ["Hardcore", "hardcore"], ["Anarchy", "anarchy"]];
const $ = (id) => document.getElementById(id);
let page = 0;
let saved = new Set();

function make(tag, text, className) {
  const node = document.createElement(tag);
  if (text !== undefined) node.textContent = text;
  if (className) node.className = className;
  return node;
}

function normal(address) {
  return address.toLowerCase().replace(/:25565$/, "");
}

/* One server, whatever it is called on each site: the host without a www., play., mc., join.,
   jugar., minecraft. or java. in front, and the port. */
function key(address) {
  let host = address.toLowerCase().replace(/:25565$/, "").replace(/\.(?=:|$)/, "");
  const found = /^(?:www|play|mc|join|jugar|minecraft|java)\.(.+)$/.exec(host);
  if (found && found[1].includes(".")) host = found[1];
  return host;
}

/* Reading the listing pages the game fetched. DOMParser builds an inert document: nothing in
   it runs or loads, and only text and attributes are taken out of it. */
function parse(html) {
  return new DOMParser().parseFromString(html, "text/html");
}

function textOf(root, selector) {
  const node = root.querySelector(selector);
  return node ? node.textContent.trim() : "";
}

function digits(value) {
  const found = value.replace(/\D/g, "");
  return found ? parseInt(found, 10) : 0;
}

function resolve(value, site) {
  try {
    return value ? new URL(value, site).href : "";
  } catch (error) {
    return "";
  }
}

function topMc(html) {
  const out = [];
  for (const card of parse(html).querySelectorAll("article.server-row-card")) {
    const copy = card.querySelector(".js-copy-ip-trigger[data-ip]");
    if (!copy) continue;
    const icon = card.querySelector("img.server-icon-img");
    const flag = card.querySelector("img.flag-img");
    const code = flag ? /\/([a-z]{2})\.[a-z]+$/.exec(flag.getAttribute("src") || "") : null;
    out.push({
      name: textOf(card, ".server-name"),
      address: copy.getAttribute("data-ip").trim(),
      players: digits(textOf(card, ".stat-current")),
      max: digits(textOf(card, ".stat-max")),
      country: code ? code[1].toUpperCase() : "",
      icon: resolve(icon && icon.getAttribute("src"), "https://top-mc-servers.net/"),
      source: "top-mc-servers.net",
    });
  }
  return out;
}

function mcServers(html) {
  const out = [];
  for (const card of parse(html).querySelectorAll("div.server-listing:not(.highlight)")) {
    const copy = card.querySelector(".copy[data-clipboard-content]");
    const name = card.querySelector(".name a");
    if (!copy || !name) continue;
    const counts = textOf(card, ".players .value").split("/");
    const id = card.getAttribute("data-id") || "";
    out.push({
      name: name.textContent.trim(),
      address: copy.getAttribute("data-clipboard-content").trim(),
      players: digits(counts[0] || ""),
      max: digits(counts[1] || ""),
      country: "",
      icon: /^\d+$/.test(id) ? "https://status.minecraftservers.org/icon/" + id + ".png" : "",
      source: "minecraftservers.org",
    });
  }
  return out;
}

function minecraftMp(html) {
  const out = [];
  for (const row of parse(html).querySelectorAll("tr")) {
    const copy = row.querySelector("button.clipboard[data-clipboard-text]");
    const name = row.querySelector(".server-title-responsive a");
    const first = row.querySelector("td");
    if (!copy || !name || !first || first.querySelector('img[alt="Premium server"]')) continue;
    const icon = first.querySelector('img[src*="/favicon/"]');
    const flag = row.querySelector('img[src*="/flags/"]');
    const users = row.querySelector("td.hidden-xs .fa-users");
    const parent = users ? users.parentElement : null;
    out.push({
      name: name.textContent.trim(),
      address: copy.getAttribute("data-clipboard-text").trim(),
      players: parent ? digits(textOf(parent, "strong")) : 0,
      max: parent ? digits(textOf(parent, "small")) : 0,
      country: flag ? (flag.getAttribute("alt") || "") : "",
      icon: resolve(icon && icon.getAttribute("src"), "https://minecraft-mp.com/"),
      source: "minecraft-mp.com",
    });
  }
  return out;
}

const SITES = { topmc: topMc, mcservers: mcServers, mp: minecraftMp };

async function fromSite(site, category) {
  const params = new URLSearchParams({ site: site, page: String(page) });
  if (category) params.set("category", category);
  const reply = await fetch(base + "/api/html?" + params);
  if (!reply.ok) return [];
  return SITES[site](await reply.text());
}

async function fromModrinth(query, category, offline) {
  const params = new URLSearchParams({ q: query, category: category, page: String(page), offline: offline ? "1" : "0" });
  const reply = await fetch(base + "/api/servers?" + params);
  const servers = await reply.json();
  if (!reply.ok) throw new Error(servers.error || "error");
  return servers.map((server) => Object.assign({ source: "Modrinth" }, server));
}

/* The other sites do not say whether a server takes offline-mode players, and they cannot be
   searched: they are used when the offline-mode filter is off, and for top-mc-servers.net's
   own list of cracked servers when it is on. */
function sitesFor(query, category, offline) {
  if (query) return [];
  if (!offline) return ["topmc", "mcservers", "mp"].map((site) => [site, category]);
  return category ? [] : [["topmc", "cracked"]];
}

async function loadSaved() {
  try {
    const list = await (await fetch(base + "/api/saved")).json();
    saved = new Set(list.map((server) => normal(server.ip)));
  } catch (error) {
    saved = new Set();
  }
}

async function add(server, button) {
  button.disabled = true;
  button.textContent = "Adding...";
  try {
    const reply = await fetch(base + "/api/add", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: server.name, address: server.address }),
    });
    const result = await reply.json();
    button.textContent = result.ok ? "In your list" : "Could not add";
    if (result.ok) saved.add(normal(server.address));
    else button.disabled = false;
    $("status").textContent = result.message;
  } catch (error) {
    button.textContent = "Add";
    button.disabled = false;
    $("status").textContent = "The game did not answer.";
  }
}

function card(server) {
  const picture = make("img");
  picture.alt = "";
  if (server.icon) picture.src = base + "/icon?u=" + encodeURIComponent(server.icon);
  const text = make("div");
  text.append(make("h2", server.name));
  const place = server.country ? " · " + server.country : "";
  text.append(make("p", server.address + " · " + server.players + "/" + server.max + " players" + place, "meta"));
  text.append(make("p", "From " + server.source));
  const button = make("button", saved.has(normal(server.address)) ? "In your list" : "Add");
  button.disabled = saved.has(normal(server.address));
  button.addEventListener("click", () => add(server, button));
  const row = make("div", undefined, "card");
  row.append(picture, text, button);
  return row;
}

async function load() {
  const query = $("q").value.trim();
  const category = $("category").value;
  const offline = $("offline").checked;
  $("status").textContent = "Loading...";
  $("list").replaceChildren();
  $("hint").textContent = offline
    ? "Horizon joins offline-mode servers only. Modrinth's offline-mode servers are shown, and top-mc-servers.net's cracked list when no category is picked. Turn the filter off to see every site."
    : "Servers from sites that do not say whether they take offline-mode players may refuse Horizon.";
  try {
    await loadSaved();
    const requests = [fromModrinth(query, category, offline)].concat(sitesFor(query, category, offline).map(([site, name]) => fromSite(site, name)));
    const results = await Promise.allSettled(requests);
    if (results[0].status === "rejected" && results.every((result) => result.status === "rejected")) {
      throw results[0].reason;
    }
    const seen = new Set();
    const merged = [];
    for (const result of results) {
      if (result.status !== "fulfilled") continue;
      for (const server of result.value) {
        const id = key(server.address);
        if (seen.has(id) || !server.address) continue;
        seen.add(id);
        merged.push(server);
      }
    }
    $("list").replaceChildren(...merged.map(card));
    $("status").textContent = merged.length ? "" : "No servers found.";
    $("prev").disabled = page === 0;
    $("next").disabled = merged.length === 0;
    $("page").textContent = "Page " + (page + 1);
  } catch (error) {
    $("status").textContent = "The game could not get the list. " + (error.message || "");
  }
}

for (const [label, value] of CATEGORIES) {
  const option = make("option", label);
  option.value = value;
  $("category").append(option);
}
$("form").addEventListener("submit", (event) => { event.preventDefault(); page = 0; load(); });
$("category").addEventListener("change", () => { page = 0; load(); });
$("offline").addEventListener("change", () => { page = 0; load(); });
$("prev").addEventListener("click", () => { page = Math.max(0, page - 1); load(); });
$("next").addEventListener("click", () => { page += 1; load(); });
load();
</script>
</body>
</html>
"####;
