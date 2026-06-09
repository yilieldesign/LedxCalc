import SwiftUI
import Shared

@main
struct LedxCalcApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView(controller: MainViewControllerKt.MainViewController())
                .ignoresSafeArea()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let controller: UIViewController

    func makeUIViewController(context: Context) -> UIViewController {
        controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
