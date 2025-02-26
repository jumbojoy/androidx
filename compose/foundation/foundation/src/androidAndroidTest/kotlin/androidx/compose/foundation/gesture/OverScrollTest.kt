/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.foundation.gesture

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.OverScrollController
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.matchers.isZero
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@MediumTest
@RunWith(AndroidJUnit4::class)
class OverScrollTest {
    @get:Rule
    val rule = createComposeRule()

    private val boxTag = "box"

    @Test
    fun overscrollController_scrollable_drag() {
        testDrag(reverseDirection = false)
    }

    @Test
    fun overscrollController_scrollable_drag_reverseDirection() {
        // same asserts for `reverseDirection = true`, but that's the point
        // we don't want overscroll to depend on reverseLayout, it's coordinate-driven logic
        testDrag(reverseDirection = true)
    }

    @Test
    fun overscrollController_scrollable_fling() {
        var acummulatedScroll = 0f
        val controller = TestOverScrollController()
        val scrollableState = ScrollableState { delta ->
            if (acummulatedScroll > 1000f) {
                0f
            } else {
                acummulatedScroll += delta
                delta
            }
        }
        rule.setOverscrollContentAndReturnViewConfig(
            scrollableState = scrollableState,
            overScrollController = controller
        )

        rule.runOnIdle {
            // we passed isContentScrolls = 1, so initial draw must occur
            assertThat(controller.drawCallsCount).isEqualTo(1)
        }

        rule.onNodeWithTag(boxTag).assertExists()

        rule.onNodeWithTag(boxTag).performTouchInput {
            swipeWithVelocity(
                center,
                Offset(centerX + 10800, centerY),
                endVelocity = 30000f
            )
        }

        rule.runOnIdle {
            assertThat(controller.lastVelocity.x).isGreaterThan(0f)
            assertThat(controller.lastNestedScrollSource).isEqualTo(NestedScrollSource.Fling)
        }
    }

    @Test
    fun overscrollController_scrollable_preDrag_respectsConsumption() {
        var acummulatedScroll = 0f
        val controller = TestOverScrollController(consumePreCycles = true)
        val scrollableState = ScrollableState { delta ->
            acummulatedScroll += delta
            delta
        }
        val viewConfig = rule.setOverscrollContentAndReturnViewConfig(
            scrollableState = scrollableState,
            overScrollController = controller
        )

        rule.runOnIdle {
            // we passed isContentScrolls = 1, so initial draw must occur
            assertThat(controller.drawCallsCount).isEqualTo(1)
        }

        var centerXAxis = 0f
        rule.onNodeWithTag(boxTag).performTouchInput {
            centerXAxis = centerX
            down(center)
            moveBy(Offset(1000f, 0f))
        }

        rule.runOnIdle {
            val slop = viewConfig.touchSlop
            // since we consume 1/10 of the delta in the pre scroll during overscroll, expect 9/10
            assertThat(abs(acummulatedScroll - 1000f * 9 / 10)).isWithin(0.1f)

            assertThat(controller.preScrollDelta).isEqualTo(Offset(1000f - slop, 0f))
            assertThat(controller.preScrollPointerPosition?.x)
                .isEqualTo(centerXAxis + slop)
            assertThat(controller.lastNestedScrollSource).isEqualTo(NestedScrollSource.Drag)
        }

        rule.onNodeWithTag(boxTag).performTouchInput {
            up()
        }
    }

