# WormGame

A klasszikus kukacos (Snake-típusú) játék Androidra, Firebase-alapú bejelentkezéssel, helyi beállításokkal
és többnyelvű (magyar/angol) felülettel.

Ez a projekt egy **funkcionális demó**, nem publikálásra szánt kiadás – a célja a
mobiljáték-fejlesztés, a többnyelvűség és az adattárolás gyakorlása volt.

## Funkciók

- E-mail/jelszó alapú regisztráció és bejelentkezés (Firebase Authentication)
- A felhasználónév mentése a Firebase Realtime Database-be
- Három választható kukac-kinézet, `SharedPreferences`-ben megjegyezve
- Rácsalapú kukacjáték: falnak és önmagának ütközés, növekedés, pontszám, fokozatos gyorsulás
- Szünet / folytatás / új játék, magyar és angol nyelvű felület

## Felépítés

| Fájl | Szerep |
| --- | --- |
| `MainActivity` | Bejelentkezés, továbblépés a regisztrációra |
| `Register` | Regisztráció, felhasználónév mentése |
| `Login` | Főmenü: játék indítása, kinézet választása |
| `SkinChange` | A kukac kinézetének kiválasztása és mentése |
| `Wormgame` | A játék képernyője: kirajzolás és vezérlés |
| `WormEngine` | A játékszabályok Android-függőség nélkül (egységtesztelhető) |

A játéklogika szándékosan külön, tiszta Java osztályban él, így emulátor nélkül is
tesztelhető – lásd `app/src/test/java/com/example/wormgame/WormEngineTest.java`.

## Fordítás és futtatás

Szükséges: JDK 17, Android SDK (compileSdk 35), minimum Android 9 (API 28) eszköz vagy emulátor.

```bash
./gradlew assembleDebug      # debug APK
./gradlew testDebugUnitTest  # egységtesztek
./gradlew installDebug       # telepítés csatlakoztatott eszközre
```

## Firebase beállítása

A `google-services.json` projektspecifikus, ezért **nincs verziókövetve**. Nélküle a projekt
lefordul és az alkalmazás elindul, de a bejelentkezés és a regisztráció le van tiltva
(a képernyő meg is mondja, mi hiányzik). Mivel a játék a bejelentkezés mögött van, a
játékhoz saját Firebase konfiguráció kell.

Saját Firebase háttér beüzemeléséhez:

1. Hozz létre egy projektet a [Firebase konzolban](https://console.firebase.google.com/).
2. Adj hozzá egy Android alkalmazást `com.example.wormgame` csomagnévvel.
3. Kapcsold be az **Authentication → Email/Password** szolgáltatást.
4. Hozz létre egy **Realtime Database**-t.
5. Töltsd le a `google-services.json` fájlt, és másold az `app/` könyvtárba.

A build automatikusan felismeri a fájlt: ha ott van, alkalmazza a Google Services plugint,
ha nincs, figyelmeztetéssel kihagyja.

> Ha korábban `com.example.snakegame` néven volt regisztrálva az alkalmazás a Firebase
> projektben, vegyél fel egy új Android appot `com.example.wormgame` csomagnévvel, és
> töltsd le hozzá az új `google-services.json`-t – a régi fájllal a build hibára fut
> („No matching client found for package name").

## Ismert korlátok

- Nincs kijelentkezés és jelszó-visszaállítás.
- A pontszámok csak helyben látszanak, nem kerülnek fel a Realtime Database-be.
- A játék képernyője álló tájolásra van rögzítve.
