// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)
//
// Serverify for Horizon: find Minecraft servers from Modrinth and add them to the
// Multiplayer list. This file is both the mod's Cargo library (its tests run with
// `cargo test`) and the module the bundle adds to the game as crate::game::serverify.
// It touches nothing of the game: the requests are made by serverify_net, the
// Multiplayer list is written by serverify_files.
use serde_json::Value;
use std::cell::RefCell;
use std::sync::Mutex;
use std::sync::atomic::{AtomicBool, Ordering};

pub const ID: &str = "serverify";
pub const VERSION: &str = "0.1.0";
pub const USER_AGENT: &str = "Louchatfroff/serverify (https://github.com/Louchatfroff/serverify)";
pub const PAGE_SIZE: usize = 10;
const API: &str = "https://api.modrinth.com";
const DEFAULT_PORT: u16 = 25565;

const CATEGORIES: [(&str, &str); 24] = [
    ("survival", "survival-mode"),
    ("smp", "smp"),
    ("vanilla", "vanilla-like"),
    ("skyblock", "skyblock"),
    ("prison", "prison"),
    ("factions", "factions"),
    ("pvp", "pvp"),
    ("kitpvp", "kitpvp"),
    ("lifesteal", "lifesteal"),
    ("oneblock", "oneblock"),
    ("towny", "towns"),
    ("economy", "economy"),
    ("rpg", "rpg"),
    ("creative", "creative-mode"),
    ("minigames", "minigames"),
    ("bedwars", "bedwars"),
    ("parkour", "parkour"),
    ("cobblemon", "pokemon"),
    ("pixelmon", "pokemon"),
    ("modded", "custom-content"),
    ("hardcore", "hardcore-mode"),
    ("anarchy", "anarchy"),
    ("cracked", "offline-mode"),
    ("roleplay", "rpg"),
];

#[derive(Clone, Debug, PartialEq)]
pub struct Server {
    pub name: String,
    pub host: String,
    pub port: u16,
    pub players: u32,
    pub max_players: u32,
    pub country: String,
    pub icon: String,
}

impl Server {
    pub fn address(&self) -> String {
        if self.port == DEFAULT_PORT {
            self.host.clone()
        } else {
            format!("{}:{}", self.host, self.port)
        }
    }
}

#[derive(Clone, Debug, PartialEq)]
pub struct Query {
    pub text: String,
    pub category: Option<String>,
    pub offline_only: bool,
    pub page: usize,
}

#[derive(Debug, PartialEq)]
pub enum Plan {
    Say(Vec<String>),
    Fetch(Query),
    Add(usize),
    Web,
}

#[derive(Debug)]
pub enum Reply {
    Servers(Query, Vec<Server>),
    Failed(String),
}

#[derive(Default)]
struct Last {
    query: Option<Query>,
    servers: Vec<Server>,
}

thread_local! {
    // The last page shown, for next, prev and add: only the window's game reads it.
    static LAST: RefCell<Last> = RefCell::new(Last::default());
}

static REPLIES: Mutex<Vec<Reply>> = Mutex::new(Vec::new());
static BUSY: AtomicBool = AtomicBool::new(false);

/// Claims the one request that may run at a time: false when one is running.
pub fn begin() -> bool {
    BUSY.compare_exchange(false, true, Ordering::SeqCst, Ordering::SeqCst).is_ok()
}

/// Hands an answer to the window's game, which prints it, and frees the request.
pub fn push_reply(reply: Reply) {
    if let Ok(mut replies) = REPLIES.lock() {
        replies.push(reply);
    }
    BUSY.store(false, Ordering::SeqCst);
}

fn take_replies() -> Vec<Reply> {
    REPLIES.lock().map(|mut replies| std::mem::take(&mut *replies)).unwrap_or_default()
}

/// The chat lines for the answers that came in, keeping the newest page for add.
pub fn answers() -> Vec<String> {
    let mut lines = Vec::new();
    for reply in take_replies() {
        match reply {
            Reply::Servers(query, servers) => {
                lines.extend(describe(&query, &servers));
                LAST.with(|last| *last.borrow_mut() = Last { query: Some(query), servers });
            }
            Reply::Failed(message) => lines.push(format!("Serverify: {message}")),
        }
    }
    lines
}

/// The server `number` (counted from 1) of the page last shown.
pub fn pick(number: usize) -> Result<Server, String> {
    LAST.with(|last| {
        let last = last.borrow();
        if last.servers.is_empty() {
            return Err("Serverify: list servers first with /serverify list".to_owned());
        }
        number
            .checked_sub(1)
            .and_then(|index| last.servers.get(index))
            .cloned()
            .ok_or_else(|| format!("Serverify: pick a number from 1 to {}", last.servers.len()))
    })
}

