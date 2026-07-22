package edu.cit.estillore.mentormatch.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.estillore.mentormatch.AppContainer
import edu.cit.estillore.mentormatch.data.model.Role
import edu.cit.estillore.mentormatch.ui.auth.AuthViewModel
import edu.cit.estillore.mentormatch.ui.auth.LoginScreen
import edu.cit.estillore.mentormatch.ui.auth.RegisterScreen
import edu.cit.estillore.mentormatch.ui.dashboard.AdminDashboardScreen
import edu.cit.estillore.mentormatch.ui.dashboard.AdminDashboardViewModel
import edu.cit.estillore.mentormatch.ui.dashboard.MentorDashboardScreen
import edu.cit.estillore.mentormatch.ui.dashboard.MentorDashboardViewModel
import edu.cit.estillore.mentormatch.ui.dashboard.StudentDashboardScreen
import edu.cit.estillore.mentormatch.ui.dashboard.StudentDashboardViewModel
import edu.cit.estillore.mentormatch.ui.notification.NotificationViewModel
import edu.cit.estillore.mentormatch.ui.tutor.MentorProfileEditScreen
import edu.cit.estillore.mentormatch.ui.tutor.MentorProfileEditViewModel
import edu.cit.estillore.mentormatch.ui.tutor.TutorSearchViewModel

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard/{role}"
    const val MENTOR_PROFILE_EDIT = "mentor/profile/edit"

    fun dashboard(role: Role) = "dashboard/${role.name}"
}

@Composable
fun MentorMatchNavGraph(
    appContainer: AppContainer,
    startDestination: String = Routes.LOGIN
) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.Factory(appContainer.authRepository)
            )
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { auth ->
                    navController.navigate(Routes.dashboard(auth.user.role)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.Factory(appContainer.authRepository)
            )
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { auth ->
                    navController.navigate(Routes.dashboard(auth.user.role)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) { backStackEntry ->
            val roleArg = backStackEntry.arguments?.getString("role") ?: Role.STUDENT.name
            val role = Role.valueOf(roleArg)

            val notificationViewModel: NotificationViewModel = viewModel(
                factory = NotificationViewModel.Factory(appContainer.notificationRepository)
            )

            val onLoggedOut: () -> Unit = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }

            when (role) {
                Role.ADMIN -> {
                    val adminViewModel: AdminDashboardViewModel = viewModel(
                        factory = AdminDashboardViewModel.Factory(
                            userRepository = appContainer.userRepository,
                            subjectRepository = appContainer.subjectRepository,
                            tutorProfileRepository = appContainer.tutorProfileRepository,
                            reportRepository = appContainer.reportRepository,
                            authRepository = appContainer.authRepository
                        )
                    )
                    AdminDashboardScreen(
                        viewModel = adminViewModel,
                        notificationViewModel = notificationViewModel,
                        onLoggedOut = onLoggedOut
                    )
                }

                Role.MENTOR -> {
                    val mentorViewModel: MentorDashboardViewModel = viewModel(
                        factory = MentorDashboardViewModel.Factory(
                            bookingRepository = appContainer.bookingRepository,
                            tutorProfileRepository = appContainer.tutorProfileRepository,
                            reviewRepository = appContainer.reviewRepository,
                            authRepository = appContainer.authRepository
                        )
                    )
                    MentorDashboardScreen(
                        viewModel = mentorViewModel,
                        notificationViewModel = notificationViewModel,
                        onEditProfile = { navController.navigate(Routes.MENTOR_PROFILE_EDIT) },
                        onLoggedOut = onLoggedOut
                    )
                }

                Role.STUDENT -> {
                    val studentViewModel: StudentDashboardViewModel = viewModel(
                        factory = StudentDashboardViewModel.Factory(
                            bookingRepository = appContainer.bookingRepository,
                            reviewRepository = appContainer.reviewRepository,
                            authRepository = appContainer.authRepository
                        )
                    )
                    val tutorSearchViewModel: TutorSearchViewModel = viewModel(
                        factory = TutorSearchViewModel.Factory(
                            tutorProfileRepository = appContainer.tutorProfileRepository,
                            subjectRepository = appContainer.subjectRepository,
                            bookingRepository = appContainer.bookingRepository
                        )
                    )
                    StudentDashboardScreen(
                        viewModel = studentViewModel,
                        tutorSearchViewModel = tutorSearchViewModel,
                        notificationViewModel = notificationViewModel,
                        onLoggedOut = onLoggedOut
                    )
                }
            }
        }

        composable(Routes.MENTOR_PROFILE_EDIT) {
            val editViewModel: MentorProfileEditViewModel = viewModel(
                factory = MentorProfileEditViewModel.Factory(
                    tutorProfileRepository = appContainer.tutorProfileRepository,
                    subjectRepository = appContainer.subjectRepository,
                    availabilityRepository = appContainer.availabilityRepository
                )
            )
            MentorProfileEditScreen(
                viewModel = editViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
