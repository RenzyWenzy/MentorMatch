package edu.cit.estillore.mentormatch.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import edu.cit.estillore.mentormatch.data.model.AuthResponse
import edu.cit.estillore.mentormatch.data.model.Role

/**
 * Note: ADMIN is intentionally excluded from the role picker — the backend
 * (UserServiceImpl.registerUser) rejects self-registered admin accounts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: (AuthResponse) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.STUDENT) }

    // Student-only
    var studentNumber by remember { mutableStateOf("") }
    var program by remember { mutableStateOf("") }

    // Mentor-only
    var expertise by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess((uiState as AuthUiState.Success).auth)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Create an account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName, onValueChange = { fullName = it },
            label = { Text("Full name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password (min 8 characters)") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it },
            label = { Text("Confirm password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        Text("I am a...", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = role == Role.STUDENT,
                onClick = { role = Role.STUDENT },
                label = { Text("Student") }
            )
            FilterChip(
                selected = role == Role.MENTOR,
                onClick = { role = Role.MENTOR },
                label = { Text("Mentor") }
            )
        }

        Spacer(Modifier.height(12.dp))

        if (role == Role.STUDENT) {
            OutlinedTextField(
                value = studentNumber, onValueChange = { studentNumber = it },
                label = { Text("Student number") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = program, onValueChange = { program = it },
                label = { Text("Program") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = expertise, onValueChange = { expertise = it },
                label = { Text("Expertise") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = department, onValueChange = { department = it },
                label = { Text("Department") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState is AuthUiState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.register(
                    fullName = fullName,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    role = role,
                    studentNumber = studentNumber,
                    program = program,
                    expertise = expertise,
                    department = department
                )
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Sign Up")
            }
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in")
        }
        Spacer(Modifier.height(16.dp))
    }
}
