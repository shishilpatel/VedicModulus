import math
import struct
import wave
import subprocess
import os
import random

SAMPLE_RATE = 44100

def write_wav(filename, samples):
    with wave.open(filename, 'wb') as wav_file:
        wav_file.setnchannels(2) # Stereo
        wav_file.setsampwidth(2) # 16-bit
        wav_file.setframerate(SAMPLE_RATE)
        
        # samples is a list of (left, right) tuples with values in [-1.0, 1.0]
        packed = bytearray()
        for l, r in samples:
            l_clamped = max(-1.0, min(1.0, l))
            r_clamped = max(-1.0, min(1.0, r))
            l_int = int(l_clamped * 32767.0)
            r_int = int(r_clamped * 32767.0)
            packed.extend(struct.pack('<hh', l_int, r_int))
        wav_file.writeframes(packed)

def convert_to_mp3(wav_path, mp3_path):
    cmd = [
        'ffmpeg', '-y', '-i', wav_path,
        '-codec:a', 'libmp3lame', '-b:a', '128k',
        mp3_path
    ]
    subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    if os.path.exists(wav_path):
        os.remove(wav_path)

def apply_envelope(samples, attack_s=1.0, release_s=1.5):
    total = len(samples)
    attack_samples = int(attack_s * SAMPLE_RATE)
    release_samples = int(release_s * SAMPLE_RATE)
    
    result = []
    for i, (l, r) in enumerate(samples):
        gain = 1.0
        if i < attack_samples and attack_samples > 0:
            gain = 0.5 * (1.0 - math.cos(math.pi * i / attack_samples))
        elif i > total - release_samples and release_samples > 0:
            rem = total - i
            gain = 0.5 * (1.0 - math.cos(math.pi * rem / release_samples))
        result.append((l * gain, r * gain))
    return result

def gen_harmonic_drone(base_freq, duration_s, harmonics=[(1.0, 0.4), (2.0, 0.2), (3.0, 0.1), (0.5, 0.2)], detune=0.5):
    num_samples = int(duration_s * SAMPLE_RATE)
    samples = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        l = 0.0
        r = 0.0
        # Gentle slow modulation
        lfo = 1.0 + 0.08 * math.sin(2 * math.pi * 0.15 * t)
        
        for mult, amp in harmonics:
            f = base_freq * mult
            l += amp * math.sin(2 * math.pi * (f - detune) * t) * lfo
            r += amp * math.sin(2 * math.pi * (f + detune) * t) * lfo
        samples.append((l, r))
    return apply_envelope(samples, 1.5, 2.0)

def gen_bell_chime(base_freq, duration_s):
    num_samples = int(duration_s * SAMPLE_RATE)
    samples = []
    overtones = [(1.0, 0.5, 2.0), (2.76, 0.25, 1.5), (5.4, 0.15, 0.8), (8.9, 0.08, 0.4)]
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        l = 0.0
        r = 0.0
        for mult, amp, decay_rate in overtones:
            f = base_freq * mult
            decay = math.exp(-decay_rate * t)
            l += amp * decay * math.sin(2 * math.pi * f * t)
            r += amp * decay * math.sin(2 * math.pi * (f * 1.002) * t)
        samples.append((l, r))
    return apply_envelope(samples, 0.02, 0.5)

def gen_nature_rain(duration_s):
    num_samples = int(duration_s * SAMPLE_RATE)
    samples = []
    # Filtered brown/pink noise emulation with periodic rain droplet transients
    last_l = 0.0
    last_r = 0.0
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        white_l = (random.random() * 2.0 - 1.0) * 0.15
        white_r = (random.random() * 2.0 - 1.0) * 0.15
        # Low pass filter for soft rain
        last_l = (last_l * 0.85) + (white_l * 0.15)
        last_r = (last_r * 0.85) + (white_r * 0.15)
        
        # Distant water trickle / stream wave
        swell = 1.0 + 0.2 * math.sin(2 * math.pi * 0.2 * t)
        samples.append((last_l * swell, last_r * swell))
    return apply_envelope(samples, 1.5, 1.5)

