import math
import struct
import wave
from pathlib import Path

SAMPLE_RATE = 44100
OUTPUT_DIR = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res" / "raw"


def note(frequency, seconds, amplitude=0.32, harmonic=0.10):
    total = int(SAMPLE_RATE * seconds)
    attack_samples = max(1, int(SAMPLE_RATE * 0.006))
    release_samples = max(1, int(SAMPLE_RATE * 0.06))
    samples = []
    for index in range(total):
        time = index / SAMPLE_RATE
        attack = min(1.0, index / attack_samples)
        release = min(1.0, (total - index) / release_samples)
        envelope = attack * release
        value = amplitude * envelope * (
            math.sin(2 * math.pi * frequency * time)
            + harmonic * math.sin(4 * math.pi * frequency * time)
        )
        samples.append(value)
    return samples


def silence(seconds):
    return [0.0] * int(SAMPLE_RATE * seconds)


def write_wav(path, samples):
    path.parent.mkdir(parents=True, exist_ok=True)
    frames = b"".join(
        struct.pack("<h", int(max(-1.0, min(1.0, sample)) * 32767))
        for sample in samples
    )
    with wave.open(str(path), "w") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        wav.writeframes(frames)
    print(f"wrote {path} ({len(samples) / SAMPLE_RATE:.2f}s)")


def main():
    tap = note(660, 0.045, amplitude=0.18)
    cat = (
        note(740, 0.06, amplitude=0.28)
        + silence(0.005)
        + note(988, 0.10, amplitude=0.26)
    )
    wrong = (
        note(196, 0.12, amplitude=0.30)
        + silence(0.01)
        + note(147, 0.18, amplitude=0.28)
    )
    win = (
        note(523, 0.10, amplitude=0.28)
        + silence(0.01)
        + note(659, 0.10, amplitude=0.28)
        + silence(0.01)
        + note(784, 0.10, amplitude=0.28)
        + silence(0.01)
        + note(1047, 0.20, amplitude=0.26)
    )
    write_wav(OUTPUT_DIR / "tap.wav", tap)
    write_wav(OUTPUT_DIR / "cat.wav", cat)
    write_wav(OUTPUT_DIR / "wrong.wav", wrong)
    write_wav(OUTPUT_DIR / "win.wav", win)


if __name__ == "__main__":
    main()
