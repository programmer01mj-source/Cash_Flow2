package com.example.cashflow.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cashflow.ui.theme.CashflowTheme

@Composable
fun Signup(
    onNavigateToLogin : () -> Unit = {},
    onSignupSuccess: ()-> Unit ={}) {
    var firstname by remember { mutableStateOf("") }
    var lastname by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}
    var phone by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center,){
        Box(modifier =Modifier.fillMaxWidth(),
            contentAlignment=Alignment.Center
            ) {
            Text(
                text = "SignUp",
                style = MaterialTheme.typography.titleLarge
            )
        }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = firstname,
                onValueChange = { firstname = it },
                label = { Text(text = "First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = lastname,
                onValueChange = { lastname = it },
                label = { Text(text = "Last Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = phone,
                label = { Text(text = "Phone Number") },
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                label = { Text(text = "Password") },
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { println("Sign up") },
                        modifier = Modifier.size(width = 210.dp, height = 45.dp).fillMaxSize(),
                        shape = RoundedCornerShape(22.dp),
                        onSignupSuccess()
                    )
                    { Text(text = "Sign Up") }
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateToLogin) {
                        Text(text = "Already have an Account? Login")
                    }
                }
            }

    }
}
@Preview(name= "Light Mode")
@Preview(
    uiMode= Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Preview(showBackground = true)
@Composable
fun SignupPreview(){
    CashflowTheme {
        Signup()
    }
}
