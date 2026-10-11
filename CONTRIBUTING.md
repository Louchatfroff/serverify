# Contributing

Thanks for helping. Issues and pull requests are welcome.

## Set up

1. Install JDK 25.
2. Run `./gradlew build` to compile and run the unit tests.
3. Run `./gradlew runClientGameTest` to drive the real client and save screenshots to `build/run/clientGameTest/screenshots`. Look at them before you open a pull request that changes the UI. `./gradlew runProductionClientGameTest` does the same with the real shaded jar.

## Guidelines

- Keep changes small and focused. Match the existing style.
- The interface is Kotlin on Elementa in `src/main/kotlin`; the rest is Java in `src/main/java`.
- Minecraft changes its API between versions. Keep version-specific code small and check every version in `.github/versions.json` before you open a pull request.
- New text goes in `src/main/resources/assets/serverify/lang/en_us.json`. No hard-coded English in code.
- Add a test for logic that can be tested without the game (see `src/test`).
- A new server source implements `ServerSource`, loads after minecraft-list.info, and must fail quietly when the site is down.

## License

By contributing you agree that your work is licensed under the [GNU General Public License, version 3](LICENSE), like the rest of Serverify. Source files start with an SPDX header (`// SPDX-License-Identifier: GPL-3.0-only`); keep it on new files.

## Releasing

Push a tag like `v0.2.0`. CI builds one jar per entry in `.github/versions.json` and publishes the release.
