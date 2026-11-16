import argparse
import itertools
import random
import time
from pathlib import Path

SIZES = (4, 5, 6, 7, 8)
DEFAULT_COUNTS = (20, 60, 120, 150, 150)
DEFAULT_DAILY_COUNT = 365
DEFAULT_DAILY_SIZE = 7
DEFAULT_SEED = 20261116
DEFAULT_HALL_LIMIT = 3
DEFAULT_MAX_ATTEMPTS = 400000
OUTPUT_PATH = (
    Path(__file__).resolve().parent.parent
    / "app"
    / "src"
    / "main"
    / "kotlin"
    / "com"
    / "glowberrylabs"
    / "catdoku"
    / "game"
    / "PuzzleData.kt"
)


def popcount(value):
    return value.bit_count()


def bits(mask):
    while mask:
        low = mask & -mask
        yield low.bit_length() - 1
        mask ^= low


def neighbors(cell, size):
    row, col = divmod(cell, size)
    result = []
    if row > 0:
        result.append(cell - size)
    if row < size - 1:
        result.append(cell + size)
    if col > 0:
        result.append(cell - 1)
    if col < size - 1:
        result.append(cell + 1)
    return result


def generate_solution(size, rng):
    columns = [-1] * size
    used = [False] * size

    def place(row):
        if row == size:
            return True
        candidates = list(range(size))
        rng.shuffle(candidates)
        for col in candidates:
            if used[col]:
                continue
            if row > 0 and abs(col - columns[row - 1]) <= 1:
                continue
            used[col] = True
            columns[row] = col
            if place(row + 1):
                return True
            used[col] = False
        return False

    return columns if place(0) else None


def grow_regions(solution, size, rng):
    total = size * size
    grid = [-1] * total
    sizes = [0] * size

    for row, col in enumerate(solution):
        cell = row * size + col
        grid[cell] = row
        sizes[row] += 1

    remaining = total - size
    while remaining > 0:
        frontier = []
        for cell in range(total):
            if grid[cell] != -1:
                continue
            assigned = [
                grid[neighbor]
                for neighbor in neighbors(cell, size)
                if grid[neighbor] != -1
            ]
            if assigned:
                frontier.append((cell, sorted(set(assigned))))
        if not frontier:
            return None
        cell, adjacent_regions = frontier[rng.randrange(len(frontier))]
        if rng.random() < 0.7:
            smallest = min(sizes[region] for region in adjacent_regions)
            choices = [
                region for region in adjacent_regions if sizes[region] == smallest
            ]
        else:
            choices = adjacent_regions
        region = rng.choice(choices)
        grid[cell] = region
        sizes[region] += 1
        remaining -= 1

    return grid


def still_connected_without(regions, size, region, remove_cell):
    cells = [
        cell
        for cell, value in enumerate(regions)
        if value == region and cell != remove_cell
    ]
    if not cells:
        return False
    allowed = set(cells)
    seen = {cells[0]}
    stack = [cells[0]]
    while stack:
        cell = stack.pop()
        for neighbor in neighbors(cell, size):
            if neighbor in allowed and neighbor not in seen:
                seen.add(neighbor)
                stack.append(neighbor)
    return len(seen) == len(cells)


