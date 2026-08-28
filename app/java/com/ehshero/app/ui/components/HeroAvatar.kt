package com.ehshero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.theme.AvatarOption

/** Renders a preset hero avatar (see ui/theme/HeroAvatars.kt) as a circular
 * badge - the two avatar colours as background/icon, no image assets. */
@Composable
fun HeroAvatar(
    option: AvatarOption,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    selected: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(option.primary)
            .then(
                if (selected) Modifier.border(3.dp, option.secondary, CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.label,
            tint = option.secondary,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}
