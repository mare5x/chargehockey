import os


def fix_save_file(path):
    for d, _, files in os.walk(path):
        if not len(files) == 2:
            continue
        
        fpath = os.path.join(d, files[1])
        with open(fpath, 'r+', encoding="UTF-8", newline="\n") as f:
            lines = f.readlines()
            f.seek(0)
            f.write("0\n")
            f.truncate()
            f.writelines(lines)
        print(fpath)

# fix_save_file("android/assets/LEVELS/CUSTOM")

# for p in os.listdir("android/assets/LEVELS/"):
#   fix_save_file(os.path.join("android/assets/LEVELS/", p))


def fix_positions(path):
    for d, _, files in os.walk(path):
        if not files:
            continue

        for fp in files:
            if fp.endswith(".grid"):
                fpath = os.path.join(d, fp)
                print(fpath)
                lines = []
                with open(fpath, "r", encoding="UTF-8", newline="\n") as f:
                    lines = f.readlines()

                for idx, line in enumerate(lines[3:]):
                    if not line:
                        continue

                    split = line.split()
                    x, y = split
                    x = float(x)
                    y = float(y)
                    split[0] = str(x + 0.5)
                    split[1] = str(y + 0.5)
                    s = " ".join(split)
                    s += "\n"
                    lines[idx + 3] = s

                with open(fpath, 'w', encoding="UTF-8", newline="\n") as f:
                    f.writelines(lines)

            elif fp.endswith(".save"):
                fpath = os.path.join(d, fp)
                print(fpath)
                lines = []
                with open(fpath, 'r+', encoding="UTF-8", newline="\n") as f:
                    lines = f.readlines()
                
                # print(lines)
                # print("--------------")

                for idx, line in enumerate(lines[2:]):
                    if not line:
                        continue

                    split = line.split()
                    x, y = split[1:3]
                    x = float(x)
                    y = float(y)
                    split[1] = str(x + 0.5)
                    split[2] = str(y + 0.5)
                    s = " ".join(split)
                    s += "\n"
                    lines[idx + 2] = s


                # print(lines)

                with open(fpath, 'w', encoding="UTF-8", newline="\n") as f:
                    f.writelines(lines)
            

fix_positions("android/assets/LEVELS/")