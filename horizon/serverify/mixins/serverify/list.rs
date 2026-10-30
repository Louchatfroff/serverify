// The mod's row in Options > Mods.
fn serverify_listed(mods: &mut Vec<crate::game::native_hooks::NativeMod>) {
    use crate::game::serverify as m;
    mods.push(crate::game::native_hooks::NativeMod {
        id: m::ID.into(),
        title: format!("Serverify {}", m::VERSION),
        description: "Find servers and add them to Multiplayer, from a page on this computer (the key, /serverify web, or Settings) or with /serverify. The game asks api.modrinth.com only when you look for servers.".into(),
        keys: vec![["open".into(), "Open the server browser".into(), "key.keyboard.f8".into()]],
    });
}
