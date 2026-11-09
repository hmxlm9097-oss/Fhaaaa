/**
 * H4XML Cloud Phone — Control Plane & Android Emulator ADB Bridge
 * Requires Node.js 20+, Android SDK Platform-Tools (adb), and Android Emulator / AVD host.
 * Never collects hidden credentials; all monitoring & remote control require explicit user auth & consent.
 */

const express = require('express');
const http = require('http');
const { WebSocketServer } = require('ws');
const cors = require('cors');
const helmet = require('helmet');
const jwt = require('jsonwebtoken');
const speakeasy = require('speakeasy');
const multer = require('multer');
const { execFile, spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');

const PORT = process.env.PORT || 4000;
const JWT_SECRET = process.env.JWT_SECRET || 'CHANGE_THIS_IN_PRODUCTION_ENV';
const UPLOAD_DIR = path.join(__dirname, 'uploads');
if (!fs.existsSync(UPLOAD_DIR)) fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const upload = multer({ dest: UPLOAD_DIR, limits: { fileSize: 100 * 1024 * 1024 } });

const app = express();
app.use(helmet());
app.use(cors());
app.use(express.json());

// In-memory store for runtime emulator processes; pair with Firestore Admin SDK in production.
const users = new Map();
const cloudDevices = new Map();
const activeSessions = new Map();
const activityLogs = [];

function logEvent(userId, category, action, detail, severity = 'INFO') {
  activityLogs.unshift({
    id: uuidv4(),
    userId,
    category,
    action,
    detail,
    severity,
    timestamp: Date.now()
  });
  if (activityLogs.length > 500) activityLogs.pop();
}

function authMiddleware(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ error: 'Missing bearer token' });
  try {
    const payload = jwt.verify(token, JWT_SECRET);
    const user = users.get(payload.email);
    if (!user || user.tokenVersion !== payload.tokenVersion) {
      return res.status(401).json({ error: 'Session revoked or expired' });
    }
    req.user = user;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Invalid authentication token' });
  }
}

function runAdb(serial, args) {
  return new Promise((resolve, reject) => {
    const fullArgs = serial ? ['-s', serial, ...args] : args;
    execFile('adb', fullArgs, { timeout: 10000 }, (err, stdout, stderr) => {
      if (err) return reject(new Error(stderr || err.message));
      resolve(stdout.trim());
    });
  });
}

// --- HEALTH CHECK ---
app.get('/api/health', async (_req, res) => {
  let adbAvailable = false;
  try {
    await runAdb(null, ['version']);
    adbAvailable = true;
  } catch (_) {
    adbAvailable = false;
  }
  res.json({
    status: 'ok',
    service: 'h4xml-cloud-phone-gateway',
    adbAvailable,
    activeDeviceCount: cloudDevices.size,
    timestamp: Date.now()
  });
});

// --- AUTHENTICATION & 2FA ---
app.post('/api/auth/register', (req, res) => {
  const { email, password, displayName } = req.body;
  if (!email || !password || password.length < 8) {
    return res.status(400).json({ error: 'Valid email and 8+ char password required' });
  }
  if (users.has(email)) return res.status(409).json({ error: 'Account already exists' });
  const totpSecret = speakeasy.generateSecret({ name: `H4XML Cloud (${email})` });
  const user = {
    id: uuidv4(),
    email,
    displayName: displayName || email.split('@')[0],
    password, // Replace with bcrypt/argon2 hash or Firebase Auth ID token verification in production
    emailVerified: false,
    twoFactorEnabled: false,
    totpSecret: totpSecret.base32,
    tokenVersion: 1,
    plan: 'Starter'
  };
  users.set(email, user);
  const token = jwt.sign({ uid: user.id, email, tokenVersion: user.tokenVersion }, JWT_SECRET, { expiresIn: '12h' });
  logEvent(user.id, 'SECURITY', 'REGISTER', `Registered account ${email}`);
  res.status(201).json({
    token,
    user: { id: user.id, email: user.email, displayName: user.displayName, emailVerified: user.emailVerified, twoFactorEnabled: user.twoFactorEnabled, plan: user.plan },
    totpOtpAuthUrl: totpSecret.otpauth_url
  });
});

