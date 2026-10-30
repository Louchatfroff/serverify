// /serverify finds servers on Modrinth and adds them to the Multiplayer list.
fn serverify_command(game: &mut crate::game::Game<'_>, command: &str, ran: &mut bool) {
    use crate::game::serverify as m;
    let Some(args) = command.strip_prefix("serverify") else {
        return;
    };
    if !game.native_on(m::ID) || !(args.is_empty() || args.starts_with(' ')) {
        return;
    }
    *ran = true;
    let offline_only = game.native_setting(m::ID, "offline_only").as_deref() != Some("false");
    match m::plan(args, offline_only) {
        m::Plan::Say(lines) => {
            for line in lines {
                game.native_chat(&line);
            }
        }
        m::Plan::Fetch(query) => {
            if !m::begin() {
                game.native_chat("Serverify: still loading, try again in a moment");
                return;
            }
            game.native_chat("Serverify: loading...");
            std::thread::spawn(move || m::push_reply(crate::game::serverify_net::fetch(&query)));
        }
        m::Plan::Web => {
            let line = match crate::game::serverify_web::open_page(&game.native_data_dir(m::ID)) {
                Some(url) => format!("Serverify: opened the server browser: {url}"),
                None => "Serverify: could not start the page on this computer".to_owned(),
            };
            game.native_chat(&line);
        }
        m::Plan::Add(number) => {
            let line = match m::pick(number) {
                Ok(server) => crate::game::serverify_files::add(&game.native_data_dir(m::ID), &server.name, &server.address()),
                Err(message) => message,
            };
            game.native_chat(&line);
        }
    }
}
