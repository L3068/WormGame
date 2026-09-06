# WormGame

A klasszikus kukacos (Snake-típusú) játék Androidra, Firebase-alapú bejelentkezéssel, helyi beállításokkal
és többnyelvű (magyar/angol) felülettel.

Ez a projekt egy **funkcionális demó**, nem publikálásra szánt kiadás – a célja a
mobiljáték-fejlesztés, a többnyelvűség és az adattárolás gyakorlása volt.

## Funkciók

- E-mail/jelszó alapú regisztráció, bejelentkezés, kijelentkezés és
  jelszó-visszaállítás (Firebase Authentication)
- A felhasználónév és a pontszámok mentése a Firebase Realtime Database-be,
  a legjobb eredmény megjelenítése a játék végén
- Három választható kukac-kinézet, `SharedPreferences`-ben megjegyezve
- Rácsalapú kukacjáték: falnak és önmagának ütközés, növekedés, pontszám, fokozatos gyorsulás
- Szünet / folytatás / új játék, álló és fekvő tájolás, magyar és angol nyelvű felület

## Felépítés

| Fájl | Szerep |
| --- | --- |
| `MainActivity` | Bejelentkezés, jelszó-visszaállítás, továbblépés a regisztrációra |
| `Register` | Regisztráció, felhasználónév mentése |
| `Login` | Főmenü: játék indítása, kinézet választása, kijelentkezés |
| `SkinChange` | A kukac kinézetének kiválasztása és mentése |
| `Wormgame` | A játék képernyője: kirajzolás és vezérlés |
| `WormEngine` | A játékszabályok Android-függőség nélkül (egységtesztelhető) |
| `ScoreRepository` | Pontszámok mentése és a legjobb eredmény lekérdezése |

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

Saját Firebase háttér beüzemeléséhez – **ebben a sorrendben**:

1. Hozz létre egy projektet a [Firebase konzolban](https://console.firebase.google.com/).
2. Adj hozzá egy Android alkalmazást `com.example.wormgame` csomagnévvel.
   Ez az `applicationId` az `app/build.gradle.kts`-ből; pontosan egyeznie kell.
3. Kapcsold be az **Authentication → Sign-in method → Email/Password** szolgáltatást.
4. Hozz létre egy **Realtime Database**-t (a régió szabadon választható).
5. **Most** töltsd le a `google-services.json` fájlt (Projektbeállítások → az
   alkalmazásod → *google-services.json*), és másold az `app/` könyvtárba.
6. Másold a repóban lévő `database.rules.json` tartalmát a **Realtime Database →
   Rules** fülre, és publikáld.

> **A 4. és 5. lépés sorrendje számít.** A konzol már az alkalmazás
> regisztrálásakor (2. lépés) felkínálja a `google-services.json`-t, de az még
> nem tartalmazza az adatbázis címét (`firebase_url`), mert az adatbázis akkor
> még nem létezik. Ilyen fájllal a bejelentkezés működik, a felhasználónév és a
> pontszámok mentése viszont némán kimarad. Ellenőrizni tudod:
>
> ```bash
> grep -o '"firebase_url": "[^"]*"' app/google-services.json
> ```
>
> Ha nem ír ki semmit, hozd létre az adatbázist, és töltsd le újra a fájlt.

A build automatikusan felismeri a fájlt: ha ott van, alkalmazza a Google Services plugint,
ha nincs, figyelmeztetéssel kihagyja. A fájl cseréje után futtass újra buildet.

### Az API kulcs korlátozása

A `google-services.json`-ba kerülő API kulcs minden kiadott APK-ban benne van, tehát
nem titok – a védelmet a korlátozása és az adatbázis-szabályok adják, nem a titkossága.
Google Cloud Console → *APIs & Services → Credentials* → az Android kulcs:

- *Application restrictions*: **Android apps**, `com.example.wormgame` + a
  tanúsítvány SHA-1 ujjlenyomata (`./gradlew signingReport` írja ki a debug kulcsét)
- *API restrictions*: csak a ténylegesen használt API-k

## Adatszerkezet a Realtime Database-ben

```
users/<uid>/username      – a regisztrációkor megadott felhasználónév
users/<uid>/highScore     – az eddigi legjobb eredmény
users/<uid>/scores/<id>   – { score, timestamp } minden befejezett játszmáról
```

A hozzá tartozó szabályok a repóban vannak: `database.rules.json` (a beállítás 6. lépése).
Csak a bejelentkezett felhasználó éri el a saját `users/<uid>` ágát, minden más tiltott;
a szabályok a mezők típusát is ellenőrzik.

Ezt a lépést ne hagyd ki: az új adatbázis alapból vagy zárolt (minden írás elbukik),
vagy teszt módban van (30 napig bárki írhatja) – egyik sem jó.

## Ismert korlátok

- Nincs ranglista: mindenki csak a saját legjobb eredményét látja.
- A kukac-kinézet csak helyben, eszközönként tárolódik, nem a fiókhoz kötve.
- Bejelentkezés nélkül nem lehet játszani; nincs vendég mód.
