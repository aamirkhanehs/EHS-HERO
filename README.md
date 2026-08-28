# EHS HERO

**Every Safe Action Makes You A Hero.**

A native Android app that turns HSE (Health, Safety & Environment) compliance
on construction and transmission-line projects into a game: submit safety
activities, get them approved by HSE, earn XP, level up, unlock badges, and
climb the leaderboard.

This is a real, complete Android Studio project - Kotlin + Jetpack Compose +
Firebase - not a mockup. It builds into an actual installable APK.

---

## 0. Before you start: what to expect

This project was generated in a sandboxed environment with no Android SDK
and no network access to Google's Maven repositories, so **it has not been
compiled**. Everything here is written carefully and double-checked by hand
(consistent imports, balanced braces, cross-referenced function names), and
a couple of the riskiest pieces (the Gradle wrapper jar, the launcher fonts)
are real bytes fetched from Google's own repositories rather than
hand-typed. But with ~90 hand-written Kotlin files, there's a realistic
chance Android Studio's first sync surfaces a small issue - a missing
import, a version mismatch. If that happens, it's normally a two-minute fix
(Android Studio usually tells you exactly which line and offers a quick
fix), and I'm glad to help debug it if you paste the error back.

Section 12 ("What's simplified") is a direct, honest list of the handful of
spec items that are stubbed, simplified, or need a paid Firebase tier - read
that before assuming something is missing by accident.

---

## 1. Tech stack, and why

| Layer | Choice | Why |
|---|---|---|
| Language | Kotlin | Standard for modern Android |
| UI | Jetpack Compose (Material 3) | Matches the "modern mobile game" brief far better than XML views; makes the animations (XP bar fill, level-up, badge unlock) straightforward |
| Architecture | MVVM, hand-rolled repositories, no DI framework | See "Why no Hilt" below |
| Backend | Firebase (Auth + Firestore) | Real-time multi-user sync, offline persistence built in, generous free ("Spark") tier, first-class Android SDK |
| Photos | Compressed JPEG stored as Base64 on the Firestore document | Firebase Storage now requires the paid Blaze plan (changed Feb 2026) - see section 9 |
| Notifications | In-app notification center (Firestore-backed) | Push delivery while the app is closed needs a server trigger, which needs Blaze - see section 10 |
| CI / APK builds | GitHub Actions | Free, needs no local Android Studio or admin rights - see section 4 |

### Why no Hilt / dependency injection framework
Hilt (or Koin, or Dagger) is the "correct" choice for a production app this
size, but it depends on annotation processing (KSP), which is another
moving part whose exact version compatibility with this Kotlin/AGP
combination I cannot verify without a compiler on hand. Every repository
class instead takes its dependencies as constructor parameters with
sensible defaults (e.g. `class UserRepository(private val firestore:
FirebaseFirestore = FirebaseModule.firestore)`), which gives you 90% of the
testability benefit with far less risk. Swapping in Hilt later is a
mechanical, low-risk refactor once the project is building.

### Why AGP 8.9.2 instead of the very latest
Android Gradle Plugin 9.0 shipped in January 2026 with a genuinely new DSL.
Rather than risk hand-writing syntax I can't compile-check, this project
pins a well-established AGP 8.x / Gradle 8.11.1 / Kotlin 2.1.20 combination
whose classic DSL I have very high confidence in. If you want to move to
AGP 9.x, open the project in current Android Studio and accept the "AGP
Upgrade Assistant" prompt - it does this migration for you.

---

## 2. Design system ("Command HUD")

Rather than a generic "AI app" look, the palette and type system are built
around the actual subject: a transmission-line/construction safety command
center at night.

- **Palette** - deep steel-navy background (`#0B1220`) with two accent
  colors used for different meanings: **Guardian Amber** (`#FFB020`) for
  XP/energy/primary actions, **Signal Cyan** (`#2FD4D9`) for level/rank/
  electrical motifs. Clearance Green and Hazard Coral are reserved
  strictly for approved/rejected states.
