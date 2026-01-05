package com.example.odyssey

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.UserRepoImpl
import com.example.odyssey.ui.theme.ODYSSEYTheme
import java.util.Calendar

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ODYSSEYTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    SignUpBody()
                }
            }
        }
    }
}

@Composable
fun SignUpBody() {

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }
    var selectDate by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("Select Gender") }
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    val context = LocalContext.current
    val activity = context as Activity
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val options = listOf("Male", "Female")

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datepicker = DatePickerDialog(
        context,
        { _, mYear, mMonth, mDay ->
            selectDate = "$mDay/${mMonth + 1}/$mYear"
        }, year, month, day
    )

    Scaffold(
        containerColor = Color(0xFF4A7CFF)
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(Color.White, RoundedCornerShape(30.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Sign Up",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3460FB)
                )

                Text(
                    text = "Welcome to ODYSSEY",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(25.dp))

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    placeholder = { Text("First Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFE0E6FF),
                        unfocusedIndicatorColor = Color(0xFFE0E6FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    placeholder = { Text("Last Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFE0E6FF),
                        unfocusedIndicatorColor = Color(0xFFE0E6FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    placeholder = { Text("example@gmail.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFE0E6FF),
                        unfocusedIndicatorColor = Color(0xFFE0E6FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    placeholder = { Text("Password") },
                    trailingIcon = {
                        IconButton(onClick = { visibility = !visibility }) {
                            Icon(
                                painter = if (visibility)
                                    painterResource(R.drawable.baseline_visibility_off_24)
                                else painterResource(R.drawable.baseline_visibility_24),
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation =
                        if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFE0E6FF),
                        unfocusedIndicatorColor = Color(0xFFE0E6FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker
                OutlinedTextField(
                    value = selectDate,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datepicker.show() },
                    shape = RoundedCornerShape(50.dp),
                    placeholder = { Text("Select Date") },
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color.White,
                        disabledTextColor = Color.Black,
                        unfocusedIndicatorColor = Color(0xFFE0E6FF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gender Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedOptionText,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                textFieldSize = it.size.toSize()
                            }
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(50.dp),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(
                                    if (expanded)
                                        R.drawable.outline_keyboard_arrow_up_24
                                    else R.drawable.baseline_keyboard_arrow_down_24
                                ),
                                contentDescription = null
                            )
                        },
                        placeholder = { Text("Select Gender") },
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black,
                            unfocusedIndicatorColor = Color(0xFFE0E6FF)
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(
                            with(LocalDensity.current) {
                                textFieldSize.width.toDp()
                            }
                        )
                    ) {
                        options.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    selectedOptionText = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Terms
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = terms,
                        onCheckedChange = { terms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF3460FB)
                        )
                    )
                    Text("I agree to the term and condition", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Register Button (LOGIC UNCHANGED)
                Button(
                    onClick = {
                        if (!terms) {
                            Toast.makeText(
                                context,
                                "please agree to terms &  conditions",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            userViewModel.register(email.trim(),password.trim()) {
                                    success, message, userId ->
                                if(success) {
                                    var model = UserModel(
                                        userId = userId,
                                        firstName = firstName,
                                        lastName = lastName,
                                        gender = selectedOptionText,
                                        email = email,
                                        dob = selectDate,
                                        imageUrl = ""
                                    )
                                    userViewModel.addUserToDatabase(
                                        userId,model
                                    ) {
                                            success,message ->
                                        if(success) {
                                            Toast.makeText(
                                                context,
                                                message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }else {
                                            Toast.makeText(
                                                context,
                                                message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }else{
                                    Toast.makeText(
                                        context,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3460FB)
                    )
                ) {
                    Text("Register", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF3460FB),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Login")
                        }
                    },
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity.finish()
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Preview
@Composable
fun SignUpPreview(){
    SignUpBody()
}