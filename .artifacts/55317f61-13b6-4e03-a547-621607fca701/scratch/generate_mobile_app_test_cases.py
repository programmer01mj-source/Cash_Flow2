import docx

template_path = r'C:\Users\COMADMIN\Downloads\MICHAEL MAKAWA - Test Case Template.docx'
output_path = r'C:\Users\COMADMIN\AndroidStudioProjects\Cashflow2\Mobile_App_Test_Cases.docx'

doc = docx.Document(template_path)

# 1. Fill Project Information (Table 1)
proj_table = doc.tables[1]
proj_table.rows[0].cells[1].paragraphs[0].text = 'MICHAEL MAKAWA'
proj_table.rows[1].cells[1].paragraphs[0].text = 'BSC-COM-05-22'
proj_table.rows[2].cells[1].paragraphs[0].text = 'Cashflow'
proj_table.rows[3].cells[1].paragraphs[0].text = 'Android 14'
proj_table.rows[4].cells[1].paragraphs[0].text = '25th OCTOBER, 2026'
proj_table.rows[5].cells[1].paragraphs[0].text = 'v1.0'

# 2. Remove example test case tables and template test case table (Tables 2 to 9)
for table in doc.tables[2:10]:
    tbl_elem = table._element
    tbl_elem.getparent().remove(tbl_elem)

# Add Feature-to-Test-Case Traceability Table right before test cases
doc.add_paragraph('Feature-to-Test-Case Traceability Matrix')
trace_table = doc.add_table(rows=9, cols=3)
trace_headers = ['Feature Name', 'Module', 'Associated Test Case IDs']
for c_idx, h in enumerate(trace_headers):
    trace_table.rows[0].cells[c_idx].paragraphs[0].text = h

trace_data = [
    ('User Authentication (Login)', 'AUTH', 'AUTH_LOGIN_001, AUTH_LOGIN_002, AUTH_LOGIN_003, AUTH_LOGIN_004'),
    ('User Registration (Signup)', 'AUTH', 'AUTH_SIGNUP_001, AUTH_SIGNUP_002'),
    ('Loan Application & Document Verification', 'LOAN', 'LOAN_APPLY_001, LOAN_APPLY_002'),
    ('Loan Repayment (Mobile Money / Bank)', 'LOAN', 'LOAN_PAY_001, LOAN_PAY_002'),
    ('Customer Profile & KYC Status', 'PROFILE', 'PROFILE_VIEW_001, PROFILE_LOGOUT_001'),
    ('Loan Officer Review & Status Management', 'OFFICER', 'OFFICER_REVIEW_001, OFFICER_REVIEW_002'),
    ('Admin Dashboard & NPA Monitoring', 'ADMIN', 'ADMIN_STATS_001, ADMIN_STATS_002'),
    ('Splash Screen & App Initialization', 'SPLASH', 'SPLASH_INIT_001, SPLASH_INIT_002')
]

for r_idx, (f_name, f_mod, f_tcs) in enumerate(trace_data, 1):
    trace_table.rows[r_idx].cells[0].paragraphs[0].text = f_name
    trace_table.rows[r_idx].cells[1].paragraphs[0].text = f_mod
    trace_table.rows[r_idx].cells[2].paragraphs[0].text = f_tcs

doc.add_paragraph()