fn category_slug(name: &str) -> Option<&'static str> {
    let name = name.to_ascii_lowercase();
    CATEGORIES.iter().find(|(known, _)| *known == name).map(|(_, slug)| *slug)
}

fn help() -> Vec<String> {
    vec![
        format!("Serverify {VERSION}: find servers and add them to your Multiplayer list"),
        "/serverify web (or the key in Options > Mods): the server browser in your web browser".to_owned(),
        "/serverify list [category]: the most played servers".to_owned(),
        "/serverify search <words>: search by name".to_owned(),
        "/serverify next, /serverify prev: the next or previous page".to_owned(),
        "/serverify add <number>: add a listed server to Multiplayer".to_owned(),
        "/serverify categories: the categories".to_owned(),
        "Horizon joins offline-mode servers only, so the list shows those unless you turn it off in Options > Mods.".to_owned(),
    ]
}

/// What a `/serverify` command asks for (`args` is what follows the command's name).
pub fn plan(args: &str, offline_only: bool) -> Plan {
    let args = args.trim();
    let (word, rest) = args.split_once(' ').map_or((args, ""), |(word, rest)| (word, rest.trim()));
    let query = |text: &str, category: Option<String>, page: usize| {
        Plan::Fetch(Query { text: text.to_owned(), category, offline_only, page })
    };
    match word.to_ascii_lowercase().as_str() {
        "" | "help" => Plan::Say(help()),
        "categories" => {
            let names: Vec<&str> = CATEGORIES.iter().map(|(name, _)| *name).collect();
            Plan::Say(vec![format!("Serverify categories: {}", names.join(", "))])
        }
        "web" | "browser" => Plan::Web,
        "list" if rest.is_empty() => query("", None, 0),
        "list" => match category_slug(rest) {
            Some(slug) => query("", Some(slug.to_owned()), 0),
            None => Plan::Say(vec![format!("Serverify: unknown category {}, see /serverify categories", clean(rest, 24))]),
        },
        "search" if rest.is_empty() => Plan::Say(vec!["Serverify: search for what? /serverify search <words>".to_owned()]),
        "search" => query(rest, None, 0),
        "next" | "prev" => {
            let last = LAST.with(|last| last.borrow().query.clone());
            match last {
                None => Plan::Say(vec!["Serverify: list servers first with /serverify list".to_owned()]),
                Some(last) if word.eq_ignore_ascii_case("next") => {
                    Plan::Fetch(Query { page: last.page + 1, offline_only, ..last })
                }
                Some(last) if last.page == 0 => Plan::Say(vec!["Serverify: this is the first page".to_owned()]),
                Some(last) => Plan::Fetch(Query { page: last.page - 1, offline_only, ..last }),
            }
        }
        "add" => match rest.parse::<usize>() {
            Ok(number) => Plan::Add(number),
            Err(_) => Plan::Say(vec!["Serverify: add which server? /serverify add <number>".to_owned()]),
        },
        other => Plan::Say(vec![format!("Serverify: unknown option {}, see /serverify", clean(other, 24))]),
    }
}

/// A server list or an owner's text made safe for a chat line: no colour codes, no control
/// characters, one line, at most `max` characters.
pub fn clean(text: &str, max: usize) -> String {
    let mut out = String::new();
    let mut chars = text.chars();
    while let Some(c) = chars.next() {
        if c == '\u{a7}' {
            chars.next();
        } else if !c.is_control() {
            out.push(c);
        } else if !out.ends_with(' ') {
            out.push(' ');
        }
    }
    let out = out.split_whitespace().collect::<Vec<_>>().join(" ");
    if out.chars().count() > max {
        let mut cut: String = out.chars().take(max.saturating_sub(1)).collect();
        cut.push('\u{2026}');
        cut
    } else {
        out
    }
}

fn describe(query: &Query, servers: &[Server]) -> Vec<String> {
    if servers.is_empty() {
        return vec!["Serverify: no servers found".to_owned()];
    }
    let mut lines = vec![format!("Serverify, page {}:", query.page + 1)];
    for (index, server) in servers.iter().enumerate() {
        lines.push(format!(
            "{}. {} | {} | {}/{} | {}",
            index + 1,
            clean(&server.name, 32),
            clean(&server.address(), 40),
            server.players,
            server.max_players,
            clean(&server.country, 3),
        ));
    }
    lines.push("/serverify add <number> adds one to Multiplayer, /serverify next shows more".to_owned());
    lines
}

fn encode(text: &str) -> String {
    let mut out = String::new();
    for byte in text.bytes() {
        match byte {
            b'A'..=b'Z' | b'a'..=b'z' | b'0'..=b'9' | b'-' | b'_' | b'.' | b'~' => out.push(char::from(byte)),
            _ => out.push_str(&format!("%{byte:02X}")),
        }
    }
    out
}

