#!/usr/bin/env python3
"""Render a 720p template image of a UI label from the game's font, and score it.

Requires Pillow, numpy and opencv-python-headless. Uses crop_template.py from the
extract-template-asset skill to resolve Regions, so both skills search the same pixels.

Fit the font size on a label you have a screenshot of:

    render_template.py --font fonts/FGO-Main-Font.otf --text "필요 아이템이" --color DB0052 \
        --fit --check kr-mats.png --region -501,228,597,43 --anchor center

Render a label with that size and check it against screenshots that must and must not match:

    render_template.py --font fonts/FGO-Main-Font.otf --text "QP가 부" --color DB0052 --size 25 \
        --region -500,222,397,47 --anchor center --check kr-qp.png --check kr-mats.png \
        --out app/src/main/assets/Kr/skill_insufficient_qp.png
"""

import argparse
import os
import sys
from types import SimpleNamespace

try:
    import cv2
    import numpy as np
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    sys.exit("Pillow, numpy and opencv-python-headless are required")

# Keeps a __pycache__ out of the other skill's folder
sys.dont_write_bytecode = True
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "extract-template-asset"))
from crop_template import IMAGE_H, game_area, merge_negative_values, resolve  # noqa: E402

RENDER_SCALE = 4
SHIFTS = [(0, 0), (0.5, 0), (0, 0.5), (0.5, 0.5)]


def parse_color(value: str) -> tuple[int, int, int]:
    value = value.lstrip("#")
    return int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16)


def load_search_region(path: str, region_args: SimpleNamespace) -> np.ndarray:
    """The search region of a screenshot at 720p in RGB, downscaled the way the app does it."""
    image = cv2.cvtColor(cv2.imread(path), cv2.COLOR_BGR2RGB)
    ax, ay, aw, ah = game_area(image.shape[1], image.shape[0])
    area = image[ay:ay + ah, ax:ax + aw]
    if ah != IMAGE_H:
        area = cv2.resize(area, (round(aw * IMAGE_H / ah), IMAGE_H), interpolation=cv2.INTER_AREA)
    rx, ry, rw, rh = resolve(region_args, aw, ah)
    # A 1px margin so the region can be shifted by half a pixel without running off its edge
    return area[ry - 1:ry + rh + 1, rx - 1:rx + rw + 1]


