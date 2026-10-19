const $ = (id) => document.getElementById(id);

function getTodayString() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

async function fetchReport() {
    const period = $("periodSelect").value;
    const anchorDate = $("anchorDate").value;

    const params = new URLSearchParams();
    params.set("months", period);
    if (anchorDate) {
        params.set("anchorDate", anchorDate);
    }

    $("error").textContent = "";

    try {
        const res = await fetch("/api/reports?" + params.toString());
        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(data.error || "Failed to load report data.");
        }

        renderReport(data);
    } catch (err) {
        $("error").textContent = err.message;
        $("reportOutput").classList.add("hidden");
    }
}

function renderReport(data) {
    $("reportOutput").classList.remove("hidden");
    $("reportTitle").textContent = `${data.periodMonths}-Month Aggregated Report`;
    $("dateRangeChip").textContent = `${data.startDate} to ${data.endDate}`;

    $("totalEvents").textContent = data.totalEvents;
    $("wonEvents").textContent = data.wonEvents;
    $("lostEvents").textContent = data.lostEvents;
    $("totalPoints").textContent = `${data.totalPointsEarned} pts`;

    const categoryList = $("categoryList");
    categoryList.innerHTML = "";

    const categories = Object.keys(data.eventsByCategory || {});
    if (categories.length === 0) {
        const li = document.createElement("li");
        li.className = "empty-state";
        li.textContent = "No events recorded for this timeframe.";
        categoryList.appendChild(li);
        return;
    }

    categories.forEach((cat) => {
        const count = data.eventsByCategory[cat];
        const li = document.createElement("li");
        li.className = "category-item";

        const nameSpan = document.createElement("span");
        nameSpan.className = "category-name";
        nameSpan.textContent = cat;

        const countSpan = document.createElement("span");
        countSpan.className = "category-count";
        countSpan.textContent = `${count} events`;

        li.append(nameSpan, countSpan);
        categoryList.appendChild(li);
    });
}

$("reportForm").addEventListener("submit", (e) => {
    e.preventDefault();
    fetchReport();
});

$("downloadPdfBtn").addEventListener("click", () => {
    const period = $("periodSelect").value;
    const anchorDate = $("anchorDate").value;

    const params = new URLSearchParams();
    params.set("months", period);
    if (anchorDate) params.set("anchorDate", anchorDate);

    window.location.href = "/api/reports/download-pdf?" + params.toString();
});

// Default calendar value to today
$("anchorDate").value = getTodayString();
fetchReport();