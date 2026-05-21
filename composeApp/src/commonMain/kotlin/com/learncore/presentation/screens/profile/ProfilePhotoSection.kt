package com.learncore.presentation.screens.profile

import androidx.compose.runtime.Composable

@Composable
expect fun ProfilePhotoSection(
    photoUri: String,
    userName: String,
    onPhotoSelected: (String) -> Unit
)

@Composable
expect fun ProfileAvatarDisplay(
    photoUri: String,
    userName: String
)
