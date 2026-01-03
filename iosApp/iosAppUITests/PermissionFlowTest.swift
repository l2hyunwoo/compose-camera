/*
 * Copyright (C) 2025 l2hyunwoo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import XCTest

/**
 * E2E tests for camera permission flow on iOS.
 *
 * These tests verify:
 * 1. Permission grant → Camera screen displayed
 * 2. Permission deny → Settings button displayed
 * 3. Settings button opens app settings
 *
 * Prerequisites:
 * - Run on real iOS device (Simulator has no camera)
 * - Delete the app before running to reset permissions
 *
 * Setup:
 * 1. In Xcode, go to File > New > Target...
 * 2. Select "UI Testing Bundle"
 * 3. Name it "iosAppUITests"
 * 4. Copy this file to the new target directory
 */
class PermissionFlowTest: XCTestCase {

    var app: XCUIApplication!
    let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")

    override func setUp() {
        continueAfterFailure = false
        app = XCUIApplication()
        // deleteApp() // Reset permissions by deleting the app
    }

    override func tearDown() {
        app = nil
    }

    /**
     * Test: When camera permission is granted, camera screen should be displayed.
     */
    func testPermissionGranted_showsCameraScreen() {
        // Monitor for permission dialogs
        addUIInterruptionMonitor(withDescription: "Camera Permission") { (alert) -> Bool in
            let allowButtons = ["Allow", "허용", "확인", "OK"]
            for label in allowButtons {
                if alert.buttons[label].exists {
                    alert.buttons[label].tap()
                    return true
                }
            }
            return false
        }

        app.launch()
        
        // Interact with app to trigger interruption handler
        app.tap()

        // Verify camera screen is displayed (flash button indicates camera UI)
        let flashButton = app.buttons["flash_button"]
        XCTAssertTrue(
            flashButton.waitForExistence(timeout: 10),
            "Camera screen should be displayed after permission grant"
        )
    }

    /**
     * Test: When camera permission is denied, settings button should be displayed.
     */
    func testPermissionDenied_showsSettingsButton() {
        // Monitor for permission dialogs - Deny
        addUIInterruptionMonitor(withDescription: "Camera Permission Deny") { (alert) -> Bool in
            let denyButtons = ["Don't Allow", "허용 안 함"]
            for label in denyButtons {
                if alert.buttons[label].exists {
                    alert.buttons[label].tap()
                    return true
                }
            }
            return false
        }

        app.launch()
        
        // Interact to trigger handler
        app.tap()

        // Verify settings button is displayed
        let settingsButton = app.buttons["permission_settings_button"]
        XCTAssertTrue(
            settingsButton.waitForExistence(timeout: 10),
            "Settings button should be displayed after permission denial"
        )
    }

    /**
     * Test: Clicking settings button opens app settings.
     */
    func testSettingsButton_opensAppSettings() {
        // Monitor for permission dialogs - Deny
        addUIInterruptionMonitor(withDescription: "Camera Permission Deny") { (alert) -> Bool in
            let denyButtons = ["Don't Allow", "허용 안 함"]
            for label in denyButtons {
                if alert.buttons[label].exists {
                    alert.buttons[label].tap()
                    return true
                }
            }
            return false
        }

        app.launch()
        
        // Interact to trigger handler
        app.tap()

        // Click settings button
        let settingsButton = app.buttons["permission_settings_button"]
        if settingsButton.waitForExistence(timeout: 10) {
            settingsButton.tap()
        }

        // Verify Settings app is opened
        let settingsApp = XCUIApplication(bundleIdentifier: "com.apple.Preferences")
        XCTAssertTrue(
            settingsApp.wait(for: .runningForeground, timeout: 10),
            "Settings app should open when settings button is clicked"
        )
    }

    private func deleteApp() {
        app.terminate()
        
        // Go to home screen
        XCUIDevice.shared.press(.home)
        
        // Use Spotlight to find the app (more reliable than swiping pages)
        springboard.swipeDown()
        
        let searchField = springboard.searchFields.firstMatch
        if searchField.waitForExistence(timeout: 3) {
            searchField.typeText("iosApp")
            
            let icon = springboard.icons["iosApp"]
            if icon.waitForExistence(timeout: 3) {
                icon.press(forDuration: 2)
                
                // Handle deletion flow
                // 1. "Remove App" / "앱 제거"
                if springboard.buttons["Remove App"].waitForExistence(timeout: 3) {
                    springboard.buttons["Remove App"].tap()
                } else if springboard.buttons["앱 제거"].waitForExistence(timeout: 3) {
                    springboard.buttons["앱 제거"].tap()
                }

                // 2. "Delete App" / "앱 삭제"
                if springboard.alerts.buttons["Delete App"].waitForExistence(timeout: 3) {
                    springboard.alerts.buttons["Delete App"].tap()
                } else if springboard.alerts.buttons["앱 삭제"].waitForExistence(timeout: 3) {
                    springboard.alerts.buttons["앱 삭제"].tap()
                }

                // 3. "Delete" / "삭제"
                if springboard.alerts.buttons["Delete"].waitForExistence(timeout: 3) {
                    springboard.alerts.buttons["Delete"].tap()
                } else if springboard.alerts.buttons["삭제"].waitForExistence(timeout: 3) {
                    springboard.alerts.buttons["삭제"].tap()
                }
            } else {
                // Cancel search if icon not found
                if springboard.buttons["Cancel"].exists { springboard.buttons["Cancel"].tap() }
            }
        }
    }
}
