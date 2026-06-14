package com.shoge.app.generator

/**
 * The outcome of a shortcut generation attempt.
 * MainActivity pattern-matches on this to decide what feedback to show.
 */
sealed class GeneratorResult {
    /** requestPinShortcut() was called; system dialog is now showing. */
    object DialogShown : GeneratorResult()

    /** The current launcher does not support shortcut pinning. */
    object PinningNotSupported : GeneratorResult()

    /** Something went wrong while building or requesting the shortcut. */
    data class Error(val cause: String) : GeneratorResult()
}