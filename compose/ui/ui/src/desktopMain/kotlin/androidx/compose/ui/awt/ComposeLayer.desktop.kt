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

package androidx.compose.ui.awt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.input.mouse.MouseScrollEvent
import androidx.compose.ui.input.mouse.MouseScrollOrientation
import androidx.compose.ui.input.mouse.MouseScrollUnit
import androidx.compose.ui.platform.DesktopComponent
import androidx.compose.ui.platform.DesktopOwner
import androidx.compose.ui.platform.DesktopOwners
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import java.awt.Dimension
import java.awt.Point
import java.awt.event.FocusEvent
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseWheelEvent
import java.awt.im.InputMethodRequests
import androidx.compose.ui.input.key.KeyEvent as ComposeKeyEvent

internal class ComposeLayer {
    private var isDisposed = false

    private val coroutineScope = CoroutineScope(Dispatchers.Swing)
    // TODO(demin): probably we need to get rid of asynchronous events. it was added because of
    //  slow lazy scroll. But events become unpredictable, and we can't consume them.
    //  Alternative solution to a slow scroll - merge multiple scroll events into a single one.
    private val events = AWTDebounceEventQueue()

    internal val wrapped = Wrapped().apply {
        onStateChanged(SkiaLayer.PropertyKind.ContentScale) { _ ->
            resetDensity()
        }
    }

    internal val owners: DesktopOwners = DesktopOwners(
        coroutineScope,
        wrapped,
        wrapped::needRedraw
    )

    private var owner: DesktopOwner? = null
    private var composition: Composition? = null

    private var initOwner: (() -> Unit)? = null

    private lateinit var density: Density

    inner class Wrapped : SkiaLayer(), DesktopComponent {
        var currentInputMethodRequests: InputMethodRequests? = null

        var isInit = false
            private set

        override fun init() {
            super.init()
            isInit = true
            resetDensity()
            initOwner?.invoke()
        }

        internal fun resetDensity() {
            this@ComposeLayer.density = (this as SkiaLayer).density
            owner?.density = density
        }

        override fun getInputMethodRequests() = currentInputMethodRequests

        override fun enableInput(inputMethodRequests: InputMethodRequests) {
            currentInputMethodRequests = inputMethodRequests
            enableInputMethods(true)
            val focusGainedEvent = FocusEvent(this, FocusEvent.FOCUS_GAINED)
            inputContext.dispatchEvent(focusGainedEvent)
        }

        override fun disableInput() {
            currentInputMethodRequests = null
        }

        override fun doLayout() {
            super.doLayout()
            val owner = owner
            if (owner != null) {
                val density = density.density
                owner.setSize(
                    (width * density).toInt().coerceAtLeast(0),
                    (height * density).toInt().coerceAtLeast(0)
                )
                owner.measureAndLayout()
                preferredSize = Dimension(
                    (owner.root.width / density).toInt(),
                    (owner.root.height / density).toInt()
                )
            }
        }

        override val locationOnScreen: Point
            @Suppress("ACCIDENTAL_OVERRIDE") // KT-47743
            get() = super.getLocationOnScreen()

        override val density: Density
            get() = this@ComposeLayer.density
    }

    val component: SkiaLayer
        get() = wrapped

    init {
        wrapped.renderer = object : SkiaRenderer {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                try {
                    owners.onFrame(canvas, width, height, nanoTime)
                } catch (e: Throwable) {
                    if (System.getProperty("compose.desktop.render.ignore.errors") == null) {
                        throw e
                    }
                }
            }
        }
        initCanvas()
    }

    private fun initCanvas() {
        wrapped.addInputMethodListener(object : InputMethodListener {
            override fun caretPositionChanged(event: InputMethodEvent?) {
                if (event != null) {
                    owners.onInputMethodEvent(event)
                }
            }

            override fun inputMethodTextChanged(event: InputMethodEvent) = events.post {
                owners.onInputMethodEvent(event)
            }
        })

        wrapped.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) = Unit

            override fun mousePressed(event: MouseEvent) = events.post {
                owners.onMousePressed(
                    (event.x * density.density).toInt(),
                    (event.y * density.density).toInt(),
                    event
                )
            }

            override fun mouseReleased(event: MouseEvent) = events.post {
                owners.onMouseReleased(
                    (event.x * density.density).toInt(),
                    (event.y * density.density).toInt(),
                    event
                )
            }

            override fun mouseEntered(event: MouseEvent) = events.post {
                owners.onMouseEntered(
                    (event.x * density.density).toInt(),
                    (event.y * density.density).toInt()
                )
            }

            override fun mouseExited(event: MouseEvent) = events.post {
                owners.onMouseExited()
            }
        })
        wrapped.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(event: MouseEvent) = events.post {
                owners.onMouseMoved(
                    (event.x * density.density).toInt(),
                    (event.y * density.density).toInt(),
                    event
                )
            }

            override fun mouseMoved(event: MouseEvent) = events.post {
                owners.onMouseMoved(
                    (event.x * density.density).toInt(),
                    (event.y * density.density).toInt(),
                    event
                )
            }
        })
        wrapped.addMouseWheelListener { event ->
            events.post {
                owners.onMouseScroll(
                    (event.x * density.density).toInt(),
                    (event.y * density.density).toInt(),
                    event.toComposeEvent()
                )
            }
        }
        wrapped.focusTraversalKeysEnabled = false
        wrapped.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                if (owners.onKeyPressed(event)) {
                    event.consume()
                }
            }

            override fun keyReleased(event: KeyEvent) {
                if (owners.onKeyReleased(event)) {
                    event.consume()
                }
            }

            override fun keyTyped(event: KeyEvent) {
                if (owners.onKeyTyped(event)) {
                    event.consume()
                }
            }
        })
    }

    private fun MouseWheelEvent.toComposeEvent() = MouseScrollEvent(
        delta = if (scrollType == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            MouseScrollUnit.Page((scrollAmount * preciseWheelRotation).toFloat())
        } else {
            MouseScrollUnit.Line((scrollAmount * preciseWheelRotation).toFloat())
        },

        // There are no other way to detect horizontal scrolling in AWT
        orientation = if (isShiftDown) {
            MouseScrollOrientation.Horizontal
        } else {
            MouseScrollOrientation.Vertical
        }
    )

    fun dispose() {
        check(!isDisposed)
        composition?.dispose()
        owner?.dispose()
        owners.dispose()
        events.cancel()
        coroutineScope.cancel()
        wrapped.dispose()
        initOwner = null
        isDisposed = true
    }

    internal fun setContent(
        parentComposition: CompositionContext? = null,
        onPreviewKeyEvent: (ComposeKeyEvent) -> Boolean = { false },
        onKeyEvent: (ComposeKeyEvent) -> Boolean = { false },
        content: @Composable () -> Unit
    ) {
        check(!isDisposed)
        check(composition == null && initOwner == null) { "Cannot set content twice" }
        initOwner = {
            check(!isDisposed)
            if (wrapped.isInit && owner == null) {
                owner = DesktopOwner(
                    owners,
                    density,
                    onPreviewKeyEvent = onPreviewKeyEvent,
                    onKeyEvent = onKeyEvent
                )
                composition = owner!!.setContent(parent = parentComposition, content = content)
                initOwner = null
            }
        }
        // We can't create DesktopOwner now, because we don't know density yet.
        // We will know density only after SkiaLayer will be visible.
        initOwner!!()
    }
}
