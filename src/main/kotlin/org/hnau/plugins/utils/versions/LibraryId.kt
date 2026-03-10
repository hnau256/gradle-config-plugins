package org.hnau.plugins.utils.versions

data class LibraryId(
    val groupId: GroupId,
    val artifactId: ArtifactId,
)

infix fun GroupId.withArtifact(
    artifactId: ArtifactId,
): LibraryId = LibraryId(
    groupId = this,
    artifactId = artifactId,
)

infix fun GroupId.withArtifact(
    artifactId: String,
): LibraryId = this withArtifact ArtifactId(artifactId)


infix fun String.withArtifact(
    artifactId: String,
): LibraryId = GroupId(this) withArtifact artifactId

