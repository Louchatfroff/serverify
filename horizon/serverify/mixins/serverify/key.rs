// The key chosen in Options > Mods opens the server browser page, on any screen.
fn serverify_key(game: &mut crate::game::Game<'_>, key: &str, pressed: bool) {
    use crate::game::serverify as m;
    if !pressed || !game.native_on(m::ID) || key != game.native_mod_key(m::ID, "open", "key.keyboard.f8") {
        return;
    }
    let line = match crate::game::serverify_web::open_page(&game.native_data_dir(m::ID)) {
        Some(url) => format!("Serverify: opened {url}"),
        None => "Serverify: could not start the page on this computer".to_owned(),
    };
    if game.native_playing() {
        game.native_chat(&line);
    }
}