# 3. Define comprehensive test cases covering all verified project features
test_cases_data = [
    # 1. Splash Screen
    (
        'SPLASH_INIT_001',
        'Verify that the splash screen successfully loads and displays the Cashflow logo and company name upon app launch.',
        'Positive',
        'Low',
        'App is installed on target Android device.',
        'None',
        '1. Tap application icon to launch Cashflow.\n2. Observe launch screen contents.',
        'Cashflow logo and company branding are displayed correctly before navigating to Login.',
        'App automatically transitions to the Login screen after timeout.'
    ),
    (
        'SPLASH_INIT_002',
        'Verify that the splash screen handles rapid background interruptions without crashing during initialization.',
        'Boundary',
        'Low',
        'App is launching.',
        'Device back press or home press during splash animation.',
        '1. Launch the app.\n2. Immediately press the device Home button during splash display.\n3. Re-open the app from recent tasks.',
        'App resumes smoothly without freezing or crashing.',
        'App is in a stable state at Login or Splash screen.'
    ),
    # 2. Authentication (Login)
    (
        'AUTH_LOGIN_001',
        'Verify that a registered customer can successfully log in using a valid email address and password.',
        'Positive',
        'High',
        'App installed and launched; customer account registered in the system.',
        'Email: alex.borrower@example.com; Password: SecurePass123',
        '1. Launch the application to the Login screen.\n2. Enter a valid registered email address.\n3. Enter a valid password (at least 6 characters).\n4. Tap the "Login" button.',
        'User is successfully authenticated and navigated to the Customer Home dashboard; active loan status and account balance are displayed.',
        'A user session token is generated, and the customer remains logged in.'
    ),
    (
        'AUTH_LOGIN_002',
        'Verify that login fails and an error message is shown when a registered email is entered with an incorrect password.',
        'Negative',
        'High',
        'App installed; customer account registered in the system.',
        'Email: alex.borrower@example.com; Password: WrongPassword123',
        '1. Launch the application to the Login screen.\n2. Enter a valid registered email address.\n3. Enter an incorrect password.\n4. Tap the "Login" button.',
        'Error message indicating login failure is displayed; user remains on the Login screen.',
        'No session token is created, and the password field can be retried.'
    ),
    (
        'AUTH_LOGIN_003',
        'Verify that login is blocked and an appropriate error is displayed when entering an email address not registered in the system.',
        'Negative',
        'High',
        'App installed on device.',
        'Email: unregistered@example.com; Password: SecurePass123',
        '1. Launch the application to the Login screen.\n2. Enter an unregistered email address.\n3. Enter any password.\n4. Tap the "Login" button.',
        'Authentication fails, and an error message is shown; user remains on the Login screen.',
        'No user session is created.'
    ),
    (
        'AUTH_LOGIN_004',
        'Verify that login is prevented when the email address does not conform to a valid email format.',
        'Negative',
        'Medium',
        'App installed on device.',
        'Email: invalid-email-format; Password: SecurePass123',
        '1. Launch the application to the Login screen.\n2. Enter an invalid email format without "@" or domain.\n3. Enter a valid password.\n4. Tap the "Login" button.',
        'Inline validation error "Please enter a valid email address." is displayed; login request is not sent.',
        'User remains on the Login screen.'
    ),
    # 3. User Registration (Signup)
    (
        'AUTH_SIGNUP_001',
        'Verify that a new user can successfully register an account by providing valid full name, email, phone number, and password.',
        'Positive',
        'High',
        'App installed; navigation to the Signup screen from Login.',
        'Full Name: Chikondi Banda, Email: chikondi@example.com, Phone: +265991234567, Password: NewSecurePass123',
        '1. From the Login screen, tap "Don\'t have an account? Sign up".\n2. Enter Full Name, Email, Phone Number (+265...), and Password.\n3. Tap the "Sign Up" button.',
        'Registration success dialog appears stating "Your account has been created successfully. Please login with your credentials."; tapping "OK" returns to the Login screen.',
        'User account data is recorded, ready for login.'
    ),
    (
        'AUTH_SIGNUP_002',
        'Verify that account registration is blocked when the entered password is shorter than 6 characters.',
        'Negative',
        'Medium',
        'App on the Signup screen.',
        'Full Name: Test User, Email: test@example.com, Phone: +265991112223, Password: 123',
        '1. Navigate to the Signup screen.\n2. Fill in valid full name, email, and phone number.\n3. Enter a password with fewer than 6 characters.\n4. Tap the "Sign Up" button.',
        'Validation error message "Password must be at least 6 characters." is displayed; account creation does not proceed.',
        'User remains on the Signup screen.'
    ),
    # 4. Loan Application & Document Verification
    (
        'LOAN_APPLY_001',
        'Verify that a customer can successfully submit a new loan application after uploading all 3 mandatory verification documents (Gov ID, Selfie, Pay Slip).',
        'Positive',
        'High',
        'Customer logged into Home tab; loan application dialog opened.',
        'Loan Amount: 500000, Tenure: 12 months; Uploaded documents: Gov ID, Selfie, Pay Slip.',
        '1. From the Customer Home dashboard, tap "Apply for New Loan".\n2. Enter Loan Amount and Tenure.\n3. Tap "Upload ID" and complete document selection/scan.\n4. Tap "Upload Selfie" and complete image selection/scan.\n5. Tap "Upload Pay Slip" and complete document selection/scan.\n6. Tap "Submit Application".',
        'All three document buttons show "Uploaded ✓", enabling the Submit button; loan application is successfully submitted, and dialog closes.',
        'Loan application is recorded in the system.'
    ),
    (
        'LOAN_APPLY_002',
        'Verify that loan application submission is blocked and warning text is shown when one or more mandatory verification documents have not been uploaded.',
        'Negative',
        'High',
        'Customer logged into Home tab; loan application dialog opened with 0 or 1 document uploaded.',
        'Loan Amount: 500000, Tenure: 12; Documents uploaded: None.',
        '1. Open the Loan Application dialog from the Home tab.\n2. Enter loan amount and tenure.\n3. Leave mandatory verification documents unuploaded.\n4. Observe the "Submit Application" button and warning message.',
        'Warning message "Please upload all 3 required documents to enable submission." is displayed; the "Submit Application" button remains disabled.',
        'No loan application is submitted.'
    ),
    # 5. Loan Repayment (Mobile Money / Bank)
    (
        'LOAN_PAY_001',
        'Verify that a customer can successfully pay an upcoming loan installment using the Malawian Mobile Money gateway.',
        'Positive',
        'High',
        'Customer logged in; navigate to Transactions tab with pending installments available.',
        'Selected Provider: TNM Mpamba; Mobile Number: +265991234567; PIN: 1234',
        '1. Navigate to the "Transactions" tab.\n2. Tap "Pay Now" on an active upcoming installment.\n3. Select "Mobile Money (TNM Mpamba / Airtel Money)".\n4. Select mobile money provider (e.g., TNM Mpamba).\n5. Enter mobile phone number and 4-digit PIN.\n6. Tap "Authorize Payment" and wait for processing completion.\n7. Tap "Done".',
        'Payment gateway simulates processing, displays "🎉 Payment Successful!", credits the loan account, moves the transaction to Payment History, and removes the paid installment from upcoming list.',
        'Installment status is updated to Paid, and a new payment record appears in history.'
    ),
    (
        'LOAN_PAY_002',
        'Verify that loan repayment authorization is blocked when required account/phone details are missing or the 4-digit PIN is incomplete.',
        'Negative',
        'High',
        'Payment gateway dialog opened for an installment.',
        'Phone/Account: (blank); PIN: 12',
        '1. Open payment gateway dialog for an installment.\n2. Select payment method and provider.\n3. Leave account/phone number blank or enter a PIN with fewer than 4 digits.\n4. Tap "Authorize Payment".',
        'Error message "Please enter valid account/phone and 4-digit PIN." is displayed; payment processing is not initiated.',
        'User remains on the payment details input step.'
    ),
    # 6. Customer Profile & KYC Status
    (
        'PROFILE_VIEW_001',
        'Verify that a logged-in customer can view their personal details, contact information, and KYC verification status under the Profile tab.',
        'Positive',
        'High',
        'Customer successfully authenticated and logged in.',
        'Customer Account: Alex Borrower (alex.borrower@example.com)',
        '1. Tap the "Profile" tab in the bottom tab bar.\n2. View the Customer Profile card.',
        'Customer Name, Email, Phone Number, and KYC Verification status ("Verified" or "Pending Documents") are correctly displayed.',
        'User profile details are displayed.'
    ),
    (
        'PROFILE_LOGOUT_001',
        'Verify that a user can successfully log out of their account via confirmation dialog from the Profile tab.',
        'Positive',
        'High',
        'User logged into the application.',
        'None',
        '1. Navigate to the Profile tab.\n2. Tap the "Logout" button.\n3. In the confirmation dialog ("Confirm Logout"), tap "Logout".',
        'User session is terminated, and navigation returns to the Login screen.',
        'User session cleared; app returns to Login state.'
    ),
    # 7. Loan Officer Review & Status Management
    (
        'OFFICER_REVIEW_001',
        'Verify that a Loan Officer can successfully review pending loan applications and update their status (e.g., Disbursed, Paid, Defaulted).',
        'Positive',
        'High',
        'Loan Officer logged into the Loan Officer Portal.',
        'Application ID: 1, Borrower Name: Chikondi Banda, New Status: Disbursed',
        '1. Log in as a Loan Officer (officer@loan.com).\n2. View the "Pending Loans" list.\n3. Tap "Review" on an application.\n4. In the update status dialog, select a new status (e.g., Disbursed) and tap update.',
        'Application status updates correctly in the dashboard list (e.g., status changes to Disbursed).',
        'Loan application status is updated in the system.'
    ),
    (
        'OFFICER_REVIEW_002',
        'Verify that the Loan Officer dashboard correctly handles empty pending loan lists without displaying erroneous application details.',
        'Negative',
        'Medium',
        'Loan Officer logged in with no pending applications.',
        'None',
        '1. Log in as Loan Officer.\n2. Navigate to Pending Loans tab when queue is empty.',
        'Message "No pending loan applications." is displayed clearly.',
        'Dashboard remains stable.'
    ),
    # 8. Admin Dashboard & NPA Monitoring
    (
        'ADMIN_STATS_001',
        'Verify that an Admin user can view system statistics such as Non-Performing Loans (NPAs) and system users.',
        'Positive',
        'Medium',
        'Admin user logged into Admin Dashboard.',
        'None',
        '1. Log in as Admin (admin@cashflow.com).\n2. View the Admin Dashboard overview and statistics.',
        'Non-Performing Loans statistics (e.g., NPAs: 2) and system user list are displayed.',
        'Admin statistics displayed.'
    ),
    (
        'ADMIN_STATS_002',
        'Verify that unauthorized users cannot access the Admin Dashboard or execute administrative actions.',
        'Negative',
        'High',
        'Customer account logged in.',
        'Customer email and password.',
        '1. Attempt to navigate directly to Admin Dashboard route using customer credentials.',
        'Access is denied, and user is redirected or restricted to Customer Home.',
        'System security enforced.'
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
print('Successfully generated Mobile_App_Test_Cases.docx at:', output_path)
