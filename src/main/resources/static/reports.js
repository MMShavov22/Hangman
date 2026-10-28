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

async function fetchUserAnalytics() {
    try {
        const res = await fetch("/api/users/analytics");
        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(data.error || "Failed to load user analytics.");
        }

        renderUserAnalytics(data);
    } catch (err) {
        $("userAnalyticsStatus").textContent = err.message;
        $("userAnalyticsStatus").classList.remove("hidden");
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

function renderUserAnalytics(data) {
    $("userAnalyticsStatus").classList.add("hidden");
    $("userAnalyticsBox").classList.remove("hidden");

    $("usersToday").textContent = data.today ?? 0;
    $("usersWeek").textContent = data.lastWeek ?? 0;
    $("usersMonth").textContent = data.lastMonth ?? 0;
    $("usersYear").textContent = data.lastYear ?? 0;
    $("userInsight").textContent = data.insight || "No registration pattern available.";

    const trendList = $("userTrendList");
    trendList.innerHTML = "";

    const trend = Array.isArray(data.trend) ? data.trend : [];
    if (trend.length === 0) {
        const li = document.createElement("li");
        li.className = "empty-state small";
        li.textContent = "No trend data available.";
        trendList.appendChild(li);
        return;
    }

    const maxValue = Math.max(...trend.map(item => Number(item.count) || 0), 1);
    trend.forEach((item) => {
        const li = document.createElement("li");
        li.className = "trend-item";

        const label = document.createElement("span");
        label.className = "trend-label";
        label.textContent = item.label;

        const barWrap = document.createElement("div");
        barWrap.className = "trend-bar-wrap";

        const bar = document.createElement("span");
        bar.className = "trend-bar";
        const percentage = ((Number(item.count) || 0) / maxValue) * 100;
        bar.style.width = `${Math.max(percentage, 8)}%`;

        const count = document.createElement("span");
        count.className = "trend-count";
        count.textContent = item.count;

        barWrap.appendChild(bar);
        li.append(label, barWrap, count);
        trendList.appendChild(li);
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

$("anchorDate").value = getTodayString();
fetchReport();
fetchUserAnalytics();
