import XCTest
@testable import PolskaWatahaCore

/// Testy silnika WILK — identyczne asercje jak w wersji Android (12 testów łącznie z mesh).
final class AssistantEngineTests: XCTestCase {

    func testWoda() {
        let r = AssistantEngine.answer("Jak oczyścić wodę z rzeki przed piciem?")
        XCTAssertTrue(r.text.localizedCaseInsensitiveContains("wod"))
        XCTAssertTrue(r.text.contains("przegot"))
        XCTAssertEqual(r.topic, "Woda")
    }

    func testSOS() {
        let r = AssistantEngine.answer("SOS! potrzebuję pomocy, ginę!")
        XCTAssertTrue(r.crisis)
        XCTAssertTrue(r.text.contains("TRYB KRYZYSOWY"))
        XCTAssertTrue(r.text.contains("112"))
    }

    func testChecklista72h() {
        let r = AssistantEngine.answer("co spakować na 72 godziny ewakuacji?")
        XCTAssertTrue(r.text.contains("72h") || r.text.contains("Woda"))
        XCTAssertFalse(r.crisis)
    }

    func testFallback() {
        let r = AssistantEngine.answer("zqbxylj coś dziwnego")
        XCTAssertTrue(r.text.contains("Nie mam pewnej odpowiedzi"))
        XCTAssertTrue(r.text.contains("Poradnik"))
    }

    func testPolskieZnaki() {
        let r = AssistantEngine.answer("Jak złożyć ognisko?")
        XCTAssertTrue(r.text.contains("Ogień") || r.text.contains("zapałek"))
    }

    func testBazaWiedzyNiepusta() {
        XCTAssertGreaterThan(SurvivalData.KB.count, 20)
        XCTAssertEqual(SurvivalData.sections.count, 12)
        XCTAssertEqual(SurvivalData.TIPS.count, 8)
    }
}
