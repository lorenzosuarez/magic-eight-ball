package com.lorenzosuarez.magiceightball.phone

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.lorenzosuarez.magiceightball.phone.ui.EightBallScreen
import com.lorenzosuarez.magiceightball.phone.ui.MagicEightBallPhoneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: EightBallViewModel by viewModels()

    // Portrait is a product decision for phones only: R.bool.portrait_only is false from 600dp
    // of width up, so tablets and foldables keep rotating freely, which is what the check asks for.
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Set both ways: a requested orientation outlives the activity being recreated, so a
        // foldable locked while folded would otherwise stay locked once unfolded.
        requestedOrientation = if (resources.getBoolean(R.bool.portrait_only)) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        setContent {
            MagicEightBallPhoneTheme {
                EightBallScreen(viewModel)
            }
        }
    }
}
