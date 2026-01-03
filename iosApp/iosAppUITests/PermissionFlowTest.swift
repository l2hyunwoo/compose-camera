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
    }

    override func tearDown() {
        app = nil
    }

    /**
     * Test: When camera permission is granted, camera screen should be displayed.
     */
    func testPermissionGranted_showsCameraScreen() {
        // Delete app to reset permissions (manual step or use reset in Xcode scheme)
        app.launch()

        // Handle permission alert
        let allowButton = springboard.buttons["Allow"]
        if allowButton.waitForExistence(timeout: 5) {
            allowButton.tap()
        }

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
        // Delete app to reset permissions
        app.launch()

        // Handle permission alert - deny
        let dontAllowButton = springboard.buttons["Don't Allow"]
        if dontAllowButton.waitForExistence(timeout: 5) {
            dontAllowButton.tap()
        }

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
        // Ensure we're in denied state
        app.launch()

        // Deny permission if dialog appears
        let dontAllowButton = springboard.buttons["Don't Allow"]
        if dontAllowButton.waitForExistence(timeout: 5) {
            dontAllowButton.tap()
        }

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
}
