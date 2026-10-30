// The page that shows the server list: a small web server on this computer's own address
// (127.0.0.1, never the network), started the first time it is asked for. It only shows what the
// game fetched; the page itself makes no request to any other site.
// This file becomes the module crate::game::serverify_web.
use serde_json::{Value, json};
use std::io::{Read, Write};
use std::net::{TcpListener, TcpStream};
use std::path::{Path, PathBuf};
use std::sync::OnceLock;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::time::{Duration, Instant};

static SERVER: OnceLock<Option<(u16, String)>> = OnceLock::new();
static ACTIVE: AtomicUsize = AtomicUsize::new(0);
const MAX_CONNECTIONS: usize = 16;

/// The address of the page, starting its server the first time.
pub fn url(data_dir: &Path) -> Option<String> {
    let started = SERVER.get_or_init(|| start(data_dir.to_path_buf()));
    started.as_ref().map(|(port, token)| format!("http://127.0.0.1:{port}/{token}/"))
}

/// Opens the page in the player's web browser; the address, or None when it could not start.
pub fn open_page(data_dir: &Path) -> Option<String> {
    let url = url(data_dir)?;
    open(&url);
    Some(url)
}

fn open(url: &str) {
    let mut command = if cfg!(windows) {
        let mut command = std::process::Command::new("rundll32");
        command.arg("url.dll,FileProtocolHandler").arg(url);
        command
    } else if cfg!(target_os = "macos") {
        let mut command = std::process::Command::new("open");
        command.arg(url);
        command
    } else {
        let mut command = std::process::Command::new("xdg-open");
        command.arg(url);
        command
    };
    if let Ok(mut child) = command.spawn() {
        std::thread::spawn(move || {
            let _ = child.wait();
        });
    }
}

// A secret for this session's address: whatever other programs or web pages try, they do not know it.
fn token() -> String {
    use std::hash::{BuildHasher, Hasher};
    let nanos = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .map_or(0, |elapsed| elapsed.as_nanos());
    let mut out = String::new();
    for round in 0..3u128 {
        let mut hasher = std::collections::hash_map::RandomState::new().build_hasher();
        hasher.write_u128(nanos.wrapping_add(round));
        out.push_str(&format!("{:016x}", hasher.finish()));
    }
    out
}

fn start(data_dir: PathBuf) -> Option<(u16, String)> {
    let listener = TcpListener::bind(("127.0.0.1", 0)).ok()?;
    let port = listener.local_addr().ok()?.port();
    let token = token();
    let secret = token.clone();
    std::thread::Builder::new()
        .name("serverify-web".into())
        .spawn(move || {
            for stream in listener.incoming().flatten() {
                if ACTIVE.fetch_add(1, Ordering::SeqCst) >= MAX_CONNECTIONS {
                    ACTIVE.fetch_sub(1, Ordering::SeqCst);
                    continue;
                }
                let (secret, data_dir) = (secret.clone(), data_dir.clone());
                std::thread::spawn(move || {
                    serve(stream, port, &secret, &data_dir);
                    ACTIVE.fetch_sub(1, Ordering::SeqCst);
                });
            }
        })
        .ok()?;
    Some((port, token))
}

fn serve(mut stream: TcpStream, port: u16, token: &str, data_dir: &Path) {
    use crate::game::serverify as m;
    let _ = stream.set_read_timeout(Some(Duration::from_secs(2)));
    let _ = stream.set_write_timeout(Some(Duration::from_secs(5)));
    let deadline = Instant::now() + Duration::from_secs(5);
    let mut bytes = Vec::new();
    let mut buffer = [0u8; 2048];
    let request = loop {
        match m::http::parse(&bytes) {
            Ok(Some(request)) => break request,
            Ok(None) => {}
            Err(message) => {
                let _ = stream.write_all(&m::http::text(400, message));
                return;
            }
        }
        if Instant::now() > deadline {
            return;
        }
        match stream.read(&mut buffer) {
            Ok(0) | Err(_) => return,
            Ok(read) => bytes.extend_from_slice(&buffer[..read]),
        }
    };
    let reply = match m::http::route(&request, port, token) {
        m::http::Route::Page => m::http::page(crate::game::serverify_page::PAGE),
        m::http::Route::Saved => {
            let saved: Vec<Value> = crate::game::serverify_files::saved(data_dir)
                .into_iter()
                .map(|(name, ip)| json!({"name": name, "ip": ip}))
                .collect();
            m::http::json(200, &Value::Array(saved))
        }
        m::http::Route::List(query) => match crate::game::serverify_net::search(&query) {
            Ok(servers) => {
                let list: Vec<Value> = servers
                    .iter()
                    .map(|s| json!({"name": s.name, "address": s.address(), "players": s.players, "max": s.max_players, "country": s.country, "icon": s.icon}))
                    .collect();
                m::http::json(200, &Value::Array(list))
            }
            Err(message) => m::http::json(502, &json!({"error": message})),
        },
        m::http::Route::Html(url) => match crate::game::serverify_net::html(&url) {
            Ok(text) => m::http::text(200, &text),
            Err(message) => m::http::text(502, &message),
        },
        m::http::Route::Icon(url) => match crate::game::serverify_net::icon(&url) {
            Ok((kind, bytes)) => m::http::image(&kind, &bytes),
            Err(message) => m::http::text(404, &message),
        },
        m::http::Route::Add { name, address } => match crate::game::serverify_files::add_entry(data_dir, &name, &address) {
            Ok(crate::game::serverify_files::Added::New(shown)) => {
                m::http::json(200, &json!({"ok": true, "message": format!("Added {shown} to Multiplayer")}))
            }
            Ok(crate::game::serverify_files::Added::Existing(shown)) => {
                m::http::json(200, &json!({"ok": true, "message": format!("{shown} is already in Multiplayer")}))
            }
            Err(message) => m::http::json(200, &json!({"ok": false, "message": message})),
        },
        m::http::Route::Reject(status, message) => m::http::text(status, message),
    };
    let _ = stream.write_all(&reply);
}
