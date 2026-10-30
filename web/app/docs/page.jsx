import Link from "next/link";
import { REPO_URL } from "../../lib/releases";

export default function DocsHome() {
  return (
    <>
      <h1>Serverify documentation</h1>
      <p className="lead">
        Serverify is a server browser for Minecraft. Pick the guide that matches the game you play.
      </p>

      <div className="choices">
        <Link href="/docs/java" className="choice">
          <strong>Java Edition</strong>
          <span>Fabric, Quilt, Forge and NeoForge, Minecraft 1.8.9 to 26.3. A browser built into the Multiplayer screen.</span>
        </Link>
        <Link href="/docs/horizon" className="choice">
          <strong>Horizon</strong>
          <span>The Rust rewrite of Minecraft. A native mod with a browser page and chat commands.</span>
        </Link>
      </div>

      <section id="which" aria-labelledby="which-title">
        <h2 id="which-title">Which one do I need?</h2>
        <ul className="plain">
          <li>
            You play Minecraft: Java Edition from the Minecraft launcher, with Fabric, Quilt, Forge or NeoForge: read the <Link href="/docs/java">Java Edition guide</Link>.
          </li>
          <li>
            You play <a href="https://playhorizon.dev">Horizon</a>: read the <Link href="/docs/horizon">Horizon guide</Link>. The Java Edition jars do not run in Horizon, and Horizon mods do not run in Java Edition.
          </li>
        </ul>
      </section>

      <section id="help" aria-labelledby="help-title">
        <h2 id="help-title">Need help?</h2>
        <p>
          Each guide ends with a troubleshooting list. For anything else, open an issue on <a href={`${REPO_URL}/issues`}>GitHub</a>. Say which game, loader and Minecraft version you use and attach <code>logs/latest.log</code>.
        </p>
      </section>
    </>
  );
}
