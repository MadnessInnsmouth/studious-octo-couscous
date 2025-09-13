AI IME (Merged)
================

- Your keyboard XML layouts, styles, icons, and input_method.xml are merged in.
- Working IME engine (Kotlin) + Vosk offline STT wired up.
- Bundled model: Vosk small en-US 0.15 (assets/model-en.zip). First run unpacks, then mic works offline.

Build:
1) Open this folder in Android Studio.
2) Let Gradle sync and Run on your device.
3) Enable the keyboard in System > Languages & input > On-screen keyboard > Manage keyboards.
4) Switch to AI Keyboard, tap Mic, dictate.

Notes:
- Model is ~50–60 MB zipped; unpacks on first run into app files dir.
- You can later add a model switcher and drop in 0.22 as /files/model-en (manually) for higher accuracy.
