# Plan: Catdoku

## Intent

- An offline Android cat logic puzzle in the Queens and Star Battle family.
- Place one cat in every row, column, and color region. Cats may not touch,
  not even diagonally.
- Calm, dark, cozy presentation with original cat art and original copy.
- No internet permission, ads, analytics, accounts, in-app purchases, or
  runtime permissions. Every asset is bundled in the APK.
- A finite, generated level pack with a daily puzzle, persistent progress,
  hints, undo, and simple stats.
- Pick up and play with no timer pressure. Every puzzle has one solution and
  can be solved by logic without guessing.

## Identity and conventions

- Display name: Catdoku.
- Application id and namespace: `com.glowberrylabs.catdoku`.
- Root project name: `catdoku`.
- Repository directory: `catdoku`.
- Package directories under
  `app/src/main/kotlin/com/glowberrylabs/catdoku`.
- Single `app` module. Kotlin, Jetpack Compose, Material 3.
- compileSdk 36, minSdk 26, targetSdk 35, JVM target 17, JDK 21.
- Kotlin 2.3.21, AGP 8.13.2, Compose BOM 2026.05.00, Gradle 8.14.5.
- Kotlin in `app/src/main/kotlin`, resources in `app/src/main/res`,
  screenshots and plans in `docs/`, asset generators in `scripts/`.
- ktlint and detekt, with justfile recipes matching the sibling Android
  repos.
- Dual MIT/Apache-2.0 licensing.
- Dark-only theme. No dynamic color. No `FLAG_SECURE`.
- No game engine, Room, dependency injection framework, or navigation
  library.

## Legal guardrail

- Homage only. The rules of row, column, region, and non-touching placement
  are a genre convention and are free to implement.
- No Meowdoku name, logo, cat art, palette, copy, level data, or code.
- No code, assets, or text copied from other Meowdoku clones.
- App copy and documentation call Catdoku a cat logic puzzle or a region
  puzzle. They do not use the Meowdoku trademark.
- All cat art, launcher art, sounds, region palettes, and user-facing copy
  are original.

## Board and rules

- Board is N by N cells, with N from 4 through 8.
- The board is divided into exactly N color regions.
- Every region is orthogonally connected and contains at least one cell.
  Region sizes may vary.
- Exactly one cat belongs in every row, every column, and every region.
- No two cats may touch in any of the eight directions: horizontal,
  vertical, or diagonal.
- Because rows and columns hold one cat each, diagonal touching is the
  binding spacing rule. The engine still enforces all eight directions.
- There are no prefilled cats. The region layout is the only given.
- Every puzzle has exactly one solution.
- Every puzzle is solvable from the empty board by the logic solver
  described below. Guessing is never required.
- The puzzle is solved when all N cats are placed and satisfy every rule.
- Placing a cat that conflicts with the solution costs one heart and leaves
  a red mistake mark. The cat is not placed.
- A level starts with three hearts. At zero hearts the board fails and the
  player may retry. Hearts refill on retry or on a new puzzle.

## Content

- 500 levels are generated, validated, committed, and bundled.
- Level sizes and counts:
  - 4 by 4: 20 levels.
  - 5 by 5: 60 levels.
  - 6 by 6: 120 levels.
  - 7 by 7: 150 levels.
  - 8 by 8: 150 levels.
- Levels are ordered by increasing size, then by increasing difficulty
  within the same size.
- 365 daily puzzles are generated, validated, committed, and bundled.
- Every daily puzzle is 7 by 7.
- The daily puzzle is selected by the current UTC epoch day modulo 365.
  The pack repeats annually.
- A daily board belongs to the UTC day it was started. If the day changes,
  the next daily starts fresh.
- After level 500 the pack is complete. The menu shows all levels solved and
  the daily puzzle remains available.
- No runtime level generation, level editor, or custom puzzle import.

## Puzzle generation

