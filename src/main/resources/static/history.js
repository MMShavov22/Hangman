const $ = (id) => document.getElementById(id);

function localDate() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function capitalize(text) {
    return text.charAt(0) + text.slice(1).toLowerCase();
}

async function loadHistory() {
    const params = new URLSearchParams();
    if ($("from").value) params.set("from", $("from").value);
    if ($("to").value) params.set("to", $("to").value);
    params.set("order", $("order").value);

    $("error").textContent = "";
    try {
        const res = await fetch("/api/history?" + params);
        const data = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(data.error || "Could not load the history");
        render(data);
    } catch (err) {
        $("error").textContent = err.message;
    }
}

function render(entries) {
    const body = $("historyBody");
    body.innerHTML = "";

    $("count").textContent = entries.length === 1 ? "1 game" : `${entries.length} games`;
    $("empty").classList.toggle("hidden", entries.length !== 0);
    $("historyTable").classList.toggle("hidden", entries.length === 0);

    entries.forEach((entry) => {
        const [date, time] = entry.playedAt.split(" ");
        const row = document.createElement("tr");

        [date, time, entry.username, entry.category, capitalize(entry.difficulty)].forEach((text) => {
            const cell = document.createElement("td");
            cell.textContent = text;          // textContent keeps names safe from HTML injection
            row.appendChild(cell);
        });

        const resultCell = document.createElement("td");
        const badge = document.createElement("span");
        badge.className = "badge " + (entry.result === "WON" ? "win" : "lose");
        badge.textContent = entry.result === "WON" ? "Won" : "Lost";
        resultCell.appendChild(badge);
        row.appendChild(resultCell);

        const pointsCell = document.createElement("td");
        pointsCell.textContent = entry.points;
        row.appendChild(pointsCell);

        body.appendChild(row);
    });
}

$("filterForm").addEventListener("submit", (event) => {
    event.preventDefault();
    loadHistory();
});

$("todayBtn").addEventListener("click", () => {
    $("from").value = localDate();
    $("to").value = localDate();
    loadHistory();
});

$("allBtn").addEventListener("click", () => {
    $("from").value = "";
    $("to").value = "";
    loadHistory();
});

loadHistory();