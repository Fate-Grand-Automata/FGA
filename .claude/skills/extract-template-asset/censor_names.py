#!/usr/bin/env python3
"""Black out other players' names in a reference screenshot, in place. Requires Pillow.

Boxes are X1,Y1,X2,Y2 in the image's own pixels:

    censor_names.py docs/reference-images/En/support/support-screen.png \
        406,195,608,250 406,395,552,450
"""

import argparse
import sys

try:
    from PIL import Image, ImageDraw
except ImportError:
    sys.exit("Pillow is required: pip install pillow")


def parse_box(text: str) -> tuple[int, int, int, int]:
    x1, y1, x2, y2 = (int(value) for value in text.split(","))
    return x1, y1, x2, y2


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("image")
    parser.add_argument("boxes", nargs="+", type=parse_box, metavar="X1,Y1,X2,Y2")
    args = parser.parse_args()

    image = Image.open(args.image)
    draw = ImageDraw.Draw(image)
    for box in args.boxes:
        draw.rectangle(box, fill="black")
    image.save(args.image)
    print(f"censored {len(args.boxes)} box(es) in {args.image}")


if __name__ == "__main__":
    main()
