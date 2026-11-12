package com.lorenzosuarez.magiceightball.phone.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.magiceightball.core.domain.model.AppLanguage
import java.util.Locale

/**
 * Shows [content] in the language chosen in the app rather than the device's.
 *
 * Every `stringResource` below it, the Material components' own texts included, is resolved for
 * [language], and a change of language recomposes them at once. The context stays a wrapper of
 * the activity, so everything else that reaches for it keeps working.
 */
@Composable
fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val localizedConfiguration = remember(configuration, language) {
        Configuration(configuration).apply { setLocale(Locale.forLanguageTag(language.code)) }
    }
    val localizedContext = remember(context, localizedConfiguration) {
        LocalizedContext(context, context.createConfigurationContext(localizedConfiguration).resources)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        content = content
    )
}

/**
 * What [ProvideAppLanguage] set, kept to hand on to a window of its own. A dialog or a sheet
 * starts again from the device's language: read here, outside the window, and provided again
 * inside it with [InAppLanguage], its texts stay in the app's.
 */
internal class AppLanguageHold(val context: Context, val configuration: Configuration)

@Composable
internal fun rememberAppLanguage(): AppLanguageHold {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) { AppLanguageHold(context, configuration) }
}

@Composable
internal fun InAppLanguage(held: AppLanguageHold, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalContext provides held.context,
        LocalConfiguration provides held.configuration,
        content = content
    )
}

private class LocalizedContext(base: Context, private val localized: Resources) : ContextWrapper(base) {
    override fun getResources(): Resources = localized
}
