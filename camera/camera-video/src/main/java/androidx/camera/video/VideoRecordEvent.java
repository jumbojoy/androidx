/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.camera.video;

import static androidx.camera.video.VideoRecordEvent.Finalize.ERROR_NONE;
import static androidx.camera.video.VideoRecordEvent.Finalize.VideoRecordError;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.util.Consumer;
import androidx.core.util.Preconditions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executor;

/**
 * VideoRecordEvent is used to report the video recording events and status.
 *
 * <p>Upon starting a recording by {@link PendingRecording#start()}, recording events will start to
 * be sent to the listener set in {@link PendingRecording#withEventListener(Executor, Consumer)}.
 *
 * <p>There are {@link Start}, {@link Finalize}, {@link Status}, {@link Pause} and {@link Resume}
 * events.
 *
 * <p>Example: Below is the typical way to determine the event type and cast to the event class, if
 * needed.
 *
 * <pre>{@code
 *
 * ActiveRecording activeRecording = recorder.prepareRecording(outputOptions)
 *     .withEventListener(ContextCompat.getMainExecutor(context), videoRecordEvent -> {
 *         if (videoRecordEvent instanceof VideoRecordEvent.Start) {
 *             // Handle the start of a new active recording
 *             ...
 *         } else if (videoRecordEvent instanceof VideoRecordEvent.Pause) {
 *             // Handle the case where the active recording is paused
 *             ...
 *         } else if (videoRecordEvent instanceof VideoRecordEvent.Resume) {
 *             // Handles the case where the active recording is resumed
 *             ...
 *         } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
 *             VideoRecordEvent.Finalize finalizeEvent =
 *                 (VideoRecordEvent.Finalize) videoRecordEvent;
 *             // Handles a finalize event for the active recording, checking Finalize.getError()
 *             int error = finalizeEvent.getError();
 *             if (error != Finalize.ERROR_NONE) {
 *                 ...
 *             }
 *         }
 *
 *         // All events, including VideoRecordEvent.Status, contain RecordingStats.
 *         // This can be used to update the UI or track the recording duration.
 *         RecordingStats recordingStats = videoRecordEvent.getRecordingStats();
 *         ...
 *     }).start();
 *
 * }</pre>
 *
 * <p>If using Kotlin, the VideoRecordEvent class can be treated similar to a {@code sealed
 * class}. In Kotlin, it is recommended to use a {@code when} expression rather than an {@code
 * if}-{@code else if} chain as in the above example.
 *
 * <p>When a video recording is requested, {@link Start} event will be reported at first and
 * {@link Finalize} event will be reported when the recording is finished. The stop reason can be
 * obtained via {@link Finalize#getError()}. {@link Finalize#ERROR_NONE} means that the video was
 * recorded successfully, and other error code indicate the recording is failed or stopped due to
 * a certain reason. Please note that a failed result does not mean that the video file has not been
 * generated. In some cases, the file can still be successfully generated. For example, the
 * result {@link Finalize#ERROR_INSUFFICIENT_DISK} will still have video file.
 *
 * <p>The {@link Status} event will be triggered continuously during the recording process,
 * {@link #getRecordingStats} can be used to get the recording state such as total recorded bytes
 * and total duration when the event is triggered.
 */
public abstract class VideoRecordEvent {

    private final OutputOptions mOutputOptions;
    private final RecordingStats mRecordingStats;

    // Restrict access to emulate sealed class
    // Classes will be constructed with static factory methods
    VideoRecordEvent(@NonNull OutputOptions outputOptions,
            @NonNull RecordingStats recordingStats) {
        mOutputOptions = Preconditions.checkNotNull(outputOptions);
        mRecordingStats = Preconditions.checkNotNull(recordingStats);
    }

    /**
     * Gets the recording status of current event.
     */
    @NonNull
    public RecordingStats getRecordingStats() {
        return mRecordingStats;
    }

    /**
     * Gets the {@link OutputOptions} associated with this event.
     */
    @NonNull
    public OutputOptions getOutputOptions() {
        return mOutputOptions;
    }

    @NonNull
    static Start start(@NonNull OutputOptions outputOptions,
            @NonNull RecordingStats recordingStats) {
        return new Start(outputOptions, recordingStats);
    }

    /**
     * Indicates the start of recording.
     *
     * <p>When a video recording is successfully requested by {@link PendingRecording#start()},
     * a {@code Start} event will be the first event.
     */
    public static final class Start extends VideoRecordEvent {

