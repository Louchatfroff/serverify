"use client";

import { useState } from "react";

const LOADERS = { fabric: "Fabric (also Quilt)", forge: "Forge", neoforge: "NeoForge" };
const label = (mc) => (mc.split(".").length === 2 ? `${mc}.x` : mc);

export default function Download({ downloads, tag, releasesUrl }) {
  const loaders = Object.keys(LOADERS).filter((l) => downloads.some((d) => d.loader === l));
  const [loader, setLoader] = useState(loaders[0]);
  const versions = downloads.filter((d) => d.loader === loader);
  const [mc, setMc] = useState(versions[0]?.mc);
  const current = versions.find((d) => d.mc === mc) ?? versions[0];

  return (
    <div>
      <div className="actions">
        {loaders.length > 1 && (
          <select
            aria-label="Mod loader"
            value={loader}
            onChange={(e) => {
              setLoader(e.target.value);
              setMc(downloads.find((d) => d.loader === e.target.value)?.mc);
            }}
          >
            {loaders.map((l) => (
              <option key={l} value={l}>
                {LOADERS[l]}
              </option>
            ))}
          </select>
        )}
        {versions.length > 1 && (
          <select aria-label="Minecraft version" value={current?.mc} onChange={(e) => setMc(e.target.value)}>
            {versions.map((d) => (
              <option key={d.mc} value={d.mc}>
                Minecraft {label(d.mc)}
              </option>
            ))}
          </select>
        )}
        <a className="btn" href={current ? current.url : releasesUrl} download={Boolean(current)}>
          {current ? `Download for ${label(current.mc)}` : "Download"}
        </a>
        <a className="btn ghost" href="#install">
          Install
        </a>
      </div>
      <p className="note">
        {current
          ? `${tag} · ${(current.size / 1024).toFixed(0)} KB · ${LOADERS[current.loader]}`
          : "No release published yet. Releases are built automatically for every supported version."}
      </p>
    </div>
  );
}
