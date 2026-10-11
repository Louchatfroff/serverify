export const REPO = process.env.GITHUB_REPO ?? "Louchatfroff/serverify";
const API = process.env.GITHUB_API ?? "https://api.github.com";
export const REPO_URL = `https://github.com/${REPO}`;

const ASSET = /^serverify-mc(.+?)-\d[^/]*(?<!-sources)\.jar$/;
const REVALIDATE_SECONDS = 300;

const byVersionDesc = (a, b) => b.mc.localeCompare(a.mc, undefined, { numeric: true });

export async function getLatestRelease() {
  try {
    const res = await fetch(`${API}/repos/${REPO}/releases/latest`, {
      headers: { Accept: "application/vnd.github+json" },
      next: { revalidate: REVALIDATE_SECONDS },
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