        @SuppressWarnings("WeakerAccess") /* synthetic accessor */
        Start(@NonNull OutputOptions outputOptions, @NonNull RecordingStats recordingStats) {
            super(outputOptions, recordingStats);
        }
    }

    @NonNull
    static Finalize finalize(@NonNull OutputOptions outputOptions,
            @NonNull RecordingStats recordingStats,
            @NonNull OutputResults outputResults) {
        return new Finalize(outputOptions, recordingStats, outputResults, ERROR_NONE, null);
    }

    @NonNull
    static Finalize finalizeWithError(@NonNull OutputOptions outputOptions,
            @NonNull RecordingStats recordingStats,
            @NonNull OutputResults outputResults,
            @VideoRecordError int error,
            @Nullable Throwable cause) {
        Preconditions.checkArgument(error != ERROR_NONE, "An error type is required.");
        return new Finalize(outputOptions, recordingStats, outputResults, error, cause);
    }

    /**
     * Indicates the finalization of recording.
     *
     * <p>The finalize event will be triggered regardless of whether the recording succeeds or
     * fails. Use {@link Finalize#getError()} to obtain the error type and
     * {@link Finalize#getCause()} to get the error cause. If there is no error,
     * {@link #ERROR_NONE} will be returned. Other error types indicate the recording is failed or
     * stopped due to a certain reasons. Please note that receiving a finalize event with error
     * does not necessarily mean that the video file has not been generated. In some cases, the
     * file can still be successfully generated depending on the error type. For example, a file
     * will still be generated when the recording is finalized with
     * {@link #ERROR_INSUFFICIENT_DISK}.
     *
     * <p>If there's no error that prevents the file to be generated, the file can be accessed
     * safely after receiving the finalize event.
     */
    public static final class Finalize extends VideoRecordEvent {
        /**
         * The recording succeeded with no error.
         */
        public static final int ERROR_NONE = 0;

        /**
         * An unknown error occurred.
         */
        public static final int ERROR_UNKNOWN = 1;

        /**
         * The recording failed due to file size limitation.
         *
         * <p>The file size limitation will refer to {@link OutputOptions#getFileSizeLimit()}.
         */
        // TODO(b/167481981): add more descriptions about the restrictions after getting into more
        //  details.
        public static final int ERROR_FILE_SIZE_LIMIT_REACHED = 2;

        /**
         * The recording failed due to insufficient storage space.
         */
        // TODO(b/167484136): add more descriptions about the restrictions after getting into more
        //  details.
        public static final int ERROR_INSUFFICIENT_DISK = 3;

        /**
         * The recording failed because the source becomes inactive and stops sending frames.
         *
         * <p>One case is that if camera is closed due to lifecycle stopped, the active recording
         * will be finalized with this error, and the output will be generated, containing the
         * frames produced before camera closing. Attempting to start a new recording will be
         * finalized immediately if the source remains inactive.
         */
        public static final int ERROR_SOURCE_INACTIVE = 4;

        /**
         * The recording failed due to invalid output options.
         *
         * <p>This error is generated when invalid output options have been used while preparing a
         * recording, such as with the {@link Recorder#prepareRecording(MediaStoreOutputOptions)}
         * method. The error will depend on the subclass of {@link OutputOptions} used, and more
         * information about the error can be retrieved from {@link Finalize#getCause()}.
         */
        public static final int ERROR_INVALID_OUTPUT_OPTIONS = 5;

        /**
         * The recording failed while encoding.
         *
         * <p>This error may be generated when the video or audio codec encounters an error during
         * encoding. See {@link Finalize#getCause()} for more information about the error
         * encountered by the codec.
         */
        public static final int ERROR_ENCODING_FAILED = 6;

        /**
         * The recording failed because the {@link Recorder} is in an unrecoverable error state.
         *
         * <p>More information about the error can be retrieved from {@link Finalize#getCause()}.
         * Such an error will usually require creating a new {@link Recorder} object to start a
         * new recording.
         */
        public static final int ERROR_RECORDER_ERROR = 7;

        /**
         * The recording failed because no valid data was produced to be recorded.
         *
         * <p>This error is generated when the essential data for a recording to be played correctly
         * is missing, for example, a recording must contain at least one key frame. See
         * {@link Finalize#getCause()} for more information.
         */
        public static final int ERROR_NO_VALID_DATA = 8;

