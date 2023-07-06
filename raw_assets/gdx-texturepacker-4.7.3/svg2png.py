import os
import subprocess
from lxml import etree

INKSCAPE_PATH = "C:/Program Files/Inkscape/inkscape.exe"

UI_PATH = "c:/Users/Mare5/projects/chargehockey/assets/UI"
SPRITES_PATH = "c:/Users/Mare5/projects/chargehockey/assets/Sprites"
GRID_SPRITES_PATH = "c:/Users/Mare5/projects/chargehockey/assets/Sprites/grid"

UP_COLOR = "#91dc5a"
DOWN_COLOR = "#98e597"


def svg_to_png(svg_path, png_path, width):
    print(svg_path, png_path)
    subprocess.run([INKSCAPE_PATH, "-z", "-f", svg_path, "-w", str(width), "--export-png", png_path], shell=True)


def convert_dir(dir_path, dest_dir, width):
    for name in os.listdir(dir_path):
        if name.endswith(".svg"):
            path = os.path.join(dir_path, name)
            png_name = name.replace("svg", "png")
            svg_to_png(path, os.path.join(dest_dir, png_name), width)                


def make_down_svg(up_path):
    """ Converts an svg file's style color attributes to make a tinted
        copy of the original file.
        up_path must end with *_up.svg and will be converted to *_down.svg
    """
    # xml = etree.parse(up_path)
    # for el in xml.xpath("//@style"):
    #     if UP_COLOR in el:
    #         new_style = el.replace(UP_COLOR, DOWN_COLOR)
    #         el.getparent().set('style', new_style)

    # Replaces all instances of UP_COLOR with DOWN_COLOR and copies the result
    # into a new file!
    new_lines = []
    with open(up_path, "r") as f:
        new_lines = f.readlines()

    for idx, line in enumerate(new_lines):
        new_lines[idx] = line.replace(UP_COLOR, DOWN_COLOR).replace(UP_COLOR.upper(), DOWN_COLOR.upper())
    
    up_path_split = os.path.split(up_path)
    down_path = up_path_split[1].replace("_up", "_down")  # e.g. cancel_up.png -> cancel_down.png
    down_path = os.path.join(up_path_split[0], down_path)

    with open(down_path, "w") as f:
        f.writelines(new_lines)

    # xml.write(down_path)
    print("Made {} from {}".format(os.path.basename(down_path), os.path.basename(up_path)))


def make_down_svgs(dir_path):
    for name in os.listdir(dir_path):
        if name.endswith("_up.svg"):
            make_down_svg(os.path.join(dir_path, name))


def convert_ui():
    for name in os.listdir(UI_PATH):
        path = os.path.join(UI_PATH, name)
        if os.path.isdir(path) and name.isdigit():
            make_down_svgs(path)
            w = int(name)
            convert_dir(path, UI_PATH, w)


if __name__ == '__main__':
    convert_ui()
    convert_dir(SPRITES_PATH, SPRITES_PATH, 128)
    convert_dir(GRID_SPRITES_PATH, GRID_SPRITES_PATH, 16)