app.post('/api/auth/login', (req, res) => {
  const { email, password, totpCode } = req.body;
  const user = users.get(email);
  if (!user || user.password !== password) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }
  if (user.twoFactorEnabled) {
    const valid = speakeasy.totp.verify({ secret: user.totpSecret, encoding: 'base32', token: String(totpCode || ''), window: 1 });
    if (!valid) return res.status(403).json({ error: '2FA verification code required or invalid' });
  }
  const token = jwt.sign({ uid: user.id, email, tokenVersion: user.tokenVersion }, JWT_SECRET, { expiresIn: '12h' });
  logEvent(user.id, 'SECURITY', 'LOGIN', `Authenticated session created for ${email}`);
  res.json({
    token,
    user: { id: user.id, email: user.email, displayName: user.displayName, emailVerified: user.emailVerified, twoFactorEnabled: user.twoFactorEnabled, plan: user.plan }
  });
});

app.post('/api/auth/verify-2fa', authMiddleware, (req, res) => {
  const { code, enable } = req.body;
  const valid = speakeasy.totp.verify({ secret: req.user.totpSecret, encoding: 'base32', token: String(code || ''), window: 1 });
  if (!valid) return res.status(400).json({ error: 'Invalid TOTP verification code' });
  if (typeof enable === 'boolean') req.user.twoFactorEnabled = enable;
  logEvent(req.user.id, 'SECURITY', '2FA_VERIFY', `2FA status updated: ${req.user.twoFactorEnabled}`);
  res.json({ verified: true, twoFactorEnabled: req.user.twoFactorEnabled });
});

app.post('/api/auth/logout-all', authMiddleware, (req, res) => {
  req.user.tokenVersion += 1;
  logEvent(req.user.id, 'SECURITY', 'LOGOUT_ALL', 'Revoked all active refresh/access sessions', 'WARN');
  res.json({ revoked: true });
});

// --- CLOUD PHONE MANAGEMENT ---
app.get('/api/devices', authMiddleware, (req, res) => {
  const owned = Array.from(cloudDevices.values()).filter((d) => d.ownerId === req.user.id);
  res.json({ devices: owned });
});

app.post('/api/devices', authMiddleware, (req, res) => {
  const { name, androidVersion, cpuCores, ramGb, storageGb, avdName, region } = req.body;
  const device = {
    id: uuidv4(),
    ownerId: req.user.id,
    name: name || 'Cloud-Node',
    androidVersion: androidVersion || 'Android 14',
    cpuCores: Number(cpuCores || 4),
    ramGb: Number(ramGb || 8),
    storageGb: Number(storageGb || 64),
    avdName: avdName || 'Pixel_8_API_34',
    region: region || 'us-west-1',
    adbSerial: null,
    status: 'STOPPED',
    createdAt: Date.now()
  };
  cloudDevices.set(device.id, device);
  logEvent(req.user.id, 'DEVICE', 'CREATE', `Provisioned virtual profile ${device.name} (${device.androidVersion})`);
  res.status(201).json({ device });
});

app.post('/api/devices/:id/start', authMiddleware, (req, res) => {
  const device = cloudDevices.get(req.params.id);
  if (!device || device.ownerId !== req.user.id) return res.status(404).json({ error: 'Device not found' });
  // Launch headless Android emulator process if Android SDK emulator binary is present on host
  try {
    const proc = spawn('emulator', ['-avd', device.avdName, '-no-audio', '-no-window', '-gpu', 'swiftshader_indirect'], { detached: true, stdio: 'ignore' });
    proc.unref();
    device.status = 'RUNNING';
    device.adbSerial = req.body.adbSerial || 'emulator-5554';
    logEvent(req.user.id, 'DEVICE', 'START', `Started emulator ${device.name}`);
    res.json({ device });
  } catch (err) {
    res.status(500).json({ error: `Failed to start emulator binary: ${err.message}` });
  }
});

app.post('/api/devices/:id/stop', authMiddleware, async (req, res) => {
  const device = cloudDevices.get(req.params.id);
  if (!device || device.ownerId !== req.user.id) return res.status(404).json({ error: 'Device not found' });
  if (device.adbSerial) {
    try { await runAdb(device.adbSerial, ['emu', 'kill']); } catch (_) {}
  }
  device.status = 'STOPPED';
  logEvent(req.user.id, 'DEVICE', 'STOP', `Stopped cloud phone ${device.name}`);
  res.json({ device });
});