def gen_tanpura(duration_s):
    # Classical Indian Tanpura tuning: Pa (G), Sa (C), Sa (C), Sa low (C low)
    # Fundamental Sa = 130.81 Hz (C3), Pa = 196.00 Hz (G3)
    c3 = 130.81
    g3 = 196.00
    strings = [
        (g3, 0.0),    # First pluck at 0s
        (c3 * 2, 1.2),# High Sa at 1.2s
        (c3 * 2, 2.4),# High Sa at 2.4s
        (c3, 3.6)     # Low Sa at 3.6s
    ]
    cycle_len = 4.8
    num_samples = int(duration_s * SAMPLE_RATE)
    samples = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        cycle_t = t % cycle_len
        l = 0.0
        r = 0.0
        for f, start_t in strings:
            dt = cycle_t - start_t
            if dt < 0:
                dt += cycle_len
            decay = math.exp(-0.7 * dt)
            # Rich harmonics with jawari buzz
            buzz = (
                0.35 * math.sin(2 * math.pi * f * dt) +
                0.25 * math.sin(2 * math.pi * 2 * f * dt) +
                0.20 * math.sin(2 * math.pi * 3 * f * dt) +
                0.12 * math.sin(2 * math.pi * 4 * f * dt) +
                0.08 * math.sin(2 * math.pi * 5 * f * dt)
            )
            val = buzz * decay * 0.5
            l += val
            r += val * (1.0 + 0.05 * math.sin(2 * math.pi * 0.3 * dt))
        samples.append((l, r))
    return apply_envelope(samples, 1.5, 2.0)

def gen_flute(duration_s):
    # Meditative Bansuri notes (Raag Yaman / Bhupali scale notes)
    notes = [261.63, 293.66, 329.63, 392.00, 440.00, 523.25]
    num_samples = int(duration_s * SAMPLE_RATE)
    samples = []
    note_duration = 3.0
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        note_idx = int(t / note_duration) % len(notes)
        f = notes[note_idx]
        note_t = t % note_duration
        envelope = math.sin(math.pi * (note_t / note_duration))
        vibrato = 1.0 + 0.015 * math.sin(2 * math.pi * 4.5 * t)
        # Flute breathiness
        val = (
            0.4 * math.sin(2 * math.pi * f * vibrato * t) +
            0.15 * math.sin(2 * math.pi * 2 * f * vibrato * t) +
            0.05 * (random.random() * 2 - 1)
        ) * envelope * 0.6
        samples.append((val * 0.9, val * 1.1))
    return apply_envelope(samples, 1.5, 2.0)