- `scripts/generate_puzzles.py` uses only the Python standard library.
- The generator is deterministic for a given seed and filter policy.
- Generation steps per candidate puzzle:
  - Generate a valid cat placement row by row. Each row chooses an unused
    column whose distance from the previous row's column is at least two.
  - Seed one region on each solution cell.
  - Grow the regions one cell at a time into orthogonal neighbors. Prefer the
    smallest region when choosing which region grows. Region sizes may vary.
  - Require every region to be non-empty and connected.
  - Refine the region layout to a unique solution. Find an alternative
    solution, then move one of its non-target cells into an adjacent region
    without disconnecting the source region. Repeat until no alternative
    solution exists.
  - Count all cat placements that satisfy the row, column, region, and
    spacing rules. Reject anything other than exactly one solution.
  - Run the logic solver from the empty board. Reject the puzzle if the
    solver stalls or reaches a contradiction.
  - Record the ordered solution cells produced by the logic solver. This
    order becomes the hint path.
  - Assign a difficulty score from solver rounds, advanced deductions, and
    candidate eliminations.
- The logic solver uses only deterministic deductions:
  - Naked single: a row, column, or region has only one candidate cell.
  - Hidden single: a cell is the only candidate in its row, column, or
    region.
  - Region-to-line confinement: all candidates in a region sit in one row
    or one column.
  - Line-to-region confinement: all candidates in a row or column sit in
    one region.
  - Adjacency locking: a cell adjacent to every candidate in a region cannot
    hold a cat.
  - Small Hall sets: two or three regions confined to two or three rows or
    columns, and the matching row or column variant.
  - Adjacency elimination around placed cats.
- Difficulty is primarily board size. Within a size, fewer immediate forced
  moves and more advanced deductions rank later.
- Duplicate region layouts are rejected within the pack.
- The generator prints per-size counts, retries, and elapsed time.
- Output is committed as generated Kotlin source. The app never runs the
  generator or a solver at runtime.

## Pack format

- Generated file: `app/src/main/kotlin/com/glowberrylabs/catdoku/game/PuzzleData.kt`.
- The file holds a format version and one triple-quoted raw string.
- Comment lines start with `#`.
- Level records start with `L`, daily records start with `D`.
- Record fields, separated by `|`:
  - record kind
  - size
  - region grid, N times N characters using `0` through `7`, row major
  - hint path, comma-separated cell indices from `0` to N times N minus one
  - difficulty score
- The hint path is a permutation of the N solution cells in logical solve
  order. The solution is derived from the hint path. No separate solution
  field is stored.
- A hand-written `PuzzlePack` parser validates:
  - supported size range
  - region count and non-empty region connectivity
  - region connectivity
  - hint path length, uniqueness, and bounds
  - solution row, column, region, and spacing constraints
- Parser failures report the offending line number.
- Unit tests re-verify unique solutions for every bundled puzzle with a
  test-only solver.
- The level number is the one-based position in the level section.
- `just puzzles` regenerates the committed pack.

## Controls and interaction

- Single tap on an empty cell places a neutral X note.
- Single tap on a neutral X removes it.
- Single tap on a cat does nothing.
- Double tap on an empty cell or a neutral X attempts to place a cat.
- Double tap on a cat removes it.
- A correct cat clears any note in the cell.
- A wrong cat attempt:
  - costs one heart
  - turns the cell into a permanent red mistake X for that board
  - does not place a cat
  - plays the wrong sound and a heavy haptic
- Red mistake X cells cannot be cleared, because the cell can never hold a
  cat in that puzzle.
- Undo reverses the latest note toggle, cat placement, or cat removal.
- Undo never refunds a heart and never clears a red mistake X.
- Undo is disabled after the board is solved or failed.
- Restart resets the board, hearts, hints used, and undo history.
- The hint button highlights the first cell in the hint path that does not
  yet contain a cat.
- Hints do not place the cat. The player still confirms with a double tap.
- Hints are unlimited and never cost a heart.
- A hint also works when its cell currently holds a neutral X.
- Neutral X notes never constrain hints.
- The hint highlight pulses in gold and shows a short banner. It clears on
  the next board action.
