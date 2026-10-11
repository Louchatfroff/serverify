export const REPO = process.env.GITHUB_REPO ?? "Louchatfroff/serverify";
const API = process.env.GITHUB_API ?? "https://api.github.com";
export const REPO_URL = `https://github.com/${REPO}`;

const ASSET = /^serverify-mc(.+?)-\d[^/]*(?<!-sources)\.jar$/;
const ONE_HOUR = 3600;

const byVersionDesc = (a, b) => b.mc.localeCompare(a.mc, undefined, { numeric: true });

/** Latest release as { tag, downloads: [{ mc, url, size }] }; null when there is no release yet. */
export async function getLatestRelease() {
  try {
    const res = await fetch(`${API}/repos/${REPO}/releases/latest`, {
      headers: { Accept: "application/vnd.github+json" },
      next: { revalidate: ONE_HOUR },
    });
    if (!res.ok) return null;
    const release = await res.json();
    const downloads = release.assets
      .map((asset) => {
        const match = ASSET.exec(asset.name);
        return match && { mc: match[1], url: asset.browser_download_url, size: asset.size };
      })
      .filter(Boolean)
      .sort(byVersionDesc);
    return downloads.length ? { tag: release.tag_name, downloads } : null;
  } catch {
    return null;
  }
}
