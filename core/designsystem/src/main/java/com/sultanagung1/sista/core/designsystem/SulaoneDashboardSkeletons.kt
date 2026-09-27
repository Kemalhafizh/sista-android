package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * FASE 76.5 — skeletons shaped like the real dashboard blocks, for use inside
 * SulaoneTieredLoading. Plain Row/Column only (no Lazy*, no
 * BoxWithConstraints), so they are safe inside a dashboard's LazyColumn item.
 */

/** Mirrors BentoHeroSplit: a tall hero on the start side, two stacked tiles beside it. */
@Composable
fun BentoHeroSplitSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SkeletonBox(
            modifier = Modifier
                .weight(1.15f)
                .height(150.dp),
            shape = RoundedCornerShape(20.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(16.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(16.dp))
        }
    }
}

/** Mirrors a full-width SulaoneMetricCard (icon, title, value). */
@Composable
fun MetricCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBox(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.35f).height(20.dp))
        }
    }
}

/**
 * Mirrors a list of session/schedule cards: status dot + title, a subtitle
 * line, and a row of two action pills.
 */
@Composable
fun SessionCardListSkeleton(rows: Int = 3, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(rows) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBox(modifier = Modifier.size(8.dp), shape = CircleShape)
                    Spacer(modifier = Modifier.width(8.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
                }
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBox(modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(12.dp))
                    SkeletonBox(modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        }
    }
}

/** Mirrors the parent portal's child persona card: avatar, name, class, three stat chips. */
@Composable
fun PersonaCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBox(modifier = Modifier.size(52.dp), shape = CircleShape)
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(modifier = Modifier.width(160.dp).height(16.dp))
                SkeletonBox(modifier = Modifier.width(100.dp).height(12.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonBox(modifier = Modifier.weight(1f).height(32.dp), shape = RoundedCornerShape(10.dp))
            }
        }
    }
}
