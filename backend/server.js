/**
 * POLSKA WATAHA — BACKEND (V0.1)
 * Zero zależności (czysty Node.js ≥ 18): http + JSON file storage.
 * REST API dla aplikacji Android + panel web (public/).
 *
 * Uruchom:  node server.js   (domyślnie port 8080)
 */
'use strict';

const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PORT = process.env.PORT || 8080;
const DATA_DIR = path.join(__dirname, 'data');
const DB_FILE = path.join(DATA_DIR, 'db.json');
const PUB_DIR = path.join(__dirname, 'public');

// ---------- Storage ----------
let db = {
    users: [], posts: [], messages: [], exchanges: [],
    crisis: [], badges: [], nodes: [], markers: [], commlog: [], feedback: []
};

function emptyDb() {
    return {
        users: [], posts: [], messages: [], exchanges: [],
        crisis: [], badges: [], nodes: [], markers: [], commlog: [], feedback: []
    };
}

function load() {
    try {
        if (fs.existsSync(DB_FILE)) {
            const parsed = JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
            db = Object.assign(emptyDb(), parsed);
        }
    } catch (e) { console.error('DB load error:', e.message); }
}

let saveTimer = null;
function save() {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(() => {
        try {
            fs.mkdirSync(DATA_DIR, { recursive: true });
            fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 1));
        } catch (e) { console.error('DB save error:', e.message); }
    }, 250);
}

const uid = (p) => p + '_' + crypto.randomBytes(4).toString('hex');

function seedIfEmpty() {
    if (db.users.length > 0) return;
    const now = Date.now();
    const users = [
        ['u_cwilk', 'cwilk', 'haslo123', 'Czarny Wilk', '+48 600 100 200', 'Strażnik Prawdy. Koordynator osiedla.', 4.7, 'ORZEL,WILK,KOTWICA,STRAZNIK', 52.2297, 21.0122],
        ['u_ostoja', 'ostoja', 'demo', 'Ostoja', '+48 601 211 300', 'Pomagam sąsiadom od 2022.', 4.2, 'SOLIDARNOSC,ORZEL', 52.2367, 20.9657],
        ['u_sokol', 'sokol', 'demo', 'Sokół', '+48 602 342 100', 'Kierowca. Wożę rzeczy.', 3.9, 'ORZEL', 52.2657, 20.9478],
        ['u_warta', 'warta', 'demo', 'Warta', '+48 603 111 222', 'Sanitariuszka.', 4.5, 'ORZEL,KOTWICA', 52.2222, 21.0455]
    ];
    db.users = users.map(u => ({
        id: u[0], username: u[1], passwordHash: u[2], displayName: u[3], phone: u[4],
        bio: u[5], reputation: u[6], badges: u[7], lat: u[8], lng: u[9], lastSeen: now
    }));
    db.posts = [
        { id: 'p_seed_0', authorId: 'u_cwilk', authorName: 'Czarny Wilk', type: 'GIVE', title: 'Apteczka domowa (komplet)', body: 'Bandaże, gaza, sól fizjologiczna. Wszystko ważne do 2027.', lat: 52.2297, lng: 21.0122, createdAt: now - 3600000, status: 'OPEN' },
        { id: 'p_seed_1', authorId: 'u_ostoja', authorName: 'Ostoja', type: 'OFFER', title: 'Mogę pomóc w porządkowaniu magazynu', body: 'Ręce do pracy + transport w obrębie Woli.', lat: 52.2367, lng: 20.9657, createdAt: now - 7200000, status: 'OPEN' },
        { id: 'p_seed_2', authorId: 'u_sokol', authorName: 'Sokół', type: 'OFFER', title: 'Mogę podwieźć do punktu pomocy', body: 'Bus 9 miejsc: Bielany–Wola–Centrum.', lat: 52.2657, lng: 20.9478, createdAt: now - 5400000, status: 'OPEN' },
        { id: 'p_seed_3', authorId: 'u_warta', authorName: 'Warta', type: 'NEED', title: 'Potrzebuję pasków do glukometru', body: 'Pomoc dla seniora, okolice Pragi.', lat: 52.2222, lng: 21.0455, createdAt: now - 1800000, status: 'OPEN' }
    ];
    db.nodes = [
        { id: 'bk1', name: 'Brama Warszawa', type: 'GATEWAY', status: 'ONLINE', lat: 52.2297, lng: 21.0122, battery: 100 },
        { id: 'n_A', name: 'NODE A', type: 'MESH', status: 'ONLINE', lat: 52.2297, lng: 21.0122, battery: 87 },
        { id: 'n_B', name: 'NODE B', type: 'MESH', status: 'ONLINE', lat: 52.2367, lng: 20.9657, battery: 64 },
        { id: 'n_C', name: 'NODE C', type: 'MESH', status: 'ONLINE', lat: 52.2222, lng: 21.0455, battery: 91 },
        { id: 'n_D', name: 'NODE D', type: 'MESH', status: 'ONLINE', lat: 52.2657, lng: 20.9478, battery: 45 }
    ];
    db.markers = [
        { id: 'm1', title: 'Szkoła Podstawowa nr 12', subtitle: 'Punkt pomocy', kind: 'POINT_POMOCY', emoji: '🏫', lat: 52.2297, lng: 21.0122 },
        { id: 'm2', title: 'Kościół św. Anny', subtitle: 'Dystrybucja wody', kind: 'POINT_POMOCY', emoji: '⛪', lat: 52.2367, lng: 20.9657 }
    ];
    db.commlog.push({ ts: now, from: 'SEED', to: 'BACKEND', type: 'BOOT', body: 'Backend Polska Wataha uruchomiony — dane przykładowe załadowane.' });
    save();
}