pub fn search_url(query: &Query) -> String {
    let mut facets = vec![vec!["project_type:minecraft_java_server".to_owned()]];
    if query.offline_only {
        facets.push(vec!["categories:offline-mode".to_owned()]);
    }
    if let Some(slug) = &query.category {
        facets.push(vec![format!("categories:{slug}")]);
    }
    let facets = serde_json::to_string(&facets).unwrap_or_default();
    let index = if query.text.is_empty() { "downloads" } else { "relevance" };
    let mut url = format!(
        "{API}/v2/search?limit={PAGE_SIZE}&offset={}&index={index}&facets={}",
        query.page * PAGE_SIZE,
        encode(&facets)
    );
    if !query.text.is_empty() {
        url.push_str(&format!("&query={}", encode(&query.text)));
    }
    url
}

pub fn projects_url(ids: &[String]) -> String {
    let ids = serde_json::to_string(ids).unwrap_or_default();
    format!("{API}/v3/projects?ids={}", encode(&ids))
}

pub fn parse_ids(search: &str) -> Result<Vec<String>, String> {
    let root: Value = serde_json::from_str(search).map_err(|e| format!("Modrinth answered something unexpected ({e})"))?;
    let hits = root.get("hits").and_then(Value::as_array).ok_or("Modrinth answered something unexpected")?;
    Ok(hits.iter().filter_map(|hit| hit.get("project_id")?.as_str().map(str::to_owned)).collect())
}

pub fn parse_servers(search: &str, projects: &str) -> Result<Vec<Server>, String> {
    let search: Value = serde_json::from_str(search).map_err(|e| format!("Modrinth answered something unexpected ({e})"))?;
    let projects: Value = serde_json::from_str(projects).map_err(|e| format!("Modrinth answered something unexpected ({e})"))?;
    let hits = search.get("hits").and_then(Value::as_array).ok_or("Modrinth answered something unexpected")?;
    let projects = projects.as_array().ok_or("Modrinth answered something unexpected")?;
    let mut out = Vec::new();
    for hit in hits {
        let id = hit.get("project_id").and_then(Value::as_str).unwrap_or_default();
        let Some(project) = projects.iter().find(|p| p.get("id").and_then(Value::as_str) == Some(id)) else {
            continue;
        };
        let java = project.get("minecraft_java_server");
        let address = java.and_then(|j| j.get("address")).and_then(Value::as_str).unwrap_or_default().trim();
        let Some((host, port)) = split_address(address) else {
            continue;
        };
        let ping = java.and_then(|j| j.get("ping")).and_then(|p| p.get("data"));
        let number = |key: &str| ping.and_then(|p| p.get(key)).and_then(Value::as_u64).unwrap_or(0).min(u64::from(u32::MAX)) as u32;
        out.push(Server {
            name: hit.get("title").and_then(Value::as_str).unwrap_or_default().to_owned(),
            host,
            port,
            players: number("players_online"),
            max_players: number("players_max"),
            country: project
                .get("minecraft_server")
                .and_then(|m| m.get("country"))
                .and_then(Value::as_str)
                .unwrap_or_default()
                .to_owned(),
            icon: hit.get("icon_url").and_then(Value::as_str).unwrap_or_default().to_owned(),
        });
    }
    Ok(out)
}

fn split_address(address: &str) -> Option<(String, u16)> {
    if address.is_empty() || address.contains('[') || address.chars().any(|c| c.is_whitespace() || c.is_control()) {
        return None;
    }
    match address.rsplit_once(':') {
        Some((host, port)) => {
            let port: u16 = port.parse().ok().filter(|port| *port > 0)?;
            if host.is_empty() { None } else { Some((host.to_owned(), port)) }
        }
        None => Some((address.to_owned(), DEFAULT_PORT)),
    }
}

