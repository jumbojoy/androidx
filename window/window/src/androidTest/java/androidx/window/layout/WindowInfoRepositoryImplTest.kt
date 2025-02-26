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

package androidx.window.layout

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.core.util.Consumer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.window.TestActivity
import androidx.window.TestConfigChangeHandlingActivity
import androidx.window.TestConsumer
import androidx.window.layout.WindowMetricsCalculatorCompat.computeCurrentWindowMetrics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executor

@OptIn(ExperimentalCoroutinesApi::class)
public class WindowInfoRepositoryImplTest {

    @get:Rule
    public val activityScenario: ActivityScenarioRule<TestActivity> =
        ActivityScenarioRule(TestActivity::class.java)

    private val testScope = TestCoroutineScope()

    @Test
    public fun testGetCurrentWindowMetrics(): Unit = testScope.runBlockingTest {
        activityScenario.scenario.onActivity { testActivity ->
            val repo = WindowInfoRepositoryImpl(
                testActivity,
                WindowMetricsCalculatorCompat,
                FakeWindowBackend()
            )
            val expected = computeCurrentWindowMetrics(testActivity)
            val consumer = TestConsumer<WindowMetrics>()
            testScope.launch {
                repo.currentWindowMetrics.collect { consumer.accept(it) }
            }
            consumer.assertValue(expected)
        }
    }

    @Test
    public fun testGetCurrentWindowMetrics_multicasting(): Unit = testScope.runBlockingTest {
        activityScenario.scenario.onActivity { testActivity ->
            val repo = WindowInfoRepositoryImpl(
                testActivity,
                WindowMetricsCalculatorCompat,
                FakeWindowBackend()
            )
            val expected = computeCurrentWindowMetrics(testActivity)
            val consumer = TestConsumer<WindowMetrics>()
            testScope.launch {
                repo.currentWindowMetrics.collect { consumer.accept(it) }
            }
            testScope.launch {
                repo.currentWindowMetrics.collect { consumer.accept(it) }
            }
            consumer.assertValues(expected, expected)
        }
    }

    @Test
    public fun testGetCurrentWindowMetrics_configChangesEmitNewMetrics(): Unit =
        testScope.runBlockingTest {
            val scenario = ActivityScenario.launch(TestConfigChangeHandlingActivity::class.java)
            val collector = TestConsumer<WindowMetrics>()
            val expected = mutableListOf<WindowMetrics>()
            scenario.onActivity { activity ->
                val repo = WindowInfoRepositoryImpl(
                    activity,
                    WindowMetricsCalculatorCompat,
                    FakeWindowBackend()
                )
                activity.resetLayoutCounter()
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                activity.waitForLayout()
                testScope.launch {
                    repo.currentWindowMetrics.collect { collector.accept(it) }
                }
            }
            scenario.onActivity { activity ->
                expected.add(computeCurrentWindowMetrics(activity))
                activity.resetLayoutCounter()
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                activity.waitForLayout()
            }
            scenario.onActivity { activity ->
                expected.add(computeCurrentWindowMetrics(activity))
                collector.assertValueCount(2)
                collector.assertValues(*expected.toTypedArray())
            }
        }

    @Test
    public fun testWindowLayoutFeatures(): Unit = testScope.runBlockingTest {
        activityScenario.scenario.onActivity { testActivity ->
            val windowMetricsCalculator = WindowMetricsCalculatorCompat
            val fakeBackend = FakeWindowBackend()
            val repo = WindowInfoRepositoryImpl(
                testActivity,
                windowMetricsCalculator,
                fakeBackend
            )
            val collector = TestConsumer<WindowLayoutInfo>()
            testScope.launch {
                repo.windowLayoutInfo.collect(collector::accept)
            }
            fakeBackend.triggerSignal(WindowLayoutInfo(emptyList()))
            collector.assertValue(WindowLayoutInfo(emptyList()))
        }
    }

    @Test
    public fun testWindowLayoutFeatures_multicasting(): Unit = testScope.runBlockingTest {
        activityScenario.scenario.onActivity { testActivity ->
            val windowMetricsCalculator = WindowMetricsCalculatorCompat
            val fakeBackend = FakeWindowBackend()
            val repo = WindowInfoRepositoryImpl(
                testActivity,
                windowMetricsCalculator,
                fakeBackend
            )
            val collector = TestConsumer<WindowLayoutInfo>()
            testScope.launch {
                repo.windowLayoutInfo.collect(collector::accept)
            }
            testScope.launch {
                repo.windowLayoutInfo.collect(collector::accept)
            }
            fakeBackend.triggerSignal(WindowLayoutInfo(emptyList()))
            collector.assertValues(WindowLayoutInfo(emptyList()), WindowLayoutInfo(emptyList()))
        }
    }

    private class FakeWindowBackend : WindowBackend {

        private class CallbackHolder(
            val executor: Executor,
            val callback: Consumer<WindowLayoutInfo>
        ) : Consumer<WindowLayoutInfo> {

            override fun accept(t: WindowLayoutInfo?) {
                executor.execute { callback.accept(t) }
            }
        }

        private val consumers = mutableListOf<CallbackHolder>()

        fun triggerSignal(info: WindowLayoutInfo) {
            consumers.forEach { consumer -> consumer.accept(info) }
        }

        override fun registerLayoutChangeCallback(
            activity: Activity,
            executor: Executor,
            callback: Consumer<WindowLayoutInfo>
        ) {
            consumers.add(CallbackHolder(executor, callback))
        }

        override fun unregisterLayoutChangeCallback(callback: Consumer<WindowLayoutInfo>) {
            consumers.remove(callback)
        }
    }
}
