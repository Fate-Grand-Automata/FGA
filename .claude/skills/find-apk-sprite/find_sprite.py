#!/usr/bin/env python3
"""Find a 720p template image inside the atlas PNGs in ../FGO-game-data.

Atlas sprites are stored at the game's own resolution, not 720p, so the template is
searched at a range of scales. Transparent atlas and template pixels are filled with
--background before matching, which should be the color the sprite sits on in game. Requires
opencv-python-headless and numpy.

    find_sprite.py app/src/main/assets/En/rank_up.png \
        ../FGO-game-data/*/CombineAtlas.png --background ECE6DE
"""

import argparse
import os

import cv2
import numpy as np


def gray_over_background(path: str, background_rgb: tuple) -> np.ndarray:
    """The image at `path` composited over a solid color and converted to grayscale."""
    image = cv2.imread(path, cv2.IMREAD_UNCHANGED)
    if image.shape[2] == 3:
        return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    alpha = image[:, :, 3:4].astype(np.float32) / 255
    background_bgr = np.array(background_rgb[::-1], np.float32)
    composited = image[:, :, :3] * alpha + background_bgr * (1 - alpha)
    return cv2.cvtColor(composited.astype(np.uint8), cv2.COLOR_BGR2GRAY)


def best_match(atlas: np.ndarray, template: np.ndarray, scales: np.ndarray) -> tuple:
    """(score, scale, x, y, width, height) of the best TM_CCOEFF_NORMED match over all scales."""
    best = (-1.0, 0.0, 0, 0, 0, 0)
    for scale in scales:
        scaled = cv2.resize(template, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)
        height, width = scaled.shape
        if height > atlas.shape[0] or width > atlas.shape[1]:
            continue
        _, score, _, (x, y) = cv2.minMaxLoc(cv2.matchTemplate(atlas, scaled, cv2.TM_CCOEFF_NORMED))
        if score > best[0]:
            best = (score, scale, x, y, width, height)
    return best


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("template", help="720p template, e.g. app/src/main/assets/En/rank_up.png")
    parser.add_argument("atlases", nargs="+", help="atlas PNGs to search")
    parser.add_argument("--background", default="ECE6DE",
                        help="RRGGBB filled in behind transparent atlas pixels (default: light UI panel)")
    parser.add_argument("--min-scale", type=float, default=0.5)
    parser.add_argument("--max-scale", type=float, default=3.0)
    parser.add_argument("--out-dir", help="write an enlarged crop of each best match here")
    args = parser.parse_args()

    background = tuple(int(args.background[i:i + 2], 16) for i in (0, 2, 4))
    template = gray_over_background(args.template, background)
    scales = np.arange(args.min_scale, args.max_scale + 0.001, 0.1)
    if args.out_dir:
        os.makedirs(args.out_dir, exist_ok=True)

    for path in args.atlases:
        atlas = gray_over_background(path, background)
        score, scale, x, y, width, height = best_match(atlas, template, scales)
        print(f"{score:.3f}  scale {scale:.1f}  at {x},{y} size {width}x{height}  {path}")
        if args.out_dir:
            padding = 10
            crop = atlas[max(0, y - padding):y + height + padding, max(0, x - padding):x + width + padding]
            name = path.replace(os.sep, "_").removesuffix(".png") + "-match.png"
            enlarged = cv2.resize(crop, None, fx=3, fy=3, interpolation=cv2.INTER_NEAREST)
            cv2.imwrite(os.path.join(args.out_dir, name), enlarged)


if __name__ == "__main__":
    main()