        /**
         * Describes the error that occurred during a video recording.
         *
         * <p>This is the error code returning from {@link Finalize#getError()}.
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @Retention(RetentionPolicy.SOURCE)
        @IntDef(value = {ERROR_NONE, ERROR_UNKNOWN, ERROR_FILE_SIZE_LIMIT_REACHED,
                ERROR_INSUFFICIENT_DISK, ERROR_INVALID_OUTPUT_OPTIONS, ERROR_ENCODING_FAILED,
                ERROR_RECORDER_ERROR, ERROR_NO_VALID_DATA, ERROR_SOURCE_INACTIVE})
        public @interface VideoRecordError {
        }

        private final OutputResults mOutputResults;
        @VideoRecordError
        private final int mError;
        private final Throwable mCause;

        @SuppressWarnings("WeakerAccess") /* synthetic accessor */
        Finalize(@NonNull OutputOptions outputOptions,
                @NonNull RecordingStats recordingStats,
                @NonNull OutputResults outputResults,
                @VideoRecordError int error,
                @Nullable Throwable cause) {
            super(outputOptions, recordingStats);
            mOutputResults = outputResults;
            mError = error;
            mCause = cause;
        }

        /**
         * Gets the {@link OutputResults}.
         */
        @NonNull
        public OutputResults getOutputResults() {
            return mOutputResults;
        }

        /**
         * Indicates whether an error occurred.
         *
         * <p>Returns {@code true} if {@link #getError()} returns {@link #ERROR_NONE}, otherwise
         * {@code false}.
         */
        public boolean hasError() {
            return mError != ERROR_NONE;
        }

        /**
         * Gets the error type for a video recording.
         *
         * <p>Possible values are {@link #ERROR_NONE}, {@link #ERROR_UNKNOWN},
         * {@link #ERROR_FILE_SIZE_LIMIT_REACHED}, {@link #ERROR_INSUFFICIENT_DISK},
         * {@link #ERROR_INVALID_OUTPUT_OPTIONS}, {@link #ERROR_ENCODING_FAILED},
         * {@link #ERROR_RECORDER_ERROR}, {@link #ERROR_NO_VALID_DATA} and
         * {@link #ERROR_SOURCE_INACTIVE}.
         */
        @VideoRecordError
        public int getError() {
            return mError;
        }

        /**
         * Gets the error cause.
         *
         * <p>Returns {@code null} if {@link #hasError()} returns {@code false}.
         */
        @Nullable
        public Throwable getCause() {
            return mCause;
        }
    }

    @NonNull
    static Status status(@NonNull OutputOptions outputOptions,
            @NonNull RecordingStats recordingStats) {
        return new Status(outputOptions, recordingStats);
    }

    /**
     * The status report of the recording in progress.
     */
    public static final class Status extends VideoRecordEvent {

        @SuppressWarnings("WeakerAccess") /* synthetic accessor */
        Status(@NonNull OutputOptions outputOptions, @NonNull RecordingStats recordingStats) {
            super(outputOptions, recordingStats);
        }
    }

    @NonNull
    static Pause pause(@NonNull OutputOptions outputOptions,
            @NonNull RecordingStats recordingStats) {
        return new Pause(outputOptions, recordingStats);
    }

    /**
     * Indicates the pause event of recording.
     *
     * <p>A {@code Pause} event will be triggered after calling {@link ActiveRecording#pause()}.
     */
    public static final class Pause extends VideoRecordEvent {

        @SuppressWarnings("WeakerAccess") /* synthetic accessor */
        Pause(@NonNull OutputOptions outputOptions, @NonNull RecordingStats recordingStats) {
            super(outputOptions, recordingStats);
        }
    }

    @NonNull
    static Resume resume(@NonNull OutputOptions outputOptions,
            @NonNull RecordingStats recordingStats) {
        return new Resume(outputOptions, recordingStats);
    }

    /**
     * Indicates the resume event of recording.
     *
     * <p>A {@code Resume} event will be triggered after calling {@link ActiveRecording#resume()}.
     */
    public static final class Resume extends VideoRecordEvent {

        @SuppressWarnings("WeakerAccess") /* synthetic accessor */
        Resume(@NonNull OutputOptions outputOptions, @NonNull RecordingStats recordingStats) {
            super(outputOptions, recordingStats);
        }
    }
}
