import Image from "next/image";
import Link from "next/link";
import { REPO_URL } from "../../lib/releases";
import Footer from "../footer";

export const metadata = {
  title: "Documentation · Serverify",
  description: "How to install and use Serverify on Fabric, Quilt, Forge, NeoForge and Horizon.",
};

export default function DocsLayout({ children }) {
  return (
    <div className="wrap">
      <nav className="nav" aria-label="Main">
        <Link href="/" className="brand">
          <Image src="/logo.png" alt="" width={28} height={28} />
          Serverify
        </Link>
        <span className="links">
          <Link href="/docs">Docs</Link>
          <a href={REPO_URL}>GitHub</a>
        </span>
      </nav>
      <div className="docs">
        <aside className="toc" aria-label="Documentation">
          <strong>Documentation</strong>
          <Link href="/docs">Overview</Link>
          <Link href="/docs/java">Java Edition</Link>
          <Link href="/docs/horizon">Horizon</Link>
        </aside>
        <article className="doc">{children}</article>
      </div>
      <Footer />
    </div>
  );
}
