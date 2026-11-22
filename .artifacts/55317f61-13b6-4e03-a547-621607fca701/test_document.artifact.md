# TEST CASE TEMPLATE
## Mobile Applications Development Project
### 4th Year – Test Design Submission

---

## 1. Project Information

| Field | Details |
| :--- | :--- |
| **Student Name** | Michael Makawa |
| **Student ID** | 2024-CS-042 |
| **Application Name** | Cashflow Mobile (Cashflow2) |
| **Target Platform** | Android 14 / Android Studio |
| **Date** | 26/02/2025 |
| **Template Version** | v1.0 |

---

## 2. Test Cases

### Module: Authentication & Registration (AUTH)

#### Test Case 1: Successful Customer Login
- **Test Case ID:** `AUTH_LOGIN_001`
- **Description:** Verify that a registered customer can successfully log in using a valid email address and password.
- **Test Type:** Positive
- **Priority:** High
- **Prerequisites:** App installed and launched; customer account registered in the system.
- **Test Data:** Email: `alex.borrower@example.com`; Password: `SecurePass123`
- **Test Steps:**
  1. Launch the application to the Login screen.
  2. Enter a valid registered email address.
  3. Enter a valid password (at least 6 characters).
  4. Tap the "Login" button.
- **Expected Result:** User is successfully authenticated and navigated to the Customer Home dashboard; active loan status and account balance are displayed.
- **Post-conditions:** A user session token is generated, and the customer remains logged in.
- **Status:**

---

#### Test Case 2: Login with Incorrect Password
- **Test Case ID:** `AUTH_LOGIN_002`
- **Description:** Verify that login fails and an error message is shown when a registered email is entered with an incorrect password.
- **Test Type:** Negative
- **Priority:** High
- **Prerequisites:** App installed; customer account registered in the system.
- **Test Data:** Email: `alex.borrower@example.com`; Password: `WrongPassword123`
- **Test Steps:**
  1. Launch the application to the Login screen.
  2. Enter a valid registered email address.
  3. Enter an incorrect password.
  4. Tap the "Login" button.
- **Expected Result:** Error message indicating login failure is displayed; user remains on the Login screen.
- **Post-conditions:** No session token is created, and the password field can be retried.
- **Status:**

---

#### Test Case 3: Login with Unregistered Email
- **Test Case ID:** `AUTH_LOGIN_003`
- **Description:** Verify that login is blocked and an appropriate error is displayed when entering an email address not registered in the system.
- **Test Type:** Negative
- **Priority:** High
- **Prerequisites:** App installed on device.
- **Test Data:** Email: `unregistered@example.com`; Password: `SecurePass123`
- **Test Steps:**
  1. Launch the application to the Login screen.
  2. Enter an unregistered email address.
  3. Enter any password.
  4. Tap the "Login" button.
- **Expected Result:** Authentication fails, and an error message is shown; user remains on the Login screen.
- **Post-conditions:** No user session is created.
- **Status:**

---

#### Test Case 4: Login with Invalid Email Format
- **Test Case ID:** `AUTH_LOGIN_004`
- **Description:** Verify that login is prevented when the email address does not conform to a valid email format.
- **Test Type:** Negative
- **Priority:** Medium
- **Prerequisites:** App installed on device.
- **Test Data:** Email: `invalid-email-format`; Password: `SecurePass123`
- **Test Steps:**
  1. Launch the application to the Login screen.
  2. Enter an invalid email format without '@' or domain.
  3. Enter a valid password.
  4. Tap the "Login" button.
- **Expected Result:** Inline validation error "Please enter a valid email address." is displayed; login request is not sent.
- **Post-conditions:** User remains on the Login screen.
- **Status:**

---

#### Test Case 5: Successful User Registration
- **Test Case ID:** `AUTH_SIGNUP_001`
- **Description:** Verify that a new user can successfully register an account by providing valid full name, email, phone number, and password.
- **Test Type:** Positive
- **Priority:** High
- **Prerequisites:** App installed; navigation to the Signup screen from Login.
- **Test Data:** Full Name: `Chikondi Banda`, Email: `chikondi@example.com`, Phone: `+265991234567`, Password: `NewSecurePass123`
- **Test Steps:**
  1. From the Login screen, tap "Don't have an account? Sign up".
  2. Enter Full Name, Email, Phone Number (+265...), and Password.
  3. Tap the "Sign Up" button.
