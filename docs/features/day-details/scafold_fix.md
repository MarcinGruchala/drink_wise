You are working in an Android Jetpack Compose (Material3) codebase.

Context:
- The app uses a root Scaffold in AppNavigation with `contentWindowInsets = WindowInsets(0)` to avoid double-applied system bar insets (fixed DayDetails).
- As a consequence, bottom navigation destination screens must handle top system bars (status bar / cutout) themselves.
- Material3 Scaffold passes system insets to its content via `PaddingValues` (innerPadding). These MUST be applied to the screen’s top-level content, otherwise UI will draw under the status bar.

Task:
Apply the same fix that was used in SettingsScreen to every other screen used by the bottom navigation bar (e.g., CalendarScreenContent, HomeScreen, AlcoholCalculatorView, and any other bottom-nav destination).

What to do (codebase-wide):
1) Search for screens/composables that are used as bottom-nav destinations:
    - Look at AppNavigation’s NavHost routes (Home, Calendar, Calculator, Settings).
    - Find corresponding Composables (HomeScreen, CalendarScreen/CalendarScreenContent, AlcoholCalculatorView, etc.).

2) For every destination Composable that uses `Scaffold`:
    - Ensure the Scaffold content lambda uses `innerPadding`:
      Scaffold { innerPadding -> ... }
    - Apply `Modifier.padding(innerPadding)` to the top-level container of the screen content (Column/LazyColumn/Box/etc.).
    - Keep existing padding AFTER it (e.g., `.padding(innerPadding).padding(horizontal = 16.dp)`).
    - Remove any `@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")` that was only there because innerPadding wasn’t used.

   Example pattern (match SettingsScreen):
   Scaffold(...) { innerPadding ->
   Column(
   Modifier
   .fillMaxSize()
   .padding(innerPadding)
   .padding(horizontal = 16.dp)
   ) { ... }
   }

3) Special handling for scroll containers:
    - If the top-level content is a LazyColumn/LazyVerticalGrid/etc:
      Option A (simple): apply `.padding(innerPadding)` to the LazyColumn’s modifier and keep contentPadding for your own spacing.
      Option B (preferred if you need in-list padding): set `contentPadding = innerPadding + yourPadding`.
        - If you don’t already have a helper to add PaddingValues, create a small extension:
          operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
          start = calculateStartPadding(LayoutDirection.Ltr) + other.calculateStartPadding(LayoutDirection.Ltr),
          top = calculateTopPadding() + other.calculateTopPadding(),
          end = calculateEndPadding(LayoutDirection.Ltr) + other.calculateEndPadding(LayoutDirection.Ltr),
          bottom = calculateBottomPadding() + other.calculateBottomPadding()
          )
        - Then do: contentPadding = innerPadding + PaddingValues(horizontal = 16.dp, vertical = 16.dp)

4) For destination screens that DO NOT use Scaffold:
    - Either wrap the screen in a Material3 Scaffold and consume `innerPadding` as above, OR
    - Apply safe insets manually at the root:
      Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
      (use safeDrawing, not statusBars, to handle modern edge-to-edge cases robustly).

5) Concrete fix to apply immediately:
    - CalendarScreenContent currently has `Scaffold { ... }` but ignores the padding.
      Change it to:
      Scaffold { innerPadding ->
      Column(
      Modifier
      .fillMaxSize()
      .padding(innerPadding)
      .padding(horizontal = 8.dp)
      ) { ...existing content... }
      }
      Also remove @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") if it becomes unused.

6) Validation / acceptance criteria:
    - On a real device/emulator with edge-to-edge, none of the bottom-nav screens’ content should draw under the status bar (no clipped headers).
    - DayDetails stays correct (no extra top gap).
    - No new double-inset padding appears.
    - Remove any now-unnecessary lint suppressions.

Please implement the changes in the relevant files and show diffs for each modified Composable.
