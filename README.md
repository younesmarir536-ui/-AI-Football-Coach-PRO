# AI Football Coach PRO — v0.3 prototype

This is a source project, not a ready APK or finished AI coach.

## Added in v0.3
- Initial lower-centre radar crop (percentage-based; needs calibration on the actual phone/HUD)
- Lightweight color candidate extraction for yellow and blue radar markers
- Approximate spatial grouping of colored pixels
- Initial yellow ball-candidate heuristic
- Arabic Text-to-Speech helper using the device's installed Arabic voice

## Still incomplete / must be validated
- The yellow four-arc ball marker is NOT yet specifically recognized as a four-arc shape.
- Player detections are color blobs, not reliable individual player identities or 22-player tracking.
- Tactical analysis, formation recognition, heatmaps, counterattack predictions and useful live advice are not implemented.
- TTS is a reusable speech helper; tactical messages are not yet generated.
- The detector has not been compiled or tested on the user's Infinix Hot 50i. Crop coordinates and color thresholds require calibration with actual captured frames.

## Privacy and performance
Frames are processed locally and not uploaded. Sampling is throttled to at most two frames per second in this prototype. Processing full-resolution frames may still be demanding; profiling and smaller radar-only frame extraction are required.
