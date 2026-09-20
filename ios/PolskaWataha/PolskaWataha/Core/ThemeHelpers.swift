import SwiftUI

// ============ MOTYW — biel i czerwień (identyczny jak Android) ============
enum WatahaColors {
    static let White = Color(red: 1, green: 1, blue: 1)
    static let FlagRed = Color(red: 0.831, green: 0.129, blue: 0.239)      // #D4213D
    static let DarkRed = Color(red: 0.659, green: 0.110, blue: 0.188)     // #A81C30
    static let LightRed = Color(red: 0.992, green: 0.910, blue: 0.925)    // #FDE8EC
    static let Ink = Color(red: 0.122, green: 0.165, blue: 0.267)         // #1F2A44
    static let Grey = Color(red: 0.42, green: 0.45, blue: 0.50)
    static let LightGrey = Color(red: 0.953, green: 0.957, blue: 0.965)
    static let Border = Color(red: 0.898, green: 0.906, blue: 0.918)
    static let Green = Color(red: 0.18, green: 0.49, blue: 0.196)
    static let Amber = Color(red: 0.961, green: 0.62, blue: 0.043)
    static let Blue = Color(red: 0.114, green: 0.306, blue: 0.847)
}

func stateColor(_ s: TransportState) -> Color {
    switch s {
    case .READY: return WatahaColors.Green
    case .SCANNING: return WatahaColors.Amber
    case .DEMO, .NO_HARDWARE: return WatahaColors.Blue
    case .OFFLINE: return WatahaColors.Grey
    }
}

func timeAgo(_ ts: Int64) -> String {
    let diff = nowMs() - ts
    if diff < 60000 { return "przed chwilą" }
    if diff < 3600000 { return "przed \(diff / 60000) min" }
    if diff < 86400000 { return "przed \(diff / 3600000) h" }
    return "przed \(diff / 86400000) dn."
}

func distanceText(km: Double) -> String {
    km < 1 ? "\(Int(km * 1000)) m" : String(format: "%.1f km", km)
}
