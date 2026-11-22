import docx

template_path = r'C:\Users\COMADMIN\Downloads\MICHAEL MAKAWA - Test Case Template.docx'
output_path = r'C:\Users\COMADMIN\AndroidStudioProjects\Cashflow2\Mobile_App_Test_Cases.docx'

doc = docx.Document(template_path)

# 1. Fill Project Information (Table 1)
proj_table = doc.tables[1]
proj_table.rows[0].cells[1].paragraphs[0].text = 'MICHAEL MAKAWA'
proj_table.rows[1].cells[1].paragraphs[0].text = 'BSC-COM-05-22'
proj_table.rows[2].cells[1].paragraphs[0].text = 'Cashflow (Comprehensive Functional)'
proj_table.rows[3].cells[1].paragraphs[0].text = 'Android 14'
proj_table.rows[4].cells[1].paragraphs[0].text = '25th OCTOBER, 2026'
proj_table.rows[5].cells[1].paragraphs[0].text = 'v1.0'

# 2. Remove example test case tables and template test case table (Tables 2 to 9)
for table in doc.tables[2:10]:
    tbl_elem = table._element
    tbl_elem.getparent().remove(tbl_elem)

# 3. Add Feature-to-Test-Case Traceability Table
doc.add_paragraph('Feature-to-Test-Case Traceability Matrix (Comprehensive Functional Requirements)')
trace_table = doc.add_table(rows=14, cols=3)
trace_headers = ['Functional Requirement / Screen', 'Module', 'Associated Test Case IDs']
for c_idx, h in enumerate(trace_headers):
    trace_table.rows[0].cells[c_idx].paragraphs[0].text = h

trace_data = [
    ('Splash Screen Initialization', 'FUNC_SPLASH', 'FUNC_SPLASH_001, FUNC_SPLASH_002'),
    ('User Authentication (Login & JWT)', 'FUNC_LOGIN', 'FUNC_LOGIN_001, FUNC_LOGIN_002, FUNC_LOGIN_003, FUNC_LOGIN_004'),
    ('User Registration & Verification (Signup)', 'FUNC_SIGNUP', 'FUNC_SIGNUP_001, FUNC_SIGNUP_002, FUNC_SIGNUP_003'),
    ('Customer Home Dashboard & Loan Status', 'FUNC_HOME', 'FUNC_HOME_001, FUNC_HOME_002'),
    ('Loan Eligibility & Multi-Loan Check', 'FUNC_ELIGIBILITY', 'FUNC_ELIGIBILITY_001, FUNC_ELIGIBILITY_002'),
    ('Loan Calculator & Terms (MWK Currency)', 'FUNC_CALC', 'FUNC_CALC_001, FUNC_CALC_002'),
    ('Document Upload & Verification (ID/Selfie/Payslip/Bank)', 'FUNC_DOC', 'FUNC_DOC_001, FUNC_DOC_002, FUNC_DOC_003'),
    ('Loan Status Transitions (Pending/Active/Paid/Defaulted)', 'FUNC_STATUS', 'FUNC_STATUS_001, FUNC_STATUS_002'),
    ('Installments, Payments & Late Fees', 'FUNC_PAYMENT', 'FUNC_PAYMENT_001, FUNC_PAYMENT_002, FUNC_PAYMENT_003'),
    ('Notifications (Push & SMS Triggers)', 'FUNC_NOTIF', 'FUNC_NOTIF_001, FUNC_NOTIF_002'),
    ('Monthly Reporting & PDF Generation', 'FUNC_REPORT', 'FUNC_REPORT_001, FUNC_REPORT_002'),
    ('Session Management & Security Settings', 'FUNC_SECURITY', 'FUNC_SECURITY_001, FUNC_SECURITY_002'),
    ('Privacy, Consent & Data Retention', 'FUNC_PRIVACY', 'FUNC_PRIVACY_001, FUNC_PRIVACY_002')
]

for r_idx, (f_name, f_mod, f_tcs) in enumerate(trace_data, 1):
    trace_table.rows[r_idx].cells[0].paragraphs[0].text = f_name
    trace_table.rows[r_idx].cells[1].paragraphs[0].text = f_mod
    trace_table.rows[r_idx].cells[2].paragraphs[0].text = f_tcs

doc.add_paragraph()