/// The page of a listing site that the browser page asks the game for: only these three sites,
/// only addresses built here from a known category and a page number.
pub fn html_url(site: &str, category: Option<&str>, page: usize) -> Option<String> {
    let name = category.map(str::to_ascii_lowercase);
    let name = name.as_deref();
    if let Some(name) = name {
        if !CATEGORIES.iter().any(|(known, _)| *known == name) {
            return None;
        }
    }
    let number = page + 1;
    let more = |suffix: &str| if page == 0 { String::new() } else { format!("{suffix}{number}") };
    match site {
        "topmc" => match name {
            None if page == 0 => Some("https://top-mc-servers.net/".to_owned()),
            None => Some(format!("https://top-mc-servers.net/servers/{number}")),
            Some(name) => {
                let slug = match name {
                    "survival" | "smp" | "vanilla" | "skyblock" | "prison" | "factions" | "pvp" | "kitpvp" | "lifesteal"
                    | "towny" | "economy" | "creative" | "cobblemon" | "pixelmon" | "modded" | "anarchy" | "cracked" => name,
                    "rpg" | "roleplay" => "roleplay",
                    "minigames" => "mini-games",
                    _ => return None,
                };
                Some(format!("https://top-mc-servers.net/type/minecraft-{slug}-servers{}", more("/")))
            }
        },
        "mcservers" => match name {
            None => Some(format!("https://minecraftservers.org/index/{number}")),
            Some(
                name @ ("anarchy" | "bedwars" | "cobblemon" | "creative" | "economy" | "factions" | "kitpvp" | "lifesteal"
                | "modded" | "oneblock" | "parkour" | "pixelmon" | "prison" | "pvp" | "roleplay" | "rpg" | "skyblock" | "smp"
                | "survival" | "towny" | "vanilla"),
            ) => Some(format!("https://minecraftservers.org/type/{name}{}", more("/"))),
            Some(_) => None,
        },
        "mp" => match name {
            None if page == 0 => Some("https://minecraft-mp.com/".to_owned()),
            None => Some(format!("https://minecraft-mp.com/servers/list/{number}/")),
            Some(name) => {
                let slug = match name {
                    "bedwars" | "lifesteal" | "oneblock" | "pixelmon" | "prison" | "pvp" | "roleplay" | "skyblock" | "survival"
                    | "towny" | "vanilla" => name,
                    "factions" => "faction",
                    _ => return None,
                };
                let paged = if page == 0 { String::new() } else { format!("{number}/") };
                Some(format!("https://minecraft-mp.com/type/{slug}/{paged}"))
            }
        },
        _ => None,
    }
}

/// The tiny web server of the browser page: parsing a request and deciding what it asks for,
/// with the checks that keep other web pages and other programs out. No input or output here.
pub mod http {
    use super::{DEFAULT_PORT, Query, category_slug, clean, split_address};
    use serde_json::Value;

    pub const MAX_REQUEST: usize = 16 * 1024;
    const MAX_BODY: usize = 4 * 1024;
    const CSP: &str = "default-src 'none'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; \
connect-src 'self'; img-src 'self'; \
base-uri 'none'; form-action 'none'; frame-ancestors 'none'";

    #[derive(Debug, PartialEq)]
    pub struct Request {
        pub method: String,
        pub path: String,
        pub host: String,
        pub origin: Option<String>,
        pub content_type: String,
        pub body: Vec<u8>,
    }

    const MAX_PAGE: usize = 200;

    #[derive(Debug, PartialEq)]
    pub enum Route {
        Page,
        Saved,
        List(Query),
        Icon(String),
        Html(String),
        Add { name: String, address: String },
        Reject(u16, &'static str),
    }

    fn find(hay: &[u8], needle: &[u8]) -> Option<usize> {
        hay.windows(needle.len()).position(|window| window == needle)
    }

    /// A request from the bytes read so far: Ok(None) when more bytes are needed.
    pub fn parse(bytes: &[u8]) -> Result<Option<Request>, &'static str> {
        let Some(end) = find(bytes, b"\r\n\r\n") else {
            return if bytes.len() > MAX_REQUEST { Err("request too large") } else { Ok(None) };
        };
        if end > MAX_REQUEST {
            return Err("request too large");
        }
        let head = std::str::from_utf8(&bytes[..end]).map_err(|_| "bad request")?;
        let mut lines = head.split("\r\n");
        let mut first = lines.next().unwrap_or_default().split(' ');
        let (Some(method), Some(path), Some(version)) = (first.next(), first.next(), first.next()) else {
            return Err("bad request");
        };
        if !version.starts_with("HTTP/1.") {
            return Err("bad request");
        }
        let mut request = Request {
            method: method.to_owned(),
            path: path.to_owned(),
            host: String::new(),
            origin: None,
            content_type: String::new(),
            body: Vec::new(),
        };
        let mut length = 0usize;
        for line in lines {
            let Some((name, value)) = line.split_once(':') else {
                return Err("bad request");
            };
            let value = value.trim();
            match name.trim().to_ascii_lowercase().as_str() {
                "host" => request.host = value.to_owned(),
                "origin" => request.origin = Some(value.to_owned()),
                "content-type" => request.content_type = value.to_ascii_lowercase(),
                "content-length" => length = value.parse().map_err(|_| "bad request")?,
                "transfer-encoding" => return Err("bad request"),
                _ => {}
            }
        }
        if length > MAX_BODY {
            return Err("request too large");
        }
        let start = end + 4;
        if bytes.len() < start + length {
            return Ok(None);
        }
        request.body = bytes[start..start + length].to_vec();
        Ok(Some(request))
    }

