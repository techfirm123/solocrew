import argparse
import json
from pathlib import Path

from solocrew_runtime.evaluation.benchmark import (
    evaluate_benchmark,
    load_benchmark,
)


def default_benchmark_path() -> Path:
    """Resolve the standard benchmark relative to the Python project."""

    project_root = Path(__file__).resolve().parents[3]

    return project_root / "evaluation" / "benchmarks" / "research-v1.json"


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Evaluate the SoloCrew deterministic research specialist."
    )

    parser.add_argument(
        "--benchmark",
        type=Path,
        default=default_benchmark_path(),
        help="Path to a benchmark JSON file.",
    )

    parser.add_argument(
        "--output",
        type=Path,
        help="Optional path for writing the evaluation report as JSON.",
    )

    arguments = parser.parse_args()

    benchmark = load_benchmark(arguments.benchmark)
    result = evaluate_benchmark(benchmark)

    serialized = json.dumps(
        result,
        indent=2,
        sort_keys=True,
    )

    if arguments.output:
        arguments.output.parent.mkdir(
            parents=True,
            exist_ok=True,
        )

        arguments.output.write_text(
            serialized + "\n",
            encoding="utf-8",
        )

        print(f"Evaluation report written to {arguments.output}")
    else:
        print(serialized)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
