from pathlib import Path
from PIL import Image

root = Path('/home/ubuntu/sleep-audio/assets/images')
for name in ['icon.png', 'splash-icon.png', 'favicon.png', 'android-icon-foreground.png']:
    path = root / name
    image = Image.open(path).convert('RGB')
    image.thumbnail((512, 512), Image.Resampling.LANCZOS)
    image = image.quantize(colors=128, method=Image.Quantize.MEDIANCUT)
    image.save(path, format='PNG', optimize=True)
