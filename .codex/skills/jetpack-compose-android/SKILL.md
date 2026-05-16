---
name: jetpack-compose-android
description: Use when working on Jetpack Compose Android UI in this repository, including @Composable screens, state hoisting, ViewModel StateFlow UI state, Navigation Compose, Material 3, modifiers, lazy lists, side effects, animations, accessibility, performance, previews, or Compose UI code review.
metadata:
  upstream: "https://github.com/aldefy/compose-skill"
  upstream-version: "2.3.1"
  local-scope: "Android-only pruning for Drink Wise"
---

# Jetpack Compose Android

Android-only guidance for writing correct, maintainable Jetpack Compose UI in this
repository. This skill is adapted from `aldefy/compose-skill`; see
`LICENSE.upstream` for the upstream license.

Use `AGENTS.md` for project conventions first. Use this skill for Compose-specific
implementation details, reviews, and debugging.

## When this skill applies

Use this skill for Android Jetpack Compose work involving:

- `@Composable`, `remember`, `rememberSaveable`, `mutableStateOf`,
  `derivedStateOf`, `snapshotFlow`, recomposition, or stability.
- `LaunchedEffect`, `DisposableEffect`, `SideEffect`, `rememberUpdatedState`, or
  `rememberCoroutineScope`.
- `Scaffold`, `MaterialTheme`, Material 3 components, color schemes, typography,
  shapes, or motion.
- `Modifier`, custom layout, drawing, pointer input, semantics, or accessibility.
- `LazyColumn`, `LazyRow`, lazy grids, pagers, keys, content types, or scroll state.
- `NavHost`, `NavController`, typed routes, deep links, back stack behavior, or
  bottom navigation.
- Compose performance, slow screens, unnecessary recomposition, previews, UI code
  review, or production Compose crashes.
- Design-to-Compose implementation from screenshots, Figma notes, or visual specs.

Do not use this skill for Compose Multiplatform, Desktop Compose, iOS, Web,
Android TV, or KMP platform-specific guidance; those upstream references were
intentionally removed for this Android app.

## Routing

Read the most relevant reference file before making non-trivial Compose changes.
Load only the file needed for the current task.

### State, recomposition, and side effects

- `remember`, `rememberSaveable`, `mutableStateOf`, state hoisting,
  `derivedStateOf`, `snapshotFlow`, StateFlow in UI, recomposition boundaries:
  `references/state-management.md`
- Recomposition frequency, stability, `@Stable`/`@Immutable`, Compose compiler
  metrics, baseline profiles, strong skipping: `references/performance.md`
- `LaunchedEffect`, `SideEffect`, `DisposableEffect`, `rememberUpdatedState`,
  `rememberCoroutineScope`: `references/side-effects.md`
- `CompositionLocal`, theming propagation, `LocalContext`, custom locals:
  `references/composition-locals.md`

### Layout, lists, modifiers

- `LazyColumn`, `LazyRow`, lazy grids, pager, keys, content types, scroll state,
  sticky headers: `references/lists-scrolling.md`
- Modifier ordering, custom layout, `Layout`, `SubcomposeLayout`, draw modifiers,
  `Painter`, `Modifier.Node`: `references/modifiers.md`
- Compose inside XML, `AndroidView`, `ComposeView`, `AbstractComposeView`,
  composable structure, slots, extraction, previews: `references/view-composition.md`

### Navigation

- `NavHost`, `NavController`, back stack, deep links, typed routes, navigation
  graph, nested graphs: `references/navigation.md`
- Navigation API migration and Nav 2 vs Nav 3 decisions:
  `references/navigation-migration.md`

### Material 3, animation, and design

- `MaterialTheme`, `ColorScheme`, dynamic color, typography, shapes:
  `references/theming-material3.md`
- M3 motion tokens, animation duration, easing: `references/material3-motion.md`
- `animate*AsState`, `AnimatedVisibility`, `Crossfade`, transitions,
  `Animatable`, gesture-driven animation: `references/animation.md`
- Atomic design, reusable component boundaries, design tokens:
  `references/atomic-design.md`
- Figma/screenshot decomposition, redline interpretation, design-to-code:
  `references/design-to-compose.md`

### Accessibility, paging, and production

- Semantics, TalkBack, touch targets, content descriptions, traversal order,
  accessibility testing: `references/accessibility.md`
- Paging 3 UI, `PagingSource`, `Pager`, `PagingData`, `LazyPagingItems`,
  `RemoteMediator`, paging tests: `references/paging.md`,
  `references/paging-offline.md`, or `references/paging-mvi-testing.md`
- Removed/replaced Compose APIs and migration paths:
  `references/deprecated-patterns.md`
- Experimental styles APIs: `references/styles-experimental.md`
- Production crashes, ANRs, duplicate keys, stale `derivedStateOf`, defensive
  Compose rules: `references/production-crash-playbook.md`
- Compose-focused code review: `references/pr-review.md`

### Source-code receipts

Use source receipts only when the user asks for internals or when implementation
behavior needs verification beyond guidance docs:

- Runtime/state internals: `references/source-code/runtime-source.md`
- UI/layout/measurement/draw internals: `references/source-code/ui-source.md`
- Foundation internals: `references/source-code/foundation-source.md`
- Material 3 internals: `references/source-code/material3-source.md`
- Navigation Compose internals: `references/source-code/navigation-source.md`

## Workflow

1. Identify the Compose layer involved: runtime/state, UI/layout, foundation,
   Material 3, navigation, accessibility, animation, or performance.
2. Read the routed reference file if the task is more than a small mechanical edit.
3. Follow existing project patterns from `AGENTS.md` and nearby app code.
4. Keep composables stateless where practical; pass data and event lambdas down.
5. Keep ViewModels at screen boundaries; avoid passing ViewModel instances into
   child composables.
6. Use string resources for user-visible text.
7. Add or update focused tests or Maestro flows when UI behavior changes.

## Key Principles

- Compose runs in composition, layout, and drawing phases. State reads in each
  phase only invalidate that phase and later work.
- Recomposition should be cheap and skippable. Prefer stable UI state, avoid
  unnecessary allocations in composable bodies, and use `remember` for expensive
  derived work.
- Modifier order is behavior. Preserve or reason through order when changing
  padding, click targets, semantics, backgrounds, clipping, and drawing.
- Hoist state only as high as needed. Use ViewModels for screen state and local
  Compose state for ephemeral UI state.
- Use side-effect APIs only to bridge Compose with imperative work. State writes
  during composition are a bug smell.
