package com.example.cashflow.screens

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cashflow.ui.theme.CashflowTheme

@Composable
fun Home(onLogout: ()-> Unit = {}){
    var selectedIndex by remember{mutableStateOf(0)}}
val tabs = listOf("Home","Transactions", "Profile")

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
