package com.example.cashflow.screens

import android.content.res.Configuration
import android.net.Uri
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashflow.R
import com.example.cashflow.UserSession
import com.example.cashflow.data.repository.FirebaseLoanRepository
import com.example.cashflow.data.repository.FirebaseTransactionRepository
import com.example.cashflow.ui.theme.CashflowTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Installment(
    val id: String,
    val dueDate: String,
    val amount: String,
    var status: String
)

data class PaymentRecord(
    val id: String,
    val date: String,
    val amount: String,
    val reference: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(onLogout: () -> Unit = {}) {
    if (UserSession.token.isBlank()) {
        onLogout()
        return
    }
    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Transactions", "Profile")
    val scope = rememberCoroutineScope()

    var showApplyDialog by remember { mutableStateOf(false) }
    var loanAmountInput by remember { mutableStateOf("500000") }
    var tenureInput by remember { mutableStateOf("12") }

    var idUploaded by remember { mutableStateOf(false) }
    var selfieUploaded by remember { mutableStateOf(false) }
    var payslipUploaded by remember { mutableStateOf(false) }

    var scannedNationalId by remember { mutableStateOf("") }
    var userDob by remember { mutableStateOf("") }
    var ageErrorMessage by remember { mutableStateOf("") }
    var showQrScannerDialog by remember { mutableStateOf(false) }

    var activeDocSelecting by remember { mutableStateOf<String?>(null) }
    var selectedInstallment by remember { mutableStateOf<Installment?>(null) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val todayStr = dateFormat.format(cal.time)
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var profileName by remember { mutableStateOf(UserSession.name) }
    var profileEmail by remember { mutableStateOf(UserSession.email) }
    var profilePhone by remember { mutableStateOf(UserSession.phone) }
    var profileRole by remember { mutableStateOf(UserSession.role) }
    var profileKyc by remember { mutableStateOf("Pending Documents") }
    var profileId by remember { mutableStateOf("") }
    var profileDob by remember { mutableStateOf("") }
    var profileLoading by remember { mutableStateOf(true) }

    val installments = remember {
        mutableStateListOf<Installment>()
    }

    val paymentHistory = remember {
        mutableStateListOf<PaymentRecord>()
    }

    DisposableEffect(currentUid, UserSession.email) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = currentUid.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
        val email = UserSession.email

        val profileListener = if (uid.isNotBlank()) {
            firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        profileName = snapshot.getString("name") ?: UserSession.name
                        profileEmail = snapshot.getString("email") ?: UserSession.email
                        profilePhone = snapshot.getString("phone") ?: UserSession.phone
                        profileRole = snapshot.getString("role") ?: UserSession.role
                        profileKyc = snapshot.getString("kycStatus") ?: "Pending Documents"
                        profileId = snapshot.getString("nationalId") ?: ""
                        profileDob = snapshot.getString("dob") ?: ""
                    }
                    profileLoading = false
                }
        } else {
            profileLoading = false
            null
        }

        val loansQuery = if (uid.isNotBlank()) {
            firestore.collection("loans").whereEqualTo("borrowerId", uid)
        } else {
            firestore.collection("loans").whereEqualTo("borrowerEmail", email)
        }

        val loansListener = loansQuery.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                installments.clear()
                var index = 1
                for (doc in snapshot.documents) {
                    val amount = doc.getDouble("amount") ?: 500000.0
                    val rawStatus = doc.getString("status") ?: "PENDING"
                    val displayStatus = when (rawStatus.uppercase()) {
                        "PENDING" -> "Pending Review"
                        "UNDER_REVIEW" -> "Under Review"
                        "APPROVED" -> "Approved"
                        "REJECTED" -> "Rejected"
                        "DISBURSED" -> "Disbursed"
                        "ACTIVE" -> "Active Loan"
                        "PAID" -> "Fully Paid"
                        "DEFAULTED" -> "Defaulted"
                        else -> rawStatus
                    }
                    installments.add(Installment(index.toString(), "2025-05-01", "MWK ${String.format(Locale.US, "%.2f", amount / 12)}", displayStatus))
                    index++
                }
            }
        }

        val txnsQuery = if (uid.isNotBlank()) {
            firestore.collection("transactions").whereEqualTo("borrowerId", uid)
        } else {
            firestore.collection("transactions").whereEqualTo("borrowerEmail", email)
        }

        val txnsListener = txnsQuery.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                paymentHistory.clear()
                for (doc in snapshot.documents) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    val date = doc.getString("date") ?: ""
                    val ref = doc.getString("txnId") ?: doc.id
                    paymentHistory.add(PaymentRecord(ref, date, "MWK ${String.format(Locale.US, "%.2f", amount)}", ref))
                }
            }
        }

        onDispose {
            profileListener?.remove()
            loansListener.remove()
            txnsListener.remove()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            activeDocSelecting?.let { docName ->
                when (docName) {
                    "Gov ID" -> idUploaded = true
                    "Selfie" -> selfieUploaded = true
                    "Pay Slip" -> payslipUploaded = true
                }
            }
            activeDocSelecting = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            activeDocSelecting?.let { docName ->
                when (docName) {
                    "Gov ID" -> idUploaded = true
                    "Selfie" -> selfieUploaded = true
                    "Pay Slip" -> payslipUploaded = true
                }
            }
            activeDocSelecting = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Row(
                        modifier = Modifier.padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cashflow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showLogoutConfirmDialog = true }) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedIndex) {
                    0 -> CustomerHomeTab(
                        installments = installments,
                        onApplyClick = { showApplyDialog = true }
                    )
                    1 -> CustomerTransactionsTab(
                        installments = installments,
                        history = paymentHistory,
                        onPayClick = { inst -> selectedInstallment = inst }
                    )
                    2 -> CustomerProfileTab(
                        onLogOut = { showLogoutConfirmDialog = true },
                        profileName = profileName,
                        profileEmail = profileEmail,
                        profilePhone = profilePhone,
                        profileRole = profileRole,
                        profileKyc = profileKyc,
                        profileId = profileId.ifBlank { scannedNationalId },
                        profileDob = profileDob.ifBlank { userDob },
                        profileLoading = profileLoading
                    )
                }
            }
        }
    }

    if (showApplyDialog) {
        val amount = loanAmountInput.toDoubleOrNull() ?: 500000.0
        val tenure = tenureInput.toIntOrNull() ?: 12
        val interestRate = 0.12
        val totalInterest = amount * interestRate * (tenure / 12.0)
        val totalRepayment = amount + totalInterest
        val allDocsUploaded = idUploaded && selfieUploaded && payslipUploaded

        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Loan Application & Verification") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Loan Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = loanAmountInput,
                        onValueChange = { loanAmountInput = it },
                        label = { Text("Loan Amount (MWK)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tenureInput,
                        onValueChange = { tenureInput = it },
                        label = { Text("Tenure (Months)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Interest Rate: 12% p.a.", fontSize = 13.sp)
                    Text("Total Repayment: MWK ${String.format(Locale.US, "%.2f", totalRepayment)}", fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Required Documents", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Mandatory verification documents required for loan approval:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Government ID", fontSize = 13.sp)
                        Button(
                            onClick = { activeDocSelecting = "Gov ID" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (idUploaded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (idUploaded) "Uploaded ✓" else "Upload ID", fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Identity Selfie", fontSize = 13.sp)
                        Button(
                            onClick = { activeDocSelecting = "Selfie" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selfieUploaded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (selfieUploaded) "Uploaded ✓" else "Upload Selfie", fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Proof of Income (Pay Slip)", fontSize = 13.sp)
                        Button(
                            onClick = { activeDocSelecting = "Pay Slip" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (payslipUploaded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (payslipUploaded) "Uploaded ✓" else "Upload Pay Slip", fontSize = 11.sp)
                        }
                    }

                    if (!allDocsUploaded) {
                        Text(
                            "Please upload all 3 required documents to enable submission.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = loanAmountInput.toDoubleOrNull() ?: 500000.0
                        val tenureVal = tenureInput.toIntOrNull() ?: 12
                        scope.launch {
                            try {
                                val loanRepo = FirebaseLoanRepository()
                                loanRepo.applyForLoan(amountVal, tenureVal, 300000.0, scannedNationalId, userDob)
                            } catch (_: Exception) {}
                        }
                        showApplyDialog = false
                    },
                    enabled = allDocsUploaded
                ) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApplyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    activeDocSelecting?.let { docName ->
        AlertDialog(
            onDismissRequest = { activeDocSelecting = null },
            title = { Text("Provide $docName") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Choose upload method for $docName:")
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan Document (Camera)")
                    }
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload from Device File System (Media Files)")
                    }
                    if (docName == "Gov ID") {
                        Button(
                            onClick = {
                                activeDocSelecting = null
                                showQrScannerDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Scan National ID QR Code (Age Check)")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { activeDocSelecting = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQrScannerDialog) {
        var qrInputText by remember { mutableStateOf("ID:MW-94820192|DOB:1990-05-12|Name:Alex Borrower") }
        AlertDialog(
            onDismissRequest = { showQrScannerDialog = false },
            title = { Text("National ID QR Code Scanner") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Scan or select sample National ID QR code:")
                    if (ageErrorMessage.isNotBlank()) {
                        Text(ageErrorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val id = "MW-94820192"
                            val dob = "1995-06-15"
                            val age = calculateAge(dob)
                            if (age >= 18) {
                                scannedNationalId = id
                                userDob = dob
                                idUploaded = true
                                ageErrorMessage = ""
                                showQrScannerDialog = false
                            } else {
                                ageErrorMessage = "Age Eligibility Failed: User is $age years old. Must be at least 18."
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Scan: Adult ID (DOB: 1995-06-15)")
                    }

                    Button(
                        onClick = {
                            val id = "MW-20150210"
                            val dob = "2012-08-20"
                            val age = calculateAge(dob)
                            if (age >= 18) {
                                scannedNationalId = id
                                userDob = dob
                                idUploaded = true
                                ageErrorMessage = ""
                                showQrScannerDialog = false
                            } else {
                                ageErrorMessage = "Age Eligibility Failed: User is $age years old. Must be at least 18."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Scan: Minor ID (DOB: 2012-08-20)")
                    }

                    OutlinedTextField(
                        value = qrInputText,
                        onValueChange = { qrInputText = it },
                        label = { Text("QR Payload (ID:...|DOB:YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            try {
                                var id = "MW-CUSTOM"
                                var dob = "1990-01-01"
                                val parts = qrInputText.split("|", ",")
                                for (p in parts) {
                                    if (p.contains("ID", ignoreCase = true)) {
                                        id = p.substringAfter(":").trim()
                                    }
                                    if (p.contains("DOB", ignoreCase = true)) {
                                        dob = p.substringAfter(":").trim()
                                    }
                                }
                                val age = calculateAge(dob)
                                if (age >= 18) {
                                    scannedNationalId = id
                                    userDob = dob
                                    idUploaded = true
                                    ageErrorMessage = ""
                                    showQrScannerDialog = false
                                } else {
                                    ageErrorMessage = "Age Eligibility Failed: User is $age years old. Must be at least 18."
                                }
                            } catch (e: Exception) {
                                ageErrorMessage = "Invalid QR code format. Use ID:MW123|DOB:YYYY-MM-DD"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Process Custom QR Payload")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQrScannerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out of your Cashflow account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        UserSession.clear()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedInstallment?.let { inst ->
        PaymentSimulationDialog(
            installment = inst,
            onDismiss = { selectedInstallment = null },
            onSuccess = { record ->
                paymentHistory.add(record)
                installments.remove(inst)
                selectedInstallment = null
            }
        )
    }
}

@Composable
fun PaymentSimulationDialog(
    installment: Installment,
    onDismiss: () -> Unit,
    onSuccess: (PaymentRecord) -> Unit
) {
    var step by remember { mutableStateOf("CATEGORY") }
    var methodType by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("") }
    var accountNumberOrPhone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Loan Repayment Gateway (Malawi)") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Amount Due: ${installment.amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)

                when (step) {
                    "CATEGORY" -> {
                        Text("Select Payment Method:", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { methodType = "Bank"; step = "PROVIDER" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Malawian Banks")
                        }
                        OutlinedButton(
                            onClick = { methodType = "MobileMoney"; step = "PROVIDER" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mobile Money (TNM Mpamba / Airtel Money)")
                        }
                    }
                    "PROVIDER" -> {
                        Text("Select ${if (methodType == "Bank") "Bank" else "Mobile Money Provider"}:", fontWeight = FontWeight.Bold)
                        val providers = if (methodType == "Bank") {
                            listOf(
                                "National Bank of Malawi (NBM)",
                                "Standard Bank Malawi",
                                "FDH Bank",
                                "NBS Bank",
                                "First Capital Bank"
                            )
                        } else {
                            listOf("TNM Mpamba", "Airtel Money")
                        }

                        providers.forEach { provider ->
                            OutlinedButton(
                                onClick = {
                                    selectedProvider = provider
                                    step = "DETAILS"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(provider)
                            }
                        }

                        TextButton(onClick = { step = "CATEGORY" }) {
                            Text("Back")
                        }
                    }
                    "DETAILS" -> {
                        Text("Paying via: $selectedProvider", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (errorMessage.isNotBlank()) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        OutlinedTextField(
                            value = accountNumberOrPhone,
                            onValueChange = { accountNumberOrPhone = it },
                            label = { Text(if (methodType == "Bank") "Account / Card Number" else "Mobile Number (+265...)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("4-Digit PIN / Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextButton(onClick = { step = "PROVIDER" }) {
                            Text("Back")
                        }
                    }
                    "PROCESSING" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Connecting to $selectedProvider gateway...")
                            Text("Please authorize prompt on your mobile/bank device.", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                        }
                    }
                    "SUCCESS" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉 Payment Successful!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Paid: ${installment.amount}")
                            Text("Method: $selectedProvider")
                            Text("Reference: TXN-${System.currentTimeMillis()}")
                            Text("Your loan account has been credited successfully.")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step == "DETAILS") {
                Button(
                    onClick = {
                        if (accountNumberOrPhone.isBlank() || pin.length < 4) {
                            errorMessage = "Please enter valid account/phone and 4-digit PIN."
                            return@Button
                        }
                        errorMessage = ""
                        step = "PROCESSING"
                        scope.launch {
                            delay(3000L)
                            step = "SUCCESS"
                        }
                    }
                ) {
                    Text("Authorize Payment")
                }
            } else if (step == "SUCCESS") {
                Button(
                    onClick = {
                        val ref = "TXN-${System.currentTimeMillis()}"
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        onSuccess(PaymentRecord("P-${System.currentTimeMillis()}", todayStr, installment.amount, ref))
                    }
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (step != "PROCESSING") {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun CustomerHomeTab(
    installments: List<Installment>,
    onApplyClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Active Loan Status", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    val activeStatus = if (installments.isNotEmpty()) installments.first().status else "No Active Loan"
                    Text(activeStatus, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (installments.isNotEmpty()) "Loan Status in Live Database: $activeStatus" else "No loan applied for yet. Submit an application below.")
                }
            }
        }
        item {
            LoanProgressTrackerCard(installments = installments)
        }
        item {
            Button(
                onClick = onApplyClick,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text(
                    text = "Apply for New Loan (Mandatory Docs Included)",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            Text("Upcoming Reminders", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Reminder", fontWeight = FontWeight.Bold)
                    Text(if (installments.isNotEmpty()) "Your next installment is pending review or active." else "No upcoming payment reminders.")
                }
            }
        }
    }
}

@Composable
fun LoanProgressTrackerCard(installments: List<Installment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Loan Application Progress Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            if (installments.isEmpty()) {
                Text("No active loan applications found. Please submit a loan application to start tracking its real-time status.", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            } else {
                val status = installments.first().status
                val isApproved = status.equals("Approved", true) || status.equals("Disbursed", true) || status.equals("Active", true) || status.equals("Paid", true)

                ProgressStepRow(stepNumber = "1", title = "Document Verification", status = "Completed ✓", isDone = true)
                ProgressStepRow(stepNumber = "2", title = "Credit & Eligibility Evaluation", status = "Completed ✓", isDone = true)
                ProgressStepRow(stepNumber = "3", title = "Loan Officer Review", status = if (isApproved) "Completed ✓" else "In Progress ($status)", isDone = isApproved, isCurrent = !isApproved)
                ProgressStepRow(stepNumber = "4", title = "Approval & Disbursement", status = if (isApproved) "Completed ✓" else "Pending ⏱️", isDone = isApproved)
            }
        }
    }
}

@Composable
fun ProgressStepRow(stepNumber: String, title: String, status: String, isDone: Boolean, isCurrent: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (isDone) MaterialTheme.colorScheme.primary else if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(stepNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDone || isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
        }
        Text(status, fontSize = 12.sp, color = if (isDone) MaterialTheme.colorScheme.primary else if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun CustomerTransactionsTab(
    installments: List<Installment>,
    history: List<PaymentRecord>,
    onPayClick: (Installment) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Upcoming Installments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        if (installments.isEmpty()) {
            item { Text("No pending installments.") }
        } else {
            items(installments) { inst ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Due: ${inst.dueDate}", fontWeight = FontWeight.Bold)
                            Text("Amount: ${inst.amount}", fontSize = 14.sp)
                            Text("Status: ${inst.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(onClick = { onPayClick(inst) }) {
                            Text("Pay Now")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Payment History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        if (history.isEmpty()) {
            item { Text("No transactions yet.", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp) }
        } else {
            items(history) { rec ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rec.reference, fontWeight = FontWeight.Bold)
                            Text(rec.amount, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("Paid on: ${rec.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerProfileTab(
    onLogOut: () -> Unit = {},
    profileName: String,
    profileEmail: String,
    profilePhone: String,
    profileRole: String,
    profileKyc: String,
    profileId: String,
    profileDob: String,
    profileLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Customer Profile (Live Firestore Data)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (profileLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading profile from database...", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Text("Name: ${profileName.ifBlank { "Not provided" }}")
                        Text("Email: ${profileEmail.ifBlank { "Not provided" }}")
                        Text("Role: ${profileRole.ifBlank { "Customer" }}")
                        Text("Phone: ${profilePhone.ifBlank { "Not provided" }}")
                        Text("National ID: ${profileId.ifBlank { "Not Scanned (QR Code Required)" }}")
                        if (profileDob.isNotBlank()) {
                            val age = calculateAge(profileDob)
                            Text("Date of Birth: $profileDob (Age: $age - ${if (age >= 18) "Eligible (18+)" else "Underage"})")
                        } else {
                            Text("Date of Birth: Not Verified")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("KYC Verification: $profileKyc", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            Button(
                onClick = onLogOut,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}

private fun calculateAge(dobStr: String): Int {
    try {
        val parts = dobStr.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - year
            val currentMonth = today.get(Calendar.MONTH) + 1
            val currentDay = today.get(Calendar.DAY_OF_MONTH)
            if (currentMonth < month || (currentMonth == month && currentDay < day)) {
                age--
            }
            return age
        }
    } catch (_: Exception) {
        // ignore
    }
    return 0
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun HomePreview() {
    CashflowTheme {
        Home()
    }
}