- **Expected Result:** Registration success dialog appears stating "Your account has been created successfully. Please login with your credentials."; tapping "OK" returns to the Login screen.
- **Post-conditions:** User account data is recorded, ready for login.
- **Status:**

---

#### Test Case 6: Registration with Password Less Than 6 Characters
- **Test Case ID:** `AUTH_SIGNUP_002`
- **Description:** Verify that account registration is blocked when the entered password is shorter than 6 characters.
- **Test Type:** Negative
- **Priority:** Medium
- **Prerequisites:** App on the Signup screen.
- **Test Data:** Full Name: `Test User`, Email: `test@example.com`, Phone: `+265991112223`, Password: `123`
- **Test Steps:**
  1. Navigate to the Signup screen.
  2. Fill in valid full name, email, and phone number.
  3. Enter a password with fewer than 6 characters.
  4. Tap the "Sign Up" button.
- **Expected Result:** Validation error message "Password must be at least 6 characters." is displayed; account creation does not proceed.
- **Post-conditions:** User remains on the Signup screen.
- **Status:**

---

### Module: Loan Application & Verification (LOAN)

#### Test Case 7: Successful Loan Application with All Mandatory Documents Uploaded
- **Test Case ID:** `LOAN_APPLY_001`
- **Description:** Verify that a customer can successfully submit a new loan application after uploading all 3 mandatory verification documents (Gov ID, Selfie, Pay Slip).
- **Test Type:** Positive
- **Priority:** High
- **Prerequisites:** Customer logged into Home tab; loan application dialog opened.
- **Test Data:** Loan Amount: `500000`, Tenure: `12` months; Uploaded documents: Gov ID, Selfie, Pay Slip.
- **Test Steps:**
  1. From the Customer Home dashboard, tap "Apply for New Loan".
  2. Enter Loan Amount and Tenure.
  3. Tap "Upload ID" and complete document selection/scan.
  4. Tap "Upload Selfie" and complete image selection/scan.
  5. Tap "Upload Pay Slip" and complete document selection/scan.
  6. Tap "Submit Application".
- **Expected Result:** All three document buttons show "Uploaded ✓", enabling the Submit button; loan application is successfully submitted, and dialog closes.
- **Post-conditions:** Loan application is recorded in the system.
- **Status:**

---

#### Test Case 8: Loan Application Blocked When Mandatory Documents Are Missing
- **Test Case ID:** `LOAN_APPLY_002`
- **Description:** Verify that loan application submission is blocked and warning text is shown when one or more mandatory verification documents have not been uploaded.
- **Test Type:** Negative
- **Priority:** High
- **Prerequisites:** Customer logged into Home tab; loan application dialog opened with 0 or 1 document uploaded.
- **Test Data:** Loan Amount: `500000`, Tenure: `12`; Documents uploaded: None.
- **Test Steps:**
  1. Open the Loan Application dialog from the Home tab.
  2. Enter loan amount and tenure.
  3. Leave mandatory verification documents unuploaded.
  4. Observe the "Submit Application" button and warning message.
- **Expected Result:** Warning message "Please upload all 3 required documents to enable submission." is displayed; the "Submit Application" button remains disabled.
- **Post-conditions:** No loan application is submitted.
- **Status:**

---

#### Test Case 9: Successful Loan Repayment via Mobile Money
- **Test Case ID:** `LOAN_PAY_001`
- **Description:** Verify that a customer can successfully pay an upcoming loan installment using the Malawian Mobile Money gateway.
- **Test Type:** Positive
- **Priority:** High
- **Prerequisites:** Customer logged in; navigate to Transactions tab with pending installments available.
- **Test Data:** Selected Provider: `TNM Mpamba`; Mobile Number: `+265991234567`; PIN: `1234`
- **Test Steps:**
  1. Navigate to the "Transactions" tab.
  2. Tap "Pay Now" on an active upcoming installment.
  3. Select "Mobile Money (TNM Mpamba / Airtel Money)".
  4. Select mobile money provider (e.g., TNM Mpamba).
  5. Enter mobile phone number and 4-digit PIN.
  6. Tap "Authorize Payment" and wait for processing completion.
  7. Tap "Done".
