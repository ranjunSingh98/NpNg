#!/usr/bin/env python3
"""
PNG → Android Vector Drawable Converter
Handles white-on-black OR black-on-white icons
Auto-detects color scheme, crops to square, traces with smooth bezier curves
Usage: python3 convert_to_vector.py input.png [output.xml] [--viewport 48]
"""

import sys
import os
import re
import argparse
import numpy as np
from PIL import Image
from skimage import measure
from scipy.interpolate import splprep, splev

# ── Constants ────────────────────────────────────────────────────────────────

MAX_PATH_CHARS   = 28_000   # safe Android limit (tested)
MAX_CONTOURS     = 24       # max subpaths to include
MIN_CONTOUR_LEN  = 20       # ignore tiny noise contours
WORK_SIZE        = 512      # internal raster resolution for tracing
SMOOTHING_FACTOR = 2.0      # spline smoothing: higher = smoother, less accurate
MAX_POINTS_PER_CONTOUR = 80 # max bezier anchors per contour

# ── Helpers ──────────────────────────────────────────────────────────────────

def crop_to_square(img: Image.Image) -> Image.Image:
    """Centre-crop image to square"""
    w, h = img.size
    if w == h:
        return img
    size = min(w, h)
    left = (w - size) // 2
    top  = (h - size) // 2
    print(f"  ✂  Cropped {w}×{h} → {size}×{size}")
    return img.crop((left, top, left + size, top + size))


def auto_invert(arr: np.ndarray) -> np.ndarray:
    """
    Detect whether icon is white-on-black or black-on-white.
    We want WHITE = foreground (what we trace).
    If the image is black-on-white, invert it.
    """
    # Sample corners (likely background)
    h, w = arr.shape
    corners = [arr[0,0], arr[0,w-1], arr[h-1,0], arr[h-1,w-1]]
    bg_mean = np.mean(corners)
    if bg_mean > 128:
        print("  🔄  Detected black-on-white — inverting")
        return 255 - arr
    print("  ✅  Detected white-on-black — no inversion needed")
    return arr


def fmt(val: float) -> str:
    """Format float for SVG path — no trailing zeros, max 2dp"""
    s = f"{val:.2f}".rstrip('0').rstrip('.')
    return s if s and s != '-' else '0'


