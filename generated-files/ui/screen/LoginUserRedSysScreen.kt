package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 224-5: import android.widget.Toast
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.LaunchedEffect
@Composable
fun LoginUserRedSysScreen(
    viewModel: ifac.td.taxi.viewmodel.LoginUserRedSysComposeViewModel,
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStack() },
    onNavigateToChangePassword: () -> Unit = {
        navController.navigate(R.id.action_loginUserRedSysFragment_to_changePasswordRedSysFragment)
    }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        viewModel.getUserRedSys()
    }
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is LoginUserRedSysUiEffect.ShowToast -> {
                        Toast.makeText(context, context.getString(effect.resId), Toast.LENGTH_SHORT).show()
                    }
                    LoginUserRedSysUiEffect.NavigateBack -> onBack()
                    LoginUserRedSysUiEffect.NavigateToChangePassword -> onNavigateToChangePassword()
                }
            }
        }
    }
    LoginUserRedSysContent(
        uiState = uiState,
        onUserChange = viewModel::onUserChange,
        onPasswordChange = viewModel::onPasswordChange,
        onCancelClick = viewModel::onCancelClick,
        onAcceptClick = viewModel::onAcceptClick,
        onDialogDismiss = viewModel::onDialogDismiss
    )
    uiState.dialog?.let { dialog ->
        LoginUserRedSysCustomDialog(
            title = when (dialog) {
                is LoginUserRedSysDialogState.Error -> dialog.title
            },
            message = when (dialog) {
                is LoginUserRedSysDialogState.Error -> dialog.message
            },
            confirmText = when (dialog) {
                is LoginUserRedSysDialogState.Error -> dialog.confirmText
            },
            onDismiss = viewModel::onDialogDismiss,
            onConfirm = viewModel::onDialogDismiss
        )
    }
}
@Composable
fun LoginUserRedSysContent(
    uiState: LoginUserRedSysUiState,
    onUserChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = uiState.user,
            onValueChange = onUserChange,
            label = { Text(stringResource(R.string.user)) },
            isError = uiState.userError != null,
            supportingText = uiState.userError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CustomComposeButton(
                text = stringResource(R.string.cancel),
                enabled = true,
                backgroundColor = uiState.buttons.cancelBackground,
                contentColor = uiState.buttons.cancelContentColor,
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            CustomComposeButton(
                text = if (uiState.buttons.acceptLoading) stringResource(R.string.loading) else stringResource(R.string.accept),
                enabled = uiState.buttons.acceptEnabled,
                backgroundColor = if (uiState.buttons.acceptEnabled) uiState.buttons.acceptBackground else LoginUserRedSysButtonStyles.GreenDisabled,
                contentColor = uiState.buttons.acceptContentColor,
                onClick = onAcceptClick,
                loading = uiState.buttons.acceptLoading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
