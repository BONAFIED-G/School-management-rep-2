package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolPortalApp(viewModel: SchoolViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header Bar
            if (currentScreen != Screen.Login) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCHOOL MANAGER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            val subtitle = when (val user = loggedInUser) {
                                is UserRole.Admin -> "Administrator Control"
                                is UserRole.StudentRole -> "Student: ${user.student.name}"
                                null -> "Access Control"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                when (currentScreen) {
                                    is Screen.StudentDetails, Screen.AddStudent -> viewModel.navigateTo(Screen.StudentsList)
                                    Screen.AddCourse -> viewModel.navigateTo(Screen.CoursesList)
                                    else -> {}
                                }
                            },
                            enabled = currentScreen is Screen.StudentDetails || currentScreen == Screen.AddStudent || currentScreen == Screen.AddCourse
                        ) {
                            if (currentScreen is Screen.StudentDetails || currentScreen == Screen.AddStudent || currentScreen == Screen.AddCourse) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        is Screen.Login -> LoginScreen(viewModel)
                        is Screen.StudentsList -> StudentsListScreen(viewModel)
                        is Screen.AddStudent -> AddStudentScreen(viewModel)
                        is Screen.CoursesList -> CoursesListScreen(viewModel)
                        is Screen.AddCourse -> AddCourseScreen(viewModel)
                        is Screen.NotificationsLog -> NotificationsLogScreen(viewModel)
                        is Screen.StudentDetails -> StudentDetailsScreen(viewModel, screen.studentId)
                        is Screen.StudentDashboard -> StudentDashboardScreen(viewModel)
                    }
                }
            }

            // Bottom Navigation for Admin session
            if (currentScreen != Screen.Login && loggedInUser is UserRole.Admin) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val isStudents = currentScreen is Screen.StudentsList || currentScreen is Screen.StudentDetails || currentScreen is Screen.AddStudent
                    NavigationBarItem(
                        selected = isStudents,
                        onClick = { viewModel.navigateTo(Screen.StudentsList) },
                        icon = { Icon(if (isStudents) Icons.Filled.People else Icons.Outlined.People, contentDescription = "Students") },
                        label = { Text("Students") },
                        modifier = Modifier.testTag("nav_students")
                    )
                    
                    val isCourses = currentScreen is Screen.CoursesList || currentScreen == Screen.AddCourse
                    NavigationBarItem(
                        selected = isCourses,
                        onClick = { viewModel.navigateTo(Screen.CoursesList) },
                        icon = { Icon(if (isCourses) Icons.Filled.Book else Icons.Outlined.Book, contentDescription = "Courses") },
                        label = { Text("Courses") },
                        modifier = Modifier.testTag("nav_courses")
                    )

                    val isNotifications = currentScreen is Screen.NotificationsLog
                    NavigationBarItem(
                        selected = isNotifications,
                        onClick = { viewModel.navigateTo(Screen.NotificationsLog) },
                        icon = { Icon(if (isNotifications) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications, contentDescription = "Alerts") },
                        label = { Text("AI Notices") },
                        modifier = Modifier.testTag("nav_notifications")
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: SchoolViewModel) {
    val matricInput by viewModel.loginMatric.collectAsState()
    val emailInput by viewModel.loginEmail.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    
    var showDirectHelp by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "App Icon",
                tint = Color.White,
                modifier = Modifier.size(45.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Academic Records Portal",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Verify credentials to check progress or manage accounts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Form Fields
        OutlinedTextField(
            value = matricInput,
            onValueChange = { viewModel.loginMatric.value = it },
            label = { Text("Matriculation Number / Username") },
            placeholder = { Text("e.g. STU-101 or ADMIN") },
            leadingIcon = { Icon(Icons.Default.CardMembership, contentDescription = "Matric Icon") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_matric_field"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emailInput,
            onValueChange = { viewModel.loginEmail.value = it },
            label = { Text("Registered Student/Admin Gmail Address") },
            placeholder = { Text("e.g. name@gmail.com") },
            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Email Icon") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_field"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Error message panel
        if (loginError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = loginError ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login {} },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Authenticate Account",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Demo Help Pane toggled
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { showDirectHelp = !showDirectHelp }
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Demo Info", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Access Credentials Reference",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (showDirectHelp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle info"
                    )
                }

                if (showDirectHelp) {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("Admin Control Role:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                        Text("Matric: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("ADMIN  ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(" | Gmail: ", style = MaterialTheme.typography.bodySmall)
                        Text("admin@school.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Text("Seeded Student Roles:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Row {
                            Text("Liam -> Matric: ", style = MaterialTheme.typography.bodySmall)
                            Text("STU-101", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(" | Gmail: ", style = MaterialTheme.typography.bodySmall)
                            Text("liam.j@gmail.com", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            Text("Sophia -> Matric: ", style = MaterialTheme.typography.bodySmall)
                            Text("STU-102", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(" | Gmail: ", style = MaterialTheme.typography.bodySmall)
                            Text("sophia.m@gmail.com", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            Text("Marcus -> Matric: ", style = MaterialTheme.typography.bodySmall)
                            Text("STU-103", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(" | Gmail: ", style = MaterialTheme.typography.bodySmall)
                            Text("marcus.v@gmail.com", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentsListScreen(viewModel: SchoolViewModel) {
    val studentsList by viewModel.students.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.AddStudent) },
                modifier = Modifier.testTag("add_student_fab"),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Registered Students List",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (studentsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "Empty list",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No students records saved yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Use the FAB at bottom to register one.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(studentsList) { student ->
                        StudentCard(
                            student = student,
                            onClick = { viewModel.navigateTo(Screen.StudentDetails(student.id)) },
                            onDelete = { viewModel.deleteStudent(student) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Scholar Profile") },
            text = { Text("Are you certain you wish to completely delete ${student.name}'s records (matric: ${student.matricNumber})? This is non-reversible.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Badge with profile hex colors
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color(android.graphics.Color.parseColor(student.photoColorHex)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.split(" ").map { it.take(1) }.joinToString("").take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(student.gradeLevel) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(24.dp).padding(end = 6.dp)
                    )
                    Text(
                        text = student.matricNumber,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mail, contentDescription = "Email", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = student.studentEmail,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Student",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AddStudentScreen(viewModel: SchoolViewModel) {
    val sName by viewModel.studentName.collectAsState()
    val sGrade by viewModel.studentGrade.collectAsState()
    val sEmail by viewModel.studentEmail.collectAsState()
    val sMatric by viewModel.studentMatric.collectAsState()
    val pName by viewModel.parentName.collectAsState()
    val pEmail by viewModel.parentEmail.collectAsState()
    val pPhone by viewModel.parentPhone.collectAsState()
    val sColor by viewModel.selectedColorHex.collectAsState()

    val colorOptions = listOf("#4F46E5", "#06B6D4", "#10B981", "#EC4899", "#F59E0B", "#8B5CF6", "#EF4444")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Register New Student Profile",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = sName,
            onValueChange = { viewModel.studentName.value = it },
            label = { Text("Full Legal Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
            modifier = Modifier.fillMaxWidth().testTag("form_student_name"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = sMatric,
                onValueChange = { viewModel.studentMatric.value = it },
                label = { Text("Matric Card ID") },
                placeholder = { Text("e.g. STU-105") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Matric") },
                modifier = Modifier.weight(1f).testTag("form_student_matric"),
                singleLine = true
            )

            // Simplistic Grade Level Dropdown selector
            var gradeExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = sGrade,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grade Level") },
                    trailingIcon = {
                        IconButton(onClick = { gradeExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, "Select Grade")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = gradeExpanded, onDismissRequest = { gradeExpanded = false }) {
                    val grades = listOf("Grade 9", "Grade 10", "Grade 11", "Grade 12")
                    grades.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = {
                                viewModel.studentGrade.value = g
                                gradeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = sEmail,
            onValueChange = { viewModel.studentEmail.value = it },
            label = { Text("Student Gmail Address (Login Credentials)") },
            placeholder = { Text("name@gmail.com") },
            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("form_student_email"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Primary Parent / Guardian Contact Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = pName,
            onValueChange = { viewModel.parentName.value = it },
            label = { Text("Parent/Guardian Name") },
            leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = "Relative") },
            modifier = Modifier.fillMaxWidth().testTag("form_parent_name"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pEmail,
            onValueChange = { viewModel.parentEmail.value = it },
            label = { Text("Parent Gateway Email") },
            placeholder = { Text("parent@example.com") },
            leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = "Parent Mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("form_parent_email"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pPhone,
            onValueChange = { viewModel.parentPhone.value = it },
            label = { Text("Parent Mobile Contact") },
            placeholder = { Text("+1 555-5555") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().testTag("form_parent_phone"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Color badge selector
        Text("Custom Profile Theme Color Badge:", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colorOptions.forEach { col ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = Color(android.graphics.Color.parseColor(col)), shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { viewModel.selectedColorHex.value = col }
                        .padding(2.dp)
                ) {
                    if (sColor == col) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, "Selected", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.saveStudent {
                    viewModel.navigateTo(Screen.StudentsList)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_student_button")
        ) {
            Text("Register Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun CoursesListScreen(viewModel: SchoolViewModel) {
    val coursesList by viewModel.courses.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.AddCourse) },
                modifier = Modifier.testTag("add_course_fab"),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Course")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Academic Courses Listing",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (coursesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = "Empty",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No courses defined for selection.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Click the FAB button to add courses.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(coursesList) { course ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ImportContacts, contentDescription = "Course Icon", tint = MaterialTheme.colorScheme.primary)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CoPresent, "Teacher", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(course.teacherName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, "Schedule", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(course.schedule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteCourse(course) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCourseScreen(viewModel: SchoolViewModel) {
    val name by viewModel.courseName.collectAsState()
    val teacher by viewModel.teacherName.collectAsState()
    val schedule by viewModel.courseSchedule.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create New Course Record",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.courseName.value = it },
            label = { Text("Course Name / Code") },
            placeholder = { Text("e.g. Modern Chemistry AP") },
            leadingIcon = { Icon(Icons.Default.Book, contentDescription = "Course Name") },
            modifier = Modifier.fillMaxWidth().testTag("form_course_name"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = teacher,
            onValueChange = { viewModel.teacherName.value = it },
            label = { Text("Assigned Teacher Name") },
            placeholder = { Text("e.g. Dr. Alfred Nobel") },
            leadingIcon = { Icon(Icons.Default.CoPresent, contentDescription = "Teacher Name") },
            modifier = Modifier.fillMaxWidth().testTag("form_course_teacher"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = schedule,
            onValueChange = { viewModel.courseSchedule.value = it },
            label = { Text("Weekly Schedule Timing") },
            placeholder = { Text("e.g. Mon/Wed/Fri 10:00 AM") },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Timings") },
            modifier = Modifier.fillMaxWidth().testTag("form_course_schedule"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.saveCourse {
                    viewModel.navigateTo(Screen.CoursesList)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_course_button")
        ) {
            Text("Publish Course", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun NotificationsLogScreen(viewModel: SchoolViewModel) {
    val noticesList by viewModel.allNotifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "AI-Driven Parent Updates Hub",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Intelligent automated notices that update parents instantly about performance warnings or achievements.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (noticesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Empty Logo",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No parent communications queued.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(noticesList) { notice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (notice.type) {
                                                "Attendance Alert" -> MaterialTheme.colorScheme.errorContainer
                                                "Grade Update" -> MaterialTheme.colorScheme.primaryContainer
                                                "Behavior Alert" -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> MaterialTheme.colorScheme.tertiaryContainer
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = notice.type,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = notice.status,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                IconButton(
                                    onClick = { viewModel.deleteNotification(notice) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Delete", modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = notice.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notice.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDetailsScreen(viewModel: SchoolViewModel, studentId: Long) {
    val student by viewModel.getStudentById(studentId).collectAsState(initial = null)
    val coursesList by viewModel.courses.collectAsState()
    val attendanceList by viewModel.allAttendance.collectAsState()
    val gradesList by viewModel.allGrades.collectAsState()
    val behaviorList by viewModel.allBehaviorLogs.collectAsState()

    // Forms Flow State inside Student detail page
    val currentCourseAtt by viewModel.attCourseId.collectAsState()
    val currentStatusAtt by viewModel.attStatus.collectAsState()
    val currentRemarksAtt by viewModel.attRemarks.collectAsState()

    val currentCourseGrade by viewModel.gradeCourseId.collectAsState()
    val currentAssessGrade by viewModel.gradeAssessmentName.collectAsState()
    val currentScoreGrade by viewModel.gradeScore.collectAsState()
    val currentMaxScoreGrade by viewModel.gradeMaxScore.collectAsState()

    val currentCategoryBeh by viewModel.behCategory.collectAsState()
    val currentTypeBeh by viewModel.behType.collectAsState()
    val currentDescBeh by viewModel.behDescription.collectAsState()
    val currentPtsBeh by viewModel.behPoints.collectAsState()

    val isGeneratingReport by viewModel.isGeneratingReport.collectAsState()
    val generatedReport by viewModel.generatedReport.collectAsState()

    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val st = student!!
    
    var currentTab by remember { mutableStateOf(0) } // 0: Profile/AI Counseling, 1: Attendance, 2: Grades, 3: Behavior
    var selectedSemesterFilter by remember { mutableStateOf("All") }

    // Filter reports data specifically for this particular student
    val studentAttendance = attendanceList.filter { 
        it.studentId == st.id && (selectedSemesterFilter == "All" || it.semester == selectedSemesterFilter) 
    }
    val studentGrades = gradesList.filter { 
        it.studentId == st.id && (selectedSemesterFilter == "All" || it.semester == selectedSemesterFilter) 
    }
    val studentBehaviors = behaviorList.filter { it.studentId == st.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Scholar Identity Header Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(android.graphics.Color.parseColor(st.photoColorHex)).copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color(android.graphics.Color.parseColor(st.photoColorHex)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        st.name.take(1) + st.name.split(" ").getOrNull(1)?.take(1).orEmpty(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(st.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Matric: ${st.matricNumber}  |  ${st.gradeLevel}", style = MaterialTheme.typography.bodySmall)
                    Text("Registered Gmail: ${st.studentEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Semester Filter Row
        Text(
            text = "Semester View Scope:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val semesters = listOf("All", "Spring 2026", "Fall 2025", "Summer 2025")
            semesters.forEach { sem ->
                FilterChip(
                    selected = selectedSemesterFilter == sem,
                    onClick = { selectedSemesterFilter = sem },
                    label = { Text(sem) },
                    modifier = Modifier.testTag("semester_filter_$sem")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row Choices
        TabRow(
            selectedTabIndex = currentTab,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = currentTab == 0, onClick = { currentTab = 0 }) {
                Text("Study Hub", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentTab == 1, onClick = { currentTab = 1 }) {
                Text("Attendance", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentTab == 2, onClick = { currentTab = 2 }) {
                Text("Grades", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentTab == 3, onClick = { currentTab = 3 }) {
                Text("Behavior", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content switching
        when (currentTab) {
            0 -> {
                // profile contact and AI performance Summarization Tool
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Family & Contact Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FamilyRestroom, "Parent", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardian: ${st.parentName}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MailOutline, "Email", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gateway: ${st.parentEmail}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, "Phone", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mobile: ${st.parentPhone}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Counselor report block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, "AI", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Intelligent Study Advisory Review",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Leverage Gemini generative intelligence to synthesize grades, discipline points, and attendance patterns into an instant action plan.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (isGeneratingReport) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Analyzing record databases with Gemini...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.requestAcademicSummaryReport(st.id) },
                                modifier = Modifier.fillMaxWidth().testTag("generate_report_button")
                            ) {
                                Text("Compile Academic Performance Audit")
                            }
                        }

                        if (generatedReport != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "AUDIT REVIEW CARD",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = generatedReport ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, "Alert", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Saved to communication history log.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Attendance Tab
                AttendanceDistributionChart(studentAttendance)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Log Daily Attendance Attendance Record:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Log form
                if (coursesList.isEmpty()) {
                    Text("No school courses database defined. Please define a course first.", color = MaterialTheme.colorScheme.error)
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Select Course
                            var attCourseExpanded by remember { mutableStateOf(false) }
                            val courseSelected = coursesList.firstOrNull { it.id == currentCourseAtt }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = courseSelected?.name ?: "Click to Select Course",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Applies to Course") },
                                    trailingIcon = {
                                        IconButton(onClick = { attCourseExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, "Course list")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(expanded = attCourseExpanded, onDismissRequest = { attCourseExpanded = false }) {
                                    coursesList.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c.name) },
                                            onClick = {
                                                viewModel.attCourseId.value = c.id
                                                attCourseExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Attendance status choice
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("Present", "Absent", "Tardy").forEach { status ->
                                    FilterChip(
                                        selected = currentStatusAtt == status,
                                        onClick = { viewModel.attStatus.value = status },
                                        label = { Text(status) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = currentRemarksAtt,
                                onValueChange = { viewModel.attRemarks.value = it },
                                label = { Text("Special Remarks / Reason") },
                                placeholder = { Text("e.g. excuse note or details") },
                                modifier = Modifier.fillMaxWidth().testTag("attendance_remarks"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Select Semester for logged attendance
                            var attSemExpanded by remember { mutableStateOf(false) }
                            val currentAttSem by viewModel.attSemester.collectAsState()
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = currentAttSem,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Academic Semester") },
                                    trailingIcon = {
                                        IconButton(onClick = { attSemExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, "Semester list")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(expanded = attSemExpanded, onDismissRequest = { attSemExpanded = false }) {
                                    listOf("Spring 2026", "Fall 2025", "Summer 2025").forEach { sem ->
                                        DropdownMenuItem(
                                            text = { Text(sem) },
                                            onClick = {
                                                viewModel.attSemester.value = sem
                                                attSemExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (currentCourseAtt != null) {
                                        viewModel.addAttendance(st.id) {}
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("add_attendance_button")
                            ) {
                                Text("Add Attendance Record")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Logs Register File:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (studentAttendance.isEmpty()) {
                    Text("No registered attendance logs on file.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    studentAttendance.forEach { att ->
                        val corrCourse = coursesList.firstOrNull { it.id == att.courseId }?.name ?: "Course"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = when (att.status) {
                                                "Present" -> Color(0xFF10B981)
                                                "Tardy" -> Color(0xFFF59E0B)
                                                else -> Color(0xFFEF4444)
                                            },
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("$corrCourse: ${att.status}", fontWeight = FontWeight.Bold)
                                    if (att.remarks.isNotEmpty()) {
                                        Text(att.remarks, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${att.date}  |  ${att.semester}", style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { viewModel.deleteAttendance(att) }) {
                                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Grades Tab
                Text("Add Performance Assessment Grade:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (coursesList.isEmpty()) {
                    Text("Please configure a school course first.", color = MaterialTheme.colorScheme.error)
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Select Course dropdown
                            var gradeCourseExpanded by remember { mutableStateOf(false) }
                            val courseSelected = coursesList.firstOrNull { it.id == currentCourseGrade }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = courseSelected?.name ?: "Click to Select Course",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Subject Course") },
                                    trailingIcon = {
                                        IconButton(onClick = { gradeCourseExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, "Course list")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(expanded = gradeCourseExpanded, onDismissRequest = { gradeCourseExpanded = false }) {
                                    coursesList.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c.name) },
                                            onClick = {
                                                viewModel.gradeCourseId.value = c.id
                                                gradeCourseExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = currentAssessGrade,
                                onValueChange = { viewModel.gradeAssessmentName.value = it },
                                label = { Text("Assessment Task Title") },
                                placeholder = { Text("e.g. Midterm Physics, Calculus Test 2") },
                                modifier = Modifier.fillMaxWidth().testTag("grade_assessment"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = currentScoreGrade,
                                    onValueChange = { viewModel.gradeScore.value = it },
                                    label = { Text("Marks") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("grade_score"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = currentMaxScoreGrade,
                                    onValueChange = { viewModel.gradeMaxScore.value = it },
                                    label = { Text("Max") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("grade_max"),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Select Semester for logged grade
                            var gradeSemExpanded by remember { mutableStateOf(false) }
                            val currentGradeSem by viewModel.gradeSemester.collectAsState()
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = currentGradeSem,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Academic Semester") },
                                    trailingIcon = {
                                        IconButton(onClick = { gradeSemExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, "Semester list")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(expanded = gradeSemExpanded, onDismissRequest = { gradeSemExpanded = false }) {
                                    listOf("Spring 2026", "Fall 2025", "Summer 2025").forEach { sem ->
                                        DropdownMenuItem(
                                            text = { Text(sem) },
                                            onClick = {
                                                viewModel.gradeSemester.value = sem
                                                gradeSemExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (currentCourseGrade != null && currentAssessGrade.isNotEmpty()) {
                                        viewModel.addGrade(st.id) {}
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("add_grade_button")
                            ) {
                                Text("Post Scholar Grade Outcome")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Academic Assessment Records:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (studentGrades.isEmpty()) {
                    Text("No graded performance assessments yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    studentGrades.forEach { gr ->
                        val corrCourse = coursesList.firstOrNull { it.id == gr.courseId }?.name ?: "Course"
                        val percent = if (gr.maxScore > 0) (gr.score / gr.maxScore) * 100 else 0.0
                        val fmt = DecimalFormat("#.#")
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .background(
                                            color = when {
                                                percent >= 85 -> Color(0xFF10B981).copy(alpha = 0.15f)
                                                percent >= 60 -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${fmt.format(percent)}%",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                percent >= 85 -> Color(0xFF10B981)
                                                percent >= 60 -> Color(0xFFD97706)
                                                else -> Color(0xFFEF4444)
                                            }
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("$corrCourse: ${gr.assessmentName}", fontWeight = FontWeight.Bold)
                                    Text("Score details: ${gr.score} / ${gr.maxScore}  |  ${gr.semester}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(gr.date, style = MaterialTheme.typography.labelSmall)
                                }

                                IconButton(onClick = { viewModel.deleteGrade(gr) }) {
                                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Behavior tab
                Text("Log Behavioral Performance Incident:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                selected = currentCategoryBeh == "Positive",
                                onClick = {
                                    viewModel.behCategory.value = "Positive"
                                    viewModel.behType.value = "Leadership"
                                },
                                label = { Text("Positive (+)") }
                            )
                            FilterChip(
                                selected = currentCategoryBeh == "Negative",
                                onClick = {
                                    viewModel.behCategory.value = "Negative"
                                    viewModel.behType.value = "Disruptive"
                                },
                                label = { Text("Negative (-)") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Type dropdown values
                        var behExpanded by remember { mutableStateOf(false) }
                        val positiveTypes = listOf("Leadership", "Participation", "Helpfulness", "Collaboration", "Extracurricular")
                        val negativeTypes = listOf("Disruptive", "Late Assignment", "Tardy Violation", "Unprepared", "Inattention")
                        val currentList = if (currentCategoryBeh == "Positive") positiveTypes else negativeTypes

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = currentTypeBeh,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Behavior Designation") },
                                trailingIcon = {
                                    IconButton(onClick = { behExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Type list")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = behExpanded, onDismissRequest = { behExpanded = false }) {
                                currentList.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p) },
                                        onClick = {
                                            viewModel.behType.value = p
                                            behExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = currentPtsBeh,
                                onValueChange = { viewModel.behPoints.value = it },
                                label = { Text("Deed Impact Points") },
                                placeholder = { Text("e.g. 5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("behavior_points"),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentDescBeh,
                            onValueChange = { viewModel.behDescription.value = it },
                            label = { Text("Deed Incident Description Details") },
                            placeholder = { Text("e.g. Cleared lab equipment, talked during exam") },
                            modifier = Modifier.fillMaxWidth().testTag("behavior_desc"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (currentDescBeh.isNotEmpty()) {
                                    viewModel.addBehaviorLog(st.id) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_behavior_button")
                        ) {
                            Text("Post Behavioral Impact Ledger")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Student Behavioral Ledger History:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (studentBehaviors.isEmpty()) {
                    Text("Scholar has an outstanding completely neutral behavioral score ledger.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    studentBehaviors.forEach { log ->
                        val isPos = log.category == "Positive"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPos) Icons.Default.ThumbUp else Icons.Default.Warning,
                                    contentDescription = "Behavior indicator",
                                    tint = if (isPos) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${log.behaviorType} (${if(log.pointsDelta > 0) "+" else ""}${log.pointsDelta} points)", fontWeight = FontWeight.Bold)
                                    Text(log.description, style = MaterialTheme.typography.bodySmall)
                                    Text(log.date, style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { viewModel.deleteBehaviorLog(log) }) {
                                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- STUDENT LOGIN MODE SPECIFIC VIEW ---
@Composable
fun StudentDashboardScreen(viewModel: SchoolViewModel) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val coursesList by viewModel.courses.collectAsState()
    val enrollmentsList by viewModel.allEnrollments.collectAsState()
    val attendanceList by viewModel.allAttendance.collectAsState()
    val gradesList by viewModel.allGrades.collectAsState()
    val behaviorList by viewModel.allBehaviorLogs.collectAsState()

    val isGeneratingReport by viewModel.isGeneratingReport.collectAsState()
    val generatedReport by viewModel.generatedReport.collectAsState()

    if (loggedInUser !is UserRole.StudentRole) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Text("Security Clearance Denied. Please connect to student session.")
         }
         return
    }

    val st = (loggedInUser as UserRole.StudentRole).student

    var selectedSemesterFilter by remember { mutableStateOf("All") }

    // Filter calculations
    val rawStudentAttendance = attendanceList.filter { it.studentId == st.id }
    val rawStudentGrades = gradesList.filter { it.studentId == st.id }

    val studentAttendance = rawStudentAttendance.filter {
        selectedSemesterFilter == "All" || it.semester == selectedSemesterFilter
    }
    val studentGrades = rawStudentGrades.filter {
        selectedSemesterFilter == "All" || it.semester == selectedSemesterFilter
    }
    
    val studentBehaviors = behaviorList.filter { it.studentId == st.id }
    val studentEnrollments = enrollmentsList.filter { it.studentId == st.id }

    val courseMap = coursesList.associateBy { it.id }

    // GPA calculations
    val averageGrade = if (studentGrades.isNotEmpty()) {
        studentGrades.map { (it.score / it.maxScore) * 100 }.average()
    } else 0.0

    val attendanceRate = if (studentAttendance.isNotEmpty()) {
        val presents = studentAttendance.count { it.status == "Present" || it.status == "Tardy" }
        (presents.toDouble() / studentAttendance.size) * 100
    } else 100.0

    val behaviorScore = 100 + studentBehaviors.sumOf { it.pointsDelta }

    var studentSubTab by remember { mutableStateOf(0) } // 0: Performance Hub, 1: Course Selector, 2: AI Advisor Guidance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Scholar Greeting card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(android.graphics.Color.parseColor(st.photoColorHex)),
                            Color(android.graphics.Color.parseColor(st.photoColorHex)).copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, "Academic Hub", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Welcome, ${st.name}!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Scholastic matriculation: ${st.matricNumber}  |  ${st.gradeLevel}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Score cards
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GRADE SCORE", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        val fmt = DecimalFormat("#.#")
                        Text(if (studentGrades.isNotEmpty()) "${fmt.format(averageGrade)}%" else "N/A", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ATTENDANCE", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        val fmt = DecimalFormat("#.#")
                        Text("${fmt.format(attendanceRate)}%", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BEHAVIOR LDG", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text("$behaviorScore pts", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Semester Filter Row
        Text(
            text = "Semester View Scope:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val semesters = listOf("All", "Spring 2026", "Fall 2025", "Summer 2025")
            semesters.forEach { sem ->
                FilterChip(
                    selected = selectedSemesterFilter == sem,
                    onClick = { selectedSemesterFilter = sem },
                    label = { Text(sem) },
                    modifier = Modifier.testTag("dashboard_semester_filter_$sem")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Student mode Sub tab selections
        TabRow(
            selectedTabIndex = studentSubTab,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = studentSubTab == 0, onClick = { studentSubTab = 0 }) {
                Text("My Progress", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = studentSubTab == 1, onClick = { studentSubTab = 1 }) {
                Text("My Course List", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = studentSubTab == 2, onClick = { studentSubTab = 2 }) {
                Text("AI Study Coach", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (studentSubTab) {
            0 -> {
                // MY PROGRESS HUB (Attendance list, Performance Grades list, Behavior incident log list)
                AttendanceDistributionChart(studentAttendance)
                Spacer(modifier = Modifier.height(16.dp))

                Text("My Performance Outcomes Register:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (studentGrades.isEmpty()) {
                    Text("No grading assessments logged yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    studentGrades.forEach { gr ->
                        val corrCourse = courseMap[gr.courseId]?.name ?: "Subject Class"
                        val percent = (gr.score / gr.maxScore) * 100
                        val fmt = DecimalFormat("#.#")
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${fmt.format(percent)}%",
                                    fontWeight = FontWeight.Bold,
                                    color = if (percent >= 85) Color(0xFF10B981) else if (percent >= 60) Color(0xFFF59E0B) else Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("$corrCourse: ${gr.assessmentName}", fontWeight = FontWeight.Bold)
                                    Text("Scored: ${gr.score} / ${gr.maxScore}  |  ${gr.semester}  |  ${gr.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("My Attendance Ledger:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (studentAttendance.isEmpty()) {
                    Text("No attendance logs posted yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    studentAttendance.forEach { att ->
                        val corrCourse = courseMap[att.courseId]?.name ?: "Subject Class"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.CenterVertically)
                                        .background(
                                            color = when(att.status) {
                                                "Present" -> Color(0xFF10B981)
                                                "Tardy" -> Color(0xFFF59E0B)
                                                else -> Color(0xFFEF4444)
                                            },
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$corrCourse: Status is ${att.status}", fontWeight = FontWeight.Bold)
                                    if (att.remarks.isNotEmpty()) {
                                        Text("Remarks: ${att.remarks}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("${att.date}  |  ${att.semester}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // CHOOSE COURSES THEY WANT TO OFFER
                Text("Offered Courses Registration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Browse courses provided by teachers below. Opt-in/Offer any course to begin your study journey or withdraw anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (coursesList.isEmpty()) {
                    Text("No academic courses have been programmed by school admin yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    coursesList.forEach { course ->
                        val isOffering = studentEnrollments.any { it.courseId == course.id }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOffering) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.padding(bottom = 10.dp).fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Teacher: ${course.teacherName}", style = MaterialTheme.typography.bodySmall)
                                    Text("Days/Hours: ${course.schedule}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    if (isOffering) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Enrolled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (isOffering) {
                                            viewModel.withdrawFromCourse(st.id, course.id)
                                        } else {
                                            viewModel.enrollInCourse(st.id, course.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isOffering) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("course_action_${course.id}")
                                ) {
                                    Text(if (isOffering) "Withdraw" else "Offer Course", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // STUDENT AI ADVISORY GUIDANCE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, "AI Study Advisor", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Your Personal Study Companion",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Get diagnostic study blueprints, growth metrics, and custom checklists created instantly relative to your live academic performance numbers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isGeneratingReport) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Engaging AI counseling algorithms...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.requestAcademicSummaryReport(st.id) },
                                modifier = Modifier.fillMaxWidth().testTag("student_ai_audit_button")
                            ) {
                                Text("Create Real-time Growth Blueprint")
                            }
                        }

                        if (generatedReport != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "AI GROWTH BLUEPRINT",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = generatedReport ?: "",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceDistributionChart(attendanceList: List<Attendance>, modifier: Modifier = Modifier) {
    val total = attendanceList.size
    val presentCount = attendanceList.count { it.status == "Present" }
    val tardyCount = attendanceList.count { it.status == "Tardy" }
    val absentCount = attendanceList.count { it.status == "Absent" }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analytics Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Attendance Metrics & Distribution",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (total == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No records in selected semester scope.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val presentPct = presentCount.toFloat() / total
                val tardyPct = tardyCount.toFloat() / total
                val absentPct = absentCount.toFloat() / total

                val presentPctStr = DecimalFormat("#.#").format(presentPct * 100)
                val tardyPctStr = DecimalFormat("#.#").format(tardyPct * 100)
                val absentPctStr = DecimalFormat("#.#").format(absentPct * 100)

                val attendanceRate = ((presentCount + tardyCount).toDouble() / total) * 100
                val attendanceRateStr = DecimalFormat("#.#").format(attendanceRate)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ATTENDANCE RATE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$attendanceRateStr%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = when {
                                attendanceRate >= 90.0 -> Color(0xFF10B981)
                                attendanceRate >= 75.0 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                        )
                    }

                    Surface(
                        color = when {
                            attendanceRate >= 90.0 -> Color(0xFF10B981).copy(alpha = 0.15f)
                            attendanceRate >= 75.0 -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when {
                                attendanceRate >= 90.0 -> "Outstanding"
                                attendanceRate >= 75.0 -> "Satisfactory"
                                else -> "Warning Alert"
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                attendanceRate >= 90.0 -> Color(0xFF10B981)
                                attendanceRate >= 75.0 -> Color(0xFFD97706)
                                else -> Color(0xFFEF4444)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (presentPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(presentPct)
                                .background(Color(0xFF10B981))
                        )
                    }
                    if (tardyPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(tardyPct)
                                .background(Color(0xFFF59E0B))
                        )
                    }
                    if (absentPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(absentPct)
                                .background(Color(0xFFEF4444))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Present",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$presentCount ($presentPctStr%)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFF59E0B), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Tardy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$tardyCount ($tardyPctStr%)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFEF4444), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Absent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$absentCount ($absentPctStr%)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
