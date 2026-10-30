// The Multiplayer list (servers.dat in the game folder).
// This file becomes the module crate::game::serverify_files.
pub enum Added {
    New(String),
    Existing(String),
}

// data_dir is <game folder>/horizon-mods/.horizon-mod-data/serverify.
fn list_path(data_dir: &std::path::Path) -> Option<std::path::PathBuf> {
    Some(data_dir.ancestors().nth(3)?.join("servers.dat"))
}

pub fn add_entry(data_dir: &std::path::Path, name: &str, address: &str) -> Result<Added, String> {
    use crate::game::serverify as m;
    let path = list_path(data_dir).ok_or_else(|| "could not find the game folder".to_owned())?;
    let shown = m::clean(name, 32);
    let mut servers = horizon_protocol::servers::load(&path);
    if servers.iter().any(|s| s.ip.eq_ignore_ascii_case(address)) {
        return Ok(Added::Existing(shown));
    }
    servers.insert(0, horizon_protocol::servers::ServerEntry::new(&shown, address));
    horizon_protocol::servers::save(&path, &servers).map_err(|e| format!("could not save the server list ({e})"))?;
    Ok(Added::New(shown))
}

/// The message for chat.
pub fn add(data_dir: &std::path::Path, name: &str, address: &str) -> String {
    match add_entry(data_dir, name, address) {
        Ok(Added::New(shown)) => format!("Serverify: added {shown}. Open Multiplayer to join it"),
        Ok(Added::Existing(shown)) => format!("Serverify: {shown} is already in Multiplayer"),
        Err(message) => format!("Serverify: {message}"),
    }
}

/// The names and addresses in the Multiplayer list.
pub fn saved(data_dir: &std::path::Path) -> Vec<(String, String)> {
    let Some(path) = list_path(data_dir) else {
        return Vec::new();
    };
    horizon_protocol::servers::load(&path).into_iter().map(|s| (s.name, s.ip)).collect()
}