# 4. Comprehensive Functional Test Cases
test_cases_data = [
    # Splash
    (
        'FUNC_SPLASH_001',
        'Verify that the splash screen displays the company logo and app title correctly upon launching.',
        'Positive',
        'Low',
        'App is installed on target Android device.',
        'None',
        '1. Tap application icon to launch Cashflow.\n2. Observe launch screen UI elements.',
        'Cashflow logo and company branding text are rendered correctly in the center of the screen.',
        'Splash screen remains visible until transition timer triggers.'
    ),
    (
        'FUNC_SPLASH_002',
        'Verify that the splash screen automatically navigates to the Login screen after the animation delay.',
        'Positive',
        'Low',
        'App is launched.',
        'None',
        '1. Launch the application.\n2. Wait for approximately 2 seconds.',
        'App navigates automatically from the splash screen to the Login screen UI.',
        'Login screen is displayed to the user.'
    ),
    # Login & JWT
    (
        'FUNC_LOGIN_001',
        'Verify that a registered customer can successfully log in using a valid email address and password, generating a valid JWT session.',
        'Positive',
        'High',
        'App is on the Login screen and customer account is registered.',
        'Email: alex.borrower@example.com; Password: SecurePass123',
        '1. Enter a valid registered email address in the Email field.\n2. Enter a valid password (at least 6 characters).\n3. Tap the "Login" button.',
        'User is successfully authenticated via backend/mock API, a JWT session is established, and user navigates to Customer Home dashboard.',
        'JWT token stored securely and customer remains logged in.'
    ),
    (
        'FUNC_LOGIN_002',
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
        'FUNC_LOGIN_003',
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
        'FUNC_LOGIN_004',
        'Verify that expired JWT session tokens correctly prompt re-authentication and redirect to the Login screen.',
        'Negative',
        'High',
        'User is logged in with an expired JWT token.',
        'Expired JWT Token',
        '1. Perform an API action after token expiration.\n2. Observe app behavior.',
        'App intercepts 401 Unauthorized response, clears expired token, and redirects user to Login screen with session expiry notice.',
        'User session terminated securely.'
    ),
    # Signup & Verification
    (
        'FUNC_SIGNUP_001',
        'Verify that a new user can successfully register an account providing full name, email, phone (+265...), and password with duplicate account prevention.',
        'Positive',
        'High',
        'User is on the Signup screen.',
        'Full Name: Chikondi Banda, Email: chikondi@example.com, Phone: +265991234567, Password: NewSecurePass123',
        '1. Enter Full Name, Email, Phone Number (+265...), and Password.\n2. Tap the "Sign Up" button.',
        'Registration success dialog appears stating "Your account has been created successfully. Please login with your credentials."; duplicate accounts are rejected.',
        'User account data recorded in system.'
    ),
    (
        'FUNC_SIGNUP_002',
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
        'FUNC_SIGNUP_003',
        'Verify that tapping the "Already have an Account? Login" button navigates back to the Login screen.',
        'Positive',
        'Medium',
        'User is on the Signup screen.',
        'None',
        '1. Tap the "Already have an Account? Login" text button.',
        'App pops back from the Signup screen to the Login screen UI.',
        'Login screen is displayed.'
    ),
    # Home & Loan Status
    (
        'FUNC_HOME_001',
        'Verify that the Customer Home screen correctly renders active loan status, account balance, and next due date cards.',
        'Positive',
        'High',
        'Customer logged into Home tab.',
        'None',
        '1. Access the Home tab in the customer dashboard.',
        'Cards showing "Active Loan Status" (Approved & Disbursed), "Current Balance: MWK 315,000.00", and next due date are displayed.',
        'Customer Home dashboard is rendered.'
    ),
    (
        'FUNC_HOME_002',
        'Verify that the bottom navigation tab bar allows switching between Home, Transactions, and Profile tabs.',
        'Positive',
        'High',
        'Customer logged into Home screen.',
        'None',
        '1. Tap "Transactions" tab in tab bar.\n2. Tap "Profile" tab.\n3. Tap "Home" tab.',
        'View switches dynamically to display Transaction content and Profile content respectively.',
        'Active tab state updates correctly.'
    ),
    # Eligibility & Multi-Loan Check
    (
        'FUNC_ELIGIBILITY_001',
        'Verify that the loan eligibility engine verifies that the applicant does not hold active loans from two or more other lenders.',
        'Positive',
        'High',
        'Customer applying for a new loan with 0 or 1 active external loan.',
        'External Loan Count: 1',
        '1. Submit loan application request.\n2. Eligibility engine evaluates external credit bureau data.',
        'Application passes the multi-loan rule (holding fewer than 2 active loans from other lenders) and proceeds to document verification.',
        'Eligibility check status marked as Passed.'
    ),
    (
        'FUNC_ELIGIBILITY_002',
        'Verify that the loan eligibility engine rejects loan applications when the applicant holds active loans from two or more other lenders.',
        'Negative',
        'High',
        'Customer applying for a new loan with 2 or more active external loans.',
        'External Loan Count: 2',
        '1. Submit loan application request with 2 active external loans.',
        'Eligibility engine flags rejection due to holding active loans from 2+ other lenders; error message displayed to user.',
        'Loan application rejected prior to officer review.'
    ),
    # Calculator & Terms (MWK Currency)
    (
        'FUNC_CALC_001',
        'Verify that the loan calculator correctly computes total interest (12% p.a.) and repayment schedule in Malawian Kwacha (MWK) based on amount and tenure.',
        'Positive',
        'High',
        'Loan Application dialog open.',
        'Amount: MWK 500,000; Tenure: 12 months; Interest Rate: 12% p.a.',
        '1. Enter loan amount (MWK 500,000) and tenure (12 months) in the loan application form.',
        'System calculates total interest (MWK 60,000) and total repayment (MWK 560,000) accurately using 12% p.a. formula.',
        'Calculated terms displayed correctly in UI.'
    ),
    (
        'FUNC_CALC_002',
        'Verify that entering loan amounts or tenures exceeding defined system limits triggers validation errors.',
        'Negative',
        'Medium',
        'Loan Application dialog open.',
        'Amount: MWK 50,000,000 (Exceeds limit)',
        '1. Enter an amount exceeding maximum loan limits.\n2. Tap submit.',
        'Validation error message indicates loan amount exceeds maximum allowed limit.',
        'Application submission blocked.'
    ),
    # Document Upload & Verification
    (
        'FUNC_DOC_001',
        'Verify that allowed file formats (PDF, JPEG, PNG) and maximum file size limits are enforced for ID, Selfie, Pay Slip, and Bank Statement uploads.',
        'Positive',
        'High',
        'Loan application document upload step active.',
        'File: Gov_ID.pdf (Size: 2.5 MB)',
        '1. Select a valid PDF or image file within size limits for Government ID, Selfie, and Pay Slip.',
        'System accepts uploads, shows upload progress, and marks each document as "Uploaded ✓".',
        'Documents successfully attached to loan application.'
    ),
    (
        'FUNC_DOC_002',
        'Verify that unsupported file formats (e.g. .exe, .txt) or oversized files trigger rejection messages during document upload.',
        'Negative',
        'High',
        'Loan application document upload step active.',
        'File: document.txt (Size: 15 MB)',
        '1. Attempt to upload an unsupported format or oversized file.',
        'System rejects the file upload and displays error message "Unsupported file format or file exceeds maximum size limit."',
        'File not attached; user prompted to re-upload valid document.'
    ),
    (
        'FUNC_DOC_003',
        'Verify that the automated and manual verification workflow verifies uploaded ID, Selfie, and Pay Slip documents before final loan approval.',
        'Positive',
        'High',
        'Documents uploaded and submitted.',
        'Uploaded ID, Selfie, Pay Slip',
        '1. Submit loan application with all 3 required documents.\n2. Loan officer reviews uploaded documents in portal.',
        'Documents are queued for OCR/officer verification; officer can view and verify documents to approve disbursement.',
        'Verification status updated.'
    ),
    # Loan Status Transitions
    (
        'FUNC_STATUS_001',
        'Verify permitted loan status transitions (Pending -> Under Review -> Disbursed -> Active -> Paid / Defaulted) and authorized roles.',
        'Positive',
        'High',
        'Loan Officer logged in with pending application.',
        'Application ID: 1',
        '1. Open pending loan application.\n2. Update status from Pending to Disbursed, then Active, and finally Paid or Defaulted.',
        'System transitions loan status successfully through permitted stages when executed by authorized Loan Officer role.',
        'Loan status updated across system database and UI.'
    ),
    (
        'FUNC_STATUS_002',
        'Verify that unauthorized users or invalid status transition paths are blocked by the system.',
        'Negative',
        'High',
        'Customer logged in attempting to update loan status.',
        'Target Status: Disbursed',
        '1. Attempt to trigger status update API or UI control as a customer role.',
        'System denies permission and blocks unauthorized status change.',
        'Loan status remains unchanged.'
    ),
    # Installments, Payments & Late Fees
    (
        'FUNC_PAYMENT_001',
        'Verify that upcoming installments and due dates are calculated correctly with installment breakdown in Malawian Kwacha.',
        'Positive',
        'High',
        'Customer with active disbursed loan.',
        'Loan Amount: MWK 500,000; Tenure: 12 mos',
        '1. Navigate to Transactions tab.\n2. Inspect upcoming installments list.',
        'System displays monthly installment amounts (e.g. MWK 45,000.00) with precise due dates spaced 30 days apart.',
        'Installment schedule rendered accurately.'
    ),
    (
        'FUNC_PAYMENT_002',
        'Verify that successful payment via Mobile Money (TNM Mpamba / Airtel Money) or Bank gateway updates installment status to Paid and credits account.',
        'Positive',
        'High',
        'Payment simulation dialog open for an installment.',
        'Provider: TNM Mpamba; Phone: +265991234567; PIN: 1234',
        '1. Select payment provider.\n2. Enter phone number and 4-digit PIN.\n3. Authorize payment.',
        'Payment gateway processes transaction, displays success confirmation, credits loan account, moves record to Payment History, and removes installment from upcoming list.',
        'Installment marked Paid.'
    ),
    (
        'FUNC_PAYMENT_003',
        'Verify that overdue installments automatically incur late fees in accordance with loan agreement terms.',
        'Positive',
        'Medium',
        'Installment past due date by > 5 days.',
        'Overdue Installment ID: 2',
        '1. System scheduler evaluates due dates against current date.',
        'System calculates and applies late fee to overdue installment, updating outstanding balance in customer view.',
        'Late fee recorded.'
    ),
    # Notifications (Push & SMS)
    (
        'FUNC_NOTIF_001',
        'Verify that payment reminder notifications are triggered and sent via push and SMS prior to installment due dates.',
        'Positive',
        'High',
        'Loan installment due in 3 days.',
        'Trigger: 3-day reminder',
        '1. System automated notification scheduler runs for upcoming due dates.',
        'System dispatches payment reminder push notification and SMS template to borrower phone number ("Your next installment of MWK 45,000.00 is due in 3 days.").',
        'Notification sent log recorded.'
    ),
    (
        'FUNC_NOTIF_002',
        'Verify that delivery failures for push notifications or SMS trigger retry mechanisms and error logging.',
        'Negative',
        'Medium',
        'Notification gateway temporarily unavailable.',
        'Invalid recipient phone number or network timeout',
        '1. Dispatch notification when gateway is unreachable.',
        'System captures delivery failure exception, triggers retry queue, and logs error in notification audit trail.',
        'Retry scheduled.'
    ),
    # Monthly Reporting & PDF Generation
    (
        'FUNC_REPORT_001',
        'Verify that the system generates monthly financial and loan performance PDF reports on schedule with accurate metric formulas.',
        'Positive',
        'High',
        'Admin user or automated scheduler initiating monthly report.',
        'Reporting Period: Previous Month',
        '1. Trigger monthly report generation in Admin portal or via scheduled task.',
        'System compiles previous month boundary data, calculates metrics (disbursements, repayments, NPAs), and generates formatted PDF report.',
        'PDF report generated successfully.'
    ),
    (
        'FUNC_REPORT_002',
        'Verify that monthly PDF reports are automatically emailed to designated recipients with failure handling and retry logic.',
        'Positive',
        'Medium',
        'Generated PDF report ready.',
        'Recipient List: management@cashflow.com',
        '1. System email dispatcher sends generated PDF report to recipient list.',
        'Email successfully dispatched with PDF attachment; failure to send triggers retry queue.',
        'Email delivery logged.'
    ),
    # Session Management & Security
    (
        'FUNC_SECURITY_001',
        'Verify that session management enforces secure JWT token handling, HTTPS/TLS traffic encryption, and secure logout clearing.',
        'Positive',
        'High',
        'User logged into app.',
        'JWT Token',
        '1. Inspect network traffic for HTTPS compliance.\n2. Perform logout action.',
        'All API requests use encrypted HTTPS/TLS transport; JWT tokens are securely cleared from local storage upon logout.',
        'Session destroyed securely.'
    ),
    (
        'FUNC_SECURITY_002',
        'Verify that sensitive user data (PII) is encrypted at rest using AES-256 key management in the database.',
        'Positive',
        'High',
        'Database storage containing user records.',
        'AES-256 encryption key',
        '1. Inspect database storage records for encrypted PII fields.',
        'All personally identifiable information (ID numbers, phone numbers, banking details) is stored encrypted using AES-256.',
        'Data protected at rest.'
    ),
    # Privacy & Consent
    (
        'FUNC_PRIVACY_001',
        'Verify that the application presents privacy consent disclosures during onboarding and enforces data retention/deletion rules.',
        'Positive',
        'High',
        'New user onboarding or privacy settings.',
        'Consent agreement',
        '1. Review onboarding screens for privacy policy and consent checkboxes.\n2. Request data deletion.',
        'Consent is explicitly captured before data collection; data deletion requests comply with applicable privacy regulations.',
        'Consent and retention policies enforced.'
    ),
    (
        'FUNC_PRIVACY_002',
        'Verify that unauthorized data access requests are blocked and privacy audit logs are maintained.',
        'Negative',
        'High',
        'Unauthorized data request attempt.',
        'None',
        '1. Attempt unauthorized access to user data records.',
        'System blocks access request, returns 403 Forbidden, and records security audit log entry.',
        'Privacy breach prevented.'
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
print('Successfully generated comprehensive functional test cases Word document at:', output_path)
