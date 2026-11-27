from __future__ import annotations

import argparse
import json
from pathlib import Path

from solocrew_runtime.evaluation.benchmark import (
    evaluate_benchmark,
    load_benchmark,
)
from solocrew_runtime.evaluation.diagnostics import (
    build_benchmark_diagnostics,
)


def main() -> None:
    """Run the deterministic benchmark and append question-level diagnostics."""

    default_benchmark_path = (
        Path(__file__).resolve().parents[3] / "evaluation" / "benchmarks" / "research-v1.json"
    )

    parser = argparse.ArgumentParser(
        description="Run the Personal Research Specialist benchmark with diagnostics."
    )
    parser.add_argument(
        "--benchmark",
        type=Path,
        default=default_benchmark_path,
        help="Path to the benchmark JSON file.",
    )

    args = parser.parse_args()
    benchmark_path = args.benchmark.expanduser().resolve()

    if not benchmark_path.is_file():
        parser.error(f"Benchmark file does not exist: {benchmark_path}")

    benchmark = load_benchmark(benchmark_path)

    # Preserve the existing benchmark report and its execution latency.
    baseline_report = evaluate_benchmark(benchmark)

    # Run a separate pass to explain each question and matching decision.
    diagnostic_report = build_benchmark_diagnostics(benchmark)

    report = {
        **baseline_report,
        "diagnostics": diagnostic_report,
    }

    print(json.dumps(report, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
