package com.example.cashflow.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashflow.UserSession
import com.example.cashflow.ui.theme.CashflowTheme

data class LoanApplication(
    val id: String,
    val borrowerName: String,
    val amount: String,
    val tenure: String,
    val income: String,
    var status: String
)

data class Borrower(
    val id: String,
    val name: String,
    val contact: String,
    val loanAmount: String,
    val balance: String,
    var loanStatus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanOfficerDashboard(
    onLogout: () -> Unit = {}
) {
    if (UserSession.token.isBlank()) {
        onLogout()
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Loans", "Active Borrowers")

    val pendingApplications = remember {
        mutableStateListOf(
            LoanApplication("1", "Chikondi Banda", "MWK 500,000", "12 months", "MWK 300,000/mo", "Pending"),
            LoanApplication("2", "Tiyamike Phiri", "MWK 250,000", "6 months", "MWK 200,000/mo", "Pending"),
            LoanApplication("3", "Madalitso Tembo", "MWK 1,000,000", "24 months", "MWK 550,000/mo", "Under Review")
        )
    }

    val activeBorrowers = remember {
        mutableStateListOf(
            Borrower("101", "Grace Mwale", "+265 99 123 4567", "MWK 400,000", "MWK 120,000", "Disbursed"),
            Borrower("102", "Chimwemwe Nkhoma", "+265 88 987 6543", "MWK 850,000", "MWK 620,000", "Disbursed"),
            Borrower("103", "Dalitso Chirwa", "+265 99 456 7890", "MWK 300,000", "MWK 0", "Paid")
        )
    }

    var selectedApplication by remember { mutableStateOf<LoanApplication?>(null) }
    var selectedBorrower by remember { mutableStateOf<Borrower?>(null) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Officer Portal") },
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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> PendingLoansList(
                        applications = pendingApplications,
                        onReviewClick = { app -> selectedApplication = app }
                    )
                    1 -> ActiveBorrowersList(
                        borrowers = activeBorrowers,
                        onUpdateStatusClick = { borrower -> selectedBorrower = borrower }
                    )
                }
            }
        }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out of the Loan Officer Portal?") },
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

    selectedApplication?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApplication = null },
            title = { Text("Review Loan Application") },
            text = {
                Column {
                    Text("Borrower: ${app.borrowerName}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Requested Amount: ${app.amount}")
                    Text("Tenure: ${app.tenure}")
                    Text("Verified Income: ${app.income}")
                    Text("Status: ${app.status}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pre-approval checks passed: Age verified, Debt < 40%, Not on defaulters list.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    app.status = "Approved"
                    pendingApplications.remove(app)
                    selectedApplication = null
                }) {
                    Text("Approve")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    app.status = "Rejected"
                    pendingApplications.remove(app)
                    selectedApplication = null
                }) {
                    Text("Reject")
                }
            }
        )
    }

    selectedBorrower?.let { borrower ->
        AlertDialog(
            onDismissRequest = { selectedBorrower = null },
            title = { Text("Update Loan Status") },
            text = {
                Column {
                    Text("Borrower: ${borrower.name}", fontWeight = FontWeight.Bold)
                    Text("Contact: ${borrower.contact}")
                    Text("Current Balance: ${borrower.balance}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select new status:")
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        borrower.loanStatus = "Disbursed"
                        selectedBorrower = null
                    }) { Text("Disbursed") }

                    TextButton(onClick = {
                        borrower.loanStatus = "Paid"
                        selectedBorrower = null
                    }) { Text("Paid") }

                    TextButton(onClick = {
                        borrower.loanStatus = "Defaulted"
                        selectedBorrower = null
                    }) { Text("Defaulted", color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
}

@Composable
fun PendingLoansList(
    applications: List<LoanApplication>,
    onReviewClick: (LoanApplication) -> Unit
) {
    if (applications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending loan applications.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(applications) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = app.borrowerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Amount: ${app.amount} (${app.tenure})", fontSize = 14.sp)
                            Text(text = "Status: ${app.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(onClick = { onReviewClick(app) }) {
                            Text("Review")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveBorrowersList(
    borrowers: List<Borrower>,
    onUpdateStatusClick: (Borrower) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(borrowers) { borrower ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = borrower.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Contact: ${borrower.contact}", fontSize = 13.sp)
                        Text(text = "Balance: ${borrower.balance} | Status: ${borrower.loanStatus}", fontSize = 12.sp)
                    }
                    Button(onClick = { onUpdateStatusClick(borrower) }) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoanOfficerDashboardPreview() {
    CashflowTheme {
        LoanOfficerDashboard()
    }
}
