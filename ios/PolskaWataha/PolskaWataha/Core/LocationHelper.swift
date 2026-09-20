import Foundation
import CoreLocation

/// Lokalizacja: prośba o zgodę tylko kontekstowo (przy dodaniu ogłoszenia / sygnale kryzysowym).
final class LocationHelper: NSObject, ObservableObject, CLLocationManagerDelegate {
    static let shared = LocationHelper()
    private let lm = CLLocationManager()
    @Published var last: (lat: Double, lng: Double)?

    override init() {
        super.init()
        lm.delegate = self
        lm.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    /// Kontekstowo: pyta o zgodę, gdy użytkownik faktycznie chce użyć lokalizacji.
    func requestOnce() {
        switch lm.authorizationStatus {
        case .notDetermined:
            lm.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways:
            lm.requestLocation()
        case .denied, .restricted:
            break
        @unknown default:
            break
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
            manager.requestLocation()
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let loc = locations.last {
            last = (lat: loc.coordinate.latitude, lng: loc.coordinate.longitude)
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {}
}