def contour_to_bezier(contour: np.ndarray, scale: float) -> str | None:
    """
    Convert a skimage contour (row,col) to a smooth closed SVG cubic bezier path.
    Uses scipy spline interpolation for smooth curves.
    Falls back to polyline if spline fails.
    """
    if len(contour) < 6:
        return None

    y_pts = np.clip(contour[:, 0] * scale, 0, 48)
    x_pts = np.clip(contour[:, 1] * scale, 0, 48)

    # Close contour for periodic spline
    x_c = np.append(x_pts, x_pts[0])
    y_c = np.append(y_pts, y_pts[0])

    try:
        n = len(x_c)
        s = n * SMOOTHING_FACTOR
        tck, _ = splprep([x_c, y_c], s=s, per=True, k=3)

        n_eval = min(MAX_POINTS_PER_CONTOUR, max(16, len(contour) // 3))
        u_new  = np.linspace(0, 1, n_eval, endpoint=False)
        xs, ys = splev(u_new, tck)
        dxs, dys = splev(u_new, tck, der=1)

        xs  = np.clip(xs,  0, 48)
        ys  = np.clip(ys,  0, 48)

        dt = 1.0 / n_eval
        path = f"M {fmt(xs[0])},{fmt(ys[0])}"
        n_pts = len(xs)

        for i in range(n_pts):
            ni = (i + 1) % n_pts
            cp1x = np.clip(xs[i]  + dxs[i]  * dt / 3, 0, 48)
            cp1y = np.clip(ys[i]  + dys[i]  * dt / 3, 0, 48)
            cp2x = np.clip(xs[ni] - dxs[ni] * dt / 3, 0, 48)
            cp2y = np.clip(ys[ni] - dys[ni] * dt / 3, 0, 48)
            path += (f" C {fmt(cp1x)},{fmt(cp1y)}"
                     f" {fmt(cp2x)},{fmt(cp2y)}"
                     f" {fmt(xs[ni])},{fmt(ys[ni])}")
        path += " Z"
        return path

    except Exception as e:
        # Polyline fallback
        step   = max(1, len(x_pts) // 60)
        pts    = list(zip(x_pts[::step], y_pts[::step]))
        if len(pts) < 3:
            return None
        path = f"M {fmt(pts[0][0])},{fmt(pts[0][1])}"
        for px, py in pts[1:]:
            path += f" L {fmt(px)},{fmt(py)}"
        return path + " Z"


def validate_path(path: str) -> bool:
    """Ensure path only contains valid Android vector commands"""
    # Valid: M L C S Q T A H V Z (upper and lower) plus numbers, spaces, commas, dots, minus
    return not re.search(r'[^0-9A-Za-z ,.\-\n]', path)


# ── Main converter ───────────────────────────────────────────────────────────

def convert(input_path: str, output_path: str, viewport: int = 48) -> bool:
    name = os.path.splitext(os.path.basename(input_path))[0]
    print(f"\n🔧 Converting: {name}")

    # Load & prepare
    img = Image.open(input_path).convert("L")
    img = crop_to_square(img)
    img = img.resize((WORK_SIZE, WORK_SIZE), Image.LANCZOS)

    arr  = np.array(img)
    arr  = auto_invert(arr)

    # Threshold → binary (white = True = foreground)
    binary = arr > 128

    # Find contours
    contours = measure.find_contours(binary, 0.5)
    contours_sorted = sorted(contours, key=lambda c: len(c), reverse=True)

    print(f"  📐 Found {len(contours_sorted)} contours")

    scale = viewport / WORK_SIZE
    path_parts = []
    total_chars = 0
    skipped     = 0

    for i, contour in enumerate(contours_sorted[:MAX_CONTOURS]):
        if len(contour) < MIN_CONTOUR_LEN:
            skipped += 1
            continue

        path = contour_to_bezier(contour, scale)
        if not path:
            skipped += 1
            continue

        if not validate_path(path):
            print(f"  ⚠  Contour {i} has invalid chars — skipping")
            skipped += 1
            continue

        if total_chars + len(path) > MAX_PATH_CHARS:
            print(f"  ⚠  Char limit reached at contour {i} — stopping")
            break

        path_parts.append(path)
        total_chars += len(path)

    if not path_parts:
        print("  ❌ No valid paths generated!")
        return False

    full_path = " ".join(path_parts)

    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{viewport}dp"
    android:height="{viewport}dp"
    android:viewportWidth="{viewport}"
    android:viewportHeight="{viewport}">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="{full_path}" />
</vector>'''

    with open(output_path, "w") as f:
        f.write(xml)

    print(f"  ✅ {len(path_parts)} paths, {skipped} skipped, {total_chars:,} chars")
    print(f"  💾 Saved → {output_path}")
    return True


# ── CLI ───────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="PNG → Android Vector Drawable")
    parser.add_argument("input",   help="Input PNG file (or directory of PNGs)")
    parser.add_argument("output",  nargs="?", help="Output XML file or directory")
    parser.add_argument("--viewport", type=int, default=48, help="Viewport size in dp (default: 48)")
    args = parser.parse_args()

    if os.path.isdir(args.input):
        # Batch mode
        out_dir = args.output or args.input
        os.makedirs(out_dir, exist_ok=True)
        pngs = [f for f in os.listdir(args.input) if f.lower().endswith(".png")]
        print(f"📦 Batch mode: {len(pngs)} PNGs found")
        success = 0
        for png in pngs:
            inp = os.path.join(args.input, png)
            out = os.path.join(out_dir, os.path.splitext(png)[0] + ".xml")
            if convert(inp, out, args.viewport):
                success += 1
        print(f"\n🎉 Done: {success}/{len(pngs)} converted successfully")
    else:
        # Single file
        if args.output:
            out = args.output
        else:
            out = os.path.splitext(args.input)[0] + ".xml"
        convert(args.input, out, args.viewport)
