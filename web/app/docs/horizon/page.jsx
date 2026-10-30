import { REPO_URL } from "../../../lib/releases";

export const metadata = {
  title: "Horizon guide · Serverify",
  description: "Install and use Serverify in Horizon, the Rust rewrite of Minecraft.",
};

const HORIZON_SOURCE = `${REPO_URL}/tree/main/horizon/serverify`;

const COMMANDS = [
  ["/serverify", "Shows the help."],
  ["/serverify web", "Opens the server browser page and prints its address in chat."],
  ["/serverify list [category]", "The most played servers, optionally of one category."],
  ["/serverify search <words>", "Searches servers by name."],
  ["/serverify next, /serverify prev", "The next or previous page of ten."],
  ["/serverify add <number>", "Adds a listed server to your Multiplayer list."],
  ["/serverify categories", "Lists the categories."],
];

export default function HorizonGuide() {
  return (
    <>
      <h1>Serverify for Horizon</h1>
      <p className="lead">
        <a href="https://playhorizon.dev">Horizon</a> is a rewrite of Minecraft in Rust. Serverify for Horizon is a native mod: it finds servers on Modrinth and adds them to your Multiplayer list, from a page in your web browser or from chat.
      </p>

      <ul className="onpage" aria-label="On this page">
        <li><a href="#install">Install</a></li>
        <li><a href="#use">Use it</a></li>
        <li><a href="#commands">Commands</a></li>
        <li><a href="#settings">Settings</a></li>
        <li><a href="#safety">Safety and privacy</a></li>
        <li><a href="#limits">What is different</a></li>
        <li><a href="#troubleshooting">Troubleshooting</a></li>
        <li><a href="#developers">For developers</a></li>
      </ul>

      <section id="install" aria-labelledby="install-title">
        <h2 id="install-title">Install</h2>
        <p>You need the Horizon launcher 0.2.23 or later, with the game prepared at least once.</p>
        <ol className="steps">
          <li>
            In the launcher, open <strong>Mods</strong>, find <strong>Serverify</strong> and press <strong>Install</strong>.
          </li>
          <li>
            The launcher explains that native mods are compiled into a game of their own, and turns on <strong>Settings &gt; Native mods (experimental)</strong>.
          </li>
          <li>
            There press <strong>Build</strong>. Read the red warning that names the mods, then choose <strong>I trust these mods: build</strong>. The first build downloads Rust and takes 20 to 30 minutes. Later builds take a few minutes.
          </li>
          <li>
            Pick the <strong>Native mods</strong> instance in the launcher and press <strong>Play</strong>. It has its own worlds and options.
          </li>
        </ol>
        <p>
          Until Serverify is listed in the registry, or to build it yourself, use the launcher&apos;s command line on the mod&apos;s folder (the first build is as slow as above):
        </p>
        <pre><code>{`horizon-launcher native-build /path/to/serverify/mixins/serverify --trust-native-code`}</code></pre>
        <p className="note">
          Serverify is a native mod, so it is not sandboxed: it can do what you can do on your computer. Its source is public, and the registry&apos;s reviewers read all of it before they list it. See <a href="#safety">Safety and privacy</a>.
        </p>
      </section>

      <section id="use" aria-labelledby="use-title">
        <h2 id="use-title">Use it</h2>
        <p>Open the server browser page in any of three ways:</p>
        <ul className="plain">
          <li>
            Press <kbd>F8</kbd>. It works on any screen, the title screen and Multiplayer included. Change the key in <strong>Options &gt; Mods</strong>.
          </li>
          <li>
            Type <code>/serverify web</code> in a world.
          </li>
          <li>
            Open <strong>Options &gt; Mods &gt; Serverify &gt; Settings</strong> and press <strong>Open the server browser</strong>.
          </li>
        </ul>
        <p>
          Your web browser opens a page with a search box, a category menu and cards showing each server&apos;s picture, address, player count and country. Press <strong>Add</strong> on a server: it goes to the top of your Multiplayer list. Open <strong>Multiplayer</strong> in the game to join it. A server already in your list shows <em>In your list</em>.
        </p>
        <p>
          The page is only a display. The game does every search and fetches every picture, and the page talks to nothing but the game.
        </p>
      </section>

      <section id="commands" aria-labelledby="commands-title">
        <h2 id="commands-title">Commands</h2>
        <p>Type them in chat in a world. Answers appear as chat lines only you see.</p>
        <div className="tablewrap">
          <table>
            <thead>
              <tr>
                <th scope="col">Command</th>
                <th scope="col">What it does</th>
              </tr>
            </thead>
            <tbody>
              {COMMANDS.map(([command, text]) => (
                <tr key={command}>
                  <td><code>{command}</code></td>
                  <td>{text}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p>
          Categories: survival, smp, vanilla, skyblock, prison, factions, pvp, kitpvp, lifesteal, oneblock, towny, economy, rpg, creative, minigames, bedwars, parkour, cobblemon, pixelmon, modded, hardcore, anarchy, cracked and roleplay.
        </p>
      </section>

      <section id="settings" aria-labelledby="settings-title">
        <h2 id="settings-title">Settings</h2>
        <p>
          <strong>Options &gt; Mods</strong> lists Serverify with a switch. Turn it off and it does nothing until you turn it on again. Its <strong>Settings</strong> page has:
        </p>
        <ul className="plain">
          <li>
            <strong>Open the server browser.</strong>
          </li>
          <li>
            <strong>Offline-mode servers only</strong> (on by default). Horizon cannot join servers that check Minecraft accounts yet, which is most public servers, so the list shows the servers that accept offline-mode players. Turn it off to also see servers from top-mc-servers.net, minecraftservers.org and minecraft-mp.com, knowing most of them will not let Horizon join. With it on, the page also shows top-mc-servers.net&apos;s list of cracked servers when no category is picked.
          </li>
        </ul>
        <p>
          The setting is saved in <code>horizon-mods/.horizon-mod-data/serverify/</code> in the game folder, one file per setting.
        </p>
      </section>

      <section id="safety" aria-labelledby="safety-title">
        <h2 id="safety-title">Safety and privacy</h2>
        <ul className="plain">
          <li>
            <strong>Modrinth.</strong> The game asks <code>api.modrinth.com</code> for the list (two requests per page) and <code>cdn.modrinth.com</code> for a server&apos;s picture, only when you look for servers. It sends nothing about you.
          </li>
          <li>
            <strong>Three listing sites.</strong> When the offline-mode filter is off, the game also fetches one public page of <code>top-mc-servers.net</code>, <code>minecraftservers.org</code> and <code>minecraft-mp.com</code> for each page of results, and their server pictures. The game builds those addresses itself, so the page cannot ask for any other address. The page reads the fetched pages with the browser&apos;s own HTML parser. Promoted and premium rows are skipped.
          </li>
          <li>
            <strong>A page on your computer.</strong> The first time you open the page, the mod starts a small web server on <code>127.0.0.1</code>, this computer only and never the network. Its address contains a random secret made for that session. The page only talks to that server. A request that comes with another host name, another origin or no secret is refused. The server can show the list, send a picture and add one server to your Multiplayer list, nothing else.
          </li>
          <li>
            <strong>Your Multiplayer list.</strong> Adding a server writes one entry to <code>servers.dat</code> in the game folder, with the name cleaned of colour codes and control characters.
          </li>
          <li>
            <strong>Source.</strong> The code is on <a href={HORIZON_SOURCE}>GitHub</a>, GPL-3.0-only. The file you install is that source, built by the launcher on your computer.
          </li>
        </ul>
      </section>

      <section id="limits" aria-labelledby="limits-title">
        <h2 id="limits-title">What is different from the Java Edition mod</h2>
        <p>
          Horizon&apos;s native hook points cover the HUD, keys, commands and settings pages. They have no screens, and no buttons on the title or Multiplayer screens. So Serverify for Horizon shows its screen in your web browser and has no button in Multiplayer.
        </p>
        <ul className="plain">
          <li>The page lists Modrinth and three listing sites; the chat commands list Modrinth only. minecraft-list.info, the region filter, the well known servers, favorites and live ping are not part of it.</li>
          <li>You join from Horizon&apos;s own Multiplayer screen, after adding the server.</li>
          <li>
            Adding a server rewrites your server list file, which drops the hidden direct-join entries Horizon keeps. Your saved servers stay.
          </li>
        </ul>
      </section>

      <section id="troubleshooting" aria-labelledby="troubleshooting-title">
        <h2 id="troubleshooting-title">Troubleshooting</h2>
        <dl className="faq">
          <dt>Serverify is not in Options &gt; Mods.</dt>
          <dd>Build the Native mods instance (Settings &gt; Native mods &gt; Build) and play that instance, not your normal one.</dd>
          <dt>F8 does nothing.</dt>
          <dd>
            Check that Serverify is switched on in Options &gt; Mods, and that no other mod uses F8: rebind the key on Serverify&apos;s row. <code>/serverify web</code> and the settings button do the same thing.
          </dd>
          <dt>The page does not open.</dt>
          <dd>
            Type <code>/serverify web</code>. The address is printed in chat: copy it into your browser. Open it from the same computer the game runs on.
          </dd>
          <dt>The page says the game could not get the list.</dt>
          <dd>Check that you are online. Press Search again in a moment. A listing site that is down or has changed its pages is skipped, and the others still show. If it keeps failing, open an issue with your <code>logs/latest.log</code>.</dd>
          <dt>No servers show up.</dt>
          <dd>Clear the search and the category. Few servers match some categories while the offline-mode filter is on.</dd>
          <dt>I added a server and cannot see it.</dt>
          <dd>Close and reopen the Multiplayer screen so that it reads the list again.</dd>
          <dt>I cannot join a server I added.</dt>
          <dd>The server checks Minecraft accounts (online mode), and Horizon cannot sign in yet. Use servers that accept offline-mode players.</dd>
          <dt>The build fails after a Horizon update.</dt>
          <dd>Serverify uses only Horizon&apos;s stable hook points, so it should build on every Nightly. If it does not, open an issue with the launcher&apos;s build log.</dd>
        </dl>
      </section>

      <section id="developers" aria-labelledby="developers-title">
        <h2 id="developers-title">For developers</h2>
        <p>
          The mod is a <a href="https://github.com/horizon-rust/horizon-mixins">horizon-mixins</a> bundle in <a href={HORIZON_SOURCE}><code>horizon/serverify</code></a>. It hooks <code>native_tick</code>, <code>native_key</code>, <code>native_command</code>, <code>native_mods</code>, <code>native_settings</code> and <code>native_settings_click</code> (hook points version 1) and builds on every Horizon that has them. The logic is plain Rust you can test on its own:
        </p>
        <pre><code>{`cargo test
python3 native/tools/native.py check /path/to/serverify
python3 native/tools/native.py package /path/to/serverify`}</code></pre>
        <p>
          The last two commands run from a clone of <a href="https://github.com/horizon-rust/horizon-mods">horizon-mods</a>. Horizon&apos;s guide to <a href="https://playhorizon.dev/docs/make-a-mod">making a mod</a> explains the rest.
        </p>
      </section>
    </>
  );
}
