import Foundation
import UserNotifications

/// Kanały powiadomień — odpowiednik NotificationChannels z Androida.
enum NotifChannel: String {
    case crisis = "wataha_kryzys"
    case chat = "wataha_chat"
    case exchange = "wataha_wymiany"
    case local = "wataha_lokalne"
}

enum NotificationHelper {
    static func requestAuth() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound, .criticalAlert]) { _, _ in }
    }

    static func show(channel: NotifChannel, id: Int, title: String, body: String, fullScreen: Bool = false) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = channel == .crisis ? .defaultCritical : .default
        content.userInfo = ["channel": channel.rawValue]
        if fullScreen { content.categoryIdentifier = "CRISIS_FULLSCREEN" }

        let trig = UNTimeIntervalNotificationTrigger(timeInterval: 0.5, repeats: false)
        let req = UNNotificationRequest(identifier: "\(channel.rawValue)-\(id)", content: content, trigger: trig)
        UNUserNotificationCenter.current().add(req)
    }
}
