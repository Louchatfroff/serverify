import { REPO_URL } from "../../../lib/releases";
import { TARGETS } from "../../../lib/targets";

export const metadata = {
  title: "Java Edition guide · Serverify",
  description: "Install and use Serverify on Fabric, Quilt, Forge and NeoForge, Minecraft 1.8.9 to 26.3.",
};

const LOADERS = [
  ["fabric", "Fabric and Quilt"],
  ["forge", "Forge"],
  ["neoforge", "NeoForge"],
];

const CONFIG = [
  ["modrinthEnabled", "true", "Load servers from Modrinth."],
  ["minecraftListInfoEnabled", "true", "Load servers from minecraft-list.info."],
  ["topMcServersEnabled", "true", "Load servers from top-mc-servers.net."],
  ["moreSourcesEnabled", "true", "Load servers from minecraftservers.org and minecraft-mp.com."],
  ["region", "\"auto\"", "The region filter: \"auto\" (your system language), \"all\", or NORTH_AMERICA, SOUTH_AMERICA, EUROPE, ASIA, OCEANIA or AFRICA."],
  ["refreshSeconds", "45", "How often a server page refreshes its status, from 30 to 60."],
];

export default function JavaGuide() {
  return (
    <>
      <h1>Serverify for Java Edition</h1>
      <p className="lead">
        A server browser inside the Multiplayer screen. It works on Fabric, Quilt, Forge and NeoForge, for Minecraft 1.8.9 to 26.3, with one download for each loader and version.
      </p>

      <ul className="onpage" aria-label="On this page">
        <li><a href="#install">Install</a></li>
        <li><a href="#downloads">Which download</a></li>
        <li><a href="#use">Use it</a></li>
        <li><a href="#sources">Where the servers come from</a></li>
        <li><a href="#settings">Settings and files</a></li>
        <li><a href="#privacy">Privacy</a></li>
        <li><a href="#troubleshooting">Troubleshooting</a></li>
      </ul>

      <section id="install" aria-labelledby="install-title">
        <h2 id="install-title">Install</h2>
        <ol className="steps">
          <li>Install the mod loader for your Minecraft version (below).</li>
          <li>
            Download the Serverify jar for your loader and Minecraft version from the <a href={`${REPO_URL}/releases/latest`}>latest release</a> (the <a href="/#install">home page</a> picks it for you).
          </li>
          <li>
            Put the jar in your <code>mods</code> folder: <code>.minecraft/mods</code>, or the <code>mods</code> folder of the instance if you use Prism Launcher, CurseForge or another launcher.
          </li>
          <li>Start the game, open <strong>Multiplayer</strong> and press the globe button next to <strong>Add Server</strong>.</li>
        </ol>

        <h3>Fabric and Quilt</h3>
        <ul className="plain">
          <li>
            Install <a href="https://fabricmc.net/use/installer/">Fabric Loader</a> (or Quilt).
          </li>
          <li>
            Add <a href="https://modrinth.com/mod/fabric-api">Fabric API</a> and <a href="https://modrinth.com/mod/fabric-language-kotlin">Fabric Language Kotlin</a>, both for your Minecraft version. Serverify&apos;s interface is written in Kotlin and Fabric needs this one to run it.
          </li>
          <li>
            Optional: <a href="https://modrinth.com/mod/modmenu">Mod Menu</a> adds a settings screen for Serverify.
          </li>
          <li>Quilt loads the Fabric jars. It has not been tested separately.</li>
        </ul>

        <h3>Forge</h3>
        <ul className="plain">
          <li>
            Install <a href="https://files.minecraftforge.net/">Forge</a> for your Minecraft version. Serverify carries its own Kotlin, so nothing else is needed.
          </li>
          <li>Forge builds exist for 1.8.9, 1.12.2, 1.16.5, 1.17.1, 1.18.2, 1.19.2, 1.19.4, 1.20.1, 1.20.4, 1.20.6, 1.21.1, 1.21.4 and 1.21.5.</li>
          <li>On 1.8.9 and 1.12.2 the game runs on Java 8. The interface is the same as on newer versions.</li>
        </ul>

        <h3>NeoForge</h3>
        <ul className="plain">
          <li>
            Install <a href="https://neoforged.net/">NeoForge</a> for your Minecraft version. Nothing else is needed.
          </li>
          <li>NeoForge builds exist for 1.20.4, 1.20.6, 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.11, 26.1, 26.2 and 26.3.</li>
        </ul>
      </section>

      <section id="downloads" aria-labelledby="downloads-title">
        <h2 id="downloads-title">Which download</h2>
        <p>
          Jars are named <code>serverify-&lt;loader&gt;-mc&lt;version&gt;-&lt;release&gt;.jar</code>. Each one works for the Minecraft versions in the table, so for example the 1.21.4 jar also runs on 1.21.2 and 1.21.3. Use the Java version in the last column or newer.
        </p>
        {LOADERS.map(([loader, title]) => (
          <div key={loader} className="tablewrap">
            <table>
              <caption>{title}</caption>
              <thead>
                <tr>
                  <th scope="col">Jar for</th>
                  <th scope="col">Works on Minecraft</th>
                  <th scope="col">Java</th>
                </tr>
              </thead>
              <tbody>
                {TARGETS.filter((t) => t.loader === loader).map((t) => (
                  <tr key={`${t.loader}-${t.jar}`}>
                    <td><code>mc{t.jar}</code></td>
                    <td>{t.covers}</td>
                    <td>{t.java}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
        <p className="note">Java 8 runs 1.8.9 to 1.16.5, Java 16 runs 1.17, Java 17 runs 1.18 to 1.20.4, Java 21 runs 1.20.5 to 1.21.11 and Java 25 runs 26.x.</p>
      </section>

      <section id="use" aria-labelledby="use-title">
        <h2 id="use-title">Use it</h2>

        <h3>Open the browser</h3>
        <p>
          Open <strong>Multiplayer</strong> and press the small globe button to the right of <strong>Add Server</strong>. On narrow windows where it does not fit, it sits in the top right corner instead.
        </p>

        <h3>Browse</h3>
        <ul className="plain">
          <li>
            <strong>Cards.</strong> Two columns of servers with a colored banner, icon, name, version, country, player count and description. Scroll down for more.
          </li>
          <li>
            <strong>Search.</strong> Type in the search box. It searches a moment after you stop typing. <kbd>Ctrl</kbd> + <kbd>F</kbd> jumps to it.
          </li>
          <li>
            <strong>Sort.</strong> The menu in the top right sorts by Popular, Newest, Players or Name.
          </li>
          <li>
            <strong>Categories.</strong> Tick one category in the sidebar (Survival, SkyBlock, Prison, PvP, Towny, Cobblemon and more). Tick it again to clear it. When you pick a category, the best known servers of that category come first, with live player counts.
          </li>
          <li>
            <strong>Show.</strong> <em>Favorites</em> lists the servers you saved. <em>Online only</em> hides servers that did not answer.
          </li>
          <li>
            <strong>Region.</strong> Limits the list to one part of the world, to avoid high ping and languages you do not read. It starts on the region of your system language (if your language has no country, such as plain <code>en</code>, it starts on <em>All regions</em>), and servers with an unknown country always stay in the list. The choice is saved.
          </li>
          <li>
            <strong>Back and forward.</strong> The arrows at the top of the sidebar move between the pages you opened. <em>Close</em> returns to Multiplayer.
          </li>
        </ul>

        <h3>A server page</h3>
        <p>Click a card to open its page.</p>
        <ul className="plain">
          <li>
            <strong>Live status.</strong> The page pings the server with Minecraft&apos;s own server ping and shows real latency, player count, version and the server&apos;s message of the day, with its colors. It refreshes every 30 to 60 seconds. <em>Refresh status</em> pings now.
          </li>
          <li>
            <strong>Join server</strong> connects right away. <strong>Add to Multiplayer</strong> saves it in your server list. <strong>Copy IP</strong> copies the address.
          </li>
          <li>
            <strong>Add to favorites</strong> keeps it in the local favorites list, which works offline.
          </li>
          <li>
            <kbd>Esc</kbd> goes back to the list.
          </li>
        </ul>
      </section>

      <section id="sources" aria-labelledby="sources-title">
        <h2 id="sources-title">Where the servers come from</h2>
        <ol className="steps">
          <li>
            <strong>Well known servers</strong> of the category you picked (a short list built into the mod, with live status from <a href="https://mcstatus.io">mcstatus.io</a>).
          </li>
          <li>
            <a href="https://modrinth.com/discover/servers">Modrinth</a> server projects. Modrinth only lists servers that answer a live ping.
          </li>
          <li>
            <a href="https://minecraft-list.info">minecraft-list.info</a>.
          </li>
          <li>
            <a href="https://top-mc-servers.net">top-mc-servers.net</a>, read from its public pages.
          </li>
          <li>
            <a href="https://minecraftservers.org">minecraftservers.org</a> and <a href="https://minecraft-mp.com">minecraft-mp.com</a>, read from their public pages.
          </li>
        </ol>
        <p>
          A server that several sources list shows once, even when they spell its address differently (<code>play.example.com</code>, <code>mc.example.com</code>, <code>example.com</code>). Promoted and premium rows of the scraped sites are skipped. If a source is down or changes its pages, the others keep working. Each source can be turned off in the settings.
        </p>
      </section>

      <section id="settings" aria-labelledby="settings-title">
        <h2 id="settings-title">Settings and files</h2>
        <p>
          On Fabric and Quilt, <a href="https://modrinth.com/mod/modmenu">Mod Menu</a> opens a settings screen: one switch for each source and the status refresh time. On Forge and NeoForge, edit the file instead.
        </p>
        <p>
          Settings live in <code>config/serverify.json</code>. Close the game before you edit it.
        </p>
        <div className="tablewrap">
          <table>
            <thead>
              <tr>
                <th scope="col">Key</th>
                <th scope="col">Default</th>
                <th scope="col">What it does</th>
              </tr>
            </thead>
            <tbody>
              {CONFIG.map(([key, value, text]) => (
                <tr key={key}>
                  <td><code>{key}</code></td>
                  <td><code>{value}</code></td>
                  <td>{text}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p>
          Favorites are stored in <code>config/serverify/favorites.json</code>. Delete the file to clear them. Servers you add to Multiplayer go into the game&apos;s own <code>servers.dat</code>.
        </p>
      </section>

      <section id="privacy" aria-labelledby="privacy-title">
        <h2 id="privacy-title">Privacy</h2>
        <p>Serverify runs on your computer only. It adds nothing to servers, needs no account and sends no telemetry. It contacts:</p>
        <ul className="plain">
          <li>The sources listed above, when you open the browser or change the list, to read server lists.</li>
          <li>mcstatus.io, for the live status of the well known servers of a category.</li>
          <li>
            The servers you open, with Minecraft&apos;s own server ping, for a server page.
          </li>
          <li>Server icons come from the site that lists the server.</li>
        </ul>
        <p>Server listings come from third-party sites and can change or stop working without notice.</p>
      </section>

      <section id="troubleshooting" aria-labelledby="troubleshooting-title">
        <h2 id="troubleshooting-title">Troubleshooting</h2>
        <dl className="faq">
          <dt>There is no globe button on the Multiplayer screen.</dt>
          <dd>
            Check that the jar matches your loader and Minecraft version (see <a href="#downloads">Which download</a>). On Fabric and Quilt, make sure Fabric API and Fabric Language Kotlin are installed. Look for Serverify in the game&apos;s mod list and in <code>logs/latest.log</code>.
          </dd>
          <dt>The game says Fabric Language Kotlin is missing.</dt>
          <dd>
            Install <a href="https://modrinth.com/mod/fabric-language-kotlin">Fabric Language Kotlin</a> for your Minecraft version. Forge and NeoForge do not need it.
          </dd>
          <dt>The game crashes at start with an unsupported class file version.</dt>
          <dd>Your Java is too old for that Minecraft version. Use the Java version in the table above or newer.</dd>
          <dt>The list is empty or short.</dt>
          <dd>
            Switch the region to <em>All regions</em>, untick <em>Online only</em> and clear the category. Check that your sources are on in the settings and that you are online.
          </dd>
          <dt>A server shows as offline but it is up.</dt>
          <dd>
            The status comes from a ping made from your computer. Some servers block or rate limit pings (Hypixel does), and a server may be slow to answer. Press <em>Refresh status</em> or try to join.
          </dd>
          <dt>A server asks for a different Minecraft version.</dt>
          <dd>The server page shows the versions the server accepts. Join with a matching game version.</dd>
        </dl>
        <p>
          Still stuck? Open an issue on <a href={`${REPO_URL}/issues`}>GitHub</a> with your loader, Minecraft version and <code>logs/latest.log</code>.
        </p>
      </section>

      <section id="source" aria-labelledby="source-title">
        <h2 id="source-title">Build from source</h2>
        <p>
          The <a href={`${REPO_URL}#build-from-source`}>README</a> explains how to build any loader and version from source. Releases and changelogs are on <a href={`${REPO_URL}/releases`}>GitHub</a>.
        </p>
      </section>
    </>
  );
}
