// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "PolskaWatahaCore",
    products: [
        .library(name: "PolskaWatahaCore", targets: ["PolskaWatahaCore"])
    ],
    targets: [
        .target(name: "PolskaWatahaCore", path: "Sources/PolskaWatahaCore"),
        .testTarget(name: "PolskaWatahaCoreTests", dependencies: ["PolskaWatahaCore"])
    ]
)
