package com.example.cashflow

import android.content.res.Configuration
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.MediaType.Companion.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashflow.ui.theme.CashflowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashflowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CashflowApp()
                }
            }
        }
    }
}

@Composable
fun Login(
    onNavigateToSignup : () -> Unit = {},
onLoginSuccess: ()-> Unit={}
){  var username by remember { mutableStateOf("") }
    var password by remember {mutableStateOf("")}
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
       verticalArrangement = Arrangement.Center,
    modifier= Modifier.fillMaxSize()
        .padding(24.dp)){
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo",
            modifier= Modifier.size(165.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(10.dp)

        )
        Spacer(modifier= Modifier.height(15.dp))

    Text(text= "Welcome Back",
    fontSize=25.sp,
       // color= MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodyMedium,
    )
        Spacer(modifier= Modifier.height(8.dp))
        Text(text= "Login to continue with your account",
            fontSize=15.sp,
            //color=MaterialTheme.colorScheme.secondary,
            style= MaterialTheme.typography.bodyMedium,)

        Box(
            modifier = Modifier
                //.fillMaxWidth()
                .size(350.dp),
            contentAlignment = Alignment.Center,
            content = {
                Column {

                    OutlinedTextField( value = username,
                        onValueChange = { username= it},
                        label = { Text("Username")},
                        modifier = Modifier.fillMaxWidth())

                    Spacer(modifier= Modifier.height(20.dp))
                    OutlinedTextField(value = password,
                        onValueChange ={ password = it},
                        label = { Text("Password")},
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth())

                    Spacer(modifier= Modifier.height(30.dp))
    Box( modifier = Modifier.size(width = 450.dp, height = 45.dp),
        contentAlignment = Alignment.Center) {
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = { println("Login clicked") },
            modifier = Modifier.size(width = 200.dp, height = 45.dp).fillMaxSize(),
    onLoginSuccess()) {
            Text(text = "Login")
        }
    }

                  /*  Box(modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,) {
                  Text(
                            text = ("Continue with"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 15.sp,

                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }*/
                    Spacer(modifier= Modifier.height(20.dp))


/*Row{
    Button(
        onClick = { println("Google") },
        modifier = Modifier.size(width = 170.dp, height = 45.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Text(text = "Google")
    }
    Spacer(modifier = Modifier.width(8.dp))
    Button(
        onClick = { println("Facebook") },
        modifier = Modifier.size(width = 170.dp, height = 45.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Text(text = ("Facebook"))
    }
}*/
                    Box( modifier = Modifier.size(width = 450.dp, height = 45.dp),
                        contentAlignment = Alignment.Center) {
                        TextButton(onClick = onNavigateToSignup) {
                            Text(text = "Don't have an account? Sign up")
                        }
                    }
                }
            }
        )
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
fun LoginPreview() {
    CashflowTheme {

        Login()
    }
}