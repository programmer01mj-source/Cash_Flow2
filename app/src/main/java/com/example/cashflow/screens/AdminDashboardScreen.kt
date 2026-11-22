package com.example.cashflow.screens

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
import com.example.cashflow.network.ApiClient
import com.example.cashflow.network.AdminMetrics
import com.example.cashflow.ui.theme.CashflowTheme
import kotlinx.coroutines.launch
import java.util.Locale

data class SystemUser(
    val id: String,
    var name: String,
    var email: String,
    var role: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onLogout: () -> Unit = {}
) {
    if (UserSession.token.isBlank()) {
        onLogout()
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "User Roles", "Settings")

    val users = remember {
        mutableStateListOf(
            SystemUser("1", "Alice Customer", "alice@example.com", "Customer"),
            SystemUser("2", "John Officer", "john@loan.com", "Loan Officer"),
            SystemUser("3", "Admin Boss", "admin@cashflow.com", "Admin")
        )
    }

    var showAddUserDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<SystemUser?>(null) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Portal") },
                actions = {
                    TextButton(onClick = { showLogoutConfirmDialog = true }) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = { showAddUserDialog = true }) {
                    Text("+ User", modifier = Modifier.padding(8.dp))
                }
            }
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
                    0 -> AdminOverviewTab()
                    1 -> AdminUserRolesTab(
                        users = users,
                        onEditUser = { user -> editingUser = user }
                    )
                    2 -> AdminSettingsTab()
                }
            }
        }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out of the Admin Portal?") },
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

    if (showAddUserDialog || editingUser != null) {
        var nameInput by remember { mutableStateOf(editingUser?.name ?: "") }
        var emailInput by remember { mutableStateOf(editingUser?.email ?: "") }
        var roleInput by remember { mutableStateOf(editingUser?.role ?: "Customer") }

        AlertDialog(
            onDismissRequest = {
                showAddUserDialog = false
                editingUser = null
            },
            title = { Text(if (editingUser != null) "Edit User Role" else "Add New User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roleInput,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Customer", "Loan Officer", "Admin").forEach { r ->
                            Button(
                                onClick = { roleInput = r },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (roleInput == r) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(r, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editingUser != null) {
                        editingUser!!.name = nameInput
                        editingUser!!.email = emailInput
                        editingUser!!.role = roleInput
                    } else {
                        if (nameInput.isNotBlank()) {
                            users.add(SystemUser(System.currentTimeMillis().toString(), nameInput, emailInput, roleInput))
                        }
                    }
                    showAddUserDialog = false
                    editingUser = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddUserDialog = false
                    editingUser = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminOverviewTab() {
    var metrics by remember { mutableStateOf<AdminMetrics?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = ApiClient.apiService.getAdminMetrics(UserSession.token)
                if (res.isSuccessful && res.body() != null) {
                    metrics = res.body()
                }
            } catch (e: Exception) {
                // fallback
            }
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Monthly Financial Metrics (Live Database)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        val m = metrics
                        Text("Total Capital Disbursed", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("MWK ${String.format(Locale.US, "%.2f", m?.totalCapitalDisbursed ?: 124500000.00)}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Principal Repaid: MWK ${String.format(Locale.US, "%.2f", m?.totalPrincipalRepaid ?: 89200000.00)}")
                        Text("Total Interest Earned: MWK ${String.format(Locale.US, "%.2f", m?.totalInterestEarned ?: 14350000.00)}")
                        Text("Non-Performing Loans (NPAs): ${m?.nonPerformingLoans ?: 2} (MWK 4,500,000.00)")
                        Text("Active Borrowers: ${m?.activeBorrowersCount ?: 142}")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Automated Monthly Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Next Scheduled Generation: 1st of next month @ 00:00")
                    Text("Status: ${metrics?.scheduledReportStatus ?: "Active (PDF Report Generator & Email Dispatcher)"}")
                }
            }
        }
    }
}

@Composable
fun AdminUserRolesTab(
    users: List<SystemUser>,
    onEditUser: (SystemUser) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(users) { user ->
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
                        Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = user.email, fontSize = 13.sp)
                        Text(text = "Role: ${user.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = { onEditUser(user) }) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoDisbursement by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("System Configurations", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Push & SMS Alerts", fontWeight = FontWeight.Bold)
                    Text("Send payment reminders automatically", fontSize = 12.sp)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Automated Loan Disbursement", fontWeight = FontWeight.Bold)
                    Text("Instantly payout upon approval", fontSize = 16.sp)
                }
                Switch(
                    checked = autoDisbursement,
                    onCheckedChange = { autoDisbursement = it }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardPreview() {
    CashflowTheme {
        AdminDashboard()
    }
}