    @Test
    fun overscrollController_scrollable_preFling_respectsConsumption() {
        var acummulatedScroll = 0f
        var lastFlingReceived = 0f
        val controller = TestOverScrollController(consumePreCycles = true)
        val scrollableState = ScrollableState { delta ->
            acummulatedScroll += delta
            delta
        }
        val flingBehavior = object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                lastFlingReceived = initialVelocity
                return initialVelocity
            }
        }
        rule.setOverscrollContentAndReturnViewConfig(
            scrollableState = scrollableState,
            overScrollController = controller,
            flingBehavior = flingBehavior
        )

        rule.runOnIdle {
            // we passed isContentScrolls = 1, so initial draw must occur
            assertThat(controller.drawCallsCount).isEqualTo(1)
        }

        rule.onNodeWithTag(boxTag).assertExists()

        rule.onNodeWithTag(boxTag).performTouchInput {
            swipeWithVelocity(
                center,
                Offset(centerX + 10800, centerY),
                endVelocity = 30000f
            )
        }

        rule.runOnIdle {
            assertThat(abs(controller.preFlingVelocity.x - 30000f)).isWithin(0.1f)
            assertThat(abs(lastFlingReceived - 30000f * 9 / 10)).isWithin(0.1f)
        }
    }

    @Test
    fun overscrollController_scrollable_attemptsToStopAnimation() {
        var acummulatedScroll = 0f
        val controller = TestOverScrollController()
        val scrollableState = ScrollableState { delta ->
            acummulatedScroll += delta
            delta
        }
        val viewConfiguration = rule.setOverscrollContentAndReturnViewConfig(
            scrollableState = scrollableState,
            overScrollController = controller
        )

        rule.runOnIdle {
            // no down events, hence 0 animation stops
            assertThat(controller.stopAnimationCallsCount).isEqualTo(0)
        }

        rule.onNodeWithTag(boxTag).performTouchInput {
            down(center)
            moveBy(Offset(500f, 0f))
            up()
        }

        val lastAccScroll = rule.runOnIdle {
            assertThat(controller.stopAnimationCallsCount).isEqualTo(1)
            // respect touch slop if overscroll animation is not running
            assertThat(acummulatedScroll)
                .isEqualTo(500f - viewConfiguration.touchSlop)
            // pretend we're settling the overscroll animation
            controller.animationRunning = true
            acummulatedScroll
        }

        rule.onNodeWithTag(boxTag).performTouchInput {
            down(center)
            moveBy(Offset(500f, 0f))
            up()
        }

        rule.runOnIdle {
            assertThat(controller.stopAnimationCallsCount).isEqualTo(2)
            // ignores touch slop if overscroll animation is on progress while pointer goes down
            assertThat(acummulatedScroll - lastAccScroll).isEqualTo(500f)
        }
    }

    class TestOverScrollController(
        private val consumePreCycles: Boolean = false,
        var animationRunning: Boolean = false
    ) : OverScrollController {
        var releaseCallsCount = 0
        var drawCallsCount = 0
        var stopAnimationCallsCount = 0
        var containerSize = Size.Zero
        var isContentScrolls = false

        var lastVelocity = Velocity.Zero
        var lastInitialDragDelta = Offset.Zero
        var lastOverscrollDelta = Offset.Zero
        var lastPointerPosition: Offset? = Offset.Zero
        var lastNestedScrollSource: NestedScrollSource? = null

        var preScrollDelta = Offset.Zero
        var preScrollPointerPosition: Offset? = Offset.Zero
        var preScrollSource: NestedScrollSource? = null

        var preFlingVelocity = Velocity.Zero

        override fun consumePreScroll(
            scrollDelta: Offset,
            pointerPosition: Offset?,
            source: NestedScrollSource
        ): Offset {
            preScrollDelta = scrollDelta
            preScrollPointerPosition = pointerPosition
            preScrollSource = source

            return if (consumePreCycles) scrollDelta / 10f else Offset.Zero
        }

        override fun consumePostScroll(
            initialDragDelta: Offset,
            overScrollDelta: Offset,
            pointerPosition: Offset?,
            source: NestedScrollSource
        ) {
            lastInitialDragDelta = initialDragDelta
            lastOverscrollDelta = overScrollDelta
            lastPointerPosition = pointerPosition
            lastNestedScrollSource = source
        }

        override fun consumePreFling(velocity: Velocity): Velocity {
            preFlingVelocity = velocity
            return if (consumePreCycles) velocity / 10f else Velocity.Zero
        }

        override fun consumePostFling(velocity: Velocity) {
            lastVelocity = velocity
        }

        override fun stopOverscrollAnimation(): Boolean {
            stopAnimationCallsCount += 1
            return animationRunning
        }

        override fun release() {
            releaseCallsCount += 1
        }

        override fun refreshContainerInfo(size: Size, isContentScrolls: Boolean) {
            containerSize = size
            this.isContentScrolls = isContentScrolls
        }

        override fun DrawScope.drawOverScroll() {
            drawCallsCount += 1
        }
    }

    fun testDrag(reverseDirection: Boolean) {
        var consumeOnlyHalf = false
        val controller = TestOverScrollController()
        val scrollableState = ScrollableState { delta ->
            if (consumeOnlyHalf) {
                delta / 2
            } else {
                delta
            }
        }
        val viewConfig = rule.setOverscrollContentAndReturnViewConfig(
            scrollableState = scrollableState,
            overScrollController = controller,
            reverseDirection = reverseDirection
        )

        rule.runOnIdle {
            // we passed isContentScrolls = 1, so initial draw must occur
            assertThat(controller.drawCallsCount).isEqualTo(1)
        }

        rule.onNodeWithTag(boxTag).assertExists()

        var centerXAxis = 0f
        rule.onNodeWithTag(boxTag).performTouchInput {
            centerXAxis = centerX
            down(center)
            moveBy(Offset(1000f, 0f))
        }

        rule.runOnIdle {
            assertThat(controller.releaseCallsCount).isZero()
            assertThat(controller.lastInitialDragDelta.x).isGreaterThan(0f)
            assertThat(controller.lastInitialDragDelta.y).isZero()
            // there was only one pointer position coming from the center + 1000, let's check
            assertThat(controller.lastPointerPosition?.x)
                .isEqualTo(centerXAxis + viewConfig.touchSlop)
            // consuming all, so overscroll is 0
            assertThat(controller.lastOverscrollDelta).isEqualTo(Offset.Zero)
        }

        rule.onNodeWithTag(boxTag).performTouchInput {
            up()
        }

        rule.runOnIdle {
            assertThat(controller.releaseCallsCount).isEqualTo(1)
            consumeOnlyHalf = true
        }

        rule.onNodeWithTag(boxTag).performTouchInput {
            down(center)
            moveBy(Offset(1000f, 0f))
        }

        rule.runOnIdle {
            assertThat(controller.lastInitialDragDelta.x).isGreaterThan(0f)
            assertThat(controller.lastInitialDragDelta.y).isZero()
            assertThat(controller.lastOverscrollDelta.x)
                .isEqualTo(controller.lastInitialDragDelta.x / 2)
            assertThat(controller.lastNestedScrollSource).isEqualTo(NestedScrollSource.Drag)
        }

        rule.onNodeWithTag(boxTag).performTouchInput {
            up()
        }

        rule.runOnIdle {
            assertThat(controller.releaseCallsCount).isEqualTo(2)
        }
    }
}

private fun ComposeContentTestRule.setOverscrollContentAndReturnViewConfig(
    scrollableState: ScrollableState,
    overScrollController: OverScrollController,
    flingBehavior: FlingBehavior? = null,
    reverseDirection: Boolean = false
): ViewConfiguration {
    var viewConfiguration: ViewConfiguration? = null
    setContent {
        viewConfiguration = LocalViewConfiguration.current
        SideEffect {
            // pretend to know the size
            overScrollController
                .refreshContainerInfo(
                    Size(500f, 500f),
                    true
                )
        }
        Box {
            Box(
                Modifier
                    .testTag("box")
                    .size(300.dp)
                    .scrollable(
                        state = scrollableState,
                        orientation = Orientation.Horizontal,
                        overScrollController = overScrollController,
                        flingBehavior = flingBehavior ?: ScrollableDefaults.flingBehavior(),
                        reverseDirection = reverseDirection
                    )
            )
        }
    }
    return viewConfiguration!!
}