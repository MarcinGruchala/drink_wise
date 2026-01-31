# Material 3 Expressive Setup Plan

## Overview

This plan migrates the Drink Wise app from standard `MaterialTheme` to `MaterialExpressiveTheme` to achieve a modern, native Android feeling aligned with Android 16's visual language.

### Goals
- Use M3 Expressive for energetic, emotive, and playful UI
- Enable expressive motion animations with spring-based physics
- Define a consistent expressive shape scale
- Maintain dynamic color support (Android 12+)

## Prerequisites

- **Compose BOM**: 2025.02.00 or later (already satisfied)
- **Material 3 Expressive**: `androidx.compose.material3:material3:1.5.0-alpha13` or later (required for public M3 Expressive APIs)

## Implementation Steps

### Step 1: Create Shape.kt

Create `app/src/main/java/com/mgruchala/drinkwise/presentation/theme/Shape.kt` with the expressive shape scale:

```kotlin
package com.mgruchala.drinkwise.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
```

### Step 2: Update Theme.kt

Migrate `Theme.kt` to use `MaterialExpressiveTheme`:

1. Add `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` annotation
2. Replace `MaterialTheme` with `MaterialExpressiveTheme`
3. Add `MotionScheme.expressive()` for fluid animations
4. Include `ExpressiveShapes` in the theme
5. Maintain existing dynamic color logic

## Files Modified

| File | Action |
|------|--------|
| `docs/feature_list.md` | Updated description |
| `docs/features/material3-setup/plan.md` | Created (this file) |
| `app/.../theme/Shape.kt` | Created |
| `app/.../theme/Theme.kt` | Modified |

## Testing Checklist

- [ ] Project builds successfully (`./gradlew assembleDebug`)
- [ ] App launches without crashes
- [ ] Dynamic colors work on Android 12+ devices
- [ ] Dark/light theme switching works correctly
- [ ] Animations feel more fluid with expressive motion
- [ ] No visual regressions in existing screens

## Notes

- M3 Expressive APIs are experimental and require `@OptIn` annotation
- The expressive motion scheme uses spring-based physics for smoother animations
- Existing UI components automatically inherit the new theme settings
