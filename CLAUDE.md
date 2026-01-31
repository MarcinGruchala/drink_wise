# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Claude Code Instructions

Always use Context7 MCP when needing library/API documentation, code generation, setup or configuration steps — without requiring explicit use context7 in prompts. Prioritize fetching current Jetpack Compose and Material 3 documentation since M3 Expressive APIs are experimental and evolving.

## Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run unit tests for a specific module
./gradlew :app:test
./gradlew :alcohol-database:test
./gradlew :user-preferences:test

# Run Android instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Architecture

**Drink Wise** is an Android app for tracking alcohol consumption using alcohol units (volume × ABV / 1000).

### Module Structure

- **app** - Main application module with UI, navigation, and domain logic
- **alcohol-database** - Room database module for storing drink records
- **user-preferences** - DataStore-based preferences module for user settings

### Tech Stack

- **UI**: Jetpack Compose with Material 3 Expressive
- **DI**: Hilt (all modules use `@InstallIn(SingletonComponent::class)`)
- **Database**: Room with KSP
- **Preferences**: DataStore Preferences
- **Navigation**: Compose Navigation with bottom navigation bar

### Key Patterns

**Data Flow**: ViewModels expose `StateFlow<State>` to Compose UI. Repository interfaces use Kotlin `Flow` for reactive data. Each ViewModel combines multiple flows (drinks, preferences) using `combine()` and `flatMapLatest()`.

**Dependency Injection**: Each module provides its own Hilt module:
- `AlcoholDatabaseModule` provides `AlcoholDatabase` and `DrinkDao`
- `UserPreferencesModule` provides `AlcoholLimitPreferencesDataSource` and `SummaryPeriodPreferencesDataSource`
- `DrinksRepositoryModule` provides `DrinksRepository`

**Time Abstraction**: `Clock` interface in `utils/time/` enables testable time-dependent code.

### App Screens

Four main screens via bottom navigation: Home, Calendar, Calculator, Settings.

### String Resources

All user-visible strings are in `app/src/main/res/values/strings.xml`. Use string resources for new UI text.

### Build Variants

- **debug**: Uses `.dev` application ID suffix
- **release**: Minified with ProGuard, requires `keystore.properties`

---

## UI/UX Design Guidelines

This app follows **Material Design 3 Expressive** principles to achieve a native, polished Android experience that feels modern, engaging, and emotionally resonant.

### Design Philosophy

M3 Expressive focuses on creating UIs that are intuitive, engaging, and reflective of the user's personal style. The design should feel:
- **Energetic & Emotive** - UI should evoke positive feelings
- **Playful & Creative** - Interactions should delight users
- **Modern & Fresh** - Align with Android 16's visual language
- **Native & Cohesive** - Feel like a first-party Android app

### Material 3 Expressive Setup

```kotlin
// build.gradle.kts (app module)
dependencies {
    implementation("androidx.compose.material3:material3:1.5.0-alpha13")  // or latest
}
```

```kotlin
// Theme setup with MaterialExpressiveTheme
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrinkWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> darkColorScheme()
        else -> expressiveLightColorScheme()
    }
    
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = expressiveShapes,
        typography = expressiveTypography,
        content = content
    )
}
```

### Key M3 Expressive Components

All expressive components require opt-in:
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
```

**Recommended Components for Drink Wise:**

| Component | Use Case |
|-----------|----------|
| `LoadingIndicator` / `ContainedLoadingIndicator` | Replace `CircularProgressIndicator` for short waits (<5s) |
| `SplitButtonLayout` | Add drink with quick presets |
| `ButtonGroup` | Toggle between drink categories |
| `FlexibleBottomAppBar` | Enhanced bottom navigation |
| `FloatingActionButtonMenu` | Quick actions for adding drinks |
| `VerticalFloatingToolbar` | Contextual actions on detail screens |

### Typography Guidelines

Use M3 Expressive typography roles consistently:
- `displayLarge/Medium/Small` - Hero numbers (weekly units total)
- `headlineLarge/Medium/Small` - Screen titles, section headers
- `titleLarge/Medium/Small` - Card titles, list item primary text
- `bodyLarge/Medium/Small` - Descriptions, secondary content
- `labelLarge/Medium/Small` - Buttons, chips, navigation labels

```kotlin
Text(
    text = "12.5 units",
    style = MaterialTheme.typography.displayMedium,
    color = MaterialTheme.colorScheme.primary
)
```

### Color System

Leverage the full M3 color palette for visual hierarchy:
- **Primary** - Key actions, FABs, selected states
- **Secondary** - Less prominent actions (filter chips)
- **Tertiary** - Contrasting accents for emphasis
- **Surface/SurfaceVariant** - Cards, containers
- **Error** - Over-limit warnings

**Dynamic Color**: Support Android 12+ wallpaper-based theming for a native feel.

### Shape & Elevation

Use the expressive shape scale:
```kotlin
val expressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
    largeIncreased = RoundedCornerShape(36.dp)  // Expressive addition
)
```

### Motion & Animation

Use `MotionScheme.expressive()` for fluid, natural animations:
- Sparkle ripple effects (Android 12+)
- Stretch overscroll on scrolling containers
- Shape morphing on loading indicators
- Spring-based transitions between states

```kotlin
// Animated visibility with expressive motion
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
            expandVertically(),
    exit = fadeOut() + shrinkVertically()
)
```

### Native Feel Checklist

- [ ] Dynamic color support (Android 12+)
- [ ] Edge-to-edge display with proper insets
- [ ] Predictive back gesture support
- [ ] System font scaling respect
- [ ] Dark theme support
- [ ] Haptic feedback on key interactions
- [ ] Proper keyboard handling (IME insets)
- [ ] Accessibility: TalkBack, content descriptions
- [ ] Adaptive layouts for different screen sizes

### Component Patterns

**Cards with proper containment:**
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant
    ),
    shape = MaterialTheme.shapes.medium
) {
    // Content
}
```