def find_alt_solution(regions, size, target_set):
    by_region = [[] for _ in range(size)]
    for cell, region in enumerate(regions):
        by_region[region].append(cell)
    order = sorted(range(size), key=lambda region: len(by_region[region]))
    used_rows = [False] * size
    used_cols = [False] * size
    placed = []
    found = None

    def place(index):
        nonlocal found
        if found is not None:
            return
        if index == size:
            if set(placed) != target_set:
                found = placed.copy()
            return
        region = order[index]
        for cell in by_region[region]:
            row, col = divmod(cell, size)
            if used_rows[row] or used_cols[col]:
                continue
            if any(
                abs(row - other // size) <= 1 and abs(col - other % size) <= 1
                for other in placed
            ):
                continue
            used_rows[row] = True
            used_cols[col] = True
            placed.append(cell)
            place(index + 1)
            placed.pop()
            used_rows[row] = False
            used_cols[col] = False
            if found is not None:
                return

    place(0)
    return found


def refine_to_unique(regions, size, solution_cells, rng):
    target_set = set(solution_cells)
    target_of_region = [-1] * size
    for cell in solution_cells:
        target_of_region[regions[cell]] = cell

    guard = 0
    while guard < size * size * 12:
        guard += 1
        alt = find_alt_solution(regions, size, target_set)
        if alt is None:
            return True
        wrong_cells = [cell for cell in alt if cell not in target_set]
        rng.shuffle(wrong_cells)
        moved = False
        for cell in wrong_cells:
            region = regions[cell]
            if cell == target_of_region[region]:
                continue
            adjacent_regions = sorted(
                {
                    regions[neighbor]
                    for neighbor in neighbors(cell, size)
                    if regions[neighbor] != region
                }
            )
            rng.shuffle(adjacent_regions)
            for adjacent in adjacent_regions:
                if still_connected_without(regions, size, region, cell):
                    regions[cell] = adjacent
                    moved = True
                    break
            if moved:
                break
        if not moved:
            return False
    return False



def count_solutions(regions, size, limit=2):
    solutions = 0

    def place(row, used_cols, used_regions, previous_col):
        nonlocal solutions
        if row == size:
            solutions += 1
            return solutions >= limit
        for col in range(size):
            col_bit = 1 << col
            if used_cols & col_bit:
                continue
            if row > 0 and abs(col - previous_col) <= 1:
                continue
            region = regions[row * size + col]
            region_bit = 1 << region
            if used_regions & region_bit:
                continue
            if place(row + 1, used_cols | col_bit, used_regions | region_bit, col):
                return True
        return False

    place(0, 0, 0, -2)
    return solutions


def logic_solve(regions, size, hall_limit):
    total = size * size
    row_masks = [
        sum(1 << (row * size + col) for col in range(size)) for row in range(size)
    ]
    col_masks = [
        sum(1 << (row * size + col) for row in range(size)) for col in range(size)
    ]
    region_masks = [0] * size
    for cell, region in enumerate(regions):
        region_masks[region] |= 1 << cell
    cell_col_bits = [1 << (cell % size) for cell in range(total)]
    cell_region_bits = [1 << regions[cell] for cell in range(total)]
    adjacency_masks = [0] * total
    for cell in range(total):
        row, col = divmod(cell, size)
        mask = 0
        for other_row in range(max(0, row - 1), min(size, row + 2)):
            for other_col in range(max(0, col - 1), min(size, col + 2)):
                mask |= 1 << (other_row * size + other_col)
        adjacency_masks[cell] = mask

    banned = 0
    cats = 0
    order = []
    row_has = [False] * size
    col_has = [False] * size
    region_has = [False] * size
    rounds = 0
    advanced = 0
    ban_total = 0

    def candidates(unit_mask, has_cat):
        if has_cat:
            return 0
        return unit_mask & ~banned & ~cats

    def add_ban(mask):
        nonlocal banned, ban_total
        fresh = mask & ~banned & ~cats
        if not fresh:
            return 0
        banned |= fresh
        count = popcount(fresh)
        ban_total += count
        return count

    def place(cell):
        nonlocal cats
        cats |= 1 << cell
        order.append(cell)
        row, col = divmod(cell, size)
        region = regions[cell]
        row_has[row] = True
        col_has[col] = True
        region_has[region] = True
        add_ban(
            row_masks[row]
            | col_masks[col]
            | region_masks[region]
            | adjacency_masks[cell]
        )

    def scan_singles():
        for row in range(size):
            cand = candidates(row_masks[row], row_has[row])
            if row_has[row]:
                continue
            if cand == 0:
                return "contradiction"
            if popcount(cand) == 1:
                place(next(bits(cand)))
                return "placed"
        for col in range(size):
            cand = candidates(col_masks[col], col_has[col])
            if col_has[col]:
                continue
            if cand == 0:
                return "contradiction"
            if popcount(cand) == 1:
                place(next(bits(cand)))
                return "placed"
        for region in range(size):
            cand = candidates(region_masks[region], region_has[region])
            if region_has[region]:
                continue
            if cand == 0:
                return "contradiction"
            if popcount(cand) == 1:
                place(next(bits(cand)))
                return "placed"
        return "stalled"

    def scan_row_hall(min_size, max_size):
        added = 0
        for subset_size in range(min_size, max_size + 1):
            for subset in itertools.combinations(range(size), subset_size):
                if any(row_has[row] for row in subset):
                    continue
                cand = 0
                for row in subset:
                    cand |= candidates(row_masks[row], False)
                if cand == 0:
                    return -1
                union_cols = 0
                union_regions = 0
                for cell in bits(cand):
                    union_cols |= cell_col_bits[cell]
                    union_regions |= cell_region_bits[cell]
                if popcount(union_cols) < subset_size or popcount(union_regions) < subset_size:
                    return -1
                if popcount(union_cols) == subset_size:
                    for col in bits(union_cols):
                        for row in range(size):
                            if row in subset:
                                continue
                            added += add_ban(1 << (row * size + col))
                if popcount(union_regions) == subset_size:
                    for region in bits(union_regions):
                        for cell in bits(region_masks[region]):
                            if cell // size in subset:
                                continue
                            added += add_ban(1 << cell)
        return added

    def scan_col_hall(min_size, max_size):
        added = 0
        for subset_size in range(min_size, max_size + 1):
            for subset in itertools.combinations(range(size), subset_size):
                if any(col_has[col] for col in subset):
                    continue
                cand = 0
                for col in subset:
                    cand |= candidates(col_masks[col], False)
                if cand == 0:
                    return -1
                union_rows = 0
                union_regions = 0
                for cell in bits(cand):
                    union_rows |= 1 << (cell // size)
                    union_regions |= cell_region_bits[cell]
                if popcount(union_rows) < subset_size or popcount(union_regions) < subset_size:
                    return -1
                if popcount(union_rows) == subset_size:
                    for row in bits(union_rows):
                        for col in range(size):
                            if col in subset:
                                continue
                            added += add_ban(1 << (row * size + col))
                if popcount(union_regions) == subset_size:
                    for region in bits(union_regions):
                        for cell in bits(region_masks[region]):
                            if cell % size in subset:
                                continue
                            added += add_ban(1 << cell)
        return added

    def scan_region_hall(min_size, max_size):
        added = 0
        for subset_size in range(min_size, max_size + 1):
            for subset in itertools.combinations(range(size), subset_size):
                if any(region_has[region] for region in subset):
                    continue
                cand = 0
                for region in subset:
                    cand |= candidates(region_masks[region], False)
                if cand == 0:
                    return -1
                union_rows = 0
                union_cols = 0
                for cell in bits(cand):
                    union_rows |= 1 << (cell // size)
                    union_cols |= cell_col_bits[cell]
                if popcount(union_rows) < subset_size or popcount(union_cols) < subset_size:
                    return -1
                if popcount(union_rows) == subset_size:
                    for row in bits(union_rows):
                        for region in range(size):
                            if region in subset:
                                continue
                            added += add_ban(region_masks[region] & row_masks[row])
                if popcount(union_cols) == subset_size:
                    for col in bits(union_cols):
                        for region in range(size):
                            if region in subset:
                                continue
                            added += add_ban(region_masks[region] & col_masks[col])
        return added

    def scan_adjacency():
        added = 0
        for region in range(size):
            if region_has[region]:
                continue
            cand = candidates(region_masks[region], False)
            if popcount(cand) < 2:
                continue
            intersection = None
            for cell in bits(cand):
                if intersection is None:
                    intersection = adjacency_masks[cell]
                else:
                    intersection &= adjacency_masks[cell]
            if intersection:
                added += add_ban(intersection)
        return added

    def run_hall(min_size, max_size):
        row_bans = scan_row_hall(min_size, max_size)
        if row_bans < 0:
            return -1
        if row_bans:
            return row_bans
        col_bans = scan_col_hall(min_size, max_size)
        if col_bans < 0:
            return -1
        if col_bans:
            return col_bans
        region_bans = scan_region_hall(min_size, max_size)
        if region_bans < 0:
            return -1
        return region_bans

    while len(order) < size:
        rounds += 1
        if rounds > 400:
            return None
        single = scan_singles()
        if single == "contradiction":
            return None
        if single == "placed":
            continue
        confinement = run_hall(1, 1)
        if confinement < 0:
            return None
        if confinement:
            advanced += 1
            continue
        adjacency = scan_adjacency()
        if adjacency:
            advanced += 1
            continue
        hall = run_hall(2, hall_limit)
        if hall < 0:
            return None
        if hall:
            advanced += 1
            continue
        return None

    return {
        "order": order,
        "difficulty": rounds * 10 + advanced * 5 + ban_total,
    }


def validate_regions(regions, size):
    if len(regions) != size * size:
        return False
    if any(region < 0 or region >= size for region in regions):
        return False
    for region in range(size):
        cells = [cell for cell, value in enumerate(regions) if value == region]
        if not cells:
            return False
        seen = {cells[0]}
        stack = [cells[0]]
        while stack:
            cell = stack.pop()
            for neighbor in neighbors(cell, size):
                if regions[neighbor] == region and neighbor not in seen:
                    seen.add(neighbor)
                    stack.append(neighbor)
        if len(seen) != len(cells):
            return False
    return True


def validate_solution(order, regions, size):
    if len(order) != size or len(set(order)) != size:
        return False
    if any(cell < 0 or cell >= size * size for cell in order):
        return False
    rows = {cell // size for cell in order}
    cols = {cell % size for cell in order}
    regions_used = {regions[cell] for cell in order}
    if len(rows) != size or len(cols) != size or len(regions_used) != size:
        return False
    positions = [(cell // size, cell % size) for cell in order]
    for index, (row, col) in enumerate(positions):
        for other_row, other_col in positions[index + 1 :]:
            if abs(row - other_row) <= 1 and abs(col - other_col) <= 1:
                return False
    return True


def validate_puzzle(size, regions, order, seed):
    if not validate_regions(regions, size):
        raise ValueError(f"invalid regions for size {size} seed {seed}")
    if not validate_solution(order, regions, size):
        raise ValueError(f"invalid solution for size {size} seed {seed}")
    if count_solutions(regions, size) != 1:
        raise ValueError(f"non-unique puzzle for size {size} seed {seed}")


def make_puzzle(size, regions, order, difficulty):
    return {
        "size": size,
        "regions": regions,
        "order": order,
        "difficulty": difficulty,
    }


def generate_section(
    size,
    count,
    seed,
    hall_limit,
    max_attempts,
    label,
    existing_keys=None,
):
    rng = random.Random(seed)
    seen = set() if existing_keys is None else existing_keys
    puzzles = []
    attempts = 0
    started = time.time()
    last_report = started

    while len(puzzles) < count:
        attempts += 1
        if attempts > max_attempts:
            raise SystemExit(
                f"{label}: stopped after {attempts} attempts with "
                f"{len(puzzles)}/{count} puzzles"
            )
        solution = generate_solution(size, rng)
        if solution is None:
            continue
        solution_cells = [row * size + solution[row] for row in range(size)]
        expected = set(solution_cells)
        added = False
        for _ in range(8):
            regions = grow_regions(solution, size, rng)
            if regions is None:
                continue
            if not refine_to_unique(regions, size, solution_cells, rng):
                continue
            if not validate_regions(regions, size):
                continue
            key = tuple(regions)
            if key in seen:
                continue
            if count_solutions(regions, size) != 1:
                continue
            solved = logic_solve(regions, size, hall_limit)
            if solved is None:
                continue
            order = solved["order"]
            if set(order) != expected:
                continue
            try:
                validate_puzzle(size, regions, order, seed)
            except ValueError:
                continue
            seen.add(key)
            puzzles.append(make_puzzle(size, regions, order, solved["difficulty"]))
            added = True
            break
        if not added:
            continue
        now = time.time()
        if len(puzzles) == count or now - last_report >= 5.0:
            print(
                f"{label}: {len(puzzles)}/{count} "
                f"attempts={attempts} elapsed={now - started:.1f}s",
                flush=True,
            )
            last_report = now

    puzzles.sort(key=lambda item: item["difficulty"])
    return puzzles, attempts


def format_record(kind, puzzle):
    regions = "".join(str(value) for value in puzzle["regions"])
    order = ",".join(str(cell) for cell in puzzle["order"])
    return f"{kind}|{puzzle['size']}|{regions}|{order}|{puzzle['difficulty']}"


def render_pack(levels, dailies, seed):
    lines = ["# Catdoku puzzle pack", f"# seed {seed}"]
    lines.extend(format_record("L", puzzle) for puzzle in levels)
    lines.extend(format_record("D", puzzle) for puzzle in dailies)
    content = "\n".join(lines)
    return f"""package com.glowberrylabs.catdoku.game

@Suppress("LargeClass")
internal object PuzzleData {{
    const val VERSION = 1
    val RAW = \"\"\"
{content}
\"\"\".trimIndent()
}}
"""


def parse_counts(value):
    parts = [part.strip() for part in value.split(",")]
    if len(parts) != len(SIZES):
        raise argparse.ArgumentTypeError(
            f"expected {len(SIZES)} comma-separated counts"
        )
    counts = []
    for index, part in enumerate(parts):
        try:
            count = int(part)
        except ValueError as error:
            raise argparse.ArgumentTypeError(str(error)) from error
        if count < 0:
            raise argparse.ArgumentTypeError("counts cannot be negative")
        counts.append((SIZES[index], count))
    return counts


def main():
    parser = argparse.ArgumentParser(description="Generate Catdoku puzzle data")
    parser.add_argument("--seed", type=int, default=DEFAULT_SEED)
    parser.add_argument(
        "--level-counts",
        type=parse_counts,
        default=list(zip(SIZES, DEFAULT_COUNTS)),
    )
    parser.add_argument("--daily-count", type=int, default=DEFAULT_DAILY_COUNT)
    parser.add_argument("--daily-size", type=int, default=DEFAULT_DAILY_SIZE)
    parser.add_argument("--hall-limit", type=int, default=DEFAULT_HALL_LIMIT)
    parser.add_argument("--max-attempts", type=int, default=DEFAULT_MAX_ATTEMPTS)
    parser.add_argument("--output", type=Path, default=OUTPUT_PATH)
    args = parser.parse_args()

    if args.daily_size not in SIZES:
        raise SystemExit(f"unsupported daily size: {args.daily_size}")

    started = time.time()
    levels = []
    seen_regions = set()
    total_attempts = 0

    for size, count in args.level_counts:
        if count == 0:
            continue
        section, attempts = generate_section(
            size=size,
            count=count,
            seed=args.seed + size * 1_000_003,
            hall_limit=args.hall_limit,
            max_attempts=args.max_attempts,
            label=f"level {size}x{size}",
            existing_keys=seen_regions,
        )
        levels.extend(section)
        total_attempts += attempts

    dailies = []
    if args.daily_count > 0:
        dailies, attempts = generate_section(
            size=args.daily_size,
            count=args.daily_count,
            seed=args.seed + args.daily_size * 2_000_003,
            hall_limit=args.hall_limit,
            max_attempts=args.max_attempts,
            label=f"daily {args.daily_size}x{args.daily_size}",
            existing_keys=set(),
        )
        total_attempts += attempts

    if not levels or not dailies:
        raise SystemExit("pack must contain levels and dailies")

    content = render_pack(levels, dailies, args.seed)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(content, encoding="utf-8")
    elapsed = time.time() - started
    print(
        f"wrote {args.output} with {len(levels)} levels and {len(dailies)} dailies "
        f"in {elapsed:.1f}s using {total_attempts} attempts",
        flush=True,
    )


if __name__ == "__main__":
    main()
