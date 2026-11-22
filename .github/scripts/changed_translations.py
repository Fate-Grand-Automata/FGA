"""Prints the POEditor `languages/update` data for the translations in an Android
`localized.xml` that differ from POEditor, each marked as fuzzy.

Usage: changed_translations.py <localized.xml> <terms/list response JSON>
"""

import json
import re
import sys
import xml.etree.ElementTree as ElementTree


def poeditor_content(element: ElementTree.Element) -> str:
    """Returns a <string> element's value the way POEditor stores it after an android_strings import.

    POEditor drops the surrounding quotes and unescapes only `\\"`; other escapes such as `\\'` and `\\n`
    are stored as written. An unquoted value has its whitespace collapsed and `\\'` unescaped.
    """
    value = (element.text or "") + "".join(
        ElementTree.tostring(child, encoding="unicode") for child in element
    )

    if len(value) >= 2 and value.startswith('"') and value.endswith('"'):
        return value[1:-1].replace('\\"', '"')

    return re.sub(r"\s+", " ", value).strip().replace("\\'", "'")


def main(resources_path: str, terms_path: str) -> None:
    with open(terms_path, encoding="utf-8") as file:
        terms = json.load(file)["result"]["terms"]
    terms_by_name = {term["term"]: term for term in terms}

    changes = []
    for element in ElementTree.parse(resources_path).getroot():
        if element.tag != "string":
            print(f"::warning::Skipping unsupported <{element.tag}> in {resources_path}", file=sys.stderr)
            continue

        term = terms_by_name.get(element.get("name"))
        content = poeditor_content(element)
        # Strings that aren't POEditor terms can't be translated there.
        if term is None or content == "" or content == term["translation"]["content"]:
            continue

        changes.append({
            "term": term["term"],
            "context": term["context"],
            "translation": {"content": content, "fuzzy": 1},
        })

    print(json.dumps(changes, ensure_ascii=False))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