def render(font_path: str, text: str, color, size: float, stroke: float, background) -> np.ndarray:
    """The text in RGB at 720p, rendered large and shrunk like a screenshot of a high-res device.

    The result is cropped to the glyphs, minus their anti-aliased edge, so it holds no pixels
    from beside the label.
    """
    font = ImageFont.truetype(font_path, round(size * RENDER_SCALE))
    stroke_width = round(stroke * RENDER_SCALE)
    pad = 8 * RENDER_SCALE
    left, top, right, bottom = map(int, font.getbbox(text, stroke_width=stroke_width))
    dimensions = (right - left + 2 * pad, bottom - top + 2 * pad)
    origin = (pad - left, pad - top)

    image = Image.new("RGB", dimensions, background)
    ImageDraw.Draw(image).text(origin, text, font=font, fill=color,
                               stroke_width=stroke_width, stroke_fill=(30, 30, 30))
    mask = Image.new("L", dimensions, 0)
    ImageDraw.Draw(mask).text(origin, text, font=font, fill=255,
                              stroke_width=stroke_width, stroke_fill=255)

    small = (dimensions[0] // RENDER_SCALE, dimensions[1] // RENDER_SCALE)
    rgb = cv2.resize(np.array(image), small, interpolation=cv2.INTER_AREA)
    ink = cv2.resize(np.array(mask), small, interpolation=cv2.INTER_AREA)
    ys, xs = np.where(ink > 64)
    return rgb[ys.min() + 2:ys.max() - 1, xs.min() + 1:xs.max()]


def scores(region: np.ndarray, template: np.ndarray) -> list[float] | None:
    """Best TM_CCOEFF_NORMED score in grayscale at each of SHIFTS; None if it doesn't fit."""
    gray_region = cv2.cvtColor(region, cv2.COLOR_RGB2GRAY).astype(np.float32)
    gray_template = cv2.cvtColor(template, cv2.COLOR_RGB2GRAY)
    height, width = gray_region.shape
    if gray_template.shape[0] > height - 2 or gray_template.shape[1] > width - 2:
        return None
    result = []
    for dx, dy in SHIFTS:
        shifted = cv2.warpAffine(gray_region, np.float32([[1, 0, dx], [0, 1, dy]]), (width, height))
        inner = shifted[1:-1, 1:-1].astype(np.uint8)
        match = cv2.matchTemplate(inner, gray_template, cv2.TM_CCOEFF_NORMED)
        result.append(round(float(cv2.minMaxLoc(match)[1]), 3))
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--font", required=True, help="font file from extract_game_data.py")
    parser.add_argument("--text", required=True, help="the label text, without color markup")
    parser.add_argument("--color", required=True, help="text color as RRGGBB, from the string's [RRGGBB] markup")
    parser.add_argument("--size", type=float, default=25, help="font size in 720p pixels (default: 25)")
    parser.add_argument("--stroke", type=float, default=0, help="dark outline width in 720p pixels (default: 0)")
    parser.add_argument("--background", help="RRGGBB behind the text; default: median of the first --check region")
    parser.add_argument("--region", required=True, type=lambda s: [int(v) for v in s.split(",")],
                        metavar="X,Y,W,H", help="Region(...) from locations/*.kt, in 1440p script coords")
    parser.add_argument("--anchor", choices=["left", "center", "right"], default="left")
    parser.add_argument("--anchor-y", choices=["top", "bottom"], default="top")
    parser.add_argument("--check", action="append", default=[], metavar="SCREENSHOT",
                        help="score the template in this screenshot's search region; repeatable")
    parser.add_argument("--fit", action="store_true",
                        help="pick --size by the best score on the first --check screenshot")
    parser.add_argument("--out", help="write the template here")
    args = parser.parse_args(merge_negative_values(sys.argv[1:]))

    region_args = SimpleNamespace(region=args.region, anchor=args.anchor, anchor_y=args.anchor_y)
    regions = {path: load_search_region(path, region_args) for path in args.check}
    if (args.fit or not args.background) and not regions:
        sys.exit("--fit and a missing --background need at least one --check screenshot")

    if args.background:
        background = parse_color(args.background)
    else:
        first = next(iter(regions.values()))
        background = tuple(int(v) for v in np.median(first.reshape(-1, 3), axis=0))
        print(f"background {''.join(f'{v:02X}' for v in background)} (median of the first --check region)")

    color = parse_color(args.color)
    size = args.size
    if args.fit:
        first = next(iter(regions.values()))
        fits = []
        for candidate in np.arange(15, 35.01, 0.5):
            template = render(args.font, args.text, color, candidate, args.stroke, background)
            score = scores(first, template)
            if score:
                fits.append((score[0], candidate))
        fits.sort(reverse=True)
        print("best sizes:", ", ".join(f"{s:g} -> {score:.3f}" for score, s in fits[:3]))
        size = fits[0][1]

    template = render(args.font, args.text, color, size, args.stroke, background)
    print(f"template {template.shape[1]}x{template.shape[0]} at size {size:g}")
    for path, region in regions.items():
        result = scores(region, template)
        shown = "doesn't fit the region" if result is None else " ".join(f"{s:.2f}" for s in result)
        print(f"  {os.path.basename(path)}: {shown}")

    if args.out:
        cv2.imwrite(args.out, cv2.cvtColor(template, cv2.COLOR_RGB2BGR))
        print(f"wrote {args.out}")


if __name__ == "__main__":
    main()
