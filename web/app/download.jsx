"use client";

import { useState } from "react";

const label = (mc) => (mc.split(".").length === 2 ? `${mc}.x` : mc);

export default function Download({ downloads, tag, releasesUrl }) {
  const [mc, setMc] = useState(downloads[0]?.mc);
  const current = downloads.find((d) => d.mc === mc);

  return (
    <div>
      <div className="actions">
        {downloads.length > 1 && (
          <select aria-label="Minecraft version" value={mc} onChange={(e) => setMc(e.target.value)}>
            {downloads.map((d) => (
              <option key={d.mc} value={d.mc}>
                Minecraft {label(d.mc)}
              </option>
            ))}
          </select>
        )}
        <a className="btn" href={current ? current.url : releasesUrl} download={Boolean(current)}>
          {current ? `Download for ${label(mc)}` : "Download"}
        </a>
        <a className="btn ghost" href="#install">
          Install
        </a>
      </div>
      <p className="note">
        {current
          ? `${tag} · ${(current.size / 1024).toFixed(0)} KB · Fabric · Java 25`
          : "No release published yet. Releases are built automatically for every supported version."}
      </p>
    </div>
  );
}
