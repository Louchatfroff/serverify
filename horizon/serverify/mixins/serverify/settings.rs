// The mod's settings page: a button that opens the server browser, and one setting.
fn serverify_page(game: &mut crate::game::Game<'_>, mod_id: &str, buttons: &mut Vec<String>) {
    use crate::game::serverify as m;
    if mod_id != m::ID {
        return;
    }
    let offline_only = game.native_setting(m::ID, "offline_only").as_deref() != Some("false");
    buttons.push("Open the server browser".to_owned());
    buttons.push(format!("Offline-mode servers only: {}", if offline_only { "ON" } else { "OFF" }));
}
