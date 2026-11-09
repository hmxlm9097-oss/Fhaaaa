# H4XML Cloud Phone — Infrastructure & Deployment Guide

## 1. Architecture Overview
- **Android Client (`app/`)**: Native Kotlin + Jetpack Compose controller & diagnostics client.
- **Control Plane (`backend/src/server.js`)**: Node.js REST + Secure WebSocket (`wss://`) + WebRTC signaling gateway + ADB emulator bridge.
- **Virtual Android Nodes**: KVM-enabled Linux host running headless Android Emulator (`emulator -avd ... -no-window`) or Redroid containers with `scrcpy` / WebRTC video encoder.
- **Optional Firebase Cloud Sync**: Deploy `backend/firestore.rules` and configure `Firebase Auth` + `Firebase Storage` in Firebase Console if multi-device cloud persistence is desired.

## 2. Host Prerequisites (Linux KVM Server)
1. Enable hardware virtualization (`/dev/kvm` accessible).
2. Install Android SDK Command-line Tools, `platform-tools` (`adb`), and system images (`system-images;android-34;google_apis;x86_64`).
3. Create AVD profiles:
   ```bash
   avdmanager create avd -n Pixel_8_API_34 -k "system-images;android-34;google_apis;x86_64"
   ```

## 3. Start the Backend Gateway
```bash
cd backend
npm install
export JWT_SECRET="$(openssl rand -hex 32)"
npm start
```
For Android Emulator local testing, point the H4XML app Settings -> **Server Base URL** to `http://10.0.2.2:4000` and WebSocket URL to `ws://10.0.2.2:4000/ws/control`.

## 4. Production TLS & WebRTC
- Place Nginx / Caddy reverse proxy in front of port `4000` with TLS (`https://` and `wss://`).
- Run `coturn` for NAT traversal (STUN/TURN) on UDP/TCP 3478.
- Never deploy without explicit user authorization, visible active monitoring indicators, and OS-level permission consent.
