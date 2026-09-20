import SwiftUI
import MapKit

// MARK: - MAPA DOBROCI (MapKit — natywny odpowiednik Google Maps z Android)

struct WatahaMapView: View {
    @EnvironmentObject var repo: Repo
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 52.2297, longitude: 21.0122),
        span: MKCoordinateSpan(latitudeDelta: 0.07, longitudeDelta: 0.07))
    @State private var showPosts = true
    @State private var showPoints = true

    private var annotations: [MapPoint] {
        var out: [MapPoint] = []
        if showPosts {
            for p in repo.posts where p.status == PostStatus.OPEN {
                out.append(MapPoint(id: p.id, kind: "POST", type: p.type, title: p.title,
                                    subtitle: "\(p.authorName) • \(timeAgo(p.createdAt))",
                                    emoji: PostType(rawValue: p.type)?.emoji ?? "📦",
                                    lat: p.lat, lng: p.lng,
                                    color: ColorRGB(r: 0.83, g: 0.13, b: 0.24),
                                    authorId: p.authorId))
            }
        }
        if showPoints {
            for m in repo.markers {
                out.append(MapPoint(id: m.id, kind: "MARKER", type: m.kind, title: m.title,
                                    subtitle: m.subtitle, emoji: m.emoji,
                                    lat: m.lat, lng: m.lng,
                                    color: ColorRGB(r: 0.83, g: 0.13, b: 0.24), authorId: ""))
            }
        }
        return out
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Map(coordinateRegion: $region,
                showsUserLocation: true,
                annotationItems: annotations) { pt in
                MapAnnotation(coordinate: CLLocationCoordinate2D(latitude: pt.lat, longitude: pt.lng)) {
                    MapPinView(point: pt)
                }
            }
            .ignoresSafeArea(edges: .bottom)

            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    FilterChip(text: "Ogłoszenia", emoji: "📦", selected: showPosts) { showPosts.toggle() }
                    FilterChip(text: "Punkty pomocy", emoji: "🏫", selected: showPoints) { showPoints.toggle() }
                    Spacer()
                    Button {
                        withAnimation {
                            region = MKCoordinateRegion(
                                center: CLLocationCoordinate2D(latitude: repo.myLocation().0, longitude: repo.myLocation().1),
                                span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05))
                        }
                    } label: {
                        Image(systemName: "location.fill")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(WatahaColors.FlagRed)
                            .frame(width: 36, height: 36)
                            .background(Circle().fill(Color.white))
                            .shadow(radius: 3)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                .padding(.horizontal, 14)

                HStack {
                    Image(systemName: "info.circle").foregroundColor(WatahaColors.Grey).font(.system(size: 11))
                    Text("Legenda: 🔴 oddaję • 🟡 potrzebuję • 🟢 mogę pomóc • 🏫/⛪/🚒 punkty pomocy")
                        .font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                    Spacer()
                }
                .padding(10)
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                .shadow(color: WatahaColors.Ink.opacity(0.06), radius: 4, y: 2)
                .padding(.horizontal, 14)
                .padding(.bottom, 10)
            }
        }
    }
}

struct MapPinView: View {
    let point: MapPoint
    var body: some View {
        VStack(spacing: 2) {
            Text(point.emoji)
                .font(.system(size: 17))
                .padding(6)
                .background(Circle().fill(Color.white))
                .overlay(Circle().stroke(pinColor, lineWidth: 2.5))
                .shadow(color: .black.opacity(0.25), radius: 3, y: 1)
            Triangle().fill(pinColor).frame(width: 10, height: 7)
                .rotationEffect(.degrees(180))
        }
        .onTapGesture { }
    }
    private var pinColor: Color {
        switch point.type {
        case PostType.GIVE.rawValue: return WatahaColors.FlagRed
        case PostType.NEED.rawValue: return WatahaColors.Amber
        case PostType.OFFER.rawValue: return WatahaColors.Green
        default: return WatahaColors.DarkRed
        }
    }
}

struct Triangle: Shape {
    func path(in rect: CGRect) -> Path {
        var p = Path()
        p.move(to: CGPoint(x: rect.midX, y: rect.maxY))
        p.addLine(to: CGPoint(x: rect.minX, y: rect.minY))
        p.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
        p.closeSubpath()
        return p
    }
}