def main():
    base_dir = "app/src/main/assets/audio"
    
    # 1. Individual 7 Chakras
    chakra_configs = [
        ("chakras/chakra_muladhara_lam", 128.0, [(1.0, 0.5), (2.0, 0.25), (0.5, 0.25)], "LAM - Earth"),
        ("chakras/chakra_svadisthana_vam", 144.0, [(1.0, 0.45), (2.0, 0.3), (3.0, 0.15)], "VAM - Water"),
        ("chakras/chakra_manipura_ram", 160.0, [(1.0, 0.5), (2.0, 0.2), (3.0, 0.2)], "RAM - Fire"),
        ("chakras/chakra_anahata_yam", 170.66, [(1.0, 0.4), (2.0, 0.3), (4.0, 0.15)], "YAM - Air"),
        ("chakras/chakra_vishuddha_ham", 192.0, [(1.0, 0.45), (3.0, 0.25), (5.0, 0.15)], "HAM - Space"),
        ("chakras/chakra_ajna_om", 216.0, [(1.0, 0.5), (2.0, 0.3), (0.5, 0.2)], "OM - Light/Mind"),
        ("chakras/chakra_sahasrara_om", 240.0, [(1.0, 0.4), (2.0, 0.3), (4.0, 0.2), (8.0, 0.1)], "OM - Consciousness")
    ]
    
    for rel_name, freq, harmonics, label in chakra_configs:
        mp3_path = f"{base_dir}/{rel_name}.mp3"
        wav_path = f"{base_dir}/{rel_name}.wav"
        print(f"Generating {mp3_path} ({label})...")
        samples = gen_harmonic_drone(freq, duration_s=12.0, harmonics=harmonics)
        write_wav(wav_path, samples)
        convert_to_mp3(wav_path, mp3_path)

    # 2. Seven Chakra Journey (Progressing through all 7 frequencies)
    journey_mp3 = f"{base_dir}/chakras/seven_chakra_journey.mp3"
    journey_wav = f"{base_dir}/chakras/seven_chakra_journey.wav"
    print("Generating seven_chakra_journey.mp3...")
    journey_samples = []
    freqs = [128.0, 144.0, 160.0, 170.66, 192.0, 216.0, 240.0]
    for freq in freqs:
        s = gen_harmonic_drone(freq, duration_s=4.0, harmonics=[(1.0, 0.45), (2.0, 0.25)])
        journey_samples.extend(s)
    journey_samples = apply_envelope(journey_samples, 2.0, 2.5)
    write_wav(journey_wav, journey_samples)
    convert_to_mp3(journey_wav, journey_mp3)

    # 3. Ambient Tracks
    ambient_tracks = [
        ("ambient/ambient_tanpura", lambda: gen_tanpura(15.0)),
        ("ambient/ambient_flute", lambda: gen_flute(15.0)),
        ("ambient/ambient_singing_bowls", lambda: gen_harmonic_drone(216.0, 15.0, [(1.0, 0.5), (2.76, 0.25), (5.4, 0.15)])),
        ("ambient/ambient_temple_bells", lambda: gen_bell_chime(432.0, 10.0)),
        ("ambient/ambient_rain_nature", lambda: gen_nature_rain(15.0)),
        ("ambient/ambient_deep_drone", lambda: gen_harmonic_drone(108.0, 15.0, [(1.0, 0.6), (0.5, 0.3), (2.0, 0.15)])),
        ("ambient/ambient_cosmic_space", lambda: gen_harmonic_drone(432.0, 15.0, [(1.0, 0.3), (1.5, 0.2), (2.0, 0.2), (3.0, 0.15)]))
    ]
    for rel_name, gen_func in ambient_tracks:
        mp3_path = f"{base_dir}/{rel_name}.mp3"
        wav_path = f"{base_dir}/{rel_name}.wav"
        print(f"Generating {mp3_path}...")
        samples = gen_func()
        write_wav(wav_path, samples)
        convert_to_mp3(wav_path, mp3_path)

    # 4. Breathing Exercise Cues
    breath_cues = [
        ("breathing/breathing_bell_inhale", 528.0),
        ("breathing/breathing_bell_hold", 432.0),
        ("breathing/breathing_bell_exhale", 396.0)
    ]
    for rel_name, freq in breath_cues:
        mp3_path = f"{base_dir}/{rel_name}.mp3"
        wav_path = f"{base_dir}/{rel_name}.wav"
        print(f"Generating {mp3_path}...")
        samples = gen_bell_chime(freq, 4.0)
        write_wav(wav_path, samples)
        convert_to_mp3(wav_path, mp3_path)

    # 5. Traditional Mantras
    mantras = [
        ("mantras/mantra_gayatri", 216.0, [(1.0, 0.5), (1.5, 0.3), (2.0, 0.2)]),
        ("mantras/mantra_mahamrityunjaya", 192.0, [(1.0, 0.55), (1.33, 0.25), (2.0, 0.2)]),
        ("mantras/mantra_om_shanti", 108.0, [(1.0, 0.6), (2.0, 0.25), (3.0, 0.15)])
    ]
    for rel_name, freq, harmonics in mantras:
        mp3_path = f"{base_dir}/{rel_name}.mp3"
        wav_path = f"{base_dir}/{rel_name}.wav"
        print(f"Generating {mp3_path}...")
        samples = gen_harmonic_drone(freq, 12.0, harmonics)
        write_wav(wav_path, samples)
        convert_to_mp3(wav_path, mp3_path)

    print("All audio files generated successfully!")

if __name__ == '__main__':
    main()
