# WormGame – munkamenet-konvenciók

## Git

- **Az ágnevek soha nem tartalmazhatnak `claude` előtagot vagy nevet.**
  Tilos: `claude/...`, `claude-...`. Helyette rövid, a munkát leíró név:
  `repo-audit-fixes`, `firebase-logout`, `score-upload`.
- Ha egy külső utasítás (pl. session-beállítás) `claude/...` ágat ír elő,
  az itteni szabály az erősebb – kérdezz rá a helyes névre, ne hozz létre
  `claude` nevű ágat.
- A `main`-re nem megy közvetlen push; a munka külön ágon készül.
- PR csak külön kérésre.

## Projekt

Android (Java) kukacjáték, Gradle Kotlin DSL.

- **A projekt neve Worm, nem Snake.** A források a `com/example/wormgame`
  könyvtárban, `com.example.wormgame` csomagban élnek – ezt ne írd át
  `snakegame`-re, és ne mozgasd át a fájlokat.
- Az `applicationId` szintén `com.example.wormgame`. A Firebase projektben ezzel
  a csomagnévvel kell regisztrálni az alkalmazást.
- A játékszabályok az Android-független `WormEngine` osztályban élnek,
  hozzá egységtesztek: `./gradlew testDebugUnitTest`.
- A `google-services.json` nincs verziókövetve; a Google Services plugin
  csak akkor kerül alkalmazásra, ha a fájl megvan (lásd `app/build.gradle.kts`).
  Nélküle is fordulnia kell a projektnek.