**Bottom Navigation with M3:**
```kotlin
NavigationBar {
    items.forEach { item ->
        NavigationBarItem(
            selected = currentRoute == item.route,
            onClick = { navigateTo(item.route) },
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { Text(item.label) }
        )
    }
}
```

---

## Recommended Tools & Extensions

### Recommended MCP Servers for Android/UI Development

1. **Context7** - Documentation lookup for Jetpack Compose and Material 3
   ```bash
   npx @anthropic-ai/mcp-server-context7
   ```

2. **Brave Search** - Look up latest M3 Expressive component APIs
   ```bash
   npx @anthropic-ai/mcp-server-brave-search
   ```

### Android Studio Integration

For the best experience, combine Claude Code with:
- **Android Studio Hedgehog+** for Compose previews
- **Layout Inspector** for debugging UI hierarchy
- **Compose Preview** annotations for rapid iteration

### Useful Resources

- [Material 3 Expressive Components Catalog](https://github.com/meticha/material-3-expressive-catalog)
- [Official M3 Compose Documentation](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Reply Sample App](https://github.com/android/compose-samples/tree/main/Reply) - M3 reference implementation

### Design Assets

Consider creating a dedicated design module or constants file:
```
app/src/main/java/com/drinkwise/ui/theme/
├── Color.kt           # Color definitions
├── Type.kt            # Typography definitions  
├── Shape.kt           # Shape definitions
├── Theme.kt           # MaterialExpressiveTheme setup
└── Dimensions.kt      # Spacing, sizing constants
```

---

## Code Style for UI

When implementing UI components:

1. **Prefer composition over inheritance** - Build complex UIs from small, focused composables
2. **Extract magic numbers** - Use dimension resources or constants
3. **Use preview annotations** - Add `@Preview` with light/dark variants
4. **Handle all states** - Loading, error, empty, and success states
5. **Test accessibility** - Verify with TalkBack enabled

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrinkCardPreview() {
    DrinkWiseTheme {
        DrinkCard(
            drink = previewDrink,
            onClick = {}
        )
    }
}
```

---

## Feature Planning Workflow

This project uses a structured approach to feature planning. All plans are tracked in the repository for documentation and collaboration.

### Directory Structure

```
docs/
├── feature_list.md           # Master list of features to implement
└── features/                 # Implementation plans for each feature
    ├── day-details/          # Example: Day Details feature
    │   ├── plan.md           # Initial implementation plan
    │   └── iteration-*.md    # Subsequent iteration plans
    ├── material3-setup/
    │   └── plan.md
    └── ...
```

### Planning Process

1. **Feature List**: All features are first recorded in `docs/feature_list.md` with a brief description
2. **Implementation Planning**: Before implementing a feature, use Claude Code's plan mode to create a detailed plan
3. **Plan Storage**: Plans are automatically saved to `docs/features/` (configured in `.claude/settings.json`)
4. **Feature Folders**: Each feature gets its own folder named after the feature (kebab-case)

### Using Plan Mode

When starting a new feature:
1. Enter plan mode (press **Shift+Tab** or start with `claude --permission-mode plan`)
2. Explore the codebase and design the implementation approach
3. The plan will be saved to `docs/features/[plan-name].md`
4. Move the plan to the feature's folder and rename appropriately
5. Get approval and implement

### Plan File Naming Convention

- `plan.md` - Main implementation plan
- `iteration-01.md`, `iteration-02.md` - Follow-up refinements
- `research.md` - Background research and exploration notes

### Configuration

Plan directory is configured in `.claude/settings.json`:
```json
{
  "plansDirectory": "./docs/features"
}
```
