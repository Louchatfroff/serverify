// Serverify's requests to Modrinth and the listing sites, made on threads of their own (never on the game's).
// This file becomes the module crate::game::serverify_net.
use std::io::Read;
use std::time::Duration;

fn call(url: &str) -> Result<ureq::Response, String> {
    ureq::get(url)
        .set("User-Agent", crate::game::serverify::USER_AGENT)
        .timeout(Duration::from_secs(10))
        .call()
        .map_err(|e| format!("The site did not answer ({e})"))
}

fn get(url: &str) -> Result<String, String> {
    call(url)?.into_string().map_err(|e| format!("The site did not answer ({e})"))
}

/// One page of servers: the search, then the servers' addresses and player counts.
pub fn search(query: &crate::game::serverify::Query) -> Result<Vec<crate::game::serverify::Server>, String> {
    use crate::game::serverify as m;
    let search = get(&m::search_url(query))?;
    let ids = m::parse_ids(&search)?;
    if ids.is_empty() {
        return Ok(Vec::new());
    }
    let projects = get(&m::projects_url(&ids))?;
    m::parse_servers(&search, &projects)
}

pub fn fetch(query: &crate::game::serverify::Query) -> crate::game::serverify::Reply {
    use crate::game::serverify as m;
    match search(query) {
        Ok(servers) => m::Reply::Servers(query.clone(), servers),
        Err(message) => m::Reply::Failed(message),
    }
}

/// A server's picture, for the page: only a small image of a kind a page can show.
pub fn icon(url: &str) -> Result<(String, Vec<u8>), String> {
    let response = call(url)?;
    let kind = response.content_type().to_owned();
    if !matches!(kind.as_str(), "image/webp" | "image/png" | "image/jpeg" | "image/gif") {
        return Err("not an image".to_owned());
    }
    let mut bytes = Vec::new();
    response
        .into_reader()
        .take(512 * 1024)
        .read_to_end(&mut bytes)
        .map_err(|e| format!("could not read the image ({e})"))?;
    Ok((kind, bytes))
}

/// A listing site's page, for the browser page to read: as text, at most 2 MB.
pub fn html(url: &str) -> Result<String, String> {
    let mut text = String::new();
    call(url)?
        .into_reader()
        .take(2 * 1024 * 1024)
        .read_to_string(&mut text)
        .map_err(|e| format!("could not read the page ({e})"))?;
    Ok(text)
}
