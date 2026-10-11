import Image from "next/image";
import { REPO, REPO_URL, getLatestRelease } from "../lib/releases";
import Download from "./download";

const shot = (name) => `https://raw.githubusercontent.com/${REPO}/main/docs/screenshots/${name}.png`;

const FEATURES = [
  ["Browse without leaving the game", "A server browser with large cards, categories, search and sorting, built into the Multiplayer screen."],
  ["Live status", "Real ping, player count, version and the server's own MOTD, refreshed automatically."],
  ["Join or save", "Connect straight away, or add the server to your Multiplayer list in one click."],
  ["Favorites", "Keep the servers you like in a local list that works offline."],
  ["Several sources", "minecraft-list.info first, then more sites, with duplicates removed."],
  ["Private by design", "Client-side only. No account, no telemetry."],
];

export default async function Home() {
  const release = await getLatestRelease();

  return (
    <div className="wrap">
      <nav className="nav" aria-label="Main">
        <span>Serverify</span>
        <a href={REPO_URL}>GitHub</a>
      </nav>

      <header className="hero">
        <h1>Find a Minecraft server. Join it.</h1>
        <p>Serverify is a Fabric mod that adds a fast, native server browser to the Multiplayer screen.</p>
        <Download
          downloads={release?.downloads ?? []}
          tag={release?.tag}
          releasesUrl={`${REPO_URL}/releases/latest`}
        />
      </header>

      <main>
        <div className="shot">
          <Image src={shot("browser")} alt="The Serverify server browser with category sidebar and server cards" width={854} height={480} priority />
        </div>

        <section aria-labelledby="features">
          <h2 id="features">What you get</h2>
          <ul className="features">
            {FEATURES.map(([title, text]) => (
              <li key={title}>
                <strong>{title}</strong>
                {text}
              </li>
            ))}
          </ul>
          <div className="shot">
            <Image src={shot("detail")} alt="A server page showing live ping, player count and the server message" width={854} height={480} />
          </div>
        </section>

        <section id="install" aria-labelledby="install-title">
          <h2 id="install-title">Install</h2>
          <ol className="steps">
            <li>
              Install <a href="https://fabricmc.net/use/installer/">Fabric Loader</a> for your Minecraft version.
            </li>
            <li>
              Download <a href="https://modrinth.com/mod/fabric-api">Fabric API</a> and <a href="https://modrinth.com/mod/fabric-language-kotlin">Fabric Language Kotlin</a> for the same version.
            </li>
            <li>
              Put all three files, including the Serverify download above, in your <code>.minecraft/mods</code> folder.
            </li>
            <li>
              Start the game, open <strong>Multiplayer</strong> and press the <strong>Serverify</strong> button.
            </li>
          </ol>
          <p className="note">
            Optional: <a href="https://modrinth.com/mod/modmenu">Mod Menu</a> adds a settings screen. Each download works for the Minecraft
            versions in its name (for example 26.1.x covers 26.1, 26.1.1 and 26.1.2).
          </p>
        </section>
      </main>

      <footer>
        Serverify by <a href="https://github.com/Louchatfroff">Louchatfroff</a>. Free software under the <a href="https://www.gnu.org/licenses/gpl-3.0.html">GNU GPL v3</a>. Interface heavily inspired by <a href="https://github.com/DeDiamondPro/Resourcify">Resourcify</a> by DeDiamondPro. Written with AI assistance. No AI-generated or stolen assets. Not affiliated with Mojang or Microsoft. Server listings come from third-party sites.
      </footer>
    </div>
  );
}
