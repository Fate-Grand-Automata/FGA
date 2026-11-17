#!/usr/bin/env python3
"""Cut a 720p template image for app/src/main/assets out of a game screenshot.

Mirrors FgoGameAreaManager + RealScale so a Region taken from scripts/.../locations
resolves to the same pixels the script would search at runtime. Requires Pillow.

Stage 1 - see what the search region covers:

    crop_template.py shot.png --region -590,180,190,120 --anchor right --out-dir /tmp/cs

Stage 2 - cut the template, using coordinates read off the gridded view from stage 1:

    crop_template.py shot.png --region -590,180,190,120 --anchor right \
        --template 46,31,22,13 --click-y 150 --out app/src/main/assets/Tw/command_spell.png
"""

import argparse
import os
import sys

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    sys.exit("Pillow is required: pip install pillow")

SCRIPT_W, SCRIPT_H = 2560, 1440          # FgoGameAreaManager.scriptSize
IMAGE_W, IMAGE_H = 1280, 720             # FgoGameAreaManager.imageSize
ULTRA_WIDE_SCRIPT_W = 3360               # blue side borders appear beyond 21:9


def game_area(width: int, height: int) -> tuple[int, int, int, int]:
    """The game area inside a screenshot, as (x, y, w, h), matching FgoGameAreaManager."""
    aspect = width / height
    rate = min(width / SCRIPT_W, height / SCRIPT_H)

    def without_borders(script_w: int) -> tuple[int, int, int, int]:
        scaled_w, scaled_h = round(script_w * rate), round(SCRIPT_H * rate)
        return (abs(width - scaled_w) // 2, abs(height - scaled_h) // 2, scaled_w, scaled_h)

    if aspect > 21 / 9:
        return without_borders(ULTRA_WIDE_SCRIPT_W)
    if aspect > 18 / 9:
        return (0, 0, width, height)     # wide screens are full-bleed
    return without_borders(SCRIPT_W)


def resolve(args, area_w: int, area_h: int) -> tuple[int, int, int, int]:
    """Region spec -> box in the 720p game-area image."""
    rx, ry, rw, rh = args.region
    # scriptToScreen from RealScale; height-based unless the screen is narrower than 16:9
    by_width = (area_w / SCRIPT_W) < (area_h / SCRIPT_H)
    script_to_screen = area_w / SCRIPT_W if by_width else area_h / SCRIPT_H
    script_area_w = round(area_w / script_to_screen)

    if args.anchor == "right":
        rx += script_area_w
    elif args.anchor == "center":
        rx += script_area_w // 2
    if args.anchor_y == "bottom":
        ry += SCRIPT_H

    s = IMAGE_H / SCRIPT_H               # script -> 720p, always 0.5
    return (round(rx * s), round(ry * s), round(rw * s), round(rh * s))


def gridded(crop: Image.Image, zoom: int = 9) -> Image.Image:
    """Upscale with a labelled 5px grid, for reading template coordinates off by eye."""
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 16)
    except OSError:
        font = ImageFont.load_default()
    w, h = crop.size
    big = crop.resize((w * zoom, h * zoom), Image.NEAREST)
    pad = 34
    canvas = Image.new("RGB", (big.width + pad, big.height + pad), (255, 255, 255))
    canvas.paste(big, (pad, 0))
    d = ImageDraw.Draw(canvas)
    for x in range(0, w + 1, 5):
        major = x % 10 == 0
        d.line((pad + x * zoom, 0, pad + x * zoom, big.height),
               fill=(255, 0, 0) if major else (255, 170, 170))
        if major:
            d.text((pad + x * zoom - 9, big.height + 6), str(x), font=font, fill=(200, 0, 0))
    for y in range(0, h + 1, 5):
        major = y % 10 == 0
        d.line((pad, y * zoom, canvas.width, y * zoom),
               fill=(255, 0, 0) if major else (255, 170, 170))
        if major:
            d.text((2, y * zoom - 8), str(y), font=font, fill=(200, 0, 0))
    return canvas