- Optional auto-mark shows derived Xs from currently placed cats:
  - same row
  - same column
  - same region
  - all eight neighboring cells
- Auto marks are derived, not stored. Removing a cat re-evaluates them.
- Manual X notes render darker and auto X notes render lighter.
- The auto-mark setting is off by default and persists.
- No timer, countdown, or speed score.

## Progress and daily flow

- Levels are sequential. The player starts at level 1.
- Solving a level marks it complete and unlocks the next level.
- The menu shows the next unsolved level and total solved count.
- The current level board is saved after every action and restored on
  launch, including after process death.
- Undo history is not persisted across process death.
- The daily puzzle is a separate board slot with its own saved state.
- The daily streak increments when the daily is solved on the UTC day after
  the last solved daily.
- Solving the same daily twice does not increment the streak.
- A missed UTC day resets the streak to one on the next solve.
- Switching between the level and daily boards does not discard either
  board.
- A failed daily can be retried. The streak updates only on a solve.

## Persistence

- SharedPreferences name: `catdoku`.
- Settings:
  - sound enabled
  - haptics enabled
  - auto-mark enabled
  - region letters enabled
- Level progress:
  - highest solved level
  - total solved count
  - hints used count
- Daily progress:
  - last solved UTC epoch day
  - current streak
  - total daily solves
- Current level board:
  - level number
  - cell marks string
  - hearts
  - hints used
- Current daily board:
  - UTC epoch day
  - cell marks string
  - hearts
  - hints used
- Cell marks string uses one character per cell:
  - `0` empty
  - `n` neutral X
  - `m` red mistake X
  - `c` cat
- Auto marks are never stored.
- Current boards are saved after every state change.
- A saved level board is discarded if its level number no longer matches
  the next unsolved level.
- A saved daily board is discarded if its UTC day is not today.

## UI and art

- Dark-only cozy theme with fixed brand colors, not dynamic color.
- Background uses a deep plum to navy vertical gradient.
- Board sits on a rounded dark surface with a soft border and shadow.
- Cells use rounded corners and small gaps.
- Region boundaries are drawn thicker than cell gaps so colors are not the
  only region cue.
- Region palette uses eight muted, distinguishable pastels that read on a
  dark background:
  - rose
  - amber
  - sage
  - sky
  - lavender
  - peach
  - mint
  - mauve
- Cat art is an original cream cat face with pointed ears, closed eyes,
  whiskers, and a curled tail.
- The cat mark pops in with a scale and alpha animation.
- Neutral X notes are soft lavender crosses.
- Red mistake X marks are coral crosses with a heavier stroke.
- Auto X notes use a lower opacity than manual notes.
- The hint highlight is a pulsing gold ring plus a short banner.
- A solved board plays a gentle cat bounce and a paw-print burst.
- The launcher icon is an adaptive icon combining an original cat face with
  a puzzle cell or paw motif. A monochrome layer is included.
- HUD layout:
  - back or menu button
  - level label or daily label
  - hearts
  - undo
  - hint
- Menu layout:
  - title and original cat mascot
  - continue button for the next unsolved level
  - daily button with streak
  - how to play button
  - settings button
  - solved count out of 500
- Overlays:
  - how to play with the four rules and a small board diagram
  - level solved with hearts and hints used
  - daily solved with streak
  - out of hearts with retry and menu
  - all levels complete
  - settings with sound, haptics, auto-mark, and region letters
  - pause menu with resume, restart, how to play, settings, and quit
- Optional region letters are small A through H labels in cell corners,
  using the region index. They are off by default.
- Responsive layout:
  - portrait stacks the HUD, board, and action bar
  - landscape places the board on one side and the HUD and actions on the
    other
  - the board scales to the available width or height and keeps square
    cells
- Edge-to-edge layout with safe drawing insets applied.
- Keep the screen awake while a puzzle board is open.
- Back from a puzzle opens the pause menu. Back from the menu exits.
- Every cell exposes a content description with row, column, region, and
  state.
