const apiBase = "http://localhost:8080";
document.getElementById("origin-label").textContent = window.location.host;

function showResult(id, message, kind = "") {
  const element = document.getElementById(id);
  element.textContent = message;
  element.className = `result ${kind}`.trim();
}

async function getJson(path) {
  const response = await fetch(`${apiBase}${path}`);
  const data = await response.json().catch(() => null);
  return { response, data };
}

function errorMessage(error) {
  return error instanceof TypeError
    ? "연결 실패: API 실행 상태 또는 CORS 허용 출처를 확인하세요."
    : `요청 실패: ${error.message}`;
}

async function checkAvailability(kind) {
  const value = document.getElementById(kind).value.trim();
  const resultId = `${kind}-result`;
  const jsonId = `${kind}-json`;
  showResult(resultId, "조회 중...");
  document.getElementById(jsonId).textContent = "";

  try {
    const { response, data } = await getJson(`/members/${kind}/${encodeURIComponent(value)}`);
    if (!response.ok) {
      showResult(resultId, `HTTP ${response.status} · 입력값을 확인하세요.`, "error");
      document.getElementById(jsonId).textContent = JSON.stringify(data ?? {}, null, 2);
      return;
    }
    showResult(resultId, `HTTP ${response.status} · ${data.available ? "사용 가능" : "이미 사용 중"}`, "success");
    document.getElementById(jsonId).textContent = JSON.stringify(data);
  } catch (error) {
    showResult(resultId, errorMessage(error), "error");
  }
}

function renderRatings(items) {
  const body = document.getElementById("ratings-body");
  body.replaceChildren();
  if (items.length === 0) {
    const row = body.insertRow();
    const cell = row.insertCell();
    cell.colSpan = 5;
    cell.className = "empty";
    cell.textContent = "이 페이지에 등록된 평점이 없습니다.";
    return;
  }
  for (const rating of items) {
    const row = body.insertRow();
    for (const value of [rating.ratingId, rating.movieId, rating.score, rating.comment ?? "—", rating.createdAt ?? "—"]) {
      row.insertCell().textContent = value;
    }
  }
}

async function loadRatings() {
  const memberId = document.getElementById("member-id").value;
  const page = document.getElementById("page").value;
  const size = document.getElementById("size").value;
  const params = new URLSearchParams({ page, size });
  showResult("ratings-result", "조회 중...");
  document.getElementById("previous-page").disabled = true;
  document.getElementById("next-page").disabled = true;

  try {
    const { response, data } = await getJson(`/members/${encodeURIComponent(memberId)}/ratings?${params}`);
    if (!response.ok) {
      showResult("ratings-result", `HTTP ${response.status} · 회원 ID 또는 페이지 값을 확인하세요.`, "error");
      renderRatings([]);
      return;
    }
    showResult("ratings-result", `HTTP ${response.status} · 전체 ${data.totalElements}건 · 현재 ${data.content.length}건`, "success");
    document.getElementById("page-indicator").textContent = `${data.page + 1} / ${Math.max(data.totalPages, 1)} 페이지`;
    document.getElementById("previous-page").disabled = data.page <= 0;
    document.getElementById("next-page").disabled = data.page + 1 >= data.totalPages;
    renderRatings(data.content);
  } catch (error) {
    showResult("ratings-result", errorMessage(error), "error");
    renderRatings([]);
  }
}

document.getElementById("nickname-form").addEventListener("submit", (event) => {
  event.preventDefault();
  checkAvailability("nickname");
});
document.getElementById("email-form").addEventListener("submit", (event) => {
  event.preventDefault();
  checkAvailability("email");
});
document.getElementById("ratings-form").addEventListener("submit", (event) => {
  event.preventDefault();
  loadRatings();
});
document.getElementById("previous-page").addEventListener("click", () => {
  document.getElementById("page").value = Number(document.getElementById("page").value) - 1;
  loadRatings();
});
document.getElementById("next-page").addEventListener("click", () => {
  document.getElementById("page").value = Number(document.getElementById("page").value) + 1;
  loadRatings();
});

checkAvailability("nickname");
checkAvailability("email");
loadRatings();
