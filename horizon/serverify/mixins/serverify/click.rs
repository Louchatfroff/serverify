// A click on the settings page: open the server browser, or show only the servers Horizon can
// join, or all.
fn serverify_click(game: &mut crate::game::Game<'_>, mod_id: &str, button: usize) {
    use crate::game::serverify as m;
    if mod_id != m::ID {
        return;
    }
    match button {
        0 => {
            let _ = crate::game::serverify_web::open_page(&game.native_data_dir(m::ID));
        }
        1 => {
            let offline_only = game.native_setting(m::ID, "offline_only").as_deref() != Some("false");
            game.native_set_setting(m::ID, "offline_only", if offline_only { "false" } else { "true" });
        }
        _ => {}
    }
}
