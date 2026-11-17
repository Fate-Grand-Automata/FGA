#!/usr/bin/env python3
"""Extract FGO's UI font and UI string tables from an APK or XAPK.

Writes the fonts to <out-dir>/fonts and the Localization* string tables to <out-dir>/text.
Requires UnityPy. Both outputs are the game's copyrighted files: keep them out of the repo.

    extract_game_data.py ~/Downloads/fgo-kr.xapk --out-dir /tmp/fgo-kr
"""

import argparse
import os
import shutil
import sys
import tempfile
import zipfile

try:
    import UnityPy
    from PIL import ImageFont
except ImportError:
    sys.exit("UnityPy is required: pip install UnityPy")

UNITY_DATA = "assets/bin/Data/"


def base_apk(package: str, work_dir: str) -> str:
    """The APK holding the Unity data: the package itself, or the base APK inside an XAPK."""
    with zipfile.ZipFile(package) as archive:
        names = archive.namelist()
        if any(name.startswith(UNITY_DATA) for name in names):
            return package
        apks = [name for name in names if name.endswith(".apk") and not name.startswith("config.")]
        if len(apks) != 1:
            sys.exit(f"expected one base APK inside {package}, found {apks}")
        return archive.extract(apks[0], work_dir)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("package", help="FGO .apk or .xapk")
    parser.add_argument("--out-dir", required=True)
    args = parser.parse_args()

    font_dir = os.path.join(args.out_dir, "fonts")
    text_dir = os.path.join(args.out_dir, "text")
    os.makedirs(font_dir, exist_ok=True)
    os.makedirs(text_dir, exist_ok=True)

    work_dir = tempfile.mkdtemp()
    try:
        with zipfile.ZipFile(base_apk(args.package, work_dir)) as apk:
            for name in apk.namelist():
                if name.startswith(UNITY_DATA):
                    apk.extract(name, work_dir)

        env = UnityPy.load(os.path.join(work_dir, UNITY_DATA))
        for obj in env.objects:
            if obj.type.name == "Font":
                font = obj.read()
                data = bytes(font.m_FontData or b"")
                if not data:
                    continue
                extension = ".otf" if data[:4] == b"OTTO" else ".ttf"
                path = os.path.join(font_dir, font.m_Name + extension)
                with open(path, "wb") as file:
                    file.write(data)
                family, style = ImageFont.truetype(path, 10).getname()
                print(f"font {path} ({family} {style})")
            elif obj.type.name == "TextAsset":
                text = obj.read()
                if not text.m_Name.startswith("Localization"):
                    continue
                script = text.m_Script
                if isinstance(script, str):
                    script = script.encode("utf-8", "surrogateescape")
                path = os.path.join(text_dir, f"{text.m_Name}-{obj.path_id}.txt")
                with open(path, "wb") as file:
                    file.write(bytes(script))
                print(f"strings {path}")
    finally:
        shutil.rmtree(work_dir)


if __name__ == "__main__":
    main()