- State changes are announced through standard semantics.
- Touch targets are at least 44dp on typical phone widths. An 8 by 8 board
  fits without scrolling on the pixel9a emulator.

## Audio and haptics

- SoundPool with `USAGE_GAME` attributes.
- Four short WAV effects generated by `scripts/make_sounds.py` using only
  the Python standard library:
  - soft tap for notes
  - gentle chirp for a placed cat
  - soft thud for a wrong cat
  - warm chime for a solved board
- Sound generation inputs and outputs are deterministic. Files live in
  `app/src/main/res/raw`.
- `just sounds` regenerates the WAV files.
- Sound and haptics toggles persist in SharedPreferences.
- Haptics use `View.performHapticFeedback`:
  - `CLOCK_TICK` for note toggles and cat placement
  - `LONG_PRESS` for a wrong cat
  - `LONG_PRESS` for a solved board
- Haptics are skipped when the toggle is off.
- No background music.

## Architecture

- `game` package, pure Kotlin and unit-testable:
  - `Position`, `CellMark`, and `PuzzleStatus`.
  - `Puzzle` with size, region grid, solution set, hint path, and difficulty.
  - `PuzzlePack` and `PuzzlePackParser` for the generated data.
  - `PuzzleEngine` for notes, cat placement, mistakes, hearts, undo, hints,
    restart, and solve detection.
  - `AutoMarkResolver` for derived X cells.
  - `LevelRamp` for level number, size, and pack lookup.
  - `DailySelector` for UTC day lookup and streak transitions.
  - `PuzzleUiState` with screen, puzzle state, settings, progress, and
    transient hint highlight.
- `data` package: `ProgressStore` wraps SharedPreferences for settings,
  progress, boards, and daily state.
- `audio` package: `GameSounds` wraps SoundPool loading and playback.
- `ui` package:
  - `CatdokuTheme` and `RegionPalette`.
  - `CatArt` painter helpers.
  - `PuzzleBoard` with Canvas rendering plus transparent per-cell input
    targets.
  - `GameHud`, `ActionBar`, `MenuScreen`, and overlays.
- `PuzzleViewModel`:
  - owns the current puzzle state and `StateFlow`
  - applies note, cat, undo, hint, restart, pause, and navigation actions
  - persists board and progress after each change
  - emits one-shot events for sound and haptics
  - exposes settings and daily information
- `MainActivity`:
  - edge-to-edge dark system bars
  - ViewModel wiring
  - back handling
  - keep screen awake while a board is open
  - collects events and forwards them to sound and haptics
- The UI depends only on immutable state and callbacks.
- No coroutines are needed for puzzle generation at runtime. Generated data
  is parsed once at startup.
- No navigation library. A screen enum in the UI state switches between the
  menu and a puzzle.

## Testing

- JUnit 4 unit tests for the engine:
  - single tap toggles a note off and on
  - cat placement clears a note
  - correct cat placement
  - wrong cat placement loses one heart and leaves a mistake mark
  - zero hearts fails the board
  - cat removal
  - undo of note, cat placement, and cat removal
  - undo does not refund hearts or clear mistakes
  - undo is disabled after solve or failure
  - solve detection for all four constraints
  - hint returns the first unplaced cell in the hint path
  - hint is disabled after a solve
  - restart resets hearts, hints, marks, and undo
  - auto-mark derivation for row, column, region, and adjacency
- Pack tests:
  - parse every level and daily record
  - supported sizes and correct record counts
  - N regions, each non-empty and connected
  - hint path contains N unique in-bounds cells
  - every solution satisfies row, column, region, and spacing rules
  - a test-only solver confirms every bundled puzzle has exactly one
    solution
  - levels are ordered by size then difficulty
  - dailies are all 7 by 7
- Persistence tests:
  - cell marks string round trip
  - level board round trip
  - daily board round trip
  - stale level and stale daily boards are rejected
  - streak increment, repeat solve, and missed day reset
  - daily selector is deterministic for a given UTC epoch day