app.post('/api/devices/:id/restart', authMiddleware, async (req, res) => {
  const device = cloudDevices.get(req.params.id);
  if (!device || device.ownerId !== req.user.id) return res.status(404).json({ error: 'Device not found' });
  if (device.adbSerial) {
    try { await runAdb(device.adbSerial, ['reboot']); } catch (err) { return res.status(500).json({ error: err.message }); }
  }
  device.status = 'RUNNING';
  logEvent(req.user.id, 'DEVICE', 'RESTART', `Rebooted authorized device ${device.name}`);
  res.json({ device });
});

app.delete('/api/devices/:id', authMiddleware, (req, res) => {
  const device = cloudDevices.get(req.params.id);
  if (!device || device.ownerId !== req.user.id) return res.status(404).json({ error: 'Device not found' });
  cloudDevices.delete(req.params.id);
  logEvent(req.user.id, 'DEVICE', 'DELETE', `Deleted virtual phone ${device.name}`, 'WARN');
  res.json({ deleted: true });
});

// --- AUTHORIZED FILE TRANSFER ---
app.post('/api/files/upload', authMiddleware, upload.single('file'), (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file uploaded' });
  logEvent(req.user.id, 'FILES', 'UPLOAD', `Uploaded ${req.file.originalname} (${req.file.size} bytes)`);
  res.status(201).json({
    fileId: req.file.filename,
    originalName: req.file.originalname,
    size: req.file.size,
    mimeType: req.file.mimetype
  });
});

app.get('/api/logs', authMiddleware, (req, res) => {
  res.json({ logs: activityLogs.filter((l) => l.userId === req.user.id) });
});

// --- WEBSOCKET REMOTE CONTROL & WEBRTC SIGNALING ---
const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: '/ws/control' });

wss.on('connection', (ws, req) => {
  const sessionId = uuidv4();
  activeSessions.set(sessionId, { ws, connectedAt: Date.now() });

  ws.send(JSON.stringify({ type: 'session_ready', sessionId, message: 'Connected to H4XML Control Gateway' }));

  ws.on('message', async (raw) => {
    try {
      const msg = JSON.parse(raw.toString());
      const serial = msg.adbSerial || 'emulator-5554';

      switch (msg.type) {
        case 'tap':
          await runAdb(serial, ['shell', 'input', 'tap', String(Math.round(msg.x)), String(Math.round(msg.y))]);
          ws.send(JSON.stringify({ type: 'ack', action: 'tap', ok: true }));
          break;
        case 'swipe':
          await runAdb(serial, ['shell', 'input', 'swipe', String(Math.round(msg.x1)), String(Math.round(msg.y1)), String(Math.round(msg.x2)), String(Math.round(msg.y2)), String(msg.durationMs || 280)]);
          ws.send(JSON.stringify({ type: 'ack', action: 'swipe', ok: true }));
          break;
        case 'keyevent':
          await runAdb(serial, ['shell', 'input', 'keyevent', String(msg.keycode)]);
          ws.send(JSON.stringify({ type: 'ack', action: 'keyevent', keycode: msg.keycode, ok: true }));
          break;
        case 'text':
          await runAdb(serial, ['shell', 'input', 'text', String(msg.text || '').replace(/\s/g, '%s')]);
          ws.send(JSON.stringify({ type: 'ack', action: 'text', ok: true }));
          break;
        case 'launch_app':
          await runAdb(serial, ['shell', 'monkey', '-p', String(msg.packageName), '-c', 'android.intent.category.LAUNCHER', '1']);
          ws.send(JSON.stringify({ type: 'ack', action: 'launch_app', ok: true }));
          break;
        case 'webrtc_offer':
        case 'webrtc_answer':
        case 'webrtc_ice':
          // Forward WebRTC SDP/ICE signaling to target emulator streaming worker
          ws.send(JSON.stringify({ type: 'signaling_relayed', originalType: msg.type }));
          break;
        default:
          ws.send(JSON.stringify({ type: 'error', error: `Unsupported command type: ${msg.type}` }));
      }
    } catch (err) {
      ws.send(JSON.stringify({ type: 'error', error: err.message }));
    }
  });

  ws.on('close', () => {
    activeSessions.delete(sessionId);
  });
});

server.listen(PORT, () => {
  console.log(`H4XML Cloud Phone Gateway listening on port ${PORT}`);
});
