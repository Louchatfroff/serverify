import Image from "next/image";
import { REPO, REPO_URL, getLatestRelease } from "../lib/releases";
import Link from "next/link";
import Download from "./download";
import Footer from "./footer";

const shot = (name) => `https://raw.githubusercontent.com/${REPO}/main/docs/screenshots/${name}.png`;

const FEATURES = [
  ["Browse without leaving the game", "A server browser with large cards, categories, search and sorting, built into the Multiplayer screen."],
  ["Live status", "Real ping, player count, version and the server's own MOTD, refreshed automatically."],
  ["Join or save", "Connect straight away, or add the server to your Multiplayer list in one click."],
  ["Favorites", "Keep the servers you like in a local list that works offline."],
  ["Several sources", "Modrinth first, then more sites, with duplicates removed."],
  ["Private by design", "Client-side only. No account, no telemetry."],
];

export default async function Home() {
  const release = await getLatestRelease();

  return (
    <div className="wrap">
      <nav className="nav" aria-label="Main">
        <span className="brand">
          <Image src="/logo.png" alt="" width={28} height={28} />
          Serverify
        </span>
        <span className="links">
          <Link href="/docs">Docs</Link>
          <a href={REPO_URL}>GitHub</a>
        </span>
      </nav>

      <header className="hero">
        <h1>Find a Minecraft server. Join it.</h1>
        <p>Serverify is a client-side mod for Fabric, Quilt, Forge and NeoForge that adds a fast, native server browser to the Multiplayer screen.</p>
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
              Install your mod loader for your Minecraft version: <a href="https://fabricmc.net/use/installer/">Fabric</a> (Quilt also works), <a href="https://files.minecraftforge.net/">Forge</a> or <a href="https://neoforged.net/">NeoForge</a>.
            </li>
            <li>
              On Fabric or Quilt, also download <a href="https://modrinth.com/mod/fabric-api">Fabric API</a> and <a href="https://modrinth.com/mod/fabric-language-kotlin">Fabric Language Kotlin</a> for the same version. Forge and NeoForge need nothing else.
            </li>
            <li>
              Put the Serverify download above, and the Fabric files if you use Fabric, in your <code>.minecraft/mods</code> folder.
            </li>
            <li>
              Start the game, open <strong>Multiplayer</strong> and press the globe button next to <strong>Add Server</strong>.
            </li>
          </ol>
          <p className="note">
            Optional: <a href="https://modrinth.com/mod/modmenu">Mod Menu</a> adds a settings screen. Each download works for the Minecraft
            versions in its name (for example 26.1.x covers 26.1, 26.1.1 and 26.1.2). Read the <Link href="/docs/java">Java Edition guide</Link>, or the <Link href="/docs/horizon">Horizon guide</Link> if you play Horizon.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
