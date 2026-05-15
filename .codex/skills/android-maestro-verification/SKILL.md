---
name: android-maestro-verification
description: Use when Codex needs to verify Android UI behavior with Maestro, especially after Compose screen, navigation, user flow, screenshot, animation, or accessibility selector changes in this repository.
---

# Android Maestro Verification

## Overview

Use Maestro as the real-device feedback loop for Android UI work. Maestro should complement unit tests: use unit tests for logic, and Maestro for visible navigation, screen state, gestures, screenshots, and short recordings.

## Workflow

1. Prefer an existing flow in `maestro/flows/`.
2. Run the default Home smoke flow only to verify that the Maestro harness still works:

   ```bash
   scripts/android-maestro-run.sh
   ```

3. Add or update a focused flow when implementing new user-visible behavior.
4. Build, install, and run a specific flow with:

   ```bash
   scripts/android-maestro-run.sh maestro/flows/calendar-day-details.yaml
   ```

5. Run a flow folder with tags when useful:

   ```bash
   scripts/android-maestro-run.sh maestro/flows -- --include-tags smoke
   ```

6. Inspect the Maestro output and any screenshots/videos under `artifacts/maestro/` before claiming UI behavior works.

## Flow Guidance

- Target the debug app id: `com.mgruchala.drinkwise.dev`.
- Prefer selectors by visible text, content description, or stable test tag.
- Make smoke assertions app-specific. Avoid generic labels like `Home` by themselves because Android launcher or system UI may expose similar text.
- Avoid coordinate taps unless there is no accessible selector.
- Keep flows short and behavior-focused. A new feature normally gets one happy-path flow plus targeted edge flows only when the risk justifies them.
- Use `takeScreenshot` for important end states.
- Use `startRecording` and `stopRecording` for animation-heavy or transition-heavy behavior.

## When Features Change

- Maestro harness/setup changed: run `maestro/flows/home-smoke.yaml`.
- Compose layout or screen text changed: run the closest existing flow.
- New navigation or screen flow added: add a new Maestro flow for that path.
- Example: for calendar day details, add `maestro/flows/calendar-day-details.yaml` that launches the app, opens Calendar, taps a day, asserts the day details UI, and captures a screenshot.

## Troubleshooting

- If Maestro cannot find an element, inspect whether the UI exposes stable text, content descriptions, or semantics.
- If no emulator/device is connected, the runner starts `AVD_NAME` or defaults to `Pixel_10_Pro`.
- If the runner reports a missing AVD system image, repair the AVD in Android Studio or install the matching image with `sdkmanager`.
- If the app cannot install, run `./gradlew assembleDebug` and verify `app/build/outputs/apk/debug/app-debug.apk` exists.
- If the flow is flaky, prefer assertions and `extendedWaitUntil` over fixed sleeps.
