// Prints the answers of Serverify's requests in chat, in the window's game.
fn serverify_tick(game: &mut crate::game::Game<'_>) {
    use crate::game::serverify as m;
    if !game.native_on(m::ID) || !game.native_window() {
        return;
    }
    for line in m::answers() {
        game.native_chat(&line);
    }
}
