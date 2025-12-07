package net.ririfa.bulletinboard.util

enum class PermEnum(val permissionNode: String) {
    ADMIN("bulletinboard.admin"),
    GUI_USE("bulletinboard.gui.use"),
    POST_DELETE_OWN("bulletinboard.post.delete.own"),
    POST_CREATE("bulletinboard.post.create"),
    POST_EDIT_OWN("bulletinboard.post.edit.own"),
    POST_VIEW("bulletinboard.post.view"),
    POST_ANONYMOUS("bulletinboard.post.anonymous"),
    POST_DELETE_OTHER("bulletinboard.post.delete.other"),
    POST_EDIT_OTHER("bulletinboard.post.edit.other"),
    POST_DEBUG("bulletinboard.post.debug"),
    RELOAD("bulletinboard.reload");

    companion object {
        fun getAllNodes() = entries.map { it.permissionNode }
    }
}