def merge_negative_values(argv: list[str]) -> list[str]:
    """Let `--region -590,180,190,120` work.

    Regions are routinely anchored from the right and so start with a minus sign, which
    argparse would otherwise read as the next option.
    """
    out, i = [], 0
    while i < len(argv):
        arg = argv[i]
        if arg in ("--region", "--template") and i + 1 < len(argv):
            value = argv[i + 1]
            if value.startswith("-") and all(v.lstrip("-").isdigit() for v in value.split(",")):
                out.append(f"{arg}={value}")
                i += 2
                continue
        out.append(arg)
        i += 1
    return out


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("screenshot")
    p.add_argument("--region", required=True, type=lambda s: [int(v) for v in s.split(",")],
                   metavar="X,Y,W,H", help="Region(...) from locations/*.kt, in 1440p script coords")
    p.add_argument("--anchor", choices=["left", "center", "right"], default="left",
                   help="xFromCenter() / xFromRight() on the Region (default: left)")
    p.add_argument("--anchor-y", choices=["top", "bottom"], default="top",
                   help="yFromBottom() on the Region (default: top)")
    p.add_argument("--template", type=lambda s: [int(v) for v in s.split(",")], metavar="X,Y,W,H",
                   help="final crop, in pixels relative to the region crop's top-left")
    p.add_argument("--click-y", type=int,
                   help="script y a caller clicks at, to mark on the verification image")
    p.add_argument("--out", help="write the template here (implies --template)")
    p.add_argument("--out-dir", default=".", help="where to write the working images")
    args = p.parse_args(merge_negative_values(sys.argv[1:]))

    if len(args.region) != 4:
        sys.exit("--region needs X,Y,W,H")
    if args.out and not args.template:
        sys.exit("--out needs --template")
    os.makedirs(args.out_dir, exist_ok=True)

    src = Image.open(args.screenshot).convert("RGB")
    w, h = src.size
    ax, ay, aw, ah = game_area(w, h)
    if (ax, ay) != (0, 0) or (aw, ah) != (w, h):
        print(f"game area {aw}x{ah} at ({ax},{ay}) - borders cropped")
        src = src.crop((ax, ay, ax + aw, ay + ah))

    # Reference images are stored with the game area already 720px tall; anything else
    # (a raw device screenshot, say) is downscaled here first.
    if ah == IMAGE_H:
        img = src
    else:
        img = src.resize((round(aw * IMAGE_H / ah), IMAGE_H), Image.LANCZOS)
        print(f"downscaled game area {aw}x{ah} -> {img.size[0]}x{img.size[1]}")

    rx, ry, rw, rh = resolve(args, aw, ah)
    print(f"source {w}x{h} (aspect {w / h:.4f}) -> 720p {img.size[0]}x{img.size[1]}")
    print(f"search region at 720p: ({rx},{ry}) {rw}x{rh}")

    stem = os.path.splitext(os.path.basename(args.screenshot))[0]
    region = img.crop((rx, ry, rx + rw, ry + rh))
    region.save(os.path.join(args.out_dir, f"{stem}-region.png"))
    gridded(region).save(os.path.join(args.out_dir, f"{stem}-region-grid.png"))
    print(f"wrote {stem}-region.png and {stem}-region-grid.png to {args.out_dir}")

    if not args.template:
        print("\nRead the template box off the grid image, then re-run with --template X,Y,W,H")
        return

    tx, ty, tw, th = args.template
    x, y = rx + tx, ry + ty
    tpl = img.crop((x, y, x + tw, y + th))
    out = args.out or os.path.join(args.out_dir, f"{stem}-template.png")
    tpl.save(out)
    print(f"template {tw}x{th} at 720p ({x},{y}) -> {out}")

    # Verification: search region, template, and where a match-anchored click would land.
    v = img.copy()
    d = ImageDraw.Draw(v)
    d.rectangle((rx, ry, rx + rw, ry + rh), outline=(255, 215, 0), width=2)
    d.rectangle((x, y, x + tw, y + th), outline=(0, 220, 90), width=2)
    if args.click_y is not None:
        cx, cy = x, round(args.click_y * IMAGE_H / SCRIPT_H)
        d.line((cx - 9, cy, cx + 9, cy), fill=(255, 40, 40), width=2)
        d.line((cx, cy - 9, cx, cy + 9), fill=(255, 40, 40), width=2)
        print(f"match x in script coords: {x * 2}; click marked at script ({x * 2},{args.click_y})")
    box = (max(rx - 150, 0), max(ry - 90, 0), min(rx + rw + 150, v.width), min(ry + rh + 90, v.height))
    v.crop(box).resize(((box[2] - box[0]) * 2, (box[3] - box[1]) * 2), Image.LANCZOS) \
        .save(os.path.join(args.out_dir, f"{stem}-verify.png"))
    print(f"wrote {stem}-verify.png - check the region sits on the target and the click lands on it")


if __name__ == "__main__":
    main()