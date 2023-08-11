package io.github.fate_grand_automata.ui.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    vm: OnboardingViewModel = viewModel(),
    navigateToHome: () -> Unit
) {
    OnboardingContent(
        vm,
        navigateToHome
    )
}

@Composable
fun OnboardingContent(
    vm: OnboardingViewModel = viewModel(),
    navigateToHome: () -> Unit
) {
    val pages = remember {
        listOf(
            WelcomeScreen(vm),
            PickDirectory(vm),
            DisableBatteryOptimization(vm),
            YoutubeVideo(vm)
        ).filter { !it.shouldSkip() }
    }

    val scope = rememberCoroutineScope()
    val pageState = rememberPagerState()
    var nextEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopSection(
            onBackClick = {
                if (pageState.currentPage + 1 > 1) scope.launch {
                    pageState.scrollToPage(pageState.currentPage - 1)
                    nextEnabled = true
                }
            }
        )

        HorizontalPager(
            pageCount = pages.size,
            state = pageState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            userScrollEnabled = false
        ) { page ->
            OnBoardingPage {
                pages[page].UI {
                    nextEnabled = true
                }
            }
        }
        BottomSection(size = pages.size, index = pageState.currentPage, enabled = nextEnabled) {
            if (pageState.currentPage + 1 < pages.size) scope.launch {
                pageState.scrollToPage(pageState.currentPage + 1)
                // enable next button if the new screen is optional or if the requirements were fulfilled
                nextEnabled = pages[pageState.currentPage]
                    .let { it.canSkip || it.shouldSkip() }
            } else {
                vm.prefs.completedOnboarding()
                navigateToHome()
            }
        }
    }
}

@Composable
fun TopSection(onBackClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(imageVector = Icons.Outlined.KeyboardArrowLeft, contentDescription = null)
        }
    }
}

@Composable
fun BottomSection(size: Int, index: Int, enabled: Boolean, onButtonClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Indicators
        Indicators(size, index)

        Button(
            onClick = onButtonClick,
            enabled = enabled,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
        ) {
            Icon(
                Icons.Outlined.KeyboardArrowRight,
                tint = Color.White,
                contentDescription = "Localized description"
            )
        }
    }
}

@Composable
fun BoxScope.Indicators(size: Int, index: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.align(Alignment.CenterStart)
    ) {
        repeat(size) {
            Indicator(isSelected = it == index)
        }
    }
}

@Composable
fun Indicator(isSelected: Boolean) {
    val width = animateDpAsState(
        targetValue = if (isSelected) 25.dp else 10.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .height(10.dp)
            .width(width.value)
            .clip(CircleShape)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0XFFF8E2E7)
            )
    ) {

    }
}

@Composable
fun OnBoardingPage(content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp)
    ) {
        content()
    }
}