    fn percent_decode(text: &str) -> Option<String> {
        let bytes = text.as_bytes();
        let mut out = Vec::with_capacity(bytes.len());
        let mut i = 0;
        while i < bytes.len() {
            match bytes[i] {
                b'%' => {
                    let hex = std::str::from_utf8(bytes.get(i + 1..i + 3)?).ok()?;
                    out.push(u8::from_str_radix(hex, 16).ok()?);
                    i += 3;
                }
                b'+' => {
                    out.push(b' ');
                    i += 1;
                }
                byte => {
                    out.push(byte);
                    i += 1;
                }
            }
        }
        String::from_utf8(out).ok()
    }

    fn query_params(query: &str) -> Vec<(String, String)> {
        query
            .split('&')
            .filter_map(|pair| {
                let (key, value) = pair.split_once('=')?;
                Some((percent_decode(key)?, percent_decode(value)?))
            })
            .collect()
    }

    /// An icon the page may ask for: the picture hosts of the sources only.
    fn icon_url(url: &str) -> Option<String> {
        let hosts = [
            "https://cdn.modrinth.com/",
            "https://status.minecraftservers.org/",
            "https://top-mc-servers.net/",
            "https://minecraft-mp.com/",
        ];
        let ok = hosts.iter().any(|host| url.starts_with(host))
            && url.len() <= 300
            && !url.chars().any(|c| c.is_whitespace() || c.is_control());
        ok.then(|| url.to_owned())
    }

    /// What the request asks for. Only this computer's own address is served (a web page that
    /// reaches this server through another name is refused), the path must carry this session's
    /// token, and a request that names another origin is refused.
    pub fn route(request: &Request, port: u16, token: &str) -> Route {
        if request.host != format!("127.0.0.1:{port}") && request.host != format!("localhost:{port}") {
            return Route::Reject(403, "wrong host");
        }
        if let Some(origin) = &request.origin {
            if origin != &format!("http://127.0.0.1:{port}") && origin != &format!("http://localhost:{port}") {
                return Route::Reject(403, "wrong origin");
            }
        }
        let (path, query) = request.path.split_once('?').unwrap_or((request.path.as_str(), ""));
        let Some(rest) = path.strip_prefix(&format!("/{token}")) else {
            return Route::Reject(404, "not found");
        };
        let params = query_params(query);
        let param = |key: &str| params.iter().find(|(name, _)| name == key).map_or("", |(_, value)| value.as_str());
        match (request.method.as_str(), rest) {
            ("GET", "/") => Route::Page,
            ("GET", "/api/saved") => Route::Saved,
            ("GET", "/api/servers") => {
                let category = match param("category") {
                    "" => None,
                    name => match category_slug(name) {
                        Some(slug) => Some(slug.to_owned()),
                        None => return Route::Reject(400, "bad category"),
                    },
                };
                Route::List(Query {
                    text: clean(param("q"), 64),
                    category,
                    offline_only: param("offline") != "0",
                    page: param("page").parse::<usize>().unwrap_or(0).min(MAX_PAGE),
                })
            }
            ("GET", "/api/html") => {
                let category = match param("category") {
                    "" => None,
                    name => Some(name),
                };
                let page = param("page").parse::<usize>().unwrap_or(0).min(MAX_PAGE);
                match super::html_url(param("site"), category, page) {
                    Some(url) => Route::Html(url),
                    None => Route::Reject(400, "not available"),
                }
            }
            ("GET", "/icon") => match icon_url(param("u")) {
                Some(url) => Route::Icon(url),
                None => Route::Reject(400, "bad icon"),
            },
            ("POST", "/api/add") => {
                if !request.content_type.starts_with("application/json") {
                    return Route::Reject(415, "send json");
                }
                let Ok(body) = serde_json::from_slice::<Value>(&request.body) else {
                    return Route::Reject(400, "bad json");
                };
                let text = |key: &str| body.get(key).and_then(Value::as_str).unwrap_or_default().to_owned();
                let Some((host, port)) = split_address(text("address").trim()) else {
                    return Route::Reject(400, "bad address");
                };
                let address = if port == DEFAULT_PORT { host } else { format!("{host}:{port}") };
                let name = clean(&text("name"), 64);
                Route::Add { name: if name.is_empty() { address.clone() } else { name }, address }
            }
            ("GET" | "POST", "/" | "/api/saved" | "/api/servers" | "/api/html" | "/icon" | "/api/add") => Route::Reject(405, "wrong method"),
            _ => Route::Reject(404, "not found"),
        }
    }

