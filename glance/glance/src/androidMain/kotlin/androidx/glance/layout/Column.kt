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

package androidx.glance.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.glance.Applier
import androidx.glance.EmittableWithChildren
import androidx.glance.GlanceInternalApi
import androidx.glance.Modifier

@GlanceInternalApi
public class EmittableColumn : EmittableWithChildren() {
    override var modifier: Modifier = Modifier
    public var verticalAlignment: Alignment.Vertical = Alignment.Top
    public var horizontalAlignment: Alignment.Horizontal = Alignment.Start
}

/**
 * A layout composable with [content], which lays its children out in a Column.
 *
 * By default, the [Column] will size itself to fit the content, unless a [Dimension] constraint has
 * been provided. When children are smaller than the size of the [Column], they will be placed
 * within the available space subject to [horizontalAlignment] and [verticalAlignment].
 *
 * @param modifier The modifier to be applied to the layout.
 * @param verticalAlignment The vertical alignment to apply to the set of children, when they do not
 *   consume the full height of the [Column] (i.e. whether to push the children towards the top,
 *   center or bottom of the [Column]).
 * @param horizontalAlignment The horizontal alignment to apply to children when they are smaller
 *  than the width of the [Column]
 * @param content The content inside the [Column]
 */
@OptIn(GlanceInternalApi::class)
@Composable
public fun Column(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    ComposeNode<EmittableColumn, Applier>(
        factory = ::EmittableColumn,
        update = {
            this.set(modifier) { this.modifier = it }
            this.set(horizontalAlignment) { this.horizontalAlignment = it }
            this.set(verticalAlignment) { this.verticalAlignment = it }
        },
        content = content
    )
}
