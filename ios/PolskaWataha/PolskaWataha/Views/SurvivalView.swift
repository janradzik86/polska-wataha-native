import SwiftUI
import PolskaWatahaCore

// MARK: - PORADNIK PRZETRWANIA (treść z pakietu core — testowana na Linuxie)

struct SurvivalView: View {
    @State private var openSection: String? = nil
    @State private var query = ""

    private var sections: [SurvivalSection] {
        if query.isEmpty { return SurvivalData.sections }
        let q = query.lowercased()
        return SurvivalData.sections.filter {
            $0.title.lowercased().contains(q) ||
            $0.tags.contains { $0.lowercased().contains(q) }
        }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 10) {
                // pasek Wskazówka dnia
                HStack(spacing: 10) {
                    Text("💡").font(.system(size: 24))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("WSKAZÓWKA DNIA").font(.system(size: 10, weight: .heavy)).foregroundColor(WatahaColors.FlagRed)
                        Text(SurvivalData.TIPS.randomElement() ?? "")
                            .font(.system(size: 12, weight: .medium)).foregroundColor(WatahaColors.Ink)
                            .lineLimit(2)
                    }
                    Spacer()
                }
                .padding(12)
                .background(RoundedRectangle(cornerRadius: 14).fill(WatahaColors.LightRed))
                .padding(.horizontal, 14)
                .padding(.top, 10)

                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass").foregroundColor(WatahaColors.Grey)
                    TextField("Szukaj: woda, ogień, medycyna…", text: $query)
                        .font(.system(size: 14)).autocorrectionDisabled()
                }
                .padding(.horizontal, 12).padding(.vertical, 10)
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                .padding(.horizontal, 14)

                SectionHeader(title: "\(sections.count) działów wiedzy • działa offline")

                ForEach(sections, id: \.id) { s in
                    SurvivalSectionCard(section: s, expanded: openSection == s.id) {
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.85)) {
                            openSection = openSection == s.id ? nil : s.id
                        }
                    }
                    .padding(.horizontal, 14)
                }
            }
            .padding(.bottom, 20)
        }
        .background(WatahaColors.LightGrey.opacity(0.4))
    }
}

struct SurvivalSectionCard: View {
    let section: SurvivalSection
    let expanded: Bool
    let toggle: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: toggle) {
                HStack(spacing: 12) {
                    Text(section.emoji).font(.system(size: 26))
                    VStack(alignment: .leading, spacing: 2) {
                        Text(section.title).font(.system(size: 15.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                        HStack(spacing: 4) {
                            Text("\(section.points.count) punktów")
                            Text("•")
                            Text(section.tags.prefix(3).joined(separator: ", "))
                        }
                        .font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                    }
                    Spacer()
                    Image(systemName: expanded ? "chevron.up" : "chevron.down")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(WatahaColors.FlagRed)
                }
                .padding(13)
            }
            .buttonStyle(PlainButtonStyle())

            if expanded {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(section.points, id: \.self) { p in
                        HStack(alignment: .top, spacing: 8) {
                            Circle().fill(WatahaColors.FlagRed).frame(width: 6, height: 6).padding(.top, 5)
                            Text(p).font(.system(size: 13.5)).foregroundColor(WatahaColors.Ink.opacity(0.85))
                        }
                    }
                    Text("Źródło: materiał szkoleniowy Watahy — pamiętaj, w nagłym wypadku dzwoń 112.")
                        .font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                        .padding(.top, 4)
                }
                .padding(.horizontal, 13).padding(.bottom, 13)
                .transition(.opacity)
            }
        }
        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(expanded ? WatahaColors.FlagRed.opacity(0.5) : WatahaColors.Border, lineWidth: 1))
        .shadow(color: WatahaColors.Ink.opacity(0.04), radius: 4, y: 1)
    }
}
