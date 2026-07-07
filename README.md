# Luminous Trust — Android app (v0.1 scaffold)

The phone half of the behavioral-biometrics system: it captures motion,
builds a session, sends it to the InnaIT scoring server, and shows the
decision — styled to the **Luminous Bio-Tech** brand (`DESIGN.md`).

> This is a **starting scaffold to open in Android Studio**, not a compiled
> APK. It was generated outside the Android toolchain, so let Android Studio
> sync and surface any version nudges — the structure and code are the point.

## Open & build
1. Android Studio (Ladybug / 2024.2+). **File ▸ Open** this folder.
2. Let Gradle sync. If it complains the Gradle **wrapper jar** is missing,
   run `gradle wrapper` once (or use Android Studio's prompt to regenerate it) —
   the wrapper `.jar` is a binary and isn't included here.
3. Pick an emulator or device (Android 8.0 / API 26+) and press **Run**.
4. Build an APK: **Build ▸ Build App Bundle(s) / APK(s) ▸ Build APK(s)**.

## What it does today
- Starts a foreground **SensorCaptureService** that reads accelerometer +
  gyroscope at ~50 Hz and keeps a rolling micro-tremor / tilt summary.
- On **Run trust check**, builds a session JSON (`SessionBuilder`) and POSTs it
  to `<server>/score` (`InnaitClient`), then renders the trust score + action.
- Talks to the InnaIT server from your earlier zip. Default URL
  `http://10.0.2.2:8080` = "localhost of the host machine" from the emulator.
  Start the Python service (`./run.sh`) and it just works.

## Project map
```
app/src/main/java/com/luminous/trust/
  MainActivity.kt              # entry point, starts capture, hosts Compose
  ui/TrustScreen.kt            # the branded Trust Console
  ui/theme/                    # Color / Type / Shape / Theme  <- DESIGN.md lives here
  capture/SensorCaptureService.kt   # Phase 1: always-on motion capture
  capture/SessionBuilder.kt         # builds the InnaIT /score event JSON
  net/InnaitClient.kt               # POST /score (HttpURLConnection + org.json)
  net/ScoreModels.kt                # response + UI state types
```

## How DESIGN.md maps in
- **Colours** — the tokens are Material 3 role names, dropped straight into
  `lightColorScheme` in `ui/theme/Theme.kt`. Growth Green (`#7AC943`) is the
  primary-container / CTA accent; the rest is the clinical grey/white ground.
- **Shapes** — `Shape.kt`: 4px for buttons/inputs, 8px for cards & chips.
- **Type scale** — `Type.kt` sets every size/weight/letter-spacing from the
  brief. See "Fonts" below to load the actual typefaces.
- **Components** — primary button uses Growth Green; cards are white with a
  1px `outlineVariant` hairline and no shadow; the action chip uses the
  tinted-container + dark-text pattern with 8px rounding.

## Fonts (Montserrat + Libre Franklin)
Right now `Type.kt` uses the system sans-serif so the project compiles and the
*scale* is already correct. To load the real brand faces via Downloadable Fonts:
1. Uncomment `ui-text-google-fonts` in `app/build.gradle.kts`.
2. Add the standard GMS fonts certs array (`res/values/font_certs.xml`).
3. In `Type.kt`, swap the two `FontFamily.SansSerif` values for
   `GoogleFont("Montserrat")` / `GoogleFont("Libre Franklin")` via the provider.
   (Android Studio's font tooling can generate this for you.)

## Known gap: touch capture
`SessionBuilder` fills `touchType` with zeros — mirroring the current server
data, where touch/keystroke fields came back empty. Wiring real touch capture
(pointer pressure, swipe velocity, key dwell/flight inside the app) is the next
milestone and the single biggest lift to accuracy.

## Assumptions made (change freely)
- Package / applicationId: `com.luminous.trust`
- minSdk 26, targetSdk 35, compileSdk 35
- Default user id `PB0366` and server `http://10.0.2.2:8080` (both editable in-app)

## Roadmap from here
1. Real touch/keystroke capture inside the app's own views.
2. Device Admin + `lockNow()` so BLOCK can actually lock the screen.
3. Local caching + retry when the server is unreachable.
4. Encrypt any on-device buffers (Android Keystore).
