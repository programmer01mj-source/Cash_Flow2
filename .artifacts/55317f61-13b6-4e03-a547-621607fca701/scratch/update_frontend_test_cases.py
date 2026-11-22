import docx

template_path = r'C:\Users\COMADMIN\Downloads\MICHAEL MAKAWA - Test Case Template.docx'
output_path = r'C:\Users\COMADMIN\AndroidStudioProjects\Cashflow2\Mobile_App_Test_Cases.docx'

doc = docx.Document(template_path)

# 1. Fill Project Information (Table 1)
proj_table = doc.tables[1]
proj_table.rows[0].cells[1].paragraphs[0].text = 'MICHAEL MAKAWA'
proj_table.rows[1].cells[1].paragraphs[0].text = 'BSC-COM-05-22'
proj_table.rows[2].cells[1].paragraphs[0].text = 'Cashflow (Frontend UI)'
proj_table.rows[3].cells[1].paragraphs[0].text = 'Android 14'
proj_table.rows[4].cells[1].paragraphs[0].text = '25th OCTOBER, 2026'
proj_table.rows[5].cells[1].paragraphs[0].text = 'v1.0'

# 2. Remove example test case tables and template test case table (Tables 2 to 9)
for table in doc.tables[2:10]:
    tbl_elem = table._element
    tbl_elem.getparent().remove(tbl_elem)

# 3. Add Feature-to-Test-Case Traceability Table for Frontend UI
doc.add_paragraph('Feature-to-Test-Case Traceability Matrix (Frontend UI)')
trace_table = doc.add_table(rows=10, cols=3)
trace_headers = ['Frontend Feature Name', 'UI Module', 'Associated Test Case IDs']
for c_idx, h in enumerate(trace_headers):
    trace_table.rows[0].cells[c_idx].paragraphs[0].text = h

trace_data = [
    ('Splash Screen UI', 'UI_SPLASH', 'UI_SPLASH_001, UI_SPLASH_002'),
    ('Login Screen & Validation UI', 'UI_LOGIN', 'UI_LOGIN_001, UI_LOGIN_002, UI_LOGIN_003, UI_LOGIN_004'),
    ('Signup Screen & Form UI', 'UI_SIGNUP', 'UI_SIGNUP_001, UI_SIGNUP_002, UI_SIGNUP_003'),
    ('Customer Home Dashboard UI', 'UI_HOME', 'UI_HOME_001, UI_HOME_002'),
    ('Loan Application & Document Upload UI', 'UI_LOAN_APPLY', 'UI_LOAN_APPLY_001, UI_LOAN_APPLY_002, UI_LOAN_APPLY_003'),
    ('Transactions & Payment Gateway UI', 'UI_PAYMENT', 'UI_PAYMENT_001, UI_PAYMENT_002, UI_PAYMENT_003'),
    ('Customer Profile & Logout UI', 'UI_PROFILE', 'UI_PROFILE_001, UI_PROFILE_002'),
    ('Loan Officer Portal UI', 'UI_OFFICER', 'UI_OFFICER_001'),
    ('Admin Dashboard UI', 'UI_ADMIN', 'UI_ADMIN_001')
]

for r_idx, (f_name, f_mod, f_tcs) in enumerate(trace_data, 1):
    trace_table.rows[r_idx].cells[0].paragraphs[0].text = f_name
    trace_table.rows[r_idx].cells[1].paragraphs[0].text = f_mod
    trace_table.rows[r_idx].cells[2].paragraphs[0].text = f_tcs

doc.add_paragraph()

