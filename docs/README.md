---
home: true
heroImage: /images/logo.svg
heroImageDark: /images/logo-dark.svg
heroText: 享存
tagline: 一款可以将分享内容保存为文件的应用
actions:
  - text: 使用帮助
    link: /help
footer: Copyright © 2022-2023 Mean
---

<p align="center">
  <a href="https://developer.android.google.cn/jetpack/compose" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-1.4.8-brightgreen?logo=android" />
  </a>
  <a href="https://android-arsenal.com/api?level=23" alt="API">
    <img src="https://img.shields.io/badge/API-23%2B-blue?logo=android" />
  </a>
  <a href="https://github.com/MeanZhang/Shave/actions/workflows/android.yml"  alt="Android CI">
    <img src="https://github.com/MeanZhang/Traclock/actions/workflows/android.yml/badge.svg" />
  </a>
  <a href="https://github.com/MeanZhang/Shave/releases"  alt="GitHub release">
    <img src="https://img.shields.io/github/v/release/MeanZhang/Shave?sort=semver">
  </a>
</p>

享存是一款可以将分享内容保存为文件的应用，支持文本、图片和任意类型文件。通过分享菜单或打开方式中的享存 APP，使用[存储访问框架](https://developer.android.google.cn/training/data-storage/shared/documents-files?hl=zh-cn)（Storage Access Framework, SAF）保存在本地文件中，无需获取任何权限。

使用 Jetpack Compose 构建，采用 Material You 设计。

| 主界面                     | 文本分享                               | 分享菜单                               |
| -------------------------- | -------------------------------------- | -------------------------------------- |
| ![home](./images/home.png) | ![share-text](./images/share-text.jpg) | ![share-menu](./images/share-menu.png) |
