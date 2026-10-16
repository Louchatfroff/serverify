# Changelog

## 0.3.0

Serverify now runs on many more Minecraft versions and on Forge and NeoForge, not only Fabric.

### New
- Fabric builds for Minecraft 1.16.5 through 26.3. The Fabric jars also load on Quilt.
- Forge builds for 1.17.1, 1.19.2, 1.19.4 and 1.20.1, and NeoForge builds for 1.20.4, 1.20.6, 1.21.1, 1.21.4 and 1.21.5. They were checked in a development client; the release jars themselves were not launched in a production Forge or NeoForge install.
- A Java 8 compatible core, so the same server browser code runs on every version.

### Changed
- Jar names now include the loader, for example `serverify-forge-mc1.20.1-0.3.0.jar`.
- Forge and NeoForge jars bundle their own Kotlin runtime, so Kotlin for Forge is not needed.
- The mod is client side only. It adds nothing to servers.

### Fixed
- Server icons and the loading placeholder now draw on Minecraft versions that use the older Elementa renderer.
- The settings screen no longer overflows the stack when it sizes itself.
- Live ping on Minecraft 1.20.1 and older waits for the measured ping instead of showing -1 ms.