- **Expected Result:** Payment gateway simulates processing, displays "🎉 Payment Successful!", credits the loan account, moves the transaction to Payment History, and removes the paid installment from upcoming list.
- **Post-conditions:** Installment status is updated to Paid, and a new payment record appears in history.
- **Status:**

---

#### Test Case 10: Loan Repayment Blocked Due to Missing Details or Short PIN
- **Test Case ID:** `LOAN_PAY_002`
- **Description:** Verify that loan repayment authorization is blocked when required account/phone details are missing or the 4-digit PIN is incomplete.
- **Test Type:** Negative
- **Priority:** High
- **Prerequisites:** Payment gateway dialog opened for an installment.
- **Test Data:** Phone/Account: `(blank)`; PIN: `12`
- **Test Steps:**
  1. Open payment gateway dialog for an installment.
  2. Select payment method and provider.
  3. Leave account/phone number blank or enter a PIN with fewer than 4 digits.
  4. Tap "Authorize Payment".
- **Expected Result:** Error message "Please enter valid account/phone and 4-digit PIN." is displayed; payment processing is not initiated.
- **Post-conditions:** User remains on the payment details input step.
- **Status:**

---

### Module: Profile & Session Management (PROFILE)

#### Test Case 11: View Customer Profile and KYC Status
- **Test Case ID:** `PROFILE_VIEW_001`
- **Description:** Verify that a logged-in customer can view their personal details, contact information, and KYC verification status under the Profile tab.
- **Test Type:** Positive
- **Priority:** High
- **Prerequisites:** Customer successfully authenticated and logged in.
- **Test Data:** Customer Account: Alex Borrower (`alex.borrower@example.com`)
- **Test Steps:**
  1. Tap the "Profile" tab in the bottom tab bar.
  2. View the Customer Profile card.
- **Expected Result:** Customer Name, Email, Phone Number, and KYC Verification status ("Verified" or "Pending Documents") are correctly displayed.
- **Post-conditions:** User profile details are displayed.
- **Status:**

---

#### Test Case 12: Successful User Logout
- **Test Case ID:** `PROFILE_LOGOUT_001`
- **Description:** Verify that a user can successfully log out of their account via confirmation dialog from the Profile tab.
- **Test Type:** Positive
- **Priority:** High
- **Prerequisites:** User logged into the application.
- **Test Data:** None
- **Test Steps:**
  1. Navigate to the Profile tab.
  2. Tap the "Logout" button.
  3. In the confirmation dialog ("Confirm Logout"), tap "Logout".
- **Expected Result:** User session is terminated, and navigation returns to the Login screen.
- **Post-conditions:** User session cleared; app returns to Login state.
- **Status:**

---

### Module: Dashboards & Review (DASHBOARD)

#### Test Case 13: Loan Officer Review and Status Update for Pending Application
- **Test Case ID:** `DASHBOARD_OFFICER_001`
- **Description:** Verify that a Loan Officer can view pending loan applications and update an application status (e.g., Disbursed, Paid, Defaulted).
- **Test Type:** Positive
- **Priority:** High
- **Prerequisites:** Loan Officer logged into the Loan Officer Portal.
- **Test Data:** Application ID: `1`, Borrower Name: `Chikondi Banda`, New Status: `Disbursed`
- **Test Steps:**
  1. Log in as a Loan Officer (`officer@loan.com`).
  2. View the "Pending Loans" list.
  3. Tap "Review" on an application.
  4. In the update status dialog, select a new status (e.g., Disbursed) and tap update.
- **Expected Result:** Application status updates correctly in the dashboard list (e.g., status changes to Disbursed).
- **Post-conditions:** Loan application status is updated in the system.
- **Status:**

---

#### Test Case 14: Admin View System Users and NPA Statistics
- **Test Case ID:** `DASHBOARD_ADMIN_001`
- **Description:** Verify that an Admin user can view system statistics such as Non-Performing Loans (NPAs) and system users.
- **Test Type:** Positive
- **Priority:** Medium
- **Prerequisites:** Admin user logged into Admin Dashboard.
- **Test Data:** None
- **Test Steps:**
  1. Log in as Admin (`admin@cashflow.com`).
  2. View the Admin Dashboard overview.
- **Expected Result:** Non-Performing Loans statistics (e.g., NPAs: 2) and system user list are displayed.
- **Post-conditions:** Admin statistics displayed.
- **Status:**
