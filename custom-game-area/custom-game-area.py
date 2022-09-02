from PIL import Image, ImageOps
import numpy as np


def correctOffset(offset):
    # offsets are off by one if they're not 0
    return offset if offset == 0 else offset + 1


def start():
    img = Image.open('image.jpg')

    gray = ImageOps.grayscale(img)
    # Turn gray image into matrix of booleans where black == False
    mask = np.array(gray) > 30
    # For each column, check if any value is True.
    mask0 = mask.any(0)
    # Do the same thing for each row.
    mask1 = mask.any(1)

    # Get the index of the first True value
    left = correctOffset(mask0.argmax())
    # Get the index of the first True value starting from the right
    right = correctOffset(mask0[::-1].argmax())
    top = correctOffset(mask1.argmax())
    bottom = correctOffset(mask1[::-1].argmax())

    output = ""
    if left > 0:
        output += f"left={left}, "
    if right > 0:
        output += f"right={right}, "
    if top > 0:
        output += f"top={top}, "
    if bottom > 0:
        output += f"bottom={bottom}"

    # Remove any trailing ", "
    output = output.rstrip(", ")
    print(output)


if __name__ == "__main__":
    start()
