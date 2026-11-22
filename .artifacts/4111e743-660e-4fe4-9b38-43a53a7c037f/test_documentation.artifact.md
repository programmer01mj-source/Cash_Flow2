# Cashflow App - Test Documentation & Test Plan

This document outlines the automated and manual test cases for the **Cashflow** Android application across all key components and features.

---

## 1. Automated Test Documentation

Automated instrumented UI tests are implemented using Jetpack Compose UI Testing (`androidx.compose.ui.test.junit4`) and JUnit4. They verify UI component rendering, user interactions, and navigation flows.

### Test Suite Summary

| Test Class | Target Component | Description |
| :--- | :--- | :--- |
| `SplashScreenTest` | `SplashScreen` | Verifies logo and company name ("Cashflow") visibility and splash completion callback. |
| `LoginScreenTest` | `Login` | Verifies login form inputs (username, password), labels, and login action callback. |
| `SignupScreenTest` | `Signup` | Verifies registration form fields (First Name, Last Name, Email, Phone, Password) and signup action. |
| `HomeScreenTest` | `Home` | Verifies main dashboard tab navigation ("Home", "Transactions", "Profile") and tab content switching. |
| `NavigationFlowTest` | `CashflowApp` | Verifies full app navigation flow from splash screen -> login screen -> sign-up screen. |

---

## 2. Manual Test Cases

The following manual test cases cover scenarios that complement automated tests, including edge cases, visual fidelity, system responsiveness, and error states.

### Test Case 1: Cold Boot & Splash Screen Transition
- **Objective**: Verify cold-launch splash screen appearance and smooth transition to Login.
- **Steps**:
  1. Force-close the Cashflow app if running.
  2. Tap the Cashflow app icon on the device launcher.
  3. Observe the system splash screen (`Theme.App.Starting`) followed by the in-app animated Splash screen.
  4. Wait for 2 seconds.
- **Expected Result**:
  - Logo is centered with the company name ("Cashflow") directly underneath.
  - Automatically transitions to the Login screen without flicker.

### Test Case 2: User Authentication Flow (Login)
- **Objective**: Verify login functionality with username and password.
- **Steps**:
  1. Launch the app and reach the Login screen.
  2. Tap the **Username** field and enter `testuser`.
  3. Tap the **Password** field and enter `securepassword`.
  4. Tap the **Login** button.
- **Expected Result**:
  - Input text masks correctly for password.
  - Tapping Login triggers success flow and navigates to the Home dashboard.

### Test Case 3: Registration Flow (Sign Up)
- **Objective**: Verify new account registration screen and navigation.
- **Steps**:
  1. From the Login screen, tap **"Don't have an account? Sign up"**.
  2. Fill in First Name, Last Name, Email, Phone Number, and Password.
  3. Tap **Sign Up**.
  4. Tap **"Already have an Account? Login"**.
- **Expected Result**:
  - Signup fields accept valid user data.
  - Successfully returns back to the Login screen upon tapping login link.

### Test Case 4: Dashboard Tab Navigation & Logout
- **Objective**: Verify tab switching and logout capability in Home dashboard.
- **Steps**:
  1. Log into the app to reach the Home screen.
  2. Tap the **Transactions** tab.
  3. Tap the **Profile** tab.
  4. Tap **Logout** from profile.
- **Expected Result**:
  - Each tab correctly displays its corresponding content (`Home content`, `Transaction content`, `Profile content`).
  - Logout returns the user securely to the Login screen and clears navigation backstack.

### Test Case 5: Dark Mode & Theme Adaptation
- **Objective**: Verify UI readability and styling in Dark Mode and Light Mode.
- **Steps**:
  1. Switch device system settings between Light Theme and Dark Theme.
  2. Open the app and navigate through Splash, Login, Signup, and Home screens.
- **Expected Result**:
  - Colors, typography, and borders (such as logo borders and input fields) adapt correctly to dark/light themes without contrast or legibility issues.