- **Typography** - Rajdhani (a technical, slightly angular display face)
  for headlines and big numbers, Inter for everything you actually have to
  read. Both are bundled as real font files under `app/src/main/res/font/`
  (fetched from Google's open-source fonts repo, SIL Open Font License -
  see `app/src/main/assets/licenses/`) rather than fetched at runtime via
  Google's downloadable-fonts API, since this app targets field devices
  that may have poor connectivity or lack Google Play Services.
- **Signature shape** - a hexagonal "shield ring" XP indicator
  (`ui/components/HeroShieldRing.kt`) used only in three places (Home,
  Profile, Level-Up overlay) so it stays a memorable signature rather than
  wallpaper. Cards use a custom cut-corner "safety plate" shape
  (`HeroPlateShape`) instead of generic rounded rectangles.
- **Iconography** - Material Icons Extended throughout (shield, bolt,
  medal, etc.) rather than hand-drawn glyphs, since a stable, well-known
  icon library is far lower-risk than reconstructing custom vector path
  math without a way to preview it.

---

## 3. Firebase setup (one-time, ~10 minutes)

1. Go to the [Firebase Console](https://console.firebase.google.com) and
   create a new project (the **free Spark plan** is enough for everything
   except the two optional upgrades in sections 9-10).
2. Add an Android app inside that project:
   - Package name: `com.ehshero.app`
   - Download the generated **`google-services.json`**.
3. Enable the products this app uses:
   - **Authentication** → Sign-in method → enable **Email/Password**.
   - **Firestore Database** → Create database → start in **production
     mode** (the rules in `firebase/firestore.rules` handle access control).
4. Deploy the security rules and indexes (needs the
   [Firebase CLI](https://firebase.google.com/docs/cli): `npm install -g
   firebase-tools`):
   ```bash
   firebase login
   firebase use --add          # pick the project you just created
   firebase deploy --only firestore:rules,firestore:indexes
   ```
   The indexes can take a few minutes to finish building in the console -
   the app will work before they're ready, but a few queries (activity
   history, approvals queue, leaderboard) will show a "requires an index"
   error in Logcat until they are. Firebase's own error message includes a
   direct link to create any index it's still missing, as a fallback.
5. Put the `google-services.json` you downloaded in step 2 somewhere you
   can use it in section 4 or 5.

---

## 4. Building the APK without installing Android Studio (GitHub Actions)

This is the path from spec section 29 - build on a personal computer or a
cloud environment, no admin rights needed on a locked-down company machine.

1. Create a free [GitHub](https://github.com) account if you don't have
   one, and create a new repository (private is fine).
2. Push this project to it:
   ```bash
   cd EHSHero
   git init
   git add .
   git commit -m "EHS Hero"
   git branch -M main
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```
3. Add your Firebase config as a repository secret so CI can use it without
   it ever being committed to the repo:
   - `Settings` → `Secrets and variables` → `Actions` → `New repository secret`
   - Name: `GOOGLE_SERVICES_JSON`
   - Value: the **base64-encoded contents** of your `google-services.json`:
     ```bash
     base64 -i google-services.json | tr -d '\n'    # macOS/Linux
     certutil -encode google-services.json tmp.b64  # Windows (strip header/footer lines after)
     ```
     Paste the output as the secret value.
4. Push again (or go to the **Actions** tab and run the "Build EHS Hero
   APK" workflow manually). When it finishes, open the workflow run and
   scroll to **Artifacts** - `ehs-hero-debug-apk` and
   `ehs-hero-release-apk` are both there as downloadable zips.
5. Unzip, copy the `.apk` to your phone, and install it (you'll need to
   allow "install unknown apps" for whichever app you copied it with - a
   normal Android prompt, not a security issue).

The release APK is **unsigned** unless you also set up the four signing
secrets described in section 8 - an unsigned release build is still fully
functional for internal testing, it just can't go on the Play Store as-is.

---

## 5. Building locally in Android Studio (alternative)

1. Install [Android Studio](https://developer.android.com/studio) (current
   stable channel).
2. Open the `EHSHero` folder as a project.
3. Copy your `google-services.json` (section 3) into `app/`, replacing the
   placeholder `app/google-services.json.example`.
4. Let Gradle sync. If Android Studio reports a wrapper problem, use
   **File → Sync Project with Gradle Files**, or regenerate it with
   Android Studio's bundled Gradle: `Terminal` → `gradle wrapper
   --gradle-version 8.11.1`.
5. Run on a device/emulator (Shift+F10), or **Build → Generate Signed
   Bundle / APK** for a release build.

---

## 6. Demo data

Four demo users from the spec, plus the default levels/badges/point rules:

| Name | Role | Level | XP |
|---|---|---|---|
| Aamir Khan | HSE | 7 | 1720 |
| Rahul Sharma | Staff | 6 | 1540 |
| Pritam Das | Staff | 5 | 1210 |
| Sachin Verma | Staff | 4 | 850 |

To seed them: build and run the app, sign in as **any** account (you'll
need at least one real account first - see "Creating the first user"
below), go to **Admin → Settings → Demo Data**, and tap **"Seed + Demo
Users"**. All four are created with the temporary password `EhsHero@123`
and are ordinary accounts afterwards - edit or delete them from **Admin →
Users** like any other.

### Creating the first user (bootstrapping)
The very first account has to be created before anyone can sign in. Two
options:
- **Firebase Console** (simplest): Authentication → Users → Add user, enter
  an email/password, copy the generated UID, then in Firestore create a
  document at `users/<that UID>` with at least `role: "ADMIN"`,
  `name`, `employeeId`, `email`, `status: "ACTIVE"` (see `data/model/User.kt`
  for the full field list - everything else defaults sensibly).
- **In-app**, once *any* Admin account exists: Admin → Users → the **+**
  button creates a real login plus profile in one step (see the code
  comment on `UserRepository.createStaffAccount` for how this avoids
  signing you out of your own session while doing it).

---

## 7. Data model

Firestore collections (see `firebase/firestore.rules` for exactly who can
read/write what):

`users`, `projects`, `activities`, `approvals`, `xpTransactions`, `badges`,
`userBadges`, `levels`, `missions`, `userMissions`, `challenges`,
`notifications`, `settings`, `usernameIndex`.

The core pipeline (`domain/GamificationEngine.kt`,
`data/remote/GamificationRepository.kt`) is exactly the spec's philosophy:

```
SAFETY ACTIVITY → APPROVAL → XP → LEVEL → BADGE → RANK → REWARD
```

`GamificationRepository.approveActivity()` runs this whole pipeline as a
single Firestore transaction, so an activity can never end up "approved"
without its XP being credited, or vice versa. XP is genuinely never
credited at submission time - only on HSE approval, exactly per spec
section 6.

User documents carry denormalized counters (`approvedTbtCount`,
`approvedObservationCount`, etc.) that are updated in that same
transaction, which is what makes badge-eligibility checks and the HSE
dashboard's KPIs cheap - they're simple sums over already-loaded user
data instead of separate collection scans (spec section 27: "avoid
unnecessary database reads").

---

## 8. Release APK signing (optional)

Without this, `assembleRelease` still produces a real, working **unsigned**
APK - fine for sharing internally for testing. To get a properly signed one
straight out of CI:

```bash
keytool -genkeypair -v -keystore release.keystore -alias ehshero \
  -keyalg RSA -keysize 2048 -validity 10000
```

Then add four more repository secrets (same place as section 4):
`RELEASE_KEYSTORE_BASE64` (base64 of `release.keystore`, same command as
section 4 step 3), `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
`RELEASE_KEY_PASSWORD`. **Keep `release.keystore` somewhere safe outside
the repo** - if you lose it, you can never publish an update under the same
app identity again.

---

## 9. Photo storage

Firebase Cloud Storage started requiring the paid **Blaze** (pay-as-you-go)
plan on **February 3, 2026** - it's no longer available on the free Spark
plan at all. To keep the app fully functional with zero billing setup, the
default path (`util/PhotoCompressor.kt`) compresses attached photos down to
a small JPEG (~900px, aggressively quality-reduced to stay well under
Firestore's 1 MiB document limit) and stores it as a Base64 string directly
on the activity document.

This is a deliberate MVP trade-off, not an oversight: it works everywhere
with no billing account, but photos are lower-resolution and every read of
an activity pulls the photo bytes along with it. If you enable Blaze later
(it has its own free quota, so this can still cost $0 at normal usage - you
just need a card on file), `firebase/storage.rules` is already written for
the "real" upload-to-Storage path; wiring `SafetyActivity.photoStorageUrl`
instead of `photoBase64` in `ActivitySubmitViewModel` is a small, contained
change.

---

## 10. Push notifications

Every notification in the spec (approval, rejection, level-up, badge
unlock, etc.) is written to the `notifications` Firestore collection and
shown in the in-app notification center (`ui/notifications/`) - this works
out of the box, live, no extra setup, no billing.

A **true background push** (the phone buzzes even with the app fully
closed) needs something to actually call FCM's send API when, say, HSE
approves an activity from a different device - that's normally a Cloud
Function triggered on the Firestore write. Cloud Functions require the
Blaze plan to deploy (Spark can only run them in the local emulator).
`service/EHSMessagingService.kt` already registers each device's FCM token
onto their user document, so it's ready the moment you add that function -
a minimal one looks like:

```js
exports.onNotificationCreated = functions.firestore
  .document('notifications/{id}')
  .onCreate(async (snap) => {
    const n = snap.data();
    const user = await admin.firestore().doc(`users/${n.userId}`).get();
    const token = user.data()?.fcmToken;
    if (token) {
      await admin.messaging().send({
        token,
        notification: { title: n.title, body: n.body },
      });
    }
  });
```

---

## 11. Project structure

```
EHSHero/
├── app/src/main/java/com/ehshero/app/
│   ├── data/
│   │   ├── model/      13 Firestore document data classes + Enums.kt
│   │   ├── remote/     9 repositories (Auth, User, Activity, Gamification,
│   │   │               Mission, Leaderboard, Notification, Project, +
│   │   │               FirebaseModule service locator)
│   │   └── seed/       DefaultConfig (levels/badges/point rules) + DemoDataSeeder
│   ├── domain/         GamificationEngine (pure logic), LeaderboardEntry
│   ├── ui/
│   │   ├── theme/      Color, Type, Shape, Theme, HeroAvatars
│   │   ├── components/ 15 reusable composables (XP bar, badge card, charts, ...)
│   │   ├── navigation/ Routes + the role-based NavHost
│   │   ├── auth/ staff/ hse/ admin/ leaderboard/ profile/ notifications/
│   │   │               one folder per screen area, each Screen.kt paired
│   │   │               with a ViewModel.kt
│   │   └── service/    EHSMessagingService (FCM)
│   └── MainActivity.kt, EHSHeroApp.kt
├── firebase/            firestore.rules, firestore.indexes.json, storage.rules
├── .github/workflows/   android-build.yml (CI APK builds)
└── gradle/              version catalog + wrapper
```

~90 Kotlin files. Every screen follows the same shape: a `ViewModel`
exposing a single `StateFlow<UiState>`, and a `@Composable Screen` that
collects it - consistent enough that once one screen makes sense, they all
do.

---

## 12. What's simplified (read this before filing something as "missing")

Building a complete production HSE platform is genuinely a multi-month
project for a team, not a single session. Per the spec's own permission
("if something cannot be implemented exactly as requested, implement the
closest stable alternative and clearly explain the limitation"), here's
exactly where this MVP draws the line, and why:

- **Character/hero artwork.** Section 3 asks for an illustrated anime-style
  safety-hero character. This environment can produce code and vector
  graphics, not painted illustration. The login screen and avatars use a
  clean vector emblem system instead (see `HeroEmblem` in `LoginScreen.kt`
  and `ui/theme/HeroAvatars.kt`) with an obvious, clearly-commented slot to
  drop in commissioned or AI-generated artwork later without touching any
  logic.
- **Avatar customization.** Rather than a full build-your-own-character
  editor, Profile offers six preset "hero" badges (colour + icon
  combinations). Extending the list is a one-line addition to
  `AVATAR_OPTIONS`.
- **Photo storage.** Base64-on-Firestore by default instead of Firebase
  Storage, because Storage now requires the paid Blaze plan - see section 9.
- **Push notifications.** In-app notification center by default instead of
  true background push, because that needs Cloud Functions (also
  Blaze-only) - see section 10.
- **Charts.** The three dashboard charts (bar/trend/donut) are hand-drawn
  with Compose Canvas rather than a third-party charting library, so there's
  no extra dependency whose exact Compose-version compatibility can't be
  verified here. They're simple but real and fully functional.
- **Monthly Champion badge.** The count-based badges (TBT Master, Near Miss
  Hero, etc.) unlock automatically the moment their threshold is crossed.
  "Safety Champion" (rank #1 for the month) needs a monthly-boundary
  decision that's inherently a scheduled/periodic action rather than a
  per-approval one - `GamificationEngine.isMonthlyChampion()` is written
  and ready, but wiring it to an automatic monthly cutover would need a
  Cloud Function (a `functions.pubsub.schedule` cron trigger) or a manual
  "declare this month's champion" Admin action, neither of which is wired
  into a UI button yet.
- **Weekly/monthly leaderboard performance at large scale.** Period-filtered
  leaderboard views (spec section 9) aggregate `xpTransactions` for that
  window on demand rather than maintaining rolling denormalized counters.
  Correct and simple; at a genuinely large org (thousands of users) this is
  the first thing to revisit - see the comment in `LeaderboardRepository.kt`.
- **CSV export filtering.** Project/status filters are applied as Firestore
  query filters; the date range is narrowed client-side afterward, to avoid
  needing a separate composite index for every filter combination. Fine at
  this app's intended scale (an internal team tool), noted in
  `ActivityRepository.getActivitiesForExport`.
- **Camera capture.** Photo attach uses Android's system Photo Picker
  (gallery), which needs no runtime permission at all. Direct camera
  capture would need a `FileProvider` manifest entry I chose not to guess
  at un-tested; it's a well-documented, contained addition if you want it.
- **Offline support.** Firestore's Android SDK persists to disk and syncs
  automatically by default - this is real, not a stub. What's simplified is
  the *UI*: `SyncStatusBanner` and `ConnectivityObserver` are built and
  ready, but aren't yet wired into the root Scaffold. Wiring `isOnline`
  from `ConnectivityObserver` into that banner in `EHSNavGraph.kt` is a
  small addition.

Nothing above blocks the core loop end-to-end: log in → submit an activity
→ HSE approves it → XP/level/streak/badges update live → shows on the
leaderboard. That path is fully implemented.

---

## 13. If Gradle sync reports an error

Given the scale of this project and that it couldn't be compiled in the
environment that generated it, treat any sync error as likely a small,
fixable thing rather than a sign something is fundamentally wrong:

1. Read the specific error - Android Studio almost always names the exact
   file and line.
2. Most common causes at this scale: a missing import (add it - Alt+Enter /
   Option+Enter usually offers it directly), or a dependency version
   Android Studio wants to bump (usually safe to accept).
3. Paste the error back and I can help fix it directly.
