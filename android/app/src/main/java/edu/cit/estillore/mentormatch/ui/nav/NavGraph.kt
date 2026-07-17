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
import edu.cit.estillore.mentormatch.ui.dashboard.DashboardScreen
import edu.cit.estillore.mentormatch.ui.dashboard.DashboardViewModel

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard/{role}"

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

            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(appContainer.userRepository, appContainer.authRepository)
            )
            DashboardScreen(
                role = role,
                viewModel = dashboardViewModel,
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