# 4. Frontend-only test cases
test_cases_data = [
    (
        'UI_SPLASH_001',
        'Verify that the splash screen UI displays the company logo and app title correctly.',
        'Positive',
        'Low',
        'App is installed on target Android device.',
        'None',
        '1. Tap application icon to launch Cashflow.\n2. Observe launch screen UI elements.',
        'Cashflow logo and company branding text are rendered correctly in the center of the screen.',
        'Splash screen remains visible until transition timer triggers.'
    ),
    (
        'UI_SPLASH_002',
        'Verify that the splash screen automatically navigates to the Login screen after the animation delay.',
        'Positive',
        'Low',
        'App is launched.',
        'None',
        '1. Launch the application.\n2. Wait for approximately 2 seconds.',
        'App navigates automatically from the splash screen to the Login screen UI.',
        'Login screen is displayed to the user.'
    ),
    (
        'UI_LOGIN_001',
        'Verify that the Login screen UI renders email and password text fields, show/hide toggle, and login button correctly.',
        'Positive',
        'High',
        'App is on the Login screen.',
        'None',
        '1. View the Login screen layout.',
        'Email OutlinedTextField, Password OutlinedTextField with visibility icon, and Login button are displayed properly.',
        'Login screen UI is ready for user input.'
    ),
    (
        'UI_LOGIN_002',
        'Verify that entering an invalid email format shows an inline validation error message on the Login screen.',
        'Negative',
        'High',
        'App is on the Login screen.',
        'Email: invalidemail; Password: SecurePass123',
        '1. Type an invalid email in the Email field.\n2. Enter a valid password.\n3. Tap the "Login" button.',
        'Inline validation error text "Please enter a valid email address." is displayed above input fields.',
        'User remains on the Login screen without navigating.'
    ),
    (
        'UI_LOGIN_003',
        'Verify that entering a password shorter than 6 characters shows a validation error on the Login screen.',
        'Negative',
        'High',
        'App is on the Login screen.',
        'Email: alex.borrower@example.com; Password: 123',
        '1. Enter a valid email address.\n2. Type a short password (under 6 chars).\n3. Tap the "Login" button.',
        'Inline validation error text "Password must be at least 6 characters long." is displayed.',
        'User remains on the Login screen.'
    ),
    (
        'UI_LOGIN_004',
        'Verify that tapping the password visibility toggle icon correctly shows or hides password characters on the Login screen.',
        'Positive',
        'Medium',
        'App is on the Login screen.',
        'Password: SecurePass123',
        '1. Enter text in the Password field.\n2. Tap the trailing eye icon in the password field.\n3. Tap the eye icon again.',
        'Password text toggles between masked dots and visible plain text.',
        'Password visibility state updates correctly.'
    ),
    (
        'UI_SIGNUP_001',
        'Verify that the Signup screen UI correctly displays input fields for Full Name, Email, Phone Number, and Password.',
        'Positive',
        'High',
        'User is on the Signup screen.',
        'None',
        '1. Navigate from Login to Signup screen.\n2. Inspect form elements.',
        'OutlinedTextFields for Full Name, Email, Phone Number (+265...), and Password are displayed along with the Sign Up button.',
        'Signup form UI is fully rendered.'
    ),
    (
        'UI_SIGNUP_002',
        'Verify that leaving required fields empty or entering a short password on the Signup screen displays validation errors.',
        'Negative',
        'Medium',
        'User is on the Signup screen.',
        'Full Name: (empty); Email: (empty); Password: 123',
        '1. Leave fields empty or enter invalid values.\n2. Tap the "Sign Up" button.',
        'Validation error message is displayed at the top of the form; account creation dialog does not appear.',
        'User remains on the Signup screen.'
    ),
    (
        'UI_SIGNUP_003',
        'Verify that tapping the "Already have an Account? Login" button navigates back to the Login screen.',
        'Positive',
        'Medium',
        'User is on the Signup screen.',
        'None',
        '1. Tap the "Already have an Account? Login" text button.',
        'App pops back from the Signup screen to the Login screen UI.',
        'Login screen is displayed.'
    ),
    (
        'UI_HOME_001',
        'Verify that the Customer Home screen UI correctly renders active loan status, account balance, and next due date cards.',
        'Positive',
        'High',
        'Customer logged into Home tab.',
        'None',
        '1. Access the Home tab in the customer dashboard.',
        'Cards showing "Active Loan Status", "Current Balance: MWK 315,000.00", and next due date are displayed.',
        'Customer Home dashboard UI is rendered.'
    ),
    (
        'UI_HOME_002',
        'Verify that the bottom navigation tab bar allows switching between Home, Transactions, and Profile tabs.',
        'Positive',
        'High',
        'Customer logged into Home screen.',
        'None',
        '1. Tap "Transactions" tab in tab bar.\n2. Tap "Profile" tab.\n3. Tap "Home" tab.',
        'UI view switches dynamically to display Transaction content and Profile content respectively.',
        'Active tab state updates correctly.'
    ),
    (
        'UI_LOAN_APPLY_001',
        'Verify that tapping "Apply for New Loan" on the Home screen opens the Loan Application & Verification dialog with loan amount, tenure, and document upload buttons.',
        'Positive',
        'High',
        'Customer logged into Home tab.',
        'None',
        '1. Tap the "Apply for New Loan (Mandatory Docs Included)" button.',
        'AlertDialog titled "Loan Application & Verification" appears with Loan Amount, Tenure, and 3 mandatory document upload rows.',
        'Loan application dialog is displayed over the home screen.'
    ),
    (
        'UI_LOAN_APPLY_002',
        'Verify that clicking document upload buttons (Gov ID, Identity Selfie, Pay Slip) opens camera scan or file picker dialog options.',
        'Positive',
        'High',
        'Loan Application dialog is open.',
        'None',
        '1. Tap "Upload ID" button in the dialog.\n2. Observe sub-dialog options.',
        'A sub-dialog appears with options "Scan Document (Camera)" and "Upload from Device File System (Media Files)".',
        'Document selection method dialog is displayed.'
    ),
    (
        'UI_LOAN_APPLY_003',
        'Verify that the "Submit Application" button remains disabled with a warning message until all 3 mandatory documents are uploaded.',
        'Negative',
        'High',
        'Loan Application dialog is open with fewer than 3 documents uploaded.',
        'None',
        '1. Check Submit Application button state before uploading documents.',
        'Warning text "Please upload all 3 required documents to enable submission." is shown in red; Submit button is disabled.',
        'Application submission is prevented until compliance is met.'
    ),
    (
        'UI_PAYMENT_001',
        'Verify that the Transactions tab UI displays upcoming installments and payment history records correctly.',
        'Positive',
        'High',
        'Customer navigated to Transactions tab.',
        'None',
        '1. Tap the "Transactions" tab.',
        'Lists for "Upcoming Installments" and "Payment History" with amounts, due dates, references, and status are displayed.',
        'Transactions tab UI is rendered.'
    ),
    (
        'UI_PAYMENT_002',
        'Verify that tapping "Pay Now" on an installment opens the Malawian Payment Gateway dialog allowing selection of Banks or Mobile Money providers.',
        'Positive',
        'High',
        'Customer is on Transactions tab with upcoming installments.',
        'None',
        '1. Tap "Pay Now" on an installment card.\n2. Observe payment gateway dialog steps.',
        'Dialog titled "Loan Repayment Gateway (Malawi)" opens, allowing selection between Malawian Banks and Mobile Money.',
        'Payment gateway dialog is displayed.'
    ),
    (
        'UI_PAYMENT_003',
        'Verify that entering payment details and 4-digit PIN in the payment gateway dialog shows processing progress and success confirmation UI.',
        'Positive',
        'High',
        'Payment gateway dialog is at details input step.',
        'Phone/Account: +265991234567; PIN: 1234',
        '1. Select payment provider (e.g. TNM Mpamba).\n2. Enter mobile number and 4-digit PIN.\n3. Tap "Authorize Payment".',
        'Circular progress indicator appears with "Connecting to provider...", followed by success screen UI displaying "🎉 Payment Successful!".',
        'Payment transaction state updated in UI.'
    ),
    (
        'UI_PROFILE_001',
        'Verify that the Customer Profile tab UI displays user name, email, phone, and KYC verification status correctly.',
        'Positive',
        'High',
        'Customer navigated to Profile tab.',
        'None',
        '1. Tap the "Profile" tab in the tab bar.',
        'Customer Profile card displays Name, Email, Phone, and KYC Verification status.',
        'Profile UI rendered.'
    ),
    (
        'UI_PROFILE_002',
        'Verify that tapping "Logout" on the profile screen opens a confirmation dialog before returning to the Login screen.',
        'Positive',
        'High',
        'Customer is on Profile tab.',
        'None',
        '1. Tap "Logout" button on Profile tab.\n2. In confirmation dialog, tap "Logout".',
        'Confirmation dialog "Confirm Logout" appears; confirming navigates the UI back to the Login screen.',
        'User session ended and Login screen displayed.'
    ),
    (
        'UI_OFFICER_001',
        'Verify that the Loan Officer Portal UI displays pending loan applications and active borrowers tabs with review options.',
        'Positive',
        'Medium',
        'Loan Officer logged in.',
        'None',
        '1. Launch Loan Officer Dashboard.',
        'Top bar "Loan Officer Portal" appears with tabs for Pending Loans and Active Borrowers, showing borrower balance, tenure, and status.',
        'Loan officer portal UI rendered.'
    ),
    (
        'UI_ADMIN_001',
        'Verify that the Admin Dashboard UI correctly renders system statistics, Non-Performing Loans (NPAs), and system user management lists.',
        'Positive',
        'Medium',
        'Admin logged in.',
        'None',
        '1. Launch Admin Dashboard.',
        'Admin dashboard renders Non-Performing Loans statistics (NPAs: 2), system user roles, and automated disbursement controls.',
        'Admin dashboard UI rendered.'
    )
]

for idx, data in enumerate(test_cases_data, 1):
    doc.add_paragraph(f'Test Case {idx}: {data[0]} - {data[1][:40]}...')
    table = doc.add_table(rows=10, cols=2)
    rows_content = [
        ('Test Case ID', data[0]),
        ('Description', data[1]),
        ('Test Type', data[2]),
        ('Priority', data[3]),
        ('Prerequisites', data[4]),
        ('Test Data', data[5]),
        ('Test Steps', data[6]),
        ('Expected Result', data[7]),
        ('Post-conditions', data[8]),
        ('Status', '')
    ]
    for r_idx, (field, val) in enumerate(rows_content):
        table.rows[r_idx].cells[0].paragraphs[0].text = field
        table.rows[r_idx].cells[1].paragraphs[0].text = val
    doc.add_paragraph()

doc.save(output_path)
print('Successfully updated Mobile_App_Test_Cases.docx with frontend-only test cases at:', output_path)
