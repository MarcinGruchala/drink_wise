---
name: android-testing
description: Use when adding, updating, or reviewing Android tests in this repository, including JUnit 5 JVM unit tests, ViewModel StateFlow tests, fake repositories, Room or DataStore tests, Compose UI tests, Hilt test setup, or Maestro verification strategy.
---

# Android Testing

Project-local guidance for testing Drink Wise Android changes. Match the
existing Hilt, MVVM, Room, DataStore, Compose, and Maestro setup before adding
new test libraries or patterns.

## Default Workflow

1. Identify the smallest useful test layer: pure JVM unit, ViewModel/Flow, Room
   or DataStore integration, Compose instrumentation, or Maestro end-to-end.
2. Prefer a fast JVM test when behavior can be isolated from Android framework
   APIs.
3. Use fakes over mocks for repositories and data sources unless a mock is much
   clearer for a narrow collaborator interaction.
4. Add a Maestro flow when the change is primarily navigation or user-visible
   UI behavior.
5. Run the narrow module test first, then `./gradlew test` when the change spans
   modules.

## JUnit 5 Unit Tests

Use JUnit Jupiter for local JVM tests:

```kotlin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AlcoholCalculatorTest {
    @Test
    fun `calculates alcohol units`() {
        assertEquals(2.0, calculateAlcoholUnits(quantityMl = 500, abv = 4f))
    }
}
```

Do not add new JVM tests with `org.junit.Test` or `org.junit.Assert`. Keep
AndroidX/JUnit4 imports only where an instrumentation API still requires them.

## ViewModel And Flow Tests

ViewModels expose `StateFlow<State>`. Test state transformations and public
intent methods, not private helpers.

- Use `kotlinx-coroutines-test` when testing coroutine timing or `viewModelScope`.
- Use Turbine only when multiple Flow emissions or ordering matter.
- Check the target module's test dependencies before using coroutine-test or
  Turbine helpers; add them intentionally if the module does not have them yet.
- If a ViewModel uses only `viewModelScope`, prefer `Dispatchers.setMain(...)`
  in the test over injecting dispatchers into production code.
- Inject `Clock` fakes for time-dependent behavior.

Keep fake collaborators small and stateful:

```kotlin
class FakeDrinksRepository : DrinksRepository {
    private val drinks = MutableStateFlow<List<DrinkEntity>>(emptyList())

    override fun getDrinksSince(timestamp: Long): Flow<List<DrinkEntity>> =
        drinks.map { list -> list.filter { it.timestamp >= timestamp } }

    override suspend fun addDrinks(vararg drinkEntity: DrinkEntity) {
        drinks.value += drinkEntity
    }
}
```

## Data Tests

- Room DAO/database behavior belongs in the module that owns Room code.
- Prefer in-memory Room databases for DAO behavior and close them in teardown.
- DataStore tests should use an isolated temporary file or test scope.
- Repository tests should assert observable `Flow` output, not implementation
  details of DAO calls.

## Compose And UI Behavior

For pure composables, test stateless `Screen` functions with supplied state and
event lambdas. Use stable semantic selectors or string resources; avoid tests
that depend on fragile layout details.

For real app flows, prefer Maestro:

- Add or update a focused flow under `maestro/flows/`.
- Use screenshots for static state and video recording for animation or
  transition-heavy behavior.
- Run `scripts/android-maestro-run.sh <flow>` after building the relevant
  debug APK.

## Common Mistakes

- Testing only generated `ExampleUnitTest` scaffolding instead of app behavior.
- Starting with instrumentation when a JVM test would catch the same logic.
- Mocking Flow-heavy repositories when a tiny fake would expose more real
  behavior.
- Forgetting `Clock` and making date/time tests depend on the current day.
- Adding UI text directly in tests when production text should come from
  `strings.xml`.