- Generator checks:
  - the script refuses to emit a pack with a failed uniqueness or logic
    check
  - generation is repeatable for the committed seed
- `just check` runs ktlint and detekt.
- `just test` runs unit tests.
- `just build` produces the debug APK.
- `just run` installs and launches on the pixel9a emulator.
- `just puzzles` regenerates the pack.
- `just sounds` regenerates the WAV files.
- Manual verification:
  - every board size
  - single tap notes and double tap cats
  - wrong cat, heart loss, failure, retry
  - auto-mark on and off
  - hint highlight and banner
  - undo across note and cat moves
  - level completion and next level
  - daily solve and streak
  - switching level and daily boards without losing either
  - rotation
  - process death and restore
  - sound and haptics toggles
  - pause and quit
  - content descriptions and region letters
- Capture `docs/screenshot.png` from the emulator and link it in the README.

## Stages

1. Plan document
   1a. Write this plan at `docs/plans/PLAN-catdoku.md`.
   1b. Commit only the plan document.
2. Project scaffold
   2a. Add `.gitignore`, `.editorconfig`, detekt config, dual licenses,
   Gradle wrapper, root build files, settings, manifest, dark theme,
   launcher placeholder, justfile, README skeleton, and remove `.gitkeep`.
   2b. Verify the placeholder app builds and commit.
3. Puzzle engine
   3a. Add positions, marks, puzzle model, state, engine, hearts, notes,
   hints, undo, auto-mark resolver, level ramp, and daily selector.
   3b. Add JUnit tests for rules and state transitions.
   3c. Commit.
4. Generated content
   4a. Add the Python generator with solution building, region growth,
   uniqueness checking, logic solving, difficulty ranking, and hint order.
   4b. Generate 500 levels and 365 dailies.
   4c. Add the generated pack, parser, pack tests, and `just puzzles`.
   4d. Commit.
5. State and services
   5a. Add progress store, board serialization, level progress, daily
   state, and settings persistence.
   5b. Add the ViewModel, one-shot events, sound player, sound generator,
   and `just sounds`.
   5c. Add tests for persistence, daily transitions, and hint flow.
   5d. Commit.
6. Board UI
   6a. Add the dark theme, region palette, original cat art, and board
   renderer.
   6b. Add gestures, hint highlight, animations, semantics, and haptics.
   6c. Commit.
7. Screens and polish
   7a. Add the menu, HUD, action bar, pause, settings, rules, solved,
   failed, daily, and all-levels-complete overlays.
   7b. Add responsive layout, insets, back handling, and keep-awake.
   7c. Commit.
8. Verification and finish
   8a. Run `just check`, `just test`, `just build`, and the manual matrix.
   8b. Capture the screenshot and polish the README.
   8c. Fix issues and commit.

## Non-features

- No 15,000-level content pack, level editor, custom puzzle import, or
  endless level generation.
- No timed mode, countdown, speedrun score, or daily leaderboard.
- No light theme or dynamic color.
- No accounts, ads, analytics, crash reporting, in-app purchases, cloud
  sync, or social features.
- No internet permission and no runtime permissions.
- No localization beyond English.
- No release signing configuration.
- No runtime puzzle generator, shipped solver, Room, or dependency
  injection framework.
- No undo of mistakes or heart refunds.
- No copying of Meowdoku or other clone names, art, palettes, copy, or
  code.

## Definition of done

- Debug APK builds and installs on the pixel9a emulator.
- `just check` and `just test` pass.
- The merged manifest contains no runtime permissions and no INTERNET
  permission.
- The bundled pack contains 500 validated levels and 365 validated daily
  puzzles.
- Every bundled puzzle has one solution and is solved by the logic solver
  without guessing.
- Notes, cats, mistakes, hearts, hints, undo, restart, and solve detection
  work on every board size.
- Level progress, daily streak, settings, and current boards survive
  rotation and process death.
- Auto-mark and region letters work and persist.
- Sound and haptics work and can be muted.
- The app is fully playable in portrait and landscape.
- Screenshot and README are complete.
- Both license files are present.