    fn response(status: u16, content_type: &str, extra: &str, body: &[u8]) -> Vec<u8> {
        let reason = match status {
            200 => "OK",
            400 => "Bad Request",
            403 => "Forbidden",
            404 => "Not Found",
            405 => "Method Not Allowed",
            415 => "Unsupported Media Type",
            502 => "Bad Gateway",
            _ => "Error",
        };
        let head = format!(
            "HTTP/1.1 {status} {reason}\r\nContent-Type: {content_type}\r\nContent-Length: {}\r\nCache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\nReferrer-Policy: no-referrer\r\nConnection: close\r\n{extra}\r\n",
            body.len()
        );
        let mut out = head.into_bytes();
        out.extend_from_slice(body);
        out
    }

    pub fn page(html: &str) -> Vec<u8> {
        response(200, "text/html; charset=utf-8", &format!("Content-Security-Policy: {CSP}\r\n"), html.as_bytes())
    }

    pub fn json(status: u16, value: &Value) -> Vec<u8> {
        response(status, "application/json", "", value.to_string().as_bytes())
    }

    pub fn image(content_type: &str, bytes: &[u8]) -> Vec<u8> {
        response(200, content_type, "", bytes)
    }

    pub fn text(status: u16, message: &str) -> Vec<u8> {
        response(status, "text/plain; charset=utf-8", "", message.as_bytes())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    const SEARCH: &str = r#"{"hits":[
        {"project_id":"aaa","title":"First §aServer","description":"x"},
        {"project_id":"bbb","title":"Second","description":"y"},
        {"project_id":"ccc","title":"No address","description":"z"}],"total_hits":3}"#;
    const PROJECTS: &str = r#"[
        {"id":"aaa","minecraft_server":{"country":"US"},"minecraft_java_server":{"address":"play.first.net","ping":{"data":{"players_online":12,"players_max":100}}}},
        {"id":"bbb","minecraft_server":{},"minecraft_java_server":{"address":"second.net:25570"}},
        {"id":"ccc","minecraft_server":{},"minecraft_java_server":{"address":""}}]"#;

    #[test]
    fn parses_servers_with_and_without_a_ping() {
        let servers = parse_servers(SEARCH, PROJECTS).unwrap();
        assert_eq!(servers.len(), 2);
        assert_eq!(servers[0].host, "play.first.net");
        assert_eq!((servers[0].players, servers[0].max_players), (12, 100));
        assert_eq!(servers[0].country, "US");
        assert_eq!(servers[1].port, 25570);
        assert_eq!(servers[1].address(), "second.net:25570");
        assert_eq!(servers[0].address(), "play.first.net");
    }

    #[test]
    fn reads_the_project_ids() {
        assert_eq!(parse_ids(SEARCH).unwrap(), vec!["aaa", "bbb", "ccc"]);
        assert!(parse_ids("not json").is_err());
        assert!(parse_servers(SEARCH, "{}").is_err());
    }

    #[test]
    fn builds_the_search_url() {
        let query = Query { text: "sky block".into(), category: Some("skyblock".into()), offline_only: true, page: 2 };
        let url = search_url(&query);
        assert!(url.starts_with("https://api.modrinth.com/v2/search?limit=10&offset=20&index=relevance&facets="));
        assert!(url.contains("categories%3Aoffline-mode"));
        assert!(url.contains("categories%3Askyblock"));
        assert!(url.ends_with("&query=sky%20block"));
        assert!(!search_url(&Query { offline_only: false, ..query }).contains("offline-mode"));
    }

    #[test]
    fn cleans_text_for_chat() {
        assert_eq!(clean("\u{a7}aGreen \u{a7}lBold\nname", 40), "Green Bold name");
        assert_eq!(clean("abcdef", 4), "abc\u{2026}");
        assert_eq!(clean("a\u{7}b", 10), "a b");
    }

    #[test]
    fn plans_the_commands() {
        assert!(matches!(plan("", true), Plan::Say(_)));
        assert!(matches!(plan("categories", true), Plan::Say(_)));
        assert!(matches!(plan("bogus", true), Plan::Say(_)));
        assert!(matches!(plan("search", true), Plan::Say(_)));
        assert!(matches!(plan("list nonsense", true), Plan::Say(_)));
        assert_eq!(plan("add 3", true), Plan::Add(3));
        assert!(matches!(plan("add x", true), Plan::Say(_)));
        match plan("list Survival", false) {
            Plan::Fetch(query) => {
                assert_eq!(query.category.as_deref(), Some("survival-mode"));
                assert!(!query.offline_only);
            }
            other => panic!("{other:?}"),
        }
        assert!(matches!(plan("next", true), Plan::Say(_)));
    }

    fn request(method: &str, path: &str, host: &str, body: &str) -> http::Request {
        http::Request {
            method: method.to_owned(),
            path: path.to_owned(),
            host: host.to_owned(),
            origin: None,
            content_type: "application/json".to_owned(),
            body: body.as_bytes().to_vec(),
        }
    }

    #[test]
    fn reads_a_request_in_pieces() {
        let whole = b"POST /t/api/add HTTP/1.1\r\nHost: 127.0.0.1:5\r\nContent-Type: application/json\r\nContent-Length: 4\r\n\r\n{\"a\"";
        assert_eq!(http::parse(&whole[..20]), Ok(None));
        assert_eq!(http::parse(&whole[..whole.len() - 1]), Ok(None));
        let parsed = http::parse(whole).unwrap().unwrap();
        assert_eq!((parsed.method.as_str(), parsed.path.as_str(), parsed.host.as_str()), ("POST", "/t/api/add", "127.0.0.1:5"));
        assert_eq!(parsed.body, b"{\"a\"");
        assert!(http::parse(b"GET / HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n").is_err());
        assert!(http::parse(b"nonsense\r\n\r\n").is_err());
        assert!(http::parse(&vec![b'a'; http::MAX_REQUEST + 1]).is_err());
    }

    #[test]
    fn routes_only_the_right_host_token_and_origin() {
        let host = "127.0.0.1:5000";
        assert_eq!(http::route(&request("GET", "/tok/", host, ""), 5000, "tok"), http::Route::Page);
        assert_eq!(http::route(&request("GET", "/tok/api/saved", "localhost:5000", ""), 5000, "tok"), http::Route::Saved);
        assert_eq!(http::route(&request("GET", "/tok/", "evil.example:5000", ""), 5000, "tok"), http::Route::Reject(403, "wrong host"));
        assert_eq!(http::route(&request("GET", "/other/", host, ""), 5000, "tok"), http::Route::Reject(404, "not found"));
        assert_eq!(http::route(&request("GET", "/", host, ""), 5000, "tok"), http::Route::Reject(404, "not found"));
        assert_eq!(http::route(&request("POST", "/tok/", host, ""), 5000, "tok"), http::Route::Reject(405, "wrong method"));
        let mut foreign = request("GET", "/tok/", host, "");
        foreign.origin = Some("https://evil.example".to_owned());
        assert_eq!(http::route(&foreign, 5000, "tok"), http::Route::Reject(403, "wrong origin"));
        foreign.origin = Some("http://127.0.0.1:5000".to_owned());
        assert_eq!(http::route(&foreign, 5000, "tok"), http::Route::Page);
    }

    #[test]
    fn routes_the_searches_and_icons_the_page_asks_for() {
        let host = "127.0.0.1:5000";
        let get = |path: &str| http::route(&request("GET", path, host, ""), 5000, "tok");
        match get("/tok/api/servers?q=sky+block%21&category=Survival&page=2&offline=0") {
            http::Route::List(query) => {
                assert_eq!(query.text, "sky block!");
                assert_eq!(query.category.as_deref(), Some("survival-mode"));
                assert_eq!(query.page, 2);
                assert!(!query.offline_only);
            }
            other => panic!("{other:?}"),
        }
        match get("/tok/api/servers") {
            http::Route::List(query) => assert!(query.offline_only && query.category.is_none() && query.page == 0),
            other => panic!("{other:?}"),
        }
        match get("/tok/api/servers?page=99999") {
            http::Route::List(query) => assert_eq!(query.page, 200),
            other => panic!("{other:?}"),
        }
        assert_eq!(get("/tok/api/servers?category=nonsense"), http::Route::Reject(400, "bad category"));
        assert_eq!(
            get("/tok/icon?u=https%3A%2F%2Fcdn.modrinth.com%2Fdata%2Fx%2Ficon.webp"),
            http::Route::Icon("https://cdn.modrinth.com/data/x/icon.webp".into())
        );
        assert_eq!(get("/tok/icon?u=https%3A%2F%2Fevil.example%2Fx.png"), http::Route::Reject(400, "bad icon"));
        assert_eq!(get("/tok/icon?u=http%3A%2F%2Fcdn.modrinth.com%2Fx"), http::Route::Reject(400, "bad icon"));
        assert_eq!(get("/tok/icon"), http::Route::Reject(400, "bad icon"));
        assert!(matches!(get("/tok/api/servers?q=%zz&page=1"), http::Route::List(_)));
    }

    #[test]
    fn builds_only_the_listing_pages_it_knows() {
        assert_eq!(html_url("topmc", None, 0).as_deref(), Some("https://top-mc-servers.net/"));
        assert_eq!(html_url("topmc", None, 2).as_deref(), Some("https://top-mc-servers.net/servers/3"));
        assert_eq!(html_url("topmc", Some("Cracked"), 0).as_deref(), Some("https://top-mc-servers.net/type/minecraft-cracked-servers"));
        assert_eq!(html_url("topmc", Some("minigames"), 1).as_deref(), Some("https://top-mc-servers.net/type/minecraft-mini-games-servers/2"));
        assert_eq!(html_url("mcservers", None, 0).as_deref(), Some("https://minecraftservers.org/index/1"));
        assert_eq!(html_url("mcservers", Some("survival"), 2).as_deref(), Some("https://minecraftservers.org/type/survival/3"));
        assert_eq!(html_url("mp", None, 0).as_deref(), Some("https://minecraft-mp.com/"));
        assert_eq!(html_url("mp", None, 1).as_deref(), Some("https://minecraft-mp.com/servers/list/2/"));
        assert_eq!(html_url("mp", Some("factions"), 1).as_deref(), Some("https://minecraft-mp.com/type/faction/2/"));
        assert_eq!(html_url("mp", Some("survival"), 0).as_deref(), Some("https://minecraft-mp.com/type/survival/"));
        assert_eq!(html_url("mp", Some("parkour"), 0), None);
        assert_eq!(html_url("mcservers", Some("cracked"), 0), None);
        assert_eq!(html_url("evil", None, 0), None);
        assert_eq!(html_url("topmc", Some("../etc"), 0), None);
    }

    #[test]
    fn routes_the_listing_pages_the_page_asks_for() {
        let host = "127.0.0.1:5000";
        let get = |path: &str| http::route(&request("GET", path, host, ""), 5000, "tok");
        assert_eq!(
            get("/tok/api/html?site=mcservers&category=survival&page=1"),
            http::Route::Html("https://minecraftservers.org/type/survival/2".into())
        );
        assert_eq!(get("/tok/api/html?site=topmc"), http::Route::Html("https://top-mc-servers.net/".into()));
        assert_eq!(get("/tok/api/html?site=evil"), http::Route::Reject(400, "not available"));
        assert_eq!(get("/tok/api/html?site=mp&category=parkour"), http::Route::Reject(400, "not available"));
        assert_eq!(
            get("/tok/icon?u=https%3A%2F%2Fstatus.minecraftservers.org%2Ficon%2F1.png"),
            http::Route::Icon("https://status.minecraftservers.org/icon/1.png".into())
        );
    }

    #[test]
    fn validates_what_the_page_adds() {
        let host = "127.0.0.1:5000";
        let add = |body: &str| http::route(&request("POST", "/tok/api/add", host, body), 5000, "tok");
        assert_eq!(
            add(r#"{"name":"§aMy  Server","address":" play.example.net:25570 "}"#),
            http::Route::Add { name: "My Server".into(), address: "play.example.net:25570".into() }
        );
        assert_eq!(
            add(r#"{"name":"","address":"example.net:25565"}"#),
            http::Route::Add { name: "example.net".into(), address: "example.net".into() }
        );
        assert_eq!(add(r#"{"address":"bad host"}"#), http::Route::Reject(400, "bad address"));
        assert_eq!(add(r#"{"address":"[::1]:25565"}"#), http::Route::Reject(400, "bad address"));
        assert_eq!(add("nope"), http::Route::Reject(400, "bad json"));
        let mut form = request("POST", "/tok/api/add", host, "a=b");
        form.content_type = "text/plain".into();
        assert_eq!(http::route(&form, 5000, "tok"), http::Route::Reject(415, "send json"));
    }

    #[test]
    fn builds_responses_with_their_headers() {
        let page = String::from_utf8(http::page("<p>hi</p>")).unwrap();
        assert!(page.starts_with("HTTP/1.1 200 OK\r\n"));
        assert!(page.contains("Content-Length: 9\r\n"));
        assert!(page.contains("Content-Security-Policy: default-src 'none'"));
        assert!(page.contains("connect-src 'self'; img-src 'self'"));
        assert!(!page.contains("modrinth"));
        assert!(page.ends_with("\r\n\r\n<p>hi</p>"));
        let missing = String::from_utf8(http::text(404, "not found")).unwrap();
        assert!(missing.starts_with("HTTP/1.1 404 Not Found\r\n"));
        assert!(!missing.contains("Content-Security-Policy"));
    }

    #[test]
    fn keeps_the_last_page_for_add_and_next() {
        let servers = parse_servers(SEARCH, PROJECTS).unwrap();
        let query = Query { text: String::new(), category: None, offline_only: true, page: 0 };
        assert!(begin());
        assert!(!begin());
        push_reply(Reply::Servers(query, servers));
        let lines = answers();
        assert!(lines[0].starts_with("Serverify, page 1"));
        assert!(lines[1].starts_with("1. First Server | play.first.net | 12/100"));
        assert_eq!(pick(2).unwrap().host, "second.net");
        assert!(pick(0).is_err());
        assert!(pick(3).is_err());
        match plan("next", true) {
            Plan::Fetch(next) => assert_eq!(next.page, 1),
            other => panic!("{other:?}"),
        }
        assert!(matches!(plan("prev", true), Plan::Say(_)));
        assert!(begin());
    }
}
