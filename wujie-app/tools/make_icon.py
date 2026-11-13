"""用一张方图生成 Android 各密度启动图标。

用法：
    python tools/make_icon.py 新图标.png
"""
import os
import sys

from PIL import Image

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "android", "app", "src", "main", "res")
SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def main():
    if len(sys.argv) < 2:
        print("用法: python tools/make_icon.py 图片路径.png")
        return
    src = sys.argv[1]
    im = Image.open(src).convert("RGBA")
    if im.width != im.height:
        side = min(im.size)
        left = (im.width - side) // 2
        top = (im.height - side) // 2
        im = im.crop((left, top, left + side, top + side))
    for dpi, px in SIZES.items():
        d = os.path.join(BASE, "mipmap-" + dpi)
        os.makedirs(d, exist_ok=True)
        icon = im.resize((px, px), Image.LANCZOS)
        icon.save(os.path.join(d, "ic_launcher.png"), "PNG")
        icon.save(os.path.join(d, "ic_launcher_round.png"), "PNG")
        print("已生成", dpi, str(px) + "px")
    print("完成，重新打包 APK 即可")


if __name__ == "__main__":
    main()
