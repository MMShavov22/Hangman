const ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
const $ = (id) => document.getElementById(id);

let state = null;
let settings = null;
let busy = false;
let previousMasked = "";

async function api(path, options = {}) {
    const res = await fetch(path, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Something went wrong");
    return data;
}

/* ---------- Setup ---------- */

async function loadCategories() {
    const categories = await api("/api/categories");
    const select = $("category");
    select.innerHTML = "";
    categories.forEach((name) => {
        const option = document.createElement("option");
        option.value = name;
        option.textContent = name;
        select.appendChild(option);
    });
}

async function loadLeaderboard() {
    const list = $("leaderboardList");
    try {
        const entries = await api("/api/leaderboard");
        list.innerHTML = "";
        if (entries.length === 0) {
            const li = document.createElement("li");
            li.className = "empty";
            li.textContent = "No scores yet. Be the first!";
            list.appendChild(li);
            return;
        }
        entries.forEach((entry, index) => {
            const li = document.createElement("li");
            const rank = document.createElement("span");
            rank.className = "rank";
            rank.textContent = index + 1;
            const name = document.createElement("span");
            name.className = "name";
            name.textContent = entry.username;   // textContent keeps names safe from HTML injection
            const score = document.createElement("span");
            score.className = "score";
            score.textContent = entry.score;
            li.append(rank, name, score);
            list.appendChild(li);
        });
    } catch (err) {
        list.innerHTML = "";
    }
}

function showSetup(errorMessage) {
    $("game").classList.add("hidden");
    $("setup").classList.remove("hidden");
    const error = $("setupError");
    error.textContent = errorMessage || "";
    error.classList.toggle("hidden", !errorMessage);
}

/* ---------- Game ---------- */

async function startGame(newSettings) {
    if (newSettings) settings = newSettings;
    try {
        const game = await api("/api/game/start", {
            method: "POST",
            body: JSON.stringify(settings),
        });
        previousMasked = "";
        $("setup").classList.add("hidden");
        $("game").classList.remove("hidden");
        render(game);
    } catch (err) {
        showSetup(err.message);
    }
}

async function guess(letter) {
    if (busy || !state || state.status !== "IN_PROGRESS") return;
    if (state.guessedLetters.includes(letter)) return;
    busy = true;
    try {
        const next = await api(`/api/game/${state.gameId}/guess`, {
            method: "POST",
            body: JSON.stringify({ letter }),
        });
        render(next);
        if (next.status !== "IN_PROGRESS") loadLeaderboard();
    } catch (err) {
        $("message").textContent = err.message;
    } finally {
        busy = false;
    }
}

/* ---------- Rendering ---------- */

function render(next) {
    state = next;
    $("message").textContent = "";
    $("chipCategory").textContent = next.category;
    $("chipDifficulty").textContent =
        next.difficulty.charAt(0) + next.difficulty.slice(1).toLowerCase();
    renderWord(next);
    renderKeyboard(next);
    renderLives(next);
    renderGallows(next);
    renderResult(next);
}

function renderWord(s) {
    const container = $("word");
    container.innerHTML = "";
    const shown = s.word ?? s.maskedWord;
    [...shown].forEach((ch, i) => {
        const tile = document.createElement("div");
        tile.className = "tile";
        if (s.maskedWord[i] !== "_") {
            tile.textContent = ch;
            tile.classList.add("filled");
            if (previousMasked[i] === "_") tile.classList.add("pop");
        } else if (s.word) {
            tile.textContent = ch;
            tile.classList.add("missed");
        }
        container.appendChild(tile);
    });
    previousMasked = s.maskedWord;
}

function renderKeyboard(s) {
    document.querySelectorAll(".key").forEach((btn) => {
        const letter = btn.dataset.letter;
        const used = s.guessedLetters.includes(letter);
        const hit = s.maskedWord.includes(letter);
        btn.disabled = used || s.status !== "IN_PROGRESS";
        btn.classList.toggle("correct", used && hit);
        btn.classList.toggle("wrong", used && !hit);
    });
}

function renderLives(s) {
    const lives = $("lives");
    lives.innerHTML = "";
    for (let i = 0; i < s.maxMistakes; i++) {
        const dot = document.createElement("span");
        dot.className = "dot" + (i < s.mistakes ? " used" : "");
        lives.appendChild(dot);
    }
    $("mistakesText").textContent = `${s.mistakes} / ${s.maxMistakes} mistakes`;
}

function renderGallows(s) {
    const parts = document.querySelectorAll("#gallows .part");
    const visible = s.mistakes === 0
        ? 0
        : Math.ceil((s.mistakes * parts.length) / s.maxMistakes);
    parts.forEach((part, i) => part.classList.toggle("show", i < visible));
}

function renderResult(s) {
    const box = $("result");
    box.className = "result hidden";
    if (s.status === "WON") {
        box.textContent = `🎉 You won! +${s.pointsEarned} points`;
        box.className = "result win";
    } else if (s.status === "LOST") {
        box.textContent = `💀 Game over. The word was ${s.word}`;
        box.className = "result lose";
    }
}

/* ---------- Setup of events ---------- */

function buildKeyboard() {
    const keyboard = $("keyboard");
    keyboard.innerHTML = "";
    ALPHABET.forEach((letter) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "key";
        btn.textContent = letter;
        btn.dataset.letter = letter;
        btn.addEventListener("click", () => guess(letter));
        keyboard.appendChild(btn);
    });
}

$("setupForm").addEventListener("submit", (event) => {
    event.preventDefault();
    const username = $("username").value.trim();
    localStorage.setItem("hangman-username", username);
    startGame({
        username,
        category: $("category").value,
        difficulty: document.querySelector('input[name="difficulty"]:checked').value,
    });
});

$("newWordBtn").addEventListener("click", () => startGame());
$("menuBtn").addEventListener("click", () => showSetup());

document.addEventListener("keydown", (event) => {
    if (event.ctrlKey || event.metaKey || event.altKey) return;
    if ($("game").classList.contains("hidden")) return;
    const letter = event.key.toUpperCase();
    if (/^[A-Z]$/.test(letter)) guess(letter);
});

async function init() {
    buildKeyboard();
    $("username").value = localStorage.getItem("hangman-username") || "";
    try {
        await loadCategories();
    } catch (err) {
        showSetup("Cannot reach the server. Is the app running?");
    }
    loadLeaderboard();
}

init();