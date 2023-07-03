package com.mean.shave

enum class State {
    /** 文本类型 */
    Text,

    /** 文件类型 */
    File,

    /** 文件保存中 */
    Saving,

    /** 错误 */
    Error,

    /** 成功 */
    Success,
}
