"""임시 프로젝트에서만 @Transactional을 제거하고 롤백 테스트의 실패를 검증합니다."""

from pathlib import Path
import difflib
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET


project = Path(__file__).resolve().parents[1]
evidence = project / "docs/week9/without-transaction"
evidence.mkdir(parents=True, exist_ok=True)
service_path = "src/main/java/com/umc/movie/service/MovieRecordService.java"
test_class = "com.umc.movie.controller.MovieRecordRollbackApiTest"
test_method = "duplicateFavoriteRollsBackPreviouslySavedRating"

with tempfile.TemporaryDirectory(prefix="umc-without-transaction-") as temporary:
    copy = Path(temporary)
    for name in ("build.gradle", "settings.gradle"):
        shutil.copy2(project / name, copy / name)
    shutil.copytree(project / "src", copy / "src")

    original = (copy / service_path).read_text()
    marker = "    @Transactional\n    public MovieRecordResponse create"
    if original.count(marker) != 1:
        raise SystemExit("영화 기록 저장 메서드의 @Transactional을 정확히 하나 찾지 못했습니다.")
    mutated = original.replace(marker, "    public MovieRecordResponse create", 1)
    (copy / service_path).write_text(mutated)
    (evidence / "removed-transaction.patch").write_text("".join(difflib.unified_diff(
        original.splitlines(keepends=True), mutated.splitlines(keepends=True),
        fromfile=service_path, tofile=service_path)))

    command = [str(project / "gradlew"), "-p", str(copy), "test", "--tests",
               f"{test_class}.{test_method}", "--console=plain"]
    with (evidence / "gradle.log").open("w") as log:
        result = subprocess.run(command, stdout=log, stderr=subprocess.STDOUT)
    report = copy / f"build/test-results/test/TEST-{test_class}.xml"
    if result.returncode == 0 or not report.exists():
        raise SystemExit("예상한 테스트 실패를 확인하지 못했습니다. gradle.log를 확인하세요.")
    suite = ET.parse(report).getroot()
    failure = suite.find("testcase/failure")
    if (suite.get("tests") != "1" or suite.get("failures") != "1"
            or failure is None or "실패한 요청의 평점은 DB에 남지 않아야 한다" not in failure.get("message", "")):
        raise SystemExit("DB 잔여 평점 검증 이외의 이유로 실패했습니다. 테스트 리포트를 확인하세요.")

    shutil.copy2(report, evidence / report.name)
    shutil.copytree(copy / "build/reports/tests/test", evidence / "report", dirs_exist_ok=True)
    print("검증 완료: @Transactional 제거 시 평점이 1개 남아 롤백 테스트가 예상대로 실패했습니다.")
    print(f"리포트: {evidence / 'report/index.html'}")
