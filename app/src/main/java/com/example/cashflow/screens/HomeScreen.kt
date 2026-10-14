package com.example.cashflow.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cashflow.ui.theme.CashflowTheme

@Composable
fun Home(onLogout: () -> Unit = {}){
    var selectedIndex by remember{mutableStateOf(0)}
    val tabs = listOf("Home","Transactions", "Profile")

    Column(modifier = Modifier.fillMaxSize(),){
        TabRow(selectedTabIndex = selectedIndex){
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected= selectedIndex== index,
                    onClick= {selectedIndex= index},
                    text= {Text(title)}
                )
            }
        }
        Box(modifier= Modifier.fillMaxSize()){
            when (selectedIndex){
                0->HomeTab()
                1->TransactionTab()
                2->ProfileTab(onLogOut = onLogout)
            }
        }
    }
}
@Composable
private fun HomeTab(){
    Text(text="Home content")
}
@Composable
private fun TransactionTab(){
    Text(text="Transaction content")
}
@Composable
private fun ProfileTab(onLogOut: () -> Unit = {}){
    Text(text="Profile content")
}
@Preview(name= "Light Mode")
@Preview(
    uiMode= Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Preview(showBackground = true)
@Composable
fun HomePreview(){
    CashflowTheme {
        Home()
    }
}
