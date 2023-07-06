""" Run this script from raw_assets/LEVELS to copy the files
    in this directory to the appropriate place in android/assets/LEVELS.
    The files will be appropriately renamed. Only .grid files are copied.
"""

import glob
import shutil
import os


TARGET_LEVELS_DIR = "../../assets/LEVELS"


def get_level_paths(level_type):
    paths = []
    for name in os.listdir("."):
        if level_type == "EASY" and name.startswith("e"):
            paths.append(name)
        elif level_type == "MEDIUM" and name.startswith("m"):
            paths.append(name)
        elif level_type == "HARD" and name.startswith("h"):
            paths.append(name)
    return paths


def copy_func(src, dst):
    if not dst.endswith(".grid"):
        return
    # Remove the leading level_type char ('e', 'm', 'h').
    split = os.path.split(dst)
    new_name = split[1][1:]
    dst = os.path.join(split[0], new_name)
    shutil.copy2(src, dst)


def ignore_func(path, names):
    ignored = []
    for name in names:
        if name.endswith("save"):
            ignored.append(name)
    return ignored


if __name__ == '__main__':
    for level_type in os.listdir(TARGET_LEVELS_DIR):
        for path in get_level_paths(level_type):
            dst = os.path.join(TARGET_LEVELS_DIR, level_type, path[1:])
            print(dst)
            if os.path.exists(dst):
                shutil.rmtree(dst)
            shutil.copytree(path, dst, copy_function=copy_func)
        
