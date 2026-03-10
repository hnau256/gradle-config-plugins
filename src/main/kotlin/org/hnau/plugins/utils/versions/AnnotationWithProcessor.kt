package org.hnau.plugins.utils.versions

data class AnnotationWithProcessor<out T>(
    val annotation: T,
    val processor: T,
) {

    inline fun <O> map(
        transform: (T) -> O,
    ): AnnotationWithProcessor<O> = AnnotationWithProcessor(
        annotation = transform(annotation),
        processor = transform(processor),
    )
}