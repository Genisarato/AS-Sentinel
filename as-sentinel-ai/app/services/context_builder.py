from typing import Any, Dict, List


class ASTContextBuilder:
    """
    Transforms raw AST structural summaries into clean semantic descriptions
    optimized for vector search (Qdrant) and LLM architectural evaluation.
    """

    @staticmethod
    def build_file_context(ast_summary: Dict[str, Any]) -> str:
        file_path = ast_summary.get("file_path", "unknown")
        imports = ast_summary.get("imports", [])
        classes = ast_summary.get("classes", [])

        lines: List[str] = [f"File path: {file_path}"]

        # Format imports cleanly
        if imports:
            imported_modules: List[str] = []
            for imp in imports:
                if not isinstance(imp, dict):
                    continue

                mod = str(imp.get("module") or "")
                names = imp.get("names") or imp.get("imported_names") or []
                alias = imp.get("alias")
                level = imp.get("level", 0)
                if isinstance(names, str):
                    names = [names]
                names = [str(name) for name in names if name]
                module_name = f"{'.' * level}{mod}" if level else mod

                if module_name and names:
                    imported_modules.append(f"{module_name} ({', '.join(names)})")
                elif module_name and alias and alias != mod:
                    imported_modules.append(f"{module_name} ({alias})")
                elif module_name:
                    imported_modules.append(module_name)
            lines.append(
                f"Dependencies/Imports: {', '.join(imported_modules) or 'None'}"
            )
        else:
            lines.append("Dependencies/Imports: None")

        # Format classes and methods (handling both dict and str items)
        if classes:
            for cls in classes:
                if isinstance(cls, str):
                    lines.append(f"Class '{cls}' defines methods: [no methods]")
                    continue
                if not isinstance(cls, dict):
                    continue

                raw_bases = cls.get("bases", [])
                if isinstance(raw_bases, str):
                    raw_bases = [raw_bases]
                base_names = [
                    str(b.get("name", "")) if isinstance(b, dict) else str(b)
                    for b in raw_bases
                ]
                base_names = [name for name in base_names if name]
                bases = f" inherits from ({', '.join(base_names)})" if base_names else ""

                raw_methods = cls.get("methods", [])
                if isinstance(raw_methods, str):
                    raw_methods = [raw_methods]
                method_names = [
                    str(m.get("name", "")) if isinstance(m, dict) else str(m)
                    for m in raw_methods
                ]
                method_names = [name for name in method_names if name]
                methods = ", ".join(method_names) or "no methods"

                class_name = cls.get("name", "Unknown")
                lines.append(f"Class '{class_name}'{bases} defines methods: [{methods}]")
        else:
            lines.append("Classes defined: None")

        return "\n".join(lines)