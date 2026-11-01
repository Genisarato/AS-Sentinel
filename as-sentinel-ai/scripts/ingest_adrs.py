import argparse
import re
import sys
from pathlib import Path
from typing import Iterator

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.core.config import settings
from app.vector_store.qdrant import QdrantVectorStore


REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_ADR_DIRECTORY = REPOSITORY_ROOT / "docs" / "adr"
ADR_ID_PATTERN = re.compile(r"^(?P<rule_id>ADR-\d+)(?:-|$)", re.IGNORECASE)
ADR_TITLE_PATTERN = re.compile(r"^#\s+(?P<title>.+?)\s*$", re.MULTILINE)


def parse_adr(path: Path) -> tuple[str, str, str]:
    """Read an ADR and return its stable identifier, title, and full content."""
    content = path.read_text(encoding="utf-8").strip()
    if not content:
        raise ValueError(f"ADR file is empty: {path}")

    title_match = ADR_TITLE_PATTERN.search(content)
    if title_match is None:
        raise ValueError(f"ADR has no level-one title: {path}")

    id_match = ADR_ID_PATTERN.match(path.stem)
    if id_match is None:
        raise ValueError(f"ADR filename must start with ADR-<number>: {path}")

    return id_match.group("rule_id").upper(), title_match.group("title"), content


def iter_adrs(directory: Path) -> Iterator[Path]:
    """Yield ADR Markdown files in deterministic order."""
    if not directory.is_dir():
        raise FileNotFoundError(f"ADR directory does not exist: {directory}")
    yield from sorted(directory.glob("ADR-*.md"))


def ingest_directory(store: QdrantVectorStore, directory: Path) -> int:
    """Index all ADRs in a directory and return the number of indexed files."""
    store.init_collection()
    indexed = 0
    for path in iter_adrs(directory):
        rule_id, title, content = parse_adr(path)
        store.upsert_rule(rule_id, title, content)
        indexed += 1
        print(f"Indexed {rule_id}: {path.name}")
    return indexed


def main() -> None:
    parser = argparse.ArgumentParser(description="Index ADR Markdown files in Qdrant.")
    parser.add_argument(
        "--adr-dir",
        type=Path,
        default=DEFAULT_ADR_DIRECTORY,
        help=f"ADR directory (default: {DEFAULT_ADR_DIRECTORY})",
    )
    parser.add_argument("--host", default=settings.QDRANT_HOST, help="Qdrant host")
    parser.add_argument(
        "--port", type=int, default=settings.QDRANT_PORT, help="Qdrant REST port"
    )
    args = parser.parse_args()

    store = QdrantVectorStore(host=args.host, port=args.port)
    indexed = ingest_directory(store, args.adr_dir.resolve())
    print(f"Indexed {indexed} ADR(s) into '{store.collection_name}'.")


if __name__ == "__main__":
    main()