// ---------- Helpers ----------
function send(res, code, obj) {
    const body = JSON.stringify(obj);
    res.writeHead(code, {
        'Content-Type': 'application/json; charset=utf-8',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET,POST,OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type,Authorization',
        'Cache-Control': 'no-store'
    });
    res.end(body);
}

function readBody(req) {
    return new Promise((resolve) => {
        let data = '';
        req.on('data', c => { data += c; if (data.length > 1e6) req.destroy(); });
        req.on('end', () => { try { resolve(data ? JSON.parse(data) : {}); } catch (e) { resolve({}); } });
    });
}

const ok = (res, extra = {}) => send(res, 200, { ok: true, time: Date.now(), ...extra });
const bad = (res, msg, code = 400) => send(res, code, { ok: false, error: msg });

function upsertInto(arr, item) {
    const i = arr.findIndex(x => x.id === item.id);
    if (i >= 0) arr[i] = { ...arr[i], ...item }; else arr.push(item);
}

// ---------- Router ----------
const server = http.createServer(async (req, res) => {
    const url = new URL(req.url, 'http://x');
    const p = url.pathname;
    const m = req.method;

    if (m === 'OPTIONS') { res.writeHead(204, { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Methods': 'GET,POST,OPTIONS', 'Access-Control-Allow-Headers': 'Content-Type,Authorization' }); res.end(); return; }

    // Statyczne: panel web
    if (m === 'GET' && (p === '/' || p === '/index.html')) {
        const f = path.join(PUB_DIR, 'index.html');
        if (fs.existsSync(f)) {
            res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
            res.end(fs.readFileSync(f));
            return;
        }
    }
    if (m === 'GET' && p.startsWith('/static/')) {
        const f = path.join(PUB_DIR, path.basename(p));
        if (fs.existsSync(f)) {
            const ext = path.extname(f);
            const ct = ext === '.css' ? 'text/css' : ext === '.js' ? 'application/javascript' : 'application/octet-stream';
            res.writeHead(200, { 'Content-Type': ct + '; charset=utf-8' });
            res.end(fs.readFileSync(f));
            return;
        }
    }

    if (!p.startsWith('/api/')) { bad(res, 'Not found', 404); return; }

    try {
        // HEALTH
        if (m === 'GET' && p === '/api/health') return ok(res, { version: '0.1.0', service: 'polska-wataha-backend', counts: { users: db.users.length, posts: db.posts.length, nodes: db.nodes.length } });

        // LOGIN
        if (m === 'POST' && p === '/api/auth/login') {
            const b = await readBody(req);
            const u = db.users.find(x => x.username === String(b.username || '').toLowerCase());
            if (!u || u.passwordHash !== String(b.password || '')) return bad(res, 'Błędne dane logowania');
            const { passwordHash, ...pub } = u;
            return ok(res, { user: { ...pub, badges: u.badges } });
        }

        // GENERIC: POST z {data:{...}} vs GET lista
                const collections = {
            '/api/users': 'users', '/api/posts': 'posts', '/api/messages': 'messages',
            '/api/exchanges': 'exchanges', '/api/crisis': 'crisis', '/api/badges': 'badges',
            '/api/nodes': 'nodes', '/api/markers': 'markers', '/api/feedback': 'feedback'
        };
        if (collections[p]) {
            const coll = db[collections[p]];
            if (m === 'GET') return ok(res, { items: coll });
            if (m === 'POST') {
                const b = await readBody(req);
                const item = b.data;
                if (!item || !item.id) return bad(res, 'Brak danych (oczekiwano {data:{id,...}})');
                upsertInto(coll, { ...item, _serverAt: Date.now() });
                save();
                return ok(res, { stored: item.id, total: coll.length });
            }
        }

        // COMM — ramka mesh z adaptera internetowego (symulacja przekaźnika)
        if (m === 'POST' && p === '/api/comm') {
            const b = await readBody(req);
            const entry = { ts: Date.now(), from: String(b.from || '?'), to: String(b.to || '?'), type: String(b.type || '?'), body: String(b.body || '').slice(0, 2000) };
            db.commlog.push(entry);
            if (db.commlog.length > 500) db.commlog = db.commlog.slice(-500);
            save();
            return ok(res, { delivered: true, hop: 'internet-gateway' });
        }
        if (m === 'GET' && p === '/api/comm/log') return ok(res, { items: db.commlog.slice(-100) });

        // SYNC-PULL — do przyszłej synchronizacji dwukierunkowej
        if (m === 'GET' && p === '/api/sync-pull') {
            return ok(res, {
                users: db.users, posts: db.posts, messages: db.messages,
                exchanges: db.exchanges, crisis: db.crisis, badges: db.badges,
                nodes: db.nodes, markers: db.markers
            });
        }

        bad(res, 'Nieznany endpoint', 404);
    } catch (e) {
        console.error(e);
        bad(res, 'Internal: ' + e.message, 500);
    }
});

load();
seedIfEmpty();
server.listen(PORT, '0.0.0.0', () => {
    console.log('════════════════════════════════════════════');
    console.log('  POLSKA WATAHA — BACKEND V0.1 (Czarny Wilk)');
    console.log(`  REST API : http://0.0.0.0:${PORT}/api/health`);
    console.log(`  Panel web: http://0.0.0.0:${PORT}/`);
    console.log(`  Konto demo: cwilk / haslo123`);
    console.log('════════════════════════════════════════════');